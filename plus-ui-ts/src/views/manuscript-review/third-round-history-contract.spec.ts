import { describe, expect, it } from 'vitest';

import { normalizeDetailViewModel } from './detail.contract';

describe('T09_Frontend_ThirdRoundHistoryContract', () => {
  it('preserves disabled-history target information for history-only enable actions', () => {
    const viewModel = normalizeDetailViewModel({
      id: 9002,
      permissionMatrix: {
        canEdit: true
      },
      timelineItems: [
        {
          eventTime: '2026-03-31 09:00:00',
          eventCode: 'RESOURCE_DISABLE',
          eventText: '张三停用了附件《旧附件.pdf》。',
          operatorName: '张三',
          relatedResourceName: '旧附件.pdf',
          relatedResourceId: 3,
          relatedResourceType: 'ATTACHMENT',
          relatedResourceUrl: '/workflow/manuscript-review/resource/preview/3'
        },
        {
          eventTime: '2026-03-31 09:05:00',
          eventCode: 'VIDEO_MARK_DISABLE',
          eventText: '张三停用了视频标注《停用标注》（00:00:15）。',
          operatorName: '张三',
          relatedResourceName: '停用标注',
          relatedResourceId: 21,
          relatedResourceType: 'VIDEO_MARK'
        }
      ]
    });

    const firstItem = viewModel.historyItems[0] as any;
    const secondItem = viewModel.historyItems[1] as any;

    expect(firstItem.actionType).toBe('RESOURCE_DISABLE');
    expect(firstItem.relatedResourceId).toBe('3');
    expect(firstItem.relatedResourceType).toBe('ATTACHMENT');
    expect(secondItem.actionType).toBe('VIDEO_MARK_DISABLE');
    expect(secondItem.relatedResourceId).toBe('21');
    expect(secondItem.relatedResourceType).toBe('VIDEO_MARK');
  });
});
