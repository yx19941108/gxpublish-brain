import { describe, expect, it } from 'vitest';

import { buildDetailActionBar, buildReadableSummary } from './detail.contract';

describe('T07_Frontend_DetailActions_AndReadableDisplaySpec', () => {
  const baseDetail = {
    flowStatusLabel: '审批中',
    currentNodeLabel: '待二级审批',
    initiatorName: '张三',
    latestUpdatedAt: '2026-03-21 09:30:00',
    processTypeLabel: '审核流程',
    manuscriptCode: 'SH20260321001',
    externalManuscriptCode: 'WX-2026-001',
    title: '广西春讯',
    mediaChannelLabel: '广西日报 / 要闻',
    submitterDeptName: '编委会',
    authorNames: '张三、李四',
    note: '需要核对视频字幕。',
    contentPreview: '这里是正文摘要。',
    technicalId: 'db-row-9001',
    initiatorAccount: 'initiator.account',
    flowStatusCode: 'STATUS_REVIEWING',
    currentNodeCode: 'NODE_LEVEL_2',
    processTypeCode: 'PROCESS_AUDIT',
    roleKey: 'role:manuscript-review:l2'
  };

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

  it('shows only back for history participants and keeps summary free of technical leakage', () => {
    const labels = buildDetailActionBar('HISTORY_PARTICIPANT').map((action) => action.label);
    const summary = buildReadableSummary(baseDetail);
    const summaryText = JSON.stringify(summary);

    expect(labels).toEqual(['返回']);
    expect(labels).not.toEqual(expect.arrayContaining(['删除', '草稿', '保存草稿', '修改', '去审批', '再次提交']));
    expect(summary).toEqual(
      expect.arrayContaining([
        { label: '流程状态', value: '审批中' },
        { label: '当前节点', value: '待二级审批' },
        { label: '发起人', value: '张三' },
        { label: '流程类型', value: '审核流程' },
        { label: '系统稿件号', value: 'SH20260321001' },
        { label: '标题', value: '广西春讯' }
      ])
    );
    expect(summaryText).not.toContain('db-row-9001');
    expect(summaryText).not.toContain('initiator.account');
    expect(summaryText).not.toContain('STATUS_REVIEWING');
    expect(summaryText).not.toContain('NODE_LEVEL_2');
    expect(summaryText).not.toContain('PROCESS_AUDIT');
    expect(summaryText).not.toContain('role:manuscript-review:l2');
  });
});
