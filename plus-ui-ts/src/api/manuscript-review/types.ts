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

export interface ManuscriptReviewAttachmentDraftItem {
  displayName: string;
  ossId: string | number;
  resourceType?: string;
}

export interface ManuscriptReviewExternalLinkDraftItem {
  displayName: string;
  externalUrl: string;
  resourceType?: string;
}

export interface ManuscriptReviewIntegratedSubmitCommand extends ManuscriptReviewSaveCommand {
  attachmentResources?: ManuscriptReviewAttachmentDraftItem[];
  externalLinks?: ManuscriptReviewExternalLinkDraftItem[];
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

export interface ManuscriptReviewResourceEnableCommand {
  resourceId: string | number;
}

export interface ManuscriptReviewVideoMarkCreateCommand {
  reviewId: string | number;
  resourceId: string | number;
  startTimeText: string;
  endTimeText?: string;
  markContent: string;
}

export interface ManuscriptReviewVideoMarkDisableCommand {
  markId: string | number;
  disabledReason?: string;
}

export interface ManuscriptReviewVideoMarkEnableCommand {
  markId: string | number;
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
  ossId?: string | number;
  resourceType?: string;
  resourceTypeLabel?: string;
  displayName: string;
  externalUrl?: string;
  createdTime?: string;
  operatorName?: string;
  operatorTime?: string;
  fileSizeBytes?: string | number;
  fileSizeLabel?: string;
  resourceUrl?: string;
}

export interface ManuscriptReviewPreviewTicketVO {
  resourceId: string | number;
  resourceUrl: string;
  previewToken: string;
  expireAtEpochSecond: number;
}

export interface ManuscriptReviewVideoMarkItemVO {
  id: string | number;
  resourceId?: string | number;
  startTimeText: string;
  endTimeText?: string;
  markContent: string;
  operatorName?: string;
  operatorTime?: string;
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
  relatedResourceId?: string | number;
  relatedResourceOssId?: string | number;
  relatedResourceType?: string;
  relatedResourceUrl?: string;
  relatedExternalUrl?: string;
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
