import { describe, expect, it } from 'vitest';

import {
  canCancelReviewProcess,
  createLegacyReviewFallbackLocation,
  getReviewDetailSummaryItems,
  getReviewHistoryDisplayText,
  getReviewHistoryOperateLabel,
  getReviewRowActions,
  getReviewStatusMeta,
  isEditableReviewStatus,
  isWaitingReviewStatus,
  mapReviewDetailModel,
  mapReviewFormModel,
  mapReviewPageItem,
  normalizeReviewStatus,
  resolveReviewFormSubmitLabel,
  resolveReviewFormPageType,
  resolveReviewApprovalButtonPageType,
  resolveReviewWaitingTaskContext,
  shouldHideTransferButtonForReviewTask,
  shouldRequireReviewTaskContext,
  toReviewActionPayload
} from './integration';

describe('editorial review integration', () => {
  it('does not infer canEdit from ownership when page contract omits it', () => {
    const item = mapReviewPageItem({
      id: 101,
      title: '显式台账契约',
      status: 'BACK',
      reviewStatus: 'BACK',
      processType: 'AUDIT',
      user: {
        id: 9,
        name: '申请人'
      },
      dept: {
        id: 3,
        name: '编辑部'
      },
      createTime: '2026-03-17 14:00:00'
    } as any);

    expect(item.reviewStatus).toBe('BACK');
    expect(item.canEdit).toBe(false);
    expect(item.user.name).toBe('申请人');
    expect(item.dept.name).toBe('编辑部');
  });

  it('maps page contract approval fields explicitly for waiting approver rows', () => {
    const item = mapReviewPageItem({
      id: 102,
      title: '待二审样本',
      status: 'WAITING',
      reviewStatus: 'WAITING_SECOND',
      canEdit: true,
      canApprove: true,
      taskId: 'task-102',
      instanceId: 'instance-102',
      processType: 'AUDIT',
      user: {
        id: 9,
        name: '申请人'
      },
      dept: {
        id: 3,
        name: '编辑部'
      },
      createTime: '2026-03-19 12:00:00'
    } as any);

    expect(item.canApprove).toBe(true);
    expect(item.taskId).toBe('task-102');
    expect(item.instanceId).toBe('instance-102');
  });

  it('returns modify and approve actions only for current approver waiting rows', () => {
    expect(
      getReviewRowActions({
        reviewStatus: 'WAITING_SECOND',
        canEdit: true,
        canApprove: true
      } as any)
    ).toEqual({
      showEdit: true,
      showApprove: true,
      showDetail: true,
      showDelete: false
    });

    expect(
      getReviewRowActions({
        reviewStatus: 'WAITING_SECOND',
        canEdit: false,
        canApprove: false
      } as any)
    ).toEqual({
      showEdit: false,
      showApprove: false,
      showDetail: true,
      showDelete: false
    });
  });

  it('treats waiting rows with canEdit=true as approvable even when list contract omits canApprove', () => {
    expect(
      getReviewRowActions({
        reviewStatus: 'WAITING_FIRST',
        canEdit: true,
        canApprove: false
      } as any)
    ).toEqual({
      showEdit: true,
      showApprove: true,
      showDetail: true,
      showDelete: false
    });
  });

  it('recognizes both legacy and staged waiting statuses for approval shell', () => {
    expect(isWaitingReviewStatus('waiting')).toBe(true);
    expect(isWaitingReviewStatus('WAITING_SECOND')).toBe(true);
    expect(isWaitingReviewStatus('APPROVED')).toBe(false);
  });

  it('maps detail contract history and approval shell context explicitly', () => {
    const detail = mapReviewDetailModel(
      {
        id: 12,
        title: '审批详情壳',
        status: 'WAITING_FINAL',
        reviewStatus: 'WAITING_FINAL',
        canEdit: false,
        processType: 'PROOFREAD',
        createTime: '2026-03-17 15:20:00',
        updateTime: '2026-03-17 15:23:00',
        user: {
          id: 7,
          name: '申请人'
        },
        dept: {
          id: 2,
          name: '校对室'
        },
        content: 'detail content',
        remark: 'detail remark',
        attachment: {
          id: 1,
          reviewId: 12,
          fileName: 'proofread.docx',
          ossId: 'oss-1'
        },
        linkList: [],
        historyList: [
          {
            id: 87,
            reviewId: 12,
            operatorId: 7,
            operatorName: '申请人',
            operateTime: '2026-03-17 15:19:00',
            operateType: 'update',
            fieldDiff: {
              title: ['旧标题', '新标题']
            }
          },
          {
            id: 88,
            reviewId: 12,
            operatorId: 9,
            operatorName: '终审人',
            operateTime: '2026-03-17 15:21:00',
            operateType: 'approve',
            fieldDiff: {
              reviewStatus: ['WAITING_SECOND', 'WAITING_FINAL']
            }
          }
        ],
        approvalContext: {
          taskId: 'task-1',
          instanceId: 'instance-1',
          canApprove: true
        }
      },
      'approval',
      {
        id: '12',
        type: 'approval',
        taskId: 'legacy-task',
        fallback: 'legacy'
      }
    );

    expect(detail.history).toHaveLength(2);
    expect(detail.shell.taskId).toBe('task-1');
    expect(detail.shell.instanceId).toBe('instance-1');
    expect(detail.shell.canApprove).toBe(true);
    expect(detail.shell.fallback).toBe(true);
    expect(detail.processType).toBe('PROOFREAD');
    expect(detail.updateTime).toBe('2026-03-17 15:23:00');
    expect(detail.history.map((item) => item.id)).toEqual([88, 87]);
    expect(detail.history[0].fieldDiff).toEqual({
      reviewStatus: ['WAITING_SECOND', 'WAITING_FINAL']
    });
  });

  it('maps multi-attachment detail data and preserves attachment/link/dept/update-time readback', () => {
    const form = mapReviewFormModel({
      id: 13,
      title: '多附件合同',
      status: 'WAITING',
      reviewStatus: 'WAITING_FIRST',
      canEdit: true,
      processType: 'AUDIT',
      createTime: '2026-03-19 12:10:00',
      updateTime: '2026-03-19 12:15:00',
      user: {
        id: 7,
        name: '申请人'
      },
      dept: {
        id: 2,
        name: '编辑部'
      },
      content: 'detail content',
      remark: 'detail remark',
      attachmentList: [
        {
          id: 1,
          reviewId: 13,
          fileName: 'first.pdf',
          ossId: 'oss-first',
          fileUrl: 'https://example.com/first.pdf',
          fileSize: 100
        },
        {
          id: 2,
          reviewId: 13,
          fileName: 'second.mp4',
          ossId: 'oss-second',
          fileUrl: 'https://example.com/second.mp4',
          fileSize: 200
        }
      ],
      linkList: [
        {
          id: 9,
          reviewId: 13,
          url: 'https://example.com/existing',
          description: '既有链接'
        }
      ]
    } as any);

    expect(form.attachmentList).toHaveLength(2);
    expect(form.attachmentList[0].fileName).toBe('first.pdf');
    expect(form.attachmentList[0].readonly).toBe(true);
    expect(form.attachmentOssId).toBe('oss-second');
    expect(form.attachmentFileName).toBe('second.mp4');
    expect(form.linkList[0].url).toBe('https://example.com/existing');
    expect(form.linkList[0].readonly).toBe(true);
    expect(form.deptId).toBe(2);
    expect(form.deptName).toBe('编辑部');
    expect(form.updateTime).toBe('2026-03-19 12:15:00');
  });

  it('writes payload without dropping readonly existing attachments and links', () => {
    const payload = toReviewActionPayload({
      id: 13,
      title: '多附件写回',
      content: 'payload content',
      deptId: 2,
      status: 'WAITING',
      reviewStatus: 'WAITING_SECOND',
      canEdit: true,
      remark: 'payload remark',
      attachmentList: [
        {
          id: 1,
          ossId: 'oss-first',
          fileName: 'first.pdf',
          fileUrl: 'https://example.com/first.pdf',
          fileSize: 100,
          version: 1,
          readonly: true
        },
        {
          ossId: 'oss-second',
          fileName: 'second.mp4',
          fileUrl: 'https://example.com/second.mp4',
          fileSize: 200,
          version: 2
        }
      ],
      linkList: [
        {
          id: 9,
          url: 'https://example.com/existing',
          description: '既有链接',
          readonly: true
        },
        {
          url: 'https://example.com/new',
          description: '新增链接',
          readonly: false
        }
      ],
      flowCode: 'editorial_review_flow',
      processType: 'AUDIT'
    } as any);

    expect(payload.attachmentList).toHaveLength(2);
    expect(payload.attachmentList?.[0].id).toBe(1);
    expect(payload.attachmentOssId).toBe('oss-second');
    expect(payload.attachmentFileName).toBe('second.mp4');
    expect(payload.linkList).toEqual([
      {
        id: 9,
        url: 'https://example.com/existing',
        description: '既有链接'
      },
      {
        id: null,
        url: 'https://example.com/new',
        description: '新增链接'
      }
    ]);
  });

  it('redirects legacy reviewEdit entry to the new shell route with fallback marker', () => {
    expect(createLegacyReviewFallbackLocation({ id: '12', type: 'view' })).toEqual({
      path: '/editorial/review/detail',
      query: {
        id: '12',
        type: 'view',
        fallback: 'legacy'
      }
    });

    expect(createLegacyReviewFallbackLocation({ id: '12', type: 'update' })).toEqual({
      path: '/editorial/review/form',
      query: {
        id: '12',
        type: 'update',
        fallback: 'legacy'
      }
    });
  });

  it('surfaces staged review labels once backend starts returning them', () => {
    expect(normalizeReviewStatus('finish')).toBe('APPROVED');
    expect(normalizeReviewStatus(10 as any)).toBe('WAITING_FIRST');
    expect(normalizeReviewStatus('50')).toBe('BACK');
    expect(getReviewStatusMeta('WAITING_FINAL').label).toBe('待终审');
    expect(getReviewStatusMeta('TERMINATED').type).toBe('info');
  });

  it('keeps only BACK as resubmit-editable status', () => {
    expect(isEditableReviewStatus('BACK')).toBe(true);
    expect(isEditableReviewStatus('CANCELED')).toBe(false);
  });

  it('keeps update route as modify page even when waiting task context exists', () => {
    expect(
      resolveReviewFormPageType({
        routeType: 'update',
        reviewStatus: 'WAITING_SECOND',
        taskId: 'task-200',
        canApprove: true
      })
    ).toBe('update');

    expect(
      resolveReviewFormPageType({
        routeType: 'update',
        reviewStatus: 'BACK',
        taskId: undefined,
        canApprove: false
      })
    ).toBe('update');
  });

  it('uses save label for modify page and submit label for add/back page', () => {
    expect(
      resolveReviewFormSubmitLabel({
        routeType: 'update',
        reviewStatus: 'WAITING_FIRST'
      })
    ).toBe('保存');

    expect(
      resolveReviewFormSubmitLabel({
        routeType: 'add',
        reviewStatus: 'BACK'
      })
    ).toBe('再次提交');
  });

  it('resolves waiting task context by matching review id to workflow businessId', () => {
    expect(
      resolveReviewWaitingTaskContext({
        reviewId: 510,
        tasks: [
          {
            id: 'task-a',
            instanceId: 'instance-a',
            businessId: '509'
          },
          {
            id: 'task-b',
            instanceId: 'instance-b',
            businessId: '510'
          }
        ] as any
      })
    ).toEqual({
      taskId: 'task-b',
      instanceId: 'instance-b'
    });
  });

  it('requires task context for approve but never blocks update entry for waiting rows', () => {
    expect(
      shouldRequireReviewTaskContext({
        action: 'update',
        reviewStatus: 'WAITING_SECOND'
      })
    ).toBe(false);

    expect(
      shouldRequireReviewTaskContext({
        action: 'approve',
        reviewStatus: 'WAITING_SECOND'
      })
    ).toBe(true);
  });

  it('allows only applicant waiting-shell detail to cancel process', () => {
    expect(
      canCancelReviewProcess({
        pageType: 'view',
        reviewStatus: 'WAITING_SECOND',
        applicantUserId: 7,
        currentUserId: 7
      })
    ).toBe(true);

    expect(
      canCancelReviewProcess({
        pageType: 'approval',
        reviewStatus: 'WAITING_SECOND',
        applicantUserId: 7,
        currentUserId: 7
      })
    ).toBe(false);

    expect(
      canCancelReviewProcess({
        pageType: 'view',
        reviewStatus: 'APPROVED',
        applicantUserId: 7,
        currentUserId: 7
      })
    ).toBe(false);

    expect(
      canCancelReviewProcess({
        pageType: 'view',
        reviewStatus: 'WAITING_FINAL',
        applicantUserId: 7,
        currentUserId: 8
      })
    ).toBe(false);
  });

  it('downgrades approval shell button state when backend says current user cannot approve', () => {
    expect(
      resolveReviewApprovalButtonPageType({
        pageType: 'approval',
        canApprove: false
      })
    ).toBe('view');

    expect(
      resolveReviewApprovalButtonPageType({
        pageType: 'approval',
        canApprove: true
      })
    ).toBe('approval');

    expect(
      resolveReviewApprovalButtonPageType({
        pageType: 'approval'
      })
    ).toBe('approval');
  });

  it('builds detail summary items without shell/task debug fields and with status/create/update time', () => {
    const summaryItems = getReviewDetailSummaryItems({
      reviewStatus: 'WAITING_FINAL',
      processType: 'PROOFREAD',
      user: {
        name: '申请人'
      },
      dept: {
        name: '校对室'
      },
      createTime: '2026-03-17 15:20:00',
      updateTime: '2026-03-17 15:23:00',
      shell: {
        pageType: 'approval',
        taskId: 'task-1',
        instanceId: 'instance-1',
        canApprove: true,
        canEdit: false,
        fallback: false
      }
    } as any);

    expect(summaryItems.map((item) => item.label)).toEqual(['审核状态', '流程类型', '发起人', '部门', '创建时间', '修改时间']);
    expect(summaryItems.find((item) => item.label === '审核状态')?.value).toBe('待终审');
    expect(summaryItems.find((item) => item.label === '修改时间')?.value).toBe('2026-03-17 15:23:00');
    expect(summaryItems.some((item) => item.label === 'taskId')).toBe(false);
    expect(summaryItems.some((item) => item.label === '当前入口')).toBe(false);
  });

  it('maps history operation labels from operateType without inventing debug labels', () => {
    expect(getReviewHistoryOperateLabel('approve')).toBe('审批通过');
    expect(getReviewHistoryOperateLabel('update')).toBe('修改');
    expect(getReviewHistoryOperateLabel('unknown_custom_type')).toBe('unknown_custom_type');
  });

  it('renders history business copy without prepending operator name', () => {
    expect(
      getReviewHistoryDisplayText({
        operatorName: 'zhangsan',
        operateType: '三审三校发起人创建申请qa-fresh-history-1773998938538'
      } as any)
    ).toBe('三审三校发起人创建申请qa-fresh-history-1773998938538');

    expect(
      getReviewHistoryDisplayText({
        operatorName: '终审人',
        operateType: 'approve'
      } as any)
    ).toBe('审批通过');
  });

  it('hides transfer entry for editorial review flow only', () => {
    expect(
      shouldHideTransferButtonForReviewTask({
        flowCode: 'editorial_review_flow',
        businessCode: 'editorial_review',
        formPath: '/editorial/review/detail'
      } as any)
    ).toBe(true);

    expect(
      shouldHideTransferButtonForReviewTask({
        flowCode: 'leave_flow',
        businessCode: 'leave_apply',
        formPath: '/workflow/leave/detail'
      } as any)
    ).toBe(false);
  });
});
