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

const EMPTY_TEXT = '--';
const SUCCESS_CODES = new Set([0, '0', 200, '200']);

export interface ManuscriptReviewLedgerRow {
  reviewId: string;
  manuscriptCode: string;
  title: string;
  processTypeLabel: string;
  mediaChannelLabel: string;
  flowStatusLabel: string;
  currentNodeLabel: string;
  initiatorName: string;
  latestUpdatedAt: string;
}

export interface ManuscriptReviewHistoryItem {
  id: string;
  timeLabel: string;
  actionLabel: string;
  operatorName: string;
  remark?: string;
}

export interface ManuscriptReviewResourceItem {
  id: string;
  typeLabel: string;
  name: string;
  statusLabel?: string;
  note?: string;
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

const pickRecord = (source: UnknownRecord, keys: string[]): UnknownRecord =>
  pickMaybeRecord(source, keys) ?? {};

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

const normalizeHistoryItems = (source: UnknownRecord): ManuscriptReviewHistoryItem[] =>
  extractCollection(source, ['historyItems', 'historyList', 'history', 'timeline', 'records']).map(
    (item, index) => {
      const history = isRecord(item) ? item : {};
      const normalized: ManuscriptReviewHistoryItem = {
        id: pickText(history, ['historyId', 'id'], `history-${index + 1}`),
        timeLabel: pickText(history, [
          'timeLabel',
          'actionTime',
          'operateTime',
          'createdAt',
          'createTime',
          'updateTime'
        ]),
        actionLabel: pickText(history, ['actionLabel', 'operationLabel', 'content', 'title', 'text']),
        operatorName: pickMaybeText(history, ['operatorName', 'actorName', 'userName', 'displayName']) ?? ''
      };
      const remark = pickMaybeText(history, ['remark', 'description', 'note', 'opinion']);
      if (remark) {
        normalized.remark = remark;
      }
      return normalized;
    }
  );

const normalizeResourceCollection = (
  entries: unknown[],
  fallbackTypeLabel: string
): ManuscriptReviewResourceItem[] =>
  entries.map((item, index) => {
    const resource = isRecord(item) ? item : {};

    return {
      id: pickText(resource, ['resourceId', 'id'], `${fallbackTypeLabel}-${index + 1}`),
      typeLabel:
        pickMaybeText(resource, ['resourceTypeLabel', 'typeLabel', 'categoryLabel', 'kindLabel']) ??
        fallbackTypeLabel,
      name: pickText(resource, ['resourceName', 'name', 'title', 'fileName', 'linkTitle', 'url']),
      statusLabel: pickMaybeText(resource, ['statusLabel', 'lifecycleLabel', 'statusName']),
      note: pickMaybeText(resource, ['note', 'remark', 'description', 'url', 'markerText'])
    };
  });

const normalizeResourceItems = (source: UnknownRecord): ManuscriptReviewResourceItem[] => [
  ...normalizeResourceCollection(extractCollection(source, ['resourceItems', 'resourceList', 'resources']), '资源'),
  ...normalizeResourceCollection(extractCollection(source, ['attachments', 'attachmentList']), '附件'),
  ...normalizeResourceCollection(extractCollection(source, ['externalLinks', 'linkList', 'links']), '外链'),
  ...normalizeResourceCollection(extractCollection(source, ['videos', 'videoList']), '视频'),
  ...normalizeResourceCollection(extractCollection(source, ['videoMarkers', 'markerList']), '视频时间标注')
];

const includesAny = (haystack: string[], needles: string[]): boolean =>
  needles.some((needle) => haystack.includes(needle));

const resolveDetailViewRoleFromActions = (actions: unknown): ManuscriptReviewDetailViewRole | undefined => {
  if (!Array.isArray(actions)) {
    return undefined;
  }
  const labels = actions.map((item) => String(item ?? '').trim()).filter(Boolean);
  if (includesAny(labels, ['去审批'])) {
    return 'CURRENT_APPROVER';
  }
  if (includesAny(labels, ['再次提交'])) {
    return 'RETURNED_INITIATOR';
  }
  if (includesAny(labels, ['返回'])) {
    return 'HISTORY_PARTICIPANT';
  }
  return undefined;
};

const resolveDetailViewRole = (source: UnknownRecord): ManuscriptReviewDetailViewRole => {
  const explicitRole = pickMaybeText(source, ['viewRole', 'actionRole', 'detailViewRole', 'viewerRole']);
  if (
    explicitRole === 'CURRENT_APPROVER' ||
    explicitRole === 'RETURNED_INITIATOR' ||
    explicitRole === 'HISTORY_PARTICIPANT'
  ) {
    return explicitRole;
  }

  const nestedActionBar = pickMaybeRecord(source, ['actionBar']);
  const nestedRole = resolveDetailViewRoleFromActions(nestedActionBar?.actions);
  if (nestedRole) {
    return nestedRole;
  }

  if (pickBoolean(source, ['canApprove', 'showApproveAction', 'approverView'])) {
    return 'CURRENT_APPROVER';
  }

  if (pickBoolean(source, ['canResubmit', 'showResubmitAction', 'returnedInitiatorView'])) {
    return 'RETURNED_INITIATOR';
  }

  return 'HISTORY_PARTICIPANT';
};

const normalizeNestedResourceItems = (resources: UnknownRecord): ManuscriptReviewResourceItem[] => {
  const currentAttachments = extractCollection(resources, ['currentAttachments']).map((item, index) => {
    const resource = isRecord(item) ? item : {};
    return {
      id: `当前附件-${index + 1}`,
      typeLabel: '当前附件',
      name: pickText(resource, ['fileName', 'name'], `附件-${index + 1}`),
      statusLabel: '当前有效',
      note: pickMaybeText(resource, ['fileUrl', 'url'])
    };
  });

  const currentExternalLinks = extractCollection(resources, ['currentExternalLinks']).map((item, index) => {
    const resource = isRecord(item) ? item : {};
    return {
      id: `当前外链-${index + 1}`,
      typeLabel: '当前外链',
      name: pickText(resource, ['linkTitle', 'title', 'name'], `外链-${index + 1}`),
      statusLabel: '当前有效',
      note: pickMaybeText(resource, ['linkUrl', 'url'])
    };
  });

  const currentVideos = extractCollection(resources, ['currentVideos']).map((item, index) => {
    const resource = isRecord(item) ? item : {};
    const duration = pickMaybeText(resource, ['duration']);
    return {
      id: `当前视频-${index + 1}`,
      typeLabel: '当前视频',
      name: pickText(resource, ['fileName', 'name'], `视频-${index + 1}`),
      statusLabel: '当前有效',
      note: duration ? `时长：${duration}` : undefined
    };
  });

  const historyAttachments = extractCollection(resources, ['historyAttachments']).map((item, index) => {
    const resource = isRecord(item) ? item : {};
    const disabledTime = pickMaybeText(resource, ['disabledTime']);
    return {
      id: `历史附件-${index + 1}`,
      typeLabel: '历史附件',
      name: pickText(resource, ['fileName', 'name'], `附件-${index + 1}`),
      statusLabel: '已停用',
      note: disabledTime ? `停用时间：${disabledTime}` : undefined
    };
  });

  const historyExternalLinks = extractCollection(resources, ['historyExternalLinks']).map((item, index) => {
    const resource = isRecord(item) ? item : {};
    const disabledTime = pickMaybeText(resource, ['disabledTime']);
    return {
      id: `历史外链-${index + 1}`,
      typeLabel: '历史外链',
      name: pickText(resource, ['linkTitle', 'title', 'name'], `外链-${index + 1}`),
      statusLabel: '已停用',
      note: disabledTime ? `停用时间：${disabledTime}` : undefined
    };
  });

  const historyVideoMarkers = extractCollection(resources, ['historyVideoMarkers']).map((item, index) => {
    const resource = isRecord(item) ? item : {};
    return {
      id: `历史视频时间标注-${index + 1}`,
      typeLabel: '历史视频时间标注',
      name: pickText(resource, ['startTime', 'name'], `标注-${index + 1}`),
      statusLabel: '已停用',
      note: pickMaybeText(resource, ['markerNote', 'note'])
    };
  });

  return [
    ...currentAttachments,
    ...currentExternalLinks,
    ...currentVideos,
    ...historyAttachments,
    ...historyExternalLinks,
    ...historyVideoMarkers
  ];
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
      reviewId: pickText(row, ['reviewId', 'id', 'review_id'], `review-${index + 1}`),
      manuscriptCode: pickText(row, ['manuscriptCode', 'manuscript_code', 'manuscriptNo']),
      title: pickText(row, ['title']),
      processTypeLabel: pickText(row, ['processTypeLabel', 'processTypeName', 'reviewTypeLabel']),
      mediaChannelLabel: pickText(row, ['mediaChannelLabel', 'mediaChannelName', 'mediaColumnLabel']),
      flowStatusLabel: pickText(row, ['flowStatusLabel', 'statusLabel']),
      currentNodeLabel: pickText(row, ['currentNodeLabel', 'currentTaskName', 'currentNodeName']),
      initiatorName: pickText(row, ['initiatorName', 'submitterName', 'creatorName']),
      latestUpdatedAt: pickText(row, ['latestUpdatedAt', 'updateTime', 'updatedAt'])
    };
  });

