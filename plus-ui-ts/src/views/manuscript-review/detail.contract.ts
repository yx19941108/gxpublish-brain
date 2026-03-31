import type {
  ManuscriptReviewDetailVO,
  ManuscriptReviewPermissionMatrixVO,
  ManuscriptReviewTimelineItemVO,
  ManuscriptReviewVideoMarkItemVO
} from '@/api/manuscript-review/types';
import type {
  ManuscriptReviewDetailAction,
  ManuscriptReviewDetailReadableSource,
  ManuscriptReviewDetailViewRole,
  ManuscriptReviewReadableSummaryItem
} from '@/types/manuscript-review/detail';

type UnknownRecord = Record<string, unknown>;

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
  { label: '流程状态', pick: (detail) => detail.businessStatusLabel },
  { label: '当前节点', pick: (detail) => detail.currentNodeLabel },
  { label: '发起人', pick: (detail) => detail.initiatorName },
  { label: '最近更新时间', pick: (detail) => detail.updateTime },
  { label: '流程类型', pick: (detail) => detail.processTypeLabel },
  { label: '系统稿件号', pick: (detail) => detail.manuscriptCode },
  { label: '外部稿件编号', pick: (detail) => detail.externalManuscriptCode },
  { label: '标题', pick: (detail) => detail.title },
  { label: '所属媒体/栏目', pick: (detail) => detail.mediaChannel },
  { label: '报送部门', pick: (detail) => detail.submitDepartment },
  { label: '作者', pick: (detail) => detail.authorName },
  { label: '说明', pick: (detail) => detail.remark },
  { label: '正文摘要', pick: (detail) => detail.contentSummary }
];

const EMPTY_TEXT = '--';
const SUCCESS_CODES = new Set([0, '0', 200, '200']);
const API_BASE_URL = import.meta.env.VITE_APP_BASE_API ?? '';

export interface ManuscriptReviewLedgerRow {
  id: string;
  manuscriptCode: string;
  title: string;
  processTypeLabel: string;
  mediaChannel: string;
  businessStatusLabel: string;
  currentNodeLabel: string;
  initiatorName: string;
  updateTime: string;
}

export interface ManuscriptReviewHistoryItem {
  id: string;
  timeLabel: string;
  actionType?: string;
  actionLabel: string;
  operatorName: string;
  remark?: string;
  relatedResourceId?: string;
  relatedResourceType?: string;
  actionPrefix?: string;
  actionLinkLabel?: string;
  actionSuffix?: string;
  actionLinkOssId?: string;
  actionLinkHref?: string;
}

export interface ManuscriptReviewResourceItem {
  id: string;
  resourceType: string;
  typeLabel: string;
  name: string;
  statusLabel?: string;
  metaLines?: string[];
  note?: string;
  href?: string;
  resourceUrl?: string;
  ossId?: string;
  resourceId?: string;
  startTimeText?: string;
  endTimeText?: string;
  markContent?: string;
}

export interface ManuscriptReviewVideoPlaybackMarkItem {
  id: string;
  resourceId: string;
  startTimeText: string;
  endTimeText?: string;
  markContent: string;
  startSeconds: number;
}

export interface ManuscriptReviewVideoPlaybackItem {
  id: string;
  name: string;
  resourceUrl?: string;
  ossId?: string;
  marks: ManuscriptReviewVideoPlaybackMarkItem[];
}

export interface ManuscriptReviewDetailReadableViewModel {
  actionRole: ManuscriptReviewDetailViewRole;
  detail: ManuscriptReviewDetailReadableSource;
  historyItems: ManuscriptReviewHistoryItem[];
  resourceItems: ManuscriptReviewResourceItem[];
}

const isRecord = (value: unknown): value is UnknownRecord =>
  typeof value === 'object' && value !== null && !Array.isArray(value);

const toTrimmedString = (value: unknown): string | undefined => {
  if (value === undefined || value === null) {
    return undefined;
  }
  const text = String(value).trim();
  return text ? text : undefined;
};

const pickMaybeText = (source: UnknownRecord, keys: string[]): string | undefined => {
  for (const key of keys) {
    const value = toTrimmedString(source[key]);
    if (value) {
      return value;
    }
  }
  return undefined;
};

const pickText = (source: UnknownRecord, keys: string[], fallback = EMPTY_TEXT): string =>
  pickMaybeText(source, keys) ?? fallback;

const pickBoolean = (source: UnknownRecord, keys: string[]): boolean => {
  for (const key of keys) {
    const value = source[key];
    if (typeof value === 'boolean') {
      return value;
    }
    if (value === 'true' || value === '1' || value === 1) {
      return true;
    }
  }
  return false;
};

