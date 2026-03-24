export interface ManuscriptReviewListQuery extends PageQuery {
  keyword?: string;
  processType?: string;
  mediaChannel?: string;
  businessStatus?: string;
  currentNodeCode?: string;
  startTimeFrom?: string;
  startTimeTo?: string;
}

export interface ManuscriptReviewSaveCommand extends BaseEntity {
  id?: string | number;
  processType?: string;
  externalManuscriptCode?: string;
  title?: string;
  mediaChannel?: string;
  submitDepartment?: string;
  authorName?: string;
  remark?: string;
  contentBody?: string;
}

export interface ManuscriptReviewReviewIdCommand {
  id: string | number;
}

export interface ManuscriptReviewCancelCommand extends ManuscriptReviewReviewIdCommand {
  reason?: string;
}

export interface ManuscriptReviewResourceCreateCommand {
  reviewId: string | number;
  resourceType: string;
  displayName: string;
  ossId?: string | number;
  externalUrl?: string;
}

export interface ManuscriptReviewResourceDisableCommand {
  resourceId: string | number;
  disabledReason?: string;
}

export interface ManuscriptReviewVideoMarkCreateCommand {
  reviewId: string | number;
  resourceId: string | number;
  startTimeText: string;
  endTimeText?: string;
  markContent: string;
}

export interface ManuscriptReviewPermissionMatrixVO {
  isInitiator?: boolean;
  isCurrentApprover?: boolean;
  isHistoryParticipant?: boolean;
  canView?: boolean;
  canEdit?: boolean;
  canResubmit?: boolean;
  canCancel?: boolean;
  canGotoApproval?: boolean;
  buttonReason?: string;
}

export interface ManuscriptReviewResourceItemVO {
  id: string | number;
  resourceType?: string;
  resourceTypeLabel?: string;
  displayName: string;
  externalUrl?: string;
  createdTime?: string;
  resourceUrl?: string;
}

export interface ManuscriptReviewVideoMarkItemVO {
  id: string | number;
  startTimeText: string;
  endTimeText?: string;
  markContent: string;
}

export interface ManuscriptReviewTimelineItemVO {
  eventTime: string;
  eventType?: string;
  eventTypeLabel?: string;
  eventCode?: string;
  eventText: string;
  operatorName?: string;
  relatedNode?: string;
  relatedResourceName?: string;
  statusLabel?: string;
  diffSummary?: string;
}

export interface ManuscriptReviewListItemVO {
  id: string | number;
  processType?: string;
  processTypeLabel?: string;
  manuscriptCode: string;
  title: string;
  mediaChannel?: string;
  businessStatus?: string;
  businessStatusLabel?: string;
  currentNodeCode?: string;
  currentNodeLabel?: string;
  initiatorName?: string;
  updateTime?: string;
}

export interface ManuscriptReviewListPageVO {
  total: number;
  rows: ManuscriptReviewListItemVO[];
}

export interface ManuscriptReviewDetailVO {
  id: string | number;
  processType?: string;
  processTypeLabel?: string;
  manuscriptCode: string;
  externalManuscriptCode?: string;
  title: string;
  mediaChannel?: string;
  submitDepartment: string;
  authorName?: string;
  remark?: string;
  contentBody?: string;
  contentSummary?: string;
  businessStatus?: string;
  businessStatusLabel: string;
  currentNodeCode?: string;
  currentNodeLabel: string;
  initiatorName?: string;
  firstSubmitTime?: string;
  latestSubmitTime?: string;
  updateTime?: string;
  attachmentList?: ManuscriptReviewResourceItemVO[] | null;
  externalLinkList?: ManuscriptReviewResourceItemVO[] | null;
  videoList?: ManuscriptReviewResourceItemVO[] | null;
  videoMarkList?: ManuscriptReviewVideoMarkItemVO[] | null;
  timelineItems?: ManuscriptReviewTimelineItemVO[] | null;
  permissionMatrix?: ManuscriptReviewPermissionMatrixVO | null;
}
