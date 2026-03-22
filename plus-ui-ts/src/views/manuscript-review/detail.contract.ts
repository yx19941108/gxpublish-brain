import type {
  ManuscriptReviewDetailAction,
  ManuscriptReviewDetailReadableSource,
  ManuscriptReviewDetailViewRole,
  ManuscriptReviewReadableSummaryItem
} from '@/types/manuscript-review/detail';

const ACTIONS_BY_ROLE: Record<ManuscriptReviewDetailViewRole, ManuscriptReviewDetailAction[]> = {
  CURRENT_APPROVER: [
    { key: 'edit', label: '修改' },
    { key: 'approve', label: '去审批' },
    { key: 'back', label: '返回' }
  ],
  RETURNED_INITIATOR: [
    { key: 'edit', label: '修改' },
    { key: 'resubmit', label: '再次提交' },
    { key: 'back', label: '返回' }
  ],
  HISTORY_PARTICIPANT: [{ key: 'back', label: '返回' }]
};

const SUMMARY_FIELDS: Array<{
  label: string;
  pick: (detail: ManuscriptReviewDetailReadableSource) => string | undefined;
}> = [
  { label: '流程状态', pick: (detail) => detail.flowStatusLabel },
  { label: '当前节点', pick: (detail) => detail.currentNodeLabel },
  { label: '发起人', pick: (detail) => detail.initiatorName },
  { label: '最近更新时间', pick: (detail) => detail.latestUpdatedAt },
  { label: '流程类型', pick: (detail) => detail.processTypeLabel },
  { label: '系统稿件号', pick: (detail) => detail.manuscriptCode },
  { label: '外部稿件编号', pick: (detail) => detail.externalManuscriptCode },
  { label: '标题', pick: (detail) => detail.title },
  { label: '所属媒体/栏目', pick: (detail) => detail.mediaChannelLabel },
  { label: '报送部门', pick: (detail) => detail.submitterDeptName },
  { label: '作者', pick: (detail) => detail.authorNames },
  { label: '说明', pick: (detail) => detail.note },
  { label: '正文摘要', pick: (detail) => detail.contentPreview }
];

export const buildDetailActionBar = (
  role: ManuscriptReviewDetailViewRole
): ManuscriptReviewDetailAction[] => ACTIONS_BY_ROLE[role];

export const buildReadableSummary = (
  detail: ManuscriptReviewDetailReadableSource
): ManuscriptReviewReadableSummaryItem[] =>
  SUMMARY_FIELDS.map((field) => {
    const value = field.pick(detail)?.trim();

    return value ? { label: field.label, value } : undefined;
  }).filter((item): item is ManuscriptReviewReadableSummaryItem => Boolean(item));

export const manuscriptReviewDetailShellDemo: ManuscriptReviewDetailReadableSource = {
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
  contentPreview: '这里是正文摘要。'
};