const pickMaybeRecord = (source: UnknownRecord, keys: string[]): UnknownRecord | undefined => {
  for (const key of keys) {
    const value = source[key];
    if (isRecord(value)) {
      return value;
    }
  }
  return undefined;
};

const unwrapReadablePayload = (payload: unknown): unknown => {
  if (!isRecord(payload)) {
    return payload;
  }

  if ('success' in payload && payload.success === false) {
    throw new Error('BUSINESS_RESPONSE_FAILED');
  }

  if ('code' in payload && payload.code !== undefined && !SUCCESS_CODES.has(payload.code)) {
    throw new Error('BUSINESS_RESPONSE_FAILED');
  }

  if (payload.data !== undefined && payload.data !== null) {
    return payload.data;
  }

  if (payload.result !== undefined && payload.result !== null) {
    return payload.result;
  }

  return payload;
};

const extractCollection = (payload: unknown, keys: string[]): unknown[] => {
  const unwrapped = unwrapReadablePayload(payload);
  if (Array.isArray(unwrapped)) {
    return unwrapped;
  }

  if (!isRecord(unwrapped)) {
    return [];
  }

  for (const key of keys) {
    const value = unwrapped[key];
    if (Array.isArray(value)) {
      return value;
    }
  }

  return [];
};

const extractRecord = (payload: unknown): UnknownRecord => {
  const unwrapped = unwrapReadablePayload(payload);
  if (Array.isArray(unwrapped)) {
    return isRecord(unwrapped[0]) ? unwrapped[0] : {};
  }
  return isRecord(unwrapped) ? unwrapped : {};
};

const resolveInternalResourceUrl = (value?: string): string | undefined => {
  const normalized = value?.trim();
  if (!normalized) {
    return undefined;
  }
  if (/^https?:\/\//i.test(normalized)) {
    return normalized;
  }
  if (!normalized.startsWith('/')) {
    return normalized;
  }
  if (API_BASE_URL && normalized.startsWith(API_BASE_URL + '/')) {
    return normalized;
  }
  return `${API_BASE_URL}${normalized}`;
};

const buildResourceMetaLines = (
  operatorName?: string,
  operatorTime?: string,
  fileSizeLabel?: string
): string[] => {
  const lines = [`操作人：${operatorName?.trim() || '--'}`, `操作时间：${operatorTime?.trim() || '--'}`];
  if (fileSizeLabel !== undefined) {
    lines.push(`文件大小：${fileSizeLabel.trim() || '--'}`);
  }
  return lines;
};

const normalizePermissionMatrix = (source: UnknownRecord): ManuscriptReviewPermissionMatrixVO | undefined => {
  const permissionSource = pickMaybeRecord(source, ['permissionMatrix']);
  if (!permissionSource) {
    return undefined;
  }

  return {
    isInitiator: pickBoolean(permissionSource, ['isInitiator']),
    isCurrentApprover: pickBoolean(permissionSource, ['isCurrentApprover']),
    isHistoryParticipant: pickBoolean(permissionSource, ['isHistoryParticipant']),
    canView: pickBoolean(permissionSource, ['canView']),
    canEdit: pickBoolean(permissionSource, ['canEdit']),
    canResubmit: pickBoolean(permissionSource, ['canResubmit']),
    canCancel: pickBoolean(permissionSource, ['canCancel']),
    canGotoApproval: pickBoolean(permissionSource, ['canGotoApproval']),
    buttonReason: pickMaybeText(permissionSource, ['buttonReason'])
  };
};

const normalizeTimelinePayload = (source: UnknownRecord): ManuscriptReviewTimelineItemVO[] =>
  extractCollection(source, ['timelineItems', 'timeline', 'historyItems', 'historyList', 'history']).map((item) => {
    const timelineItem = isRecord(item) ? item : {};
    return {
      eventTime: pickText(timelineItem, ['eventTime', 'createTime', 'timeLabel']),
      eventType: pickMaybeText(timelineItem, ['eventType']),
      eventTypeLabel: pickMaybeText(timelineItem, ['eventTypeLabel', 'actionLabel']),
      eventCode: pickMaybeText(timelineItem, ['eventCode']),
      eventText: pickText(timelineItem, ['eventText', 'text', 'actionLabel']),
      operatorName: pickMaybeText(timelineItem, ['operatorName']),
      relatedNode: pickMaybeText(timelineItem, ['relatedNode']),
      relatedResourceName: pickMaybeText(timelineItem, ['relatedResourceName']),
      relatedResourceId: pickMaybeText(timelineItem, ['relatedResourceId']),
      relatedResourceOssId: pickMaybeText(timelineItem, ['relatedResourceOssId']),
      relatedResourceType: pickMaybeText(timelineItem, ['relatedResourceType']),
      relatedResourceUrl: resolveInternalResourceUrl(pickMaybeText(timelineItem, ['relatedResourceUrl'])),
      relatedExternalUrl: pickMaybeText(timelineItem, ['relatedExternalUrl']),
      statusLabel: pickMaybeText(timelineItem, ['statusLabel']),
      diffSummary: pickMaybeText(timelineItem, ['diffSummary', 'remark'])
    };
  });

