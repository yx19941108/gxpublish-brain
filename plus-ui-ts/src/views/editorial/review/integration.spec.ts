import { describe, expect, it } from 'vitest';

import {
  canCancelReviewProcess,
  createLegacyReviewFallbackLocation,
  getReviewRowActions,
  getReviewStatusMeta,
  isEditableReviewStatus,
  isWaitingReviewStatus,
  mapReviewDetailModel,
  mapReviewFormModel,
  mapReviewPageItem,
  normalizeReviewStatus,
  resolveReviewFormPageType,
  resolveReviewApprovalButtonPageType,
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
      showDetail: false,
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

    expect(detail.history).toHaveLength(1);
    expect(detail.shell.taskId).toBe('task-1');
    expect(detail.shell.instanceId).toBe('instance-1');
    expect(detail.shell.canApprove).toBe(true);
    expect(detail.shell.fallback).toBe(true);
    expect(detail.processType).toBe('PROOFREAD');
  });

  it('maps multi-attachment detail data and preserves latest legacy attachment fields', () => {
    const form = mapReviewFormModel({
      id: 13,
      title: '多附件合同',
      status: 'WAITING',
      reviewStatus: 'WAITING_FIRST',
      canEdit: true,
      processType: 'AUDIT',
      createTime: '2026-03-19 12:10:00',
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
      linkList: []
    } as any);

    expect(form.attachmentList).toHaveLength(2);
    expect(form.attachmentList[0].fileName).toBe('first.pdf');
    expect(form.attachmentOssId).toBe('oss-second');
    expect(form.attachmentFileName).toBe('second.mp4');
  });

  it('writes multi-attachment payload and keeps latest attachment legacy fallback fields', () => {
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
          ossId: 'oss-first',
          fileName: 'first.pdf',
          fileUrl: 'https://example.com/first.pdf',
          fileSize: 100,
          version: 1
        },
        {
          ossId: 'oss-second',
          fileName: 'second.mp4',
          fileUrl: 'https://example.com/second.mp4',
          fileSize: 200,
          version: 2
        }
      ],
      linkList: [],
      flowCode: 'editorial_review_flow',
      processType: 'AUDIT'
    } as any);

    expect(payload.attachmentList).toHaveLength(2);
    expect(payload.attachmentOssId).toBe('oss-second');
    expect(payload.attachmentFileName).toBe('second.mp4');
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

  it('treats update route with active approval task as approval-edit page type', () => {
    expect(
      resolveReviewFormPageType({
        routeType: 'update',
        reviewStatus: 'WAITING_SECOND',
        taskId: 'task-200',
        canApprove: true
      })
    ).toBe('approval');

    expect(
      resolveReviewFormPageType({
        routeType: 'update',
        reviewStatus: 'BACK',
        taskId: undefined,
        canApprove: false
      })
    ).toBe('update');
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
});
