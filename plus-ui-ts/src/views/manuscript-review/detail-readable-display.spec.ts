import { beforeEach, describe, expect, it, vi } from 'vitest';

import {
  buildDetailActionBar,
  buildReadableSummary,
  buildVideoPlaybackItems,
  normalizeDetailViewModel,
  parseVideoTimeTextToSeconds
} from './detail.contract';

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
      attachmentResources: [
        {
          displayName: '送审单.pdf',
          ossId: 8001
        }
      ],
      externalLinks: [
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
      attachmentResources: [
        {
          displayName: '补充附件.pdf',
          ossId: 8002
        }
      ],
      externalLinks: [
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
    await api.disableManuscriptReviewVideoMark({
      markId: 7002,
      disabledReason: '标注停用'
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
      'post:/workflow/manuscript-review/video-mark',
      'put:/workflow/manuscript-review/video-mark/disable'
    ]);

    const [submitCall, updateCall] = requestInvoker.mock.calls
      .map(([config]) => config as { url: string; data?: Record<string, unknown> })
      .filter((config) => config.url === '/workflow/manuscript-review/submitAndFlowStart' || config.url === '/workflow/manuscript-review');

    expect(submitCall.data).toEqual(
      expect.objectContaining({
        processType: 'AUDIT',
        externalManuscriptCode: 'EXT-001',
        title: '审校稿件',
        attachmentResources: [{ displayName: '送审单.pdf', ossId: 8001 }],
        externalLinks: [{ displayName: '素材参考', externalUrl: 'https://example.com/ref' }]
      })
    );
    expect(updateCall.data).toEqual(
      expect.objectContaining({
        id: 9001,
        attachmentResources: [{ displayName: '补充附件.pdf', ossId: 8002 }],
        externalLinks: [{ displayName: '补充外链', externalUrl: 'https://example.com/extra' }]
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
            ossId: 8001,
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
            ossId: 8003,
            resourceType: 'VIDEO',
            resourceTypeLabel: '视频',
            displayName: '样片.mp4',
            resourceUrl: 'https://files.example/video.mp4'
          }
        ],
        videoMarkList: [
          {
            id: 4,
            resourceId: 3,
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
        resourceType: 'ATTACHMENT',
        typeLabel: '附件',
        name: '送审单.pdf',
        statusLabel: '当前有效',
        note: 'https://files.example/a.pdf',
        resourceUrl: 'https://files.example/a.pdf',
        ossId: '8001'
      },
      {
        id: '2',
        resourceType: 'EXTERNAL_LINK',
        typeLabel: '外链',
        name: '素材参考',
        statusLabel: '当前有效',
        note: 'https://example.com/ref',
        href: 'https://example.com/ref'
      },
      {
        id: '3',
        resourceType: 'VIDEO',
        typeLabel: '视频',
        name: '样片.mp4',
        statusLabel: '当前有效',
        note: 'https://files.example/video.mp4',
        resourceUrl: 'https://files.example/video.mp4',
        ossId: '8003'
      },
      {
        id: '4',
        resourceType: 'VIDEO_MARK',
        typeLabel: '视频时间标注',
        name: '00:00:05',
        statusLabel: '当前有效',
        note: '第一处问题',
        resourceId: '3',
        startTimeText: '00:00:05',
        endTimeText: '00:00:10',
        markContent: '第一处问题'
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

  it('keeps external links clickable in readable resource items', () => {
    const viewModel = normalizeDetailViewModel({
      code: 200,
      data: {
        id: 9010,
        manuscriptCode: 'SH20260321010',
        title: '外链跳转测试',
        submitDepartment: '',
        businessStatusLabel: '审批中',
        currentNodeLabel: '待一级审批',
        externalLinkList: [
          {
            id: 201,
            resourceType: 'EXTERNAL_LINK',
            resourceTypeLabel: '外链',
            displayName: '参考链接',
            externalUrl: 'https://example.com/link'
          }
        ]
      }
    });

    expect(viewModel.resourceItems).toEqual([
      {
        id: '201',
        resourceType: 'EXTERNAL_LINK',
        typeLabel: '外链',
        name: '参考链接',
        statusLabel: '当前有效',
        note: 'https://example.com/link',
        href: 'https://example.com/link'
      }
    ]);
  });

  it('keeps disabled resource and external-link names clickable in timeline history', () => {
    const viewModel = normalizeDetailViewModel({
      code: 200,
      data: {
        id: 9011,
        manuscriptCode: 'JD20260328001',
        title: '停用历史跳转测试',
        submitDepartment: '测试部',
        businessStatusLabel: '审批中',
        currentNodeLabel: '待一级审批',
        timelineItems: [
          {
            eventTime: '2026-03-28 13:05:28',
            eventCode: 'RESOURCE_DISABLE',
            eventText: 'mr_approver_l1停用了附件《取证附件.pdf》。',
            operatorName: 'mr_approver_l1',
            relatedResourceName: '取证附件.pdf',
            relatedResourceOssId: 9901,
            relatedResourceType: 'ATTACHMENT',
            relatedResourceUrl: 'https://files.example/proof.pdf'
          },
          {
            eventTime: '2026-03-28 13:05:29',
            eventCode: 'RESOURCE_DISABLE',
            eventText: 'mr_approver_l1停用了外链《取证外链》。',
            operatorName: 'mr_approver_l1',
            relatedResourceName: '取证外链',
            relatedResourceType: 'EXTERNAL_LINK',
            relatedExternalUrl: 'https://example.com/proof'
          }
        ]
      }
    });

    expect(viewModel.historyItems).toEqual([
      {
        id: 'timeline-1',
        timeLabel: '2026-03-28 13:05:28',
        actionLabel: 'mr_approver_l1停用了附件《取证附件.pdf》。',
        operatorName: 'mr_approver_l1',
        remark: undefined,
        actionPrefix: 'mr_approver_l1停用了附件',
        actionLinkLabel: '《取证附件.pdf》',
        actionSuffix: '。',
        actionLinkOssId: '9901',
        actionLinkHref: 'https://files.example/proof.pdf'
      },
      {
        id: 'timeline-2',
        timeLabel: '2026-03-28 13:05:29',
        actionLabel: 'mr_approver_l1停用了外链《取证外链》。',
        operatorName: 'mr_approver_l1',
        remark: undefined,
        actionPrefix: 'mr_approver_l1停用了外链',
        actionLinkLabel: '《取证外链》',
        actionSuffix: '。',
        actionLinkOssId: undefined,
        actionLinkHref: 'https://example.com/proof'
      }
    ]);
  });

  it('groups video marks by resource and keeps them sortable for playback interaction', () => {
    const playbackItems = buildVideoPlaybackItems({
      id: 9012,
      manuscriptCode: 'JD20260328002',
      title: '多视频切换测试',
      submitDepartment: '测试部',
      businessStatusLabel: '审批中',
      currentNodeLabel: '待一级审批',
      videoList: [
        {
          id: 301,
          resourceType: 'VIDEO',
          resourceTypeLabel: '视频',
          displayName: '样片一.mp4',
          resourceUrl: 'https://files.example/video-1.mp4'
        },
        {
          id: 302,
          resourceType: 'VIDEO',
          resourceTypeLabel: '视频',
          displayName: '样片二.mp4',
          resourceUrl: 'https://files.example/video-2.mp4'
        }
      ],
      videoMarkList: [
        {
          id: 401,
          resourceId: 302,
          startTimeText: '00:00:08',
          endTimeText: '00:00:10',
          markContent: '第二视频标注'
        },
        {
          id: 402,
          resourceId: 301,
          startTimeText: '00:00:05',
          endTimeText: '00:00:12',
          markContent: '第一视频标注'
        },
        {
          id: 403,
          resourceId: 301,
          startTimeText: '00:00:02',
          endTimeText: '00:00:04',
          markContent: '第一视频更早标注'
        }
      ]
    });

    expect(playbackItems).toEqual([
      {
        id: '301',
        name: '样片一.mp4',
        resourceUrl: 'https://files.example/video-1.mp4',
        ossId: undefined,
        marks: [
          {
            id: '403',
            resourceId: '301',
            startTimeText: '00:00:02',
            endTimeText: '00:00:04',
            markContent: '第一视频更早标注',
            startSeconds: 2
          },
          {
            id: '402',
            resourceId: '301',
            startTimeText: '00:00:05',
            endTimeText: '00:00:12',
            markContent: '第一视频标注',
            startSeconds: 5
          }
        ]
      },
      {
        id: '302',
        name: '样片二.mp4',
        resourceUrl: 'https://files.example/video-2.mp4',
        ossId: undefined,
        marks: [
          {
            id: '401',
            resourceId: '302',
            startTimeText: '00:00:08',
            endTimeText: '00:00:10',
            markContent: '第二视频标注',
            startSeconds: 8
          }
        ]
      }
    ]);
  });

  it('parses HH:mm:ss video mark time text into seconds', () => {
    expect(parseVideoTimeTextToSeconds('00:00:05')).toBe(5);
    expect(parseVideoTimeTextToSeconds('00:10:15')).toBe(615);
    expect(parseVideoTimeTextToSeconds('3:7')).toBeUndefined();
  });
});