const normalizeVideoMarkList = (payload: unknown): ManuscriptReviewVideoMarkItemVO[] =>
  extractCollection(payload, ['videoMarkList', 'videoMarkers', 'markerList']).map((item, index) => {
    const marker = isRecord(item) ? item : {};
    return {
      id: pickText(marker, ['id', 'markerId'], `video-mark-${index + 1}`),
      resourceId: pickMaybeText(marker, ['resourceId', 'videoAttachmentId', 'attachmentId']),
      startTimeText: pickText(marker, ['startTimeText', 'startTime']),
      endTimeText: pickMaybeText(marker, ['endTimeText', 'endTime']),
      markContent: pickText(marker, ['markContent', 'markerNote', 'note']),
      operatorName: pickMaybeText(marker, ['operatorName']),
      operatorTime: pickMaybeText(marker, ['operatorTime'])
    };
  });

const normalizeResourceList = (
  payload: unknown,
  keys: string[],
  fallbackType: string
) =>
  extractCollection(payload, keys).map((item, index) => {
    const resource = isRecord(item) ? item : {};
    return {
      id: pickText(resource, ['id', 'resourceId'], `${fallbackType}-${index + 1}`),
      ossId: pickMaybeText(resource, ['ossId']),
      resourceType: pickMaybeText(resource, ['resourceType']) ?? fallbackType,
      resourceTypeLabel: pickMaybeText(resource, ['resourceTypeLabel']) ?? fallbackType,
      displayName: pickText(resource, ['displayName', 'fileName', 'linkTitle', 'name'], `${fallbackType}-${index + 1}`),
      externalUrl: pickMaybeText(resource, ['externalUrl', 'linkUrl']),
      createdTime: pickMaybeText(resource, ['createdTime', 'createTime']),
      operatorName: pickMaybeText(resource, ['operatorName']),
      operatorTime: pickMaybeText(resource, ['operatorTime']),
      fileSizeBytes: pickMaybeText(resource, ['fileSizeBytes', 'fileSize']),
      fileSizeLabel: pickMaybeText(resource, ['fileSizeLabel']),
      resourceUrl: resolveInternalResourceUrl(pickMaybeText(resource, ['resourceUrl', 'fileUrl']))
    };
  });

const splitTimelineActionLink = (timelineItem: ManuscriptReviewTimelineItemVO) => {
  const resourceName = timelineItem.relatedResourceName;
  if (!resourceName) {
    return undefined;
  }
  const quotedName = `《${resourceName}》`;
  const actionText = timelineItem.eventText ?? '';
  const nameIndex = actionText.indexOf(quotedName);
  if (nameIndex < 0) {
    return undefined;
  }
  const actionLinkHref = timelineItem.relatedExternalUrl ?? timelineItem.relatedResourceUrl;
  const actionLinkOssId = timelineItem.relatedResourceOssId == null ? undefined : String(timelineItem.relatedResourceOssId);
  if (!actionLinkHref && !actionLinkOssId) {
    return undefined;
  }
  return {
    actionPrefix: actionText.slice(0, nameIndex),
    actionLinkLabel: quotedName,
    actionSuffix: actionText.slice(nameIndex + quotedName.length),
    actionLinkHref,
    actionLinkOssId
  };
};

const normalizeHistoryItems = (detail: ManuscriptReviewDetailReadableSource): ManuscriptReviewHistoryItem[] =>
  (detail.timelineItems ?? []).map((item, index) => {
    const timelineItem = item as ManuscriptReviewTimelineItemVO;
    const actionLink = splitTimelineActionLink(timelineItem);
    return {
      id: `timeline-${index + 1}`,
      timeLabel: timelineItem.eventTime,
      actionType: timelineItem.eventCode,
      actionLabel: timelineItem.eventText,
      operatorName: timelineItem.operatorName ?? '',
      remark: timelineItem.diffSummary,
      relatedResourceId:
        timelineItem.relatedResourceId == null ? undefined : String(timelineItem.relatedResourceId),
      relatedResourceType: timelineItem.relatedResourceType ?? undefined,
      actionPrefix: actionLink?.actionPrefix,
      actionLinkLabel: actionLink?.actionLinkLabel,
      actionSuffix: actionLink?.actionSuffix,
      actionLinkOssId: actionLink?.actionLinkOssId,
      actionLinkHref: actionLink?.actionLinkHref
    };
  });

