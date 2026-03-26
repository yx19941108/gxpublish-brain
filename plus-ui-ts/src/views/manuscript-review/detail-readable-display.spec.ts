import { beforeEach, describe, expect, it, vi } from 'vitest';

import { buildDetailActionBar, buildReadableSummary, normalizeDetailViewModel } from './detail.contract';

const { requestInvoker } = vi.hoisted(() => ({
  requestInvoker: vi.fn(async (config: unknown) => config)
}));

vi.mock('@/utils/request', () => ({
  default: requestInvoker
}));

describe('T07_Frontend_DetailActions_AndReadableDisplaySpec', () => {
  beforeEach(() => {
    requestInvoker.mockClear();
  });

  it('manuscript-review api uses only frozen workflow routes and integrated submit-save payloads', async () => {
    const api = await import('@/api/manuscript-review');

    await api.listManuscriptReview({ keyword: '系统稿件号' });
    await api.getManuscriptReviewDetail(9001);
    await api.submitAndFlowStartManuscriptReview({
      processType: 'AUDIT',
      externalManuscriptCode: 'EXT-001',
      title: '审校稿件',
      mediaChannel: '新华社/要闻',
      submitDepartment: '总编室',
      authorName: '张三',
      remark: '提交备注',
      contentBody: '正文内容',
      attachmentList: [
        {
          displayName: '送审单.pdf',
          ossId: 8001
        }
      ],
      externalLinkList: [
        {
          displayName: '素材参考',
          externalUrl: 'https://example.com/ref'
        }
      ]
    });
    await api.updateManuscriptReview({
      id: 9001,
      processType: 'AUDIT',
      externalManuscriptCode: 'EXT-001',
      title: '审校稿件',
      mediaChannel: '新华社/要闻',
      submitDepartment: '总编室',
      authorName: '张三',
      remark: '修改备注',
      contentBody: '正文内容',
      attachmentList: [
        {
          displayName: '补充附件.pdf',
          ossId: 8002
        }
      ],
      externalLinkList: [
        {
          displayName: '补充外链',
          externalUrl: 'https://example.com/extra'
        }
      ]
    });
    await api.resubmitManuscriptReview({ id: 9001 });
    await api.cancelManuscriptReviewProcess({ id: 9001, reason: '发起人撤销' });
    await api.addManuscriptReviewResource({
      reviewId: 9001,
      resourceType: 'ATTACHMENT',
      displayName: '送审单.pdf',
      ossId: 8001
    });
    await api.disableManuscriptReviewResource({
      resourceId: 8001,
      disabledReason: '停用附件'
    });
    await api.addManuscriptReviewVideoMark({
      reviewId: 9001,
      resourceId: 7001,
      startTimeText: '00:00:05',
      endTimeText: '00:00:10',
      markContent: '第一处问题'
    });

    expect(
      requestInvoker.mock.calls.map(([config]) => `${(config as { method: string }).method}:${(config as { url: string }).url}`)
    ).toEqual([
      'get:/workflow/manuscript-review/list',
      'get:/workflow/manuscript-review/9001',
      'post:/workflow/manuscript-review/submitAndFlowStart',
      'put:/workflow/manuscript-review',
      'post:/workflow/manuscript-review/resubmit',
      'put:/workflow/manuscript-review/cancelProcessApply',
      'post:/workflow/manuscript-review/resource',
      'put:/workflow/manuscript-review/resource/disable',
      'post:/workflow/manuscript-review/video-mark'
    ]);

    const [submitCall, updateCall] = requestInvoker.mock.calls
      .map(([config]) => config as { url: string; data?: Record<string, unknown> })
      .filter((config) => config.url === '/workflow/manuscript-review/submitAndFlowStart' || config.url === '/workflow/manuscript-review');

    expect(submitCall.data).toEqual(
      expect.objectContaining({
        processType: 'AUDIT',
        externalManuscriptCode: 'EXT-001',
        title: '审校稿件',
        attachmentList: [{ displayName: '送审单.pdf', ossId: 8001 }],
        externalLinkList: [{ displayName: '素材参考', externalUrl: 'https://example.com/ref' }]
      })
    );
    expect(updateCall.data).toEqual(
      expect.objectContaining({
        id: 9001,
        attachmentList: [{ displayName: '补充附件.pdf', ossId: 8002 }],
        externalLinkList: [{ displayName: '补充外链', externalUrl: 'https://example.com/extra' }]
      })
    );
  });

  it('exposes pending-upload delete helper for pre-submit attachments', async () => {
    const api = await import('@/api/manuscript-review');

    expect(api.deletePendingOssResource).toBeTypeOf('function');

    await api.deletePendingOssResource(8101);

    const lastCall = requestInvoker.mock.calls.at(-1)?.[0] as { method: string; url: string };
    expect(`${lastCall.method}:${lastCall.url}`).toBe('delete:/resource/oss/8101');
  });

  it('shows only modify, approve, and back for the current approver', () => {
    const labels = buildDetailActionBar('CURRENT_APPROVER').map((action) => action.label);

    expect(labels).toEqual(['修改', '去审批', '返回']);
    expect(labels).not.toEqual(expect.arrayContaining(['删除', '草稿', '保存草稿', '再次提交']));
  });

  it('shows only modify, resubmit, and back for the returned initiator', () => {
    const labels = buildDetailActionBar('RETURNED_INITIATOR').map((action) => action.label);

    expect(labels).toEqual(['修改', '再次提交', '返回']);
    expect(labels).not.toEqual(expect.arrayContaining(['删除', '草稿', '保存草稿', '去审批']));
  });

  it('normalizes frozen detail payload without leaking technical fields into summary or permissions into ledger space', () => {
    const viewModel = normalizeDetailViewModel({
      code: 200,
      data: {
        id: 9002,
        processType: 'AUDIT',
        processTypeLabel: '审核流程',
        manuscriptCode: 'SH20260321002',
        externalManuscriptCode: 'EXT-001',
        title: '稿件标题',
        mediaChannel: '新华社/要闻',
        submitDepartment: '总编室',
        authorName: '张三、李四',
        remark: '补充说明',
        contentSummary: '正文内容',
        businessStatus: 'BACK',
        businessStatusLabel: '已退回',
        currentNodeCode: 'RETURN_TO_INITIATOR',
        currentNodeLabel: '待发起人处理',
        initiatorName: '张三',
        updateTime: '2026-03-23 11:22:33',
        permissionMatrix: {
          isInitiator: true,
          isCurrentApprover: false,
          isHistoryParticipant: true,
          canView: true,
          canEdit: true,
          canResubmit: true,
          canCancel: false,
          canGotoApproval: false
        },
        attachmentList: [
          {
            id: 1,
            resourceType: 'ATTACHMENT',
            resourceTypeLabel: '附件',
            displayName: '送审单.pdf',
            resourceUrl: 'https://files.example/a.pdf'
          }
        ],
        externalLinkList: [
          {
            id: 2,
            resourceType: 'EXTERNAL_LINK',
            resourceTypeLabel: '外链',
            displayName: '素材参考',
            externalUrl: 'https://example.com/ref'
          }
        ],
        videoList: [
          {
            id: 3,
            resourceType: 'VIDEO',
            resourceTypeLabel: '视频',
            displayName: '样片.mp4',
            resourceUrl: 'https://files.example/video.mp4'
          }
        ],
        videoMarkList: [
          {
            id: 4,
            startTimeText: '00:00:05',
            endTimeText: '00:00:10',
            markContent: '第一处问题'
          }
        ],
        timelineItems: [
          {
            eventTime: '2026-03-23 11:20:00',
            eventText: '张三新增了流程。',
            diffSummary: '标题由旧值改为新值'
          }
        ]
      }
    });

    const summary = buildReadableSummary(viewModel.detail);
    const summaryText = JSON.stringify(summary);

    expect(viewModel.actionRole).toBe('RETURNED_INITIATOR');
    expect(viewModel.detail.submitDepartment).toBe('总编室');
    expect(viewModel.detail.mediaChannel).toBe('新华社/要闻');
    expect(viewModel.detail.permissionMatrix?.canResubmit).toBe(true);
    expect(viewModel.historyItems).toEqual([
      {
        id: 'timeline-1',
        timeLabel: '2026-03-23 11:20:00',
        actionLabel: '张三新增了流程。',
        operatorName: '',
        remark: '标题由旧值改为新值'
      }
    ]);
    expect(viewModel.resourceItems).toEqual([
      {
        id: '1',
        typeLabel: '附件',
        name: '送审单.pdf',
        statusLabel: '当前有效',
        note: 'https://files.example/a.pdf'
      },
      {
        id: '2',
        typeLabel: '外链',
        name: '素材参考',
        statusLabel: '当前有效',
        note: 'https://example.com/ref'
      },
      {
        id: '3',
        typeLabel: '视频',
        name: '样片.mp4',
        statusLabel: '当前有效',
        note: 'https://files.example/video.mp4'
      },
      {
        id: '4',
        typeLabel: '视频时间标注',
        name: '00:00:05',
        statusLabel: '当前有效',
        note: '第一处问题'
      }
    ]);
    expect(summary).toEqual(
      expect.arrayContaining([
        { label: '流程状态', value: '已退回' },
        { label: '当前节点', value: '待发起人处理' },
        { label: '发起人', value: '张三' },
        { label: '流程类型', value: '审核流程' },
        { label: '系统稿件号', value: 'SH20260321002' },
        { label: '报送部门', value: '总编室' }
      ])
    );
    expect(summaryText).not.toContain('permissionMatrix');
    expect(summaryText).not.toContain('tenantId');
    expect(summaryText).not.toContain('roleKey');
  });
});
