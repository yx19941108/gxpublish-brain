import { describe, expect, it } from 'vitest';
import { readFile } from 'node:fs/promises';

import { buildDetailActionBar, buildReadableSummary, normalizeDetailViewModel } from './detail.contract';

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

  it('detail view source keeps hidden reviewId anchor, reserves history/resources shells, and removes demo copy', async () => {
    const detailSource = await readFile(new URL('./detail.vue', import.meta.url), 'utf-8');

    expect(detailSource).toContain('审校详情');
    expect(detailSource).toContain('/manuscript-review/readable/detail');
    expect(detailSource).toContain("from '@/utils/request'");
    expect(detailSource).not.toContain('fetch(');
    expect(detailSource).toContain('data-testid="manuscript-review-hidden-reviewId"');
    expect(detailSource).toContain('data-testid="manuscript-review-history-shell"');
    expect(detailSource).toContain('data-testid="manuscript-review-resource-shell"');
    expect(detailSource).not.toContain('后续再接入真实接口与历史、资源区域');

    // reviewId is allowed for routing/API only, must not show as readable text.
    expect(detailSource).not.toContain('{{ reviewId }}');
    expect(detailSource).not.toContain('reviewId：');
  });

  it('ledger view source shows fixed column headers with flow status + current node, and no delete entry', async () => {
    const ledgerSource = await readFile(new URL('./index.vue', import.meta.url), 'utf-8');

    expect(ledgerSource).toContain('/manuscript-review/readable/ledger');
    expect(ledgerSource).toContain("from '@/utils/request'");
    expect(ledgerSource).not.toContain('fetch(');
    expect(ledgerSource).toContain('data-testid="ledger-col-flowStatus"');
    expect(ledgerSource).toContain('data-testid="ledger-col-currentNode"');
    expect(ledgerSource).toContain('流程状态');
    expect(ledgerSource).toContain('当前节点');
    expect(ledgerSource).not.toContain('删除');
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

  it('normalizes nested readable payload without losing summary, history, resources, or action role', () => {
    const viewModel = normalizeDetailViewModel({
      code: 200,
      data: {
        reviewId: 9002,
        summaryCard: {
          flowStatusLabel: '已退回',
          currentNodeLabel: '待发起人处理',
          initiatorName: '张三',
          updateTime: '2026-03-23 11:22:33'
        },
        manuscriptCard: {
          processTypeLabel: '审核流程',
          manuscriptCode: 'SH20260321002',
          externalManuscriptCode: 'EXT-001',
          title: '稿件标题',
          mediaChannelLabel: '新华社/要闻',
          submitterDeptName: '总编室',
          authorNames: '张三、李四',
          note: '补充说明',
          content: '正文内容'
        },
        actionBar: {
          canModify: true,
          actions: ['修改', '去审批', '返回']
        },
        timeline: [{ createTime: '2026-03-23 11:20:00', text: '张三新增了流程。' }],
        resources: {
          currentAttachments: [{ fileName: '送审单.pdf', fileUrl: 'https://files.example/a.pdf' }],
          currentExternalLinks: [{ linkTitle: '素材参考', linkUrl: 'https://example.com/ref' }],
          currentVideos: [{ fileName: '样片.mp4', duration: '00:10:00' }],
          historyAttachments: [{ fileName: '旧附件.pdf', disabledTime: '2026-03-23 11:00:00' }],
          historyExternalLinks: [{ linkTitle: '旧链接', disabledTime: '2026-03-23 11:05:00' }],
          historyVideoMarkers: [{ startTime: '00:00:15', markerNote: '停用标注', disabledTime: '2026-03-23 11:07:00' }]
        }
      }
    });

    expect(viewModel.actionRole).toBe('CURRENT_APPROVER');
    expect(viewModel.detail.flowStatusLabel).toBe('已退回');
    expect(viewModel.detail.currentNodeLabel).toBe('待发起人处理');
    expect(viewModel.detail.initiatorName).toBe('张三');
    expect(viewModel.detail.processTypeLabel).toBe('审核流程');
    expect(viewModel.detail.manuscriptCode).toBe('SH20260321002');
    expect(viewModel.detail.externalManuscriptCode).toBe('EXT-001');
    expect(viewModel.detail.title).toBe('稿件标题');
    expect(viewModel.detail.mediaChannelLabel).toBe('新华社/要闻');
    expect(viewModel.detail.submitterDeptName).toBe('总编室');
    expect(viewModel.detail.authorNames).toBe('张三、李四');
    expect(viewModel.detail.note).toBe('补充说明');
    expect(viewModel.detail.contentPreview).toBe('正文内容');
    expect(viewModel.historyItems).toEqual([
      {
        id: 'history-1',
        timeLabel: '2026-03-23 11:20:00',
        actionLabel: '张三新增了流程。',
        operatorName: ''
      }
    ]);
    expect(viewModel.resourceItems).toEqual([
      {
        id: '当前附件-1',
        typeLabel: '当前附件',
        name: '送审单.pdf',
        statusLabel: '当前有效',
        note: 'https://files.example/a.pdf'
      },
      {
        id: '当前外链-1',
        typeLabel: '当前外链',
        name: '素材参考',
        statusLabel: '当前有效',
        note: 'https://example.com/ref'
      },
      {
        id: '当前视频-1',
        typeLabel: '当前视频',
        name: '样片.mp4',
        statusLabel: '当前有效',
        note: '时长：00:10:00'
      },
      {
        id: '历史附件-1',
        typeLabel: '历史附件',
        name: '旧附件.pdf',
        statusLabel: '已停用',
        note: '停用时间：2026-03-23 11:00:00'
      },
      {
        id: '历史外链-1',
        typeLabel: '历史外链',
        name: '旧链接',
        statusLabel: '已停用',
        note: '停用时间：2026-03-23 11:05:00'
      },
      {
        id: '历史视频时间标注-1',
        typeLabel: '历史视频时间标注',
        name: '00:00:15',
        statusLabel: '已停用',
        note: '停用标注'
      }
    ]);
  });
});
