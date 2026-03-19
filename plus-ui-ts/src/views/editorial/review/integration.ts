import type { ReviewApprovalContextResp, ReviewActionPayload, ReviewDetailResp, ReviewHistoryResp, ReviewPageItemResp } from '@/api/editorial/review';

import {
  REVIEW_DETAIL_ROUTE,
  REVIEW_FORM_ROUTE,
  REVIEW_FLOW_CODE,
  type ReviewDetailModel,
  type ReviewFormModel,
  type ReviewHistoryItem,
  type ReviewPageItem,
  type ReviewRouteQuery,
  type ReviewRouteType
} from './model';

const REVIEW_STATUS_ALIAS: Record<string, string> = {
  '0': 'DRAFT',
  '10': 'WAITING_FIRST',
  '20': 'WAITING_SECOND',
  '30': 'WAITING_FINAL',
  '40': 'APPROVED',
  '50': 'BACK',
  '60': 'TERMINATED',
  DRAFT: 'DRAFT',
  WAITING: 'WAITING',
  WAITING_FIRST: 'WAITING_FIRST',
  WAITING_SECOND: 'WAITING_SECOND',
  WAITING_FINAL: 'WAITING_FINAL',
  FINISH: 'APPROVED',
  APPROVED: 'APPROVED',
  BACK: 'BACK',
  CANCEL: 'CANCELED',
  CANCELED: 'CANCELED',
  TERMINATION: 'TERMINATED',
  TERMINATED: 'TERMINATED',
  INVALID: 'TERMINATED'
};

const REVIEW_STATUS_META: Record<string, { label: string; type: 'info' | 'primary' | 'success' | 'warning' | 'danger' }> = {
  DRAFT: { label: '草稿', type: 'info' },
  WAITING: { label: '待审批', type: 'warning' },
  WAITING_FIRST: { label: '待一审', type: 'warning' },
  WAITING_SECOND: { label: '待二审', type: 'warning' },
  WAITING_FINAL: { label: '待终审', type: 'warning' },
  APPROVED: { label: '已通过', type: 'success' },
  BACK: { label: '已退回', type: 'danger' },
  CANCELED: { label: '已撤销', type: 'info' },
  TERMINATED: { label: '已终止', type: 'info' }
};

const REVIEW_PROCESS_TYPE_META: Record<string, { label: string; type: 'primary' | 'success' | 'info' }> = {
  AUDIT: { label: '审核流程', type: 'primary' },
  PROOFREAD: { label: '校验流程', type: 'success' }
};

const FORM_ROUTE_TYPES = new Set(['add', 'update']);
const DETAIL_ROUTE_TYPES = new Set(['view', 'approval']);
const EDITABLE_STATUSES = new Set(['DRAFT', 'BACK']);

const pickQueryValue = (value: unknown) => {
  if (Array.isArray(value)) {
    return typeof value[0] === 'string' ? value[0] : undefined;
  }
  return typeof value === 'string' ? value : undefined;
};

export const sanitizeReviewQuery = (query: Record<string, unknown> = {}): ReviewRouteQuery => {
  const nextQuery: ReviewRouteQuery = {};
  const id = pickQueryValue(query.id);
  const type = pickQueryValue(query.type);
  const taskId = pickQueryValue(query.taskId);
  const instanceId = pickQueryValue(query.instanceId);
  const processType = pickQueryValue(query.processType);
  const fallback = pickQueryValue(query.fallback);

  if (id) {
    nextQuery.id = id;
  }
  if (type) {
    nextQuery.type = type;
  }
  if (taskId) {
    nextQuery.taskId = taskId;
  }
  if (instanceId) {
    nextQuery.instanceId = instanceId;
  }
  if (processType) {
    nextQuery.processType = processType;
  }
  if (fallback) {
    nextQuery.fallback = fallback;
  }
  return nextQuery;
};