const normalizeResourceItems = (detail: ManuscriptReviewDetailReadableSource): ManuscriptReviewResourceItem[] => {
  const resources: ManuscriptReviewResourceItem[] = [];

  for (const attachment of detail.attachmentList ?? []) {
    resources.push({
      id: String(attachment.id),
      resourceType: attachment.resourceType ?? 'ATTACHMENT',
      typeLabel: attachment.resourceTypeLabel ?? '附件',
      name: attachment.displayName,
      statusLabel: '当前有效',
      metaLines: buildResourceMetaLines(attachment.operatorName, attachment.operatorTime, attachment.fileSizeLabel),
      resourceUrl: attachment.resourceUrl,
      ossId: attachment.ossId == null ? undefined : String(attachment.ossId)
    });
  }

  for (const link of detail.externalLinkList ?? []) {
    resources.push({
      id: String(link.id),
      resourceType: link.resourceType ?? 'EXTERNAL_LINK',
      typeLabel: link.resourceTypeLabel ?? '外链',
      name: link.displayName,
      statusLabel: '当前有效',
      metaLines: buildResourceMetaLines(link.operatorName, link.operatorTime),
      note: link.externalUrl,
      href: link.externalUrl
    });
  }

  for (const video of detail.videoList ?? []) {
    resources.push({
      id: String(video.id),
      resourceType: video.resourceType ?? 'VIDEO',
      typeLabel: video.resourceTypeLabel ?? '视频',
      name: video.displayName,
      statusLabel: '当前有效',
      metaLines: buildResourceMetaLines(video.operatorName, video.operatorTime, video.fileSizeLabel),
      resourceUrl: video.resourceUrl,
      ossId: video.ossId == null ? undefined : String(video.ossId)
    });
  }

  for (const marker of detail.videoMarkList ?? []) {
    resources.push({
      id: String(marker.id),
      resourceType: 'VIDEO_MARK',
      typeLabel: '视频时间标注',
      name: marker.startTimeText,
      statusLabel: '当前有效',
      metaLines: buildResourceMetaLines(marker.operatorName, marker.operatorTime),
      note: marker.markContent,
      resourceId: marker.resourceId == null ? undefined : String(marker.resourceId),
      startTimeText: marker.startTimeText,
      endTimeText: marker.endTimeText,
      markContent: marker.markContent
    });
  }

  return resources;
};

export const parseVideoTimeTextToSeconds = (value?: string): number | undefined => {
  if (!value) {
    return undefined;
  }

  const match = value.trim().match(/^(\d{2}):(\d{2}):(\d{2})$/);
  if (!match) {
    return undefined;
  }

  const hours = Number(match[1]);
  const minutes = Number(match[2]);
  const seconds = Number(match[3]);

  if ([hours, minutes, seconds].some((part) => Number.isNaN(part))) {
    return undefined;
  }

  return hours * 3600 + minutes * 60 + seconds;
};

export const buildVideoPlaybackItems = (
  detail: ManuscriptReviewDetailReadableSource
): ManuscriptReviewVideoPlaybackItem[] => {
  const markMap = new Map<string, ManuscriptReviewVideoPlaybackMarkItem[]>();

  for (const marker of detail.videoMarkList ?? []) {
    const resourceId = marker.resourceId == null ? undefined : String(marker.resourceId);
    const startSeconds = parseVideoTimeTextToSeconds(marker.startTimeText);
    if (!resourceId || startSeconds == null) {
      continue;
    }

    const markItem: ManuscriptReviewVideoPlaybackMarkItem = {
      id: String(marker.id),
      resourceId,
      startTimeText: marker.startTimeText,
      endTimeText: marker.endTimeText,
      markContent: marker.markContent,
      startSeconds
    };
    const currentMarks = markMap.get(resourceId) ?? [];
    currentMarks.push(markItem);
    markMap.set(resourceId, currentMarks);
  }

  return (detail.videoList ?? []).map((video) => {
    const id = String(video.id);
    const marks = (markMap.get(id) ?? []).sort((left, right) => left.startSeconds - right.startSeconds);

    return {
      id,
      name: video.displayName,
      resourceUrl: video.resourceUrl,
      ossId: video.ossId == null ? undefined : String(video.ossId),
      marks
    };
  });
};

