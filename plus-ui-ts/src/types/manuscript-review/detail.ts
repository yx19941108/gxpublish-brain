export type ManuscriptReviewDetailViewRole =
  | 'CURRENT_APPROVER'
  | 'RETURNED_INITIATOR'
  | 'HISTORY_PARTICIPANT';

export type ManuscriptReviewDetailActionKey = 'edit' | 'approve' | 'resubmit' | 'back';

export interface ManuscriptReviewDetailAction {
  key: ManuscriptReviewDetailActionKey;
  label: string;
}

export interface ManuscriptReviewReadableSummaryItem {
  label: string;
  value: string;
}

export interface ManuscriptReviewDetailReadableSource {
  flowStatusLabel: string;
  currentNodeLabel: string;
  initiatorName: string;
  latestUpdatedAt: string;
  processTypeLabel: string;
  manuscriptCode: string;
  externalManuscriptCode?: string;
  title: string;
  mediaChannelLabel: string;
  submitterDeptName: string;
  authorNames?: string;
  note?: string;
  contentPreview: string;
  technicalId?: string;
  initiatorAccount?: string;
  flowStatusCode?: string;
  currentNodeCode?: string;
  processTypeCode?: string;
  roleKey?: string;
}