export const normalizeReviewStatus = (status?: string) => {
  const normalized = String(status ?? '')
    .trim()
    .toUpperCase();
  if (!normalized) {
    return 'DRAFT';
  }
  return REVIEW_STATUS_ALIAS[normalized] ?? normalized;
};

export const isWaitingReviewStatus = (status?: string) => {
  const normalized = normalizeReviewStatus(status);
  return normalized === 'WAITING' || normalized.startsWith('WAITING_');
};

export const isEditableReviewStatus = (status?: string) => EDITABLE_STATUSES.has(normalizeReviewStatus(status));

export const getReviewStatusMeta = (status?: string) =>
  REVIEW_STATUS_META[normalizeReviewStatus(status)] ?? { label: status || '-', type: 'info' as const };

export const getReviewProcessTypeMeta = (processType?: string) => {
  const normalized = String(processType ?? '')
    .trim()
    .toUpperCase();
  return REVIEW_PROCESS_TYPE_META[normalized] ?? { label: normalized || '-', type: 'info' as const };
};

export const resolveReviewRouteType = (query: Record<string, unknown> = {}): ReviewRouteType => {
  const sanitized = sanitizeReviewQuery(query);
  if (sanitized.type && (FORM_ROUTE_TYPES.has(sanitized.type) || DETAIL_ROUTE_TYPES.has(sanitized.type))) {
    return sanitized.type as ReviewRouteType;
  }
  if (!sanitized.id) {
    return 'add';
  }
  if (sanitized.taskId) {
    return 'approval';
  }
  return 'view';
};

export const createReviewFormLocation = (query: Record<string, unknown> = {}) => {
  const sanitized = sanitizeReviewQuery(query);
  const type = sanitized.type === 'update' ? 'update' : 'add';
  return {
    path: REVIEW_FORM_ROUTE,
    query: {
      ...sanitized,
      type
    }
  };
};

export const createReviewDetailLocation = (query: Record<string, unknown> = {}) => {
  const sanitized = sanitizeReviewQuery(query);
  const type = sanitized.type === 'approval' ? 'approval' : 'view';
  return {
    path: REVIEW_DETAIL_ROUTE,
    query: {
      ...sanitized,
      type
    }
  };
};

export const createLegacyReviewFallbackLocation = (query: Record<string, unknown> = {}) => {
  const type = resolveReviewRouteType(query);
  const nextQuery = {
    ...query,
    type,
    fallback: 'legacy'
  };
  if (type === 'add' || type === 'update') {
    return createReviewFormLocation(nextQuery);
  }
  return createReviewDetailLocation(nextQuery);
};

const toOptionalNumber = (value: unknown) => {
  if (value === undefined || value === null || value === '') {
    return undefined;
  }
  const nextValue = Number(value);
  return Number.isNaN(nextValue) ? undefined : nextValue;
};

const mapHistoryItem = (item: ReviewHistoryResp): ReviewHistoryItem => ({
  id: item.id,
  reviewId: item.reviewId,
  operatorId: item.operatorId,
  operatorName: item.operatorName,
  operateTime: item.operateTime,
  operateType: item.operateType,
  fieldDiff: item.fieldDiff ?? {}
});

export const mapReviewPageItem = (item: ReviewPageItemResp): ReviewPageItem => {
  const reviewStatus = normalizeReviewStatus(item.reviewStatus ?? item.status);

  return {
    id: item.id,
    title: item.title,
    status: item.status ?? reviewStatus,
    reviewStatus,
    canEdit: Boolean(item.canEdit),
    processType: item.processType,
    createTime: item.createTime,
    user: {
      id: item.user?.id,
      name: item.user?.name
    },
    dept: {
      id: item.dept?.id,
      name: item.dept?.name
    }
  };
};