const resolveDetailViewRole = (
  permissionMatrix?: ManuscriptReviewPermissionMatrixVO
): ManuscriptReviewDetailViewRole => {
  if (permissionMatrix?.canGotoApproval || permissionMatrix?.isCurrentApprover) {
    return 'CURRENT_APPROVER';
  }

  if (permissionMatrix?.canResubmit) {
    return 'RETURNED_INITIATOR';
  }

  return 'HISTORY_PARTICIPANT';
};

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

export const normalizeLedgerRows = (payload: unknown): ManuscriptReviewLedgerRow[] =>
  extractCollection(payload, ['rows', 'list', 'records', 'items']).map((item, index) => {
    const row = isRecord(item) ? item : {};

    return {
      id: pickText(row, ['id', 'reviewId', 'review_id'], `review-${index + 1}`),
      manuscriptCode: pickText(row, ['manuscriptCode', 'manuscript_code', 'manuscriptNo']),
      title: pickText(row, ['title']),
      processTypeLabel: pickText(row, ['processTypeLabel', 'processTypeName', 'reviewTypeLabel']),
      mediaChannel: pickText(row, ['mediaChannel', 'mediaChannelLabel', 'mediaChannelName', 'mediaColumnLabel']),
      businessStatusLabel: pickText(row, ['businessStatusLabel', 'flowStatusLabel', 'statusLabel']),
      currentNodeLabel: pickText(row, ['currentNodeLabel', 'currentTaskName', 'currentNodeName']),
      initiatorName: pickText(row, ['initiatorName', 'submitterName', 'creatorName']),
      updateTime: pickText(row, ['updateTime', 'latestUpdatedAt', 'updatedAt'])
    };
  });

export const normalizeDetailViewModel = (payload: unknown): ManuscriptReviewDetailReadableViewModel => {
  const source = extractRecord(payload);
  const permissionMatrix = normalizePermissionMatrix(source);

  const detail: ManuscriptReviewDetailVO = {
    id: pickText(source, ['id', 'reviewId', 'review_id']),
    processType: pickMaybeText(source, ['processType']),
    processTypeLabel: pickText(source, ['processTypeLabel', 'processTypeName', 'reviewTypeLabel']),
    manuscriptCode: pickText(source, ['manuscriptCode', 'manuscript_code', 'manuscriptNo']),
    externalManuscriptCode:
      pickMaybeText(source, ['externalManuscriptCode', 'externalCode', 'externalManuscriptNo']),
    title: pickText(source, ['title']),
    mediaChannel: pickText(source, ['mediaChannel', 'mediaChannelLabel', 'mediaChannelName', 'mediaColumnLabel']),
    submitDepartment: pickText(source, ['submitDepartment', 'submitterDeptName', 'submitDeptName', 'departmentName']),
    authorName: pickMaybeText(source, ['authorName', 'authorNames', 'authors']),
    remark: pickMaybeText(source, ['remark', 'note', 'description']),
    contentBody: pickMaybeText(source, ['contentBody', 'content']),
    contentSummary: pickMaybeText(source, ['contentSummary', 'contentPreview', 'content', 'contentBody']),
    businessStatus: pickMaybeText(source, ['businessStatus']),
    businessStatusLabel: pickText(source, ['businessStatusLabel', 'flowStatusLabel', 'statusLabel']),
    currentNodeCode: pickMaybeText(source, ['currentNodeCode']),
    currentNodeLabel: pickText(source, ['currentNodeLabel', 'currentTaskName', 'currentNodeName']),
    initiatorName: pickText(source, ['initiatorName', 'submitterName', 'creatorName']),
    firstSubmitTime: pickMaybeText(source, ['firstSubmitTime']),
    latestSubmitTime: pickMaybeText(source, ['latestSubmitTime']),
    updateTime: pickText(source, ['updateTime', 'latestUpdatedAt', 'updatedAt']),
    attachmentList: normalizeResourceList(source, ['attachmentList', 'attachments'], 'ATTACHMENT'),
    externalLinkList: normalizeResourceList(source, ['externalLinkList', 'externalLinks', 'linkList', 'links'], 'EXTERNAL_LINK'),
    videoList: normalizeResourceList(source, ['videoList', 'videos'], 'VIDEO'),
    videoMarkList: normalizeVideoMarkList(source),
    timelineItems: normalizeTimelinePayload(source),
    permissionMatrix
  };

  return {
    actionRole: resolveDetailViewRole(permissionMatrix),
    detail,
    historyItems: normalizeHistoryItems(detail),
    resourceItems: normalizeResourceItems(detail)
  };
};