export const normalizeDetailViewModel = (payload: unknown): ManuscriptReviewDetailReadableViewModel => {
  const source = extractRecord(payload);
  const summaryCard = pickRecord(source, ['summaryCard']);
  const manuscriptCard = pickRecord(source, ['manuscriptCard']);
  const nestedResources = pickMaybeRecord(source, ['resources']);

  return {
    actionRole: resolveDetailViewRole(source),
    detail: {
      flowStatusLabel: pickText(summaryCard, ['flowStatusLabel'], pickText(source, ['flowStatusLabel', 'statusLabel', 'flowStatusName'])),
      currentNodeLabel: pickText(summaryCard, ['currentNodeLabel'], pickText(source, ['currentNodeLabel', 'currentTaskName', 'currentNodeName'])),
      initiatorName: pickText(summaryCard, ['initiatorName'], pickText(source, ['initiatorName', 'submitterName', 'creatorName'])),
      latestUpdatedAt: pickText(summaryCard, ['latestUpdatedAt', 'updateTime', 'updatedAt'], pickText(source, ['latestUpdatedAt', 'updateTime', 'updatedAt'])),
      processTypeLabel: pickText(manuscriptCard, ['processTypeLabel'], pickText(source, ['processTypeLabel', 'processTypeName', 'reviewTypeLabel'])),
      manuscriptCode: pickText(manuscriptCard, ['manuscriptCode', 'manuscript_code', 'manuscriptNo'], pickText(source, ['manuscriptCode', 'manuscript_code', 'manuscriptNo'])),
      externalManuscriptCode: pickMaybeText(manuscriptCard, [
        'externalManuscriptCode',
        'externalCode',
        'externalManuscriptNo'
      ]) ?? pickMaybeText(source, [
        'externalManuscriptCode',
        'externalCode',
        'externalManuscriptNo'
      ]),
      title: pickText(manuscriptCard, ['title'], pickText(source, ['title'])),
      mediaChannelLabel: pickText(manuscriptCard, ['mediaChannelLabel', 'mediaChannelName', 'mediaColumnLabel'], pickText(source, ['mediaChannelLabel', 'mediaChannelName', 'mediaColumnLabel'])),
      submitterDeptName: pickMaybeText(manuscriptCard, ['submitterDeptName', 'submitDeptName', 'departmentName'])
        ?? pickMaybeText(source, ['submitterDeptName', 'submitDeptName', 'departmentName']),
      authorNames: pickMaybeText(manuscriptCard, ['authorNames', 'authors', 'authorName'])
        ?? pickMaybeText(source, ['authorNames', 'authors', 'authorName']),
      note: pickMaybeText(manuscriptCard, ['note', 'remark', 'description'])
        ?? pickMaybeText(source, ['note', 'remark', 'description']),
      contentPreview: pickMaybeText(manuscriptCard, ['contentPreview', 'content', 'contentText', 'plainTextContent'])
        ?? pickMaybeText(source, ['contentPreview', 'content', 'contentText', 'plainTextContent'])
    },
    historyItems: normalizeHistoryItems(pickMaybeRecord(source, ['timeline']) ? source : source),
    resourceItems: nestedResources ? normalizeNestedResourceItems(nestedResources) : normalizeResourceItems(source)
  };
};