export const mapReviewFormModel = (detail: ReviewDetailResp): ReviewFormModel => {
  const reviewStatus = normalizeReviewStatus(detail.reviewStatus ?? detail.status);

  return {
    id: detail.id,
    title: detail.title ?? '',
    content: detail.content ?? '',
    deptId: toOptionalNumber(detail.dept?.id),
    status: detail.status ?? reviewStatus,
    reviewStatus,
    canEdit: Boolean(detail.canEdit),
    remark: detail.remark ?? '',
    attachmentOssId: detail.attachment?.ossId,
    attachmentFileName: detail.attachment?.fileName,
    attachmentFileUrl: detail.attachment?.fileUrl,
    attachmentFileSize: detail.attachment?.fileSize ?? null,
    attachmentVersion: detail.attachment?.version ?? 0,
    linkList: (detail.linkList ?? []).map((link) => ({
      id: link.id ?? null,
      url: link.url,
      description: link.description
    })),
    flowCode: REVIEW_FLOW_CODE,
    processType: detail.processType,
    applyCode: detail.applyCode,
    userId: toOptionalNumber(detail.user?.id),
    userName: detail.user?.name,
    deptName: detail.dept?.name,
    createTime: detail.createTime
  };
};

export const mapReviewDetailModel = (detail: ReviewDetailResp, pageType: string, routeQuery: Record<string, unknown> = {}): ReviewDetailModel => {
  const formModel = mapReviewFormModel(detail);
  const sanitized = sanitizeReviewQuery(routeQuery);
  const approvalContext: ReviewApprovalContextResp = detail.approvalContext ?? {};

  return {
    ...formModel,
    user: {
      id: detail.user?.id,
      name: detail.user?.name
    },
    dept: {
      id: detail.dept?.id,
      name: detail.dept?.name
    },
    history: (detail.historyList ?? []).map(mapHistoryItem),
    shell: {
      pageType,
      taskId: approvalContext.taskId ?? sanitized.taskId,
      instanceId: approvalContext.instanceId ?? sanitized.instanceId,
      canApprove: typeof approvalContext.canApprove === 'boolean' ? approvalContext.canApprove : pageType === 'approval',
      canEdit: typeof approvalContext.canEdit === 'boolean' ? approvalContext.canEdit : Boolean(formModel.canEdit),
      fallback: sanitized.fallback === 'legacy'
    }
  };
};

export const getReviewSubmitLabel = (status?: string) => (normalizeReviewStatus(status) === 'BACK' ? '再次提交' : '提交');

export const canCancelReviewProcess = ({
  pageType,
  reviewStatus,
  applicantUserId,
  currentUserId
}: {
  pageType?: string;
  reviewStatus?: string;
  applicantUserId?: string | number;
  currentUserId?: string | number;
}) => {
  if (pageType === 'approval' || !isWaitingReviewStatus(reviewStatus)) {
    return false;
  }
  if (applicantUserId === undefined || applicantUserId === null || currentUserId === undefined || currentUserId === null) {
    return false;
  }
  return String(applicantUserId) === String(currentUserId);
};

export const resolveReviewApprovalButtonPageType = ({
  pageType,
  canApprove
}: {
  pageType?: string;
  canApprove?: boolean;
}) => {
  if (pageType !== 'approval') {
    return pageType ?? 'view';
  }
  return canApprove === false ? 'view' : 'approval';
};

export const toReviewActionPayload = (form: ReviewFormModel): ReviewActionPayload => ({
  id: form.id,
  title: form.title,
  content: form.content,
  deptId: form.deptId,
  status: form.status,
  remark: form.remark,
  attachmentOssId: form.attachmentOssId,
  attachmentFileName: form.attachmentFileName,
  attachmentFileUrl: form.attachmentFileUrl,
  attachmentFileSize: form.attachmentFileSize,
  linkList: form.linkList.map((item) => ({
    id: item.id ?? null,
    url: item.url,
    description: item.description
  })),
  flowCode: form.flowCode ?? REVIEW_FLOW_CODE,
  processType: form.processType
});
