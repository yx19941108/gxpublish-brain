import type {
  ReviewActionPayload,
  ReviewApprovalContextResp,
  ReviewAttachmentResp,
  ReviewDetailResp,
  ReviewHistoryResp,
  ReviewPageItemResp
} from '@/api/editorial/review';
import type { FlowTaskVO } from '@/api/workflow/task/types';

import {
  REVIEW_DETAIL_ROUTE,
  REVIEW_FORM_ROUTE,
  REVIEW_FLOW_CODE,
  type ReviewAttachmentFormItem,
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

const REVIEW_HISTORY_OPERATE_LABEL: Record<string, string> = {
  create: '创建',
  update: '修改',
  submit: '提交',
  resubmit: '再次提交',
  approve: '审批通过',
  back: '退回',
  cancel: '撤销',
  termination: '终止'
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

const toOptionalNumber = (value: unknown) => {
  if (value === undefined || value === null || value === '') {
    return undefined;
  }
  const nextValue = Number(value);
  return Number.isNaN(nextValue) ? undefined : nextValue;
};

const toAttachmentFormItem = (attachment?: ReviewAttachmentResp | null): ReviewAttachmentFormItem | null => {
  if (!attachment || (!attachment.ossId && !attachment.fileUrl && !attachment.fileName)) {
    return null;
  }
  return {
    id: attachment.id ?? null,
    reviewId: attachment.reviewId,
    fileName: attachment.fileName,
    ossId: attachment.ossId,
    fileUrl: attachment.fileUrl,
    fileSize: attachment.fileSize ?? null,
    version: attachment.version,
    uploaderId: attachment.uploaderId,
    uploaderName: attachment.uploaderName,
    createTime: attachment.createTime,
    readonly: true
  };
};

const buildAttachmentList = (detail: ReviewDetailResp): ReviewAttachmentFormItem[] => {
  const attachments = (detail.attachmentList ?? [])
    .map((attachment) => toAttachmentFormItem(attachment))
    .filter(Boolean) as ReviewAttachmentFormItem[];
  if (attachments.length > 0) {
    return attachments;
  }

  const legacyAttachment = toAttachmentFormItem(detail.attachment);
  return legacyAttachment ? [legacyAttachment] : [];
};

const pickLatestAttachment = (attachmentList: ReviewAttachmentFormItem[]) =>
  attachmentList.length > 0 ? attachmentList[attachmentList.length - 1] : undefined;

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

const mapHistoryItem = (item: ReviewHistoryResp): ReviewHistoryItem => ({
  id: item.id,
  reviewId: item.reviewId,
  operatorId: item.operatorId,
  operatorName: item.operatorName,
  operateTime: item.operateTime,
  operateType: item.operateType,
  fieldDiff: item.fieldDiff ?? {}
});

export const getReviewHistoryOperateLabel = (operateType?: string) => {
  const normalized = String(operateType ?? '')
    .trim()
    .toLowerCase();
  if (!normalized) {
    return '-';
  }
  return REVIEW_HISTORY_OPERATE_LABEL[normalized] ?? operateType ?? '-';
};

export const getReviewHistoryDisplayText = (history?: Pick<ReviewHistoryItem, 'operateType'> | null) =>
  getReviewHistoryOperateLabel(history?.operateType);

export const sortReviewHistoryItems = (historyList: ReviewHistoryItem[] = []) =>
  [...historyList].sort((left, right) => {
    const leftTime = new Date(left.operateTime || '').getTime();
    const rightTime = new Date(right.operateTime || '').getTime();
    return rightTime - leftTime;
  });

export const mapReviewPageItem = (item: ReviewPageItemResp): ReviewPageItem => {
  const reviewStatus = normalizeReviewStatus(item.reviewStatus ?? item.status);

  return {
    id: item.id,
    title: item.title,
    status: item.status ?? reviewStatus,
    reviewStatus,
    canEdit: Boolean(item.canEdit),
    canApprove: Boolean(item.canApprove),
    taskId: item.taskId,
    instanceId: item.instanceId,
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

export const mapReviewFormModel = (detail: ReviewDetailResp, routeQuery: Record<string, unknown> = {}): ReviewFormModel => {
  const reviewStatus = normalizeReviewStatus(detail.reviewStatus ?? detail.status);
  const sanitized = sanitizeReviewQuery(routeQuery);
  const approvalContext: ReviewApprovalContextResp = detail.approvalContext ?? {};
  const attachmentList = buildAttachmentList(detail);
  const latestAttachment = pickLatestAttachment(attachmentList);

  return {
    id: detail.id,
    title: detail.title ?? '',
    content: detail.content ?? '',
    deptId: toOptionalNumber(detail.dept?.id),
    status: detail.status ?? reviewStatus,
    reviewStatus,
    canEdit: Boolean(detail.canEdit),
    canApprove: Boolean(approvalContext.canApprove),
    taskId: approvalContext.taskId ?? sanitized.taskId,
    instanceId: approvalContext.instanceId ?? sanitized.instanceId,
    remark: detail.remark ?? '',
    attachmentList,
    attachmentOssId: latestAttachment?.ossId,
    attachmentFileName: latestAttachment?.fileName,
    attachmentFileUrl: latestAttachment?.fileUrl,
    attachmentFileSize: latestAttachment?.fileSize ?? null,
    attachmentVersion: latestAttachment?.version ?? 0,
    linkList: (detail.linkList ?? []).map((link) => ({
      id: link.id ?? null,
      url: link.url,
      description: link.description,
      readonly: true
    })),
    flowCode: REVIEW_FLOW_CODE,
    processType: detail.processType,
    applyCode: detail.applyCode,
    userId: toOptionalNumber(detail.user?.id),
    userName: detail.user?.name,
    deptName: detail.dept?.name,
    createTime: detail.createTime,
    updateTime: detail.updateTime
  };
};

export const mapReviewDetailModel = (detail: ReviewDetailResp, pageType: string, routeQuery: Record<string, unknown> = {}): ReviewDetailModel => {
  const formModel = mapReviewFormModel(detail, routeQuery);
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
    history: sortReviewHistoryItems((detail.historyList ?? []).map(mapHistoryItem)),
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

export const resolveReviewFormSubmitLabel = ({ routeType, reviewStatus }: { routeType?: ReviewRouteType; reviewStatus?: string }) => {
  if (routeType === 'update') {
    return '保存';
  }
  return getReviewSubmitLabel(reviewStatus);
};

export const resolveReviewFormPageType = ({
  routeType,
  reviewStatus,
  taskId
}: {
  routeType?: ReviewRouteType;
  reviewStatus?: string;
  taskId?: string;
  canApprove?: boolean;
}) => {
  return routeType === 'update' ? 'update' : 'add';
};

export const resolveReviewWaitingTaskContext = ({
  reviewId,
  tasks
}: {
  reviewId?: string | number;
  tasks?: Array<Pick<FlowTaskVO, 'id' | 'instanceId' | 'businessId'> | null | undefined>;
}) => {
  if (reviewId === undefined || reviewId === null || reviewId === '') {
    return undefined;
  }

  const matchedTask = tasks?.find((task) => String(task?.businessId ?? '') === String(reviewId));
  if (!matchedTask || matchedTask.id === undefined || matchedTask.id === null) {
    return undefined;
  }

  return {
    taskId: String(matchedTask.id),
    instanceId: matchedTask.instanceId === undefined || matchedTask.instanceId === null ? undefined : String(matchedTask.instanceId)
  };
};

export const shouldRequireReviewTaskContext = ({ action, reviewStatus }: { action: 'update' | 'approve'; reviewStatus?: string }) =>
  action === 'approve' && isWaitingReviewStatus(reviewStatus);

export const shouldHideTransferButtonForReviewTask = (task?: Pick<FlowTaskVO, 'flowCode' | 'businessCode' | 'formPath'> | null) => {
  const flowCode = String(task?.flowCode ?? '')
    .trim()
    .toLowerCase();
  const businessCode = String(task?.businessCode ?? '')
    .trim()
    .toLowerCase();
  const formPath = String(task?.formPath ?? '')
    .trim()
    .toLowerCase();

  return flowCode === REVIEW_FLOW_CODE.toLowerCase() || businessCode.includes('editorial') || formPath.includes('/editorial/review');
};

export const getReviewRowActions = (row: Pick<ReviewPageItem, 'reviewStatus' | 'canEdit' | 'canApprove'>) => {
  const reviewStatus = normalizeReviewStatus(row.reviewStatus);
  const isDraftOrBack = reviewStatus === 'DRAFT' || reviewStatus === 'BACK';
  const isCurrentWaitingApprover = isWaitingReviewStatus(reviewStatus) && (row.canApprove || row.canEdit);

  if (isDraftOrBack && row.canEdit) {
    return {
      showEdit: true,
      showApprove: false,
      showDetail: true,
      showDelete: true
    };
  }

  if (isCurrentWaitingApprover) {
    return {
      showEdit: true,
      showApprove: true,
      showDetail: true,
      showDelete: false
    };
  }

  return {
    showEdit: false,
    showApprove: false,
    showDetail: true,
    showDelete: false
  };
};

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

export const resolveReviewApprovalButtonPageType = ({ pageType, canApprove }: { pageType?: string; canApprove?: boolean }) => {
  if (pageType !== 'approval') {
    return pageType ?? 'view';
  }
  return canApprove === false ? 'view' : 'approval';
};

export const getReviewDetailSummaryItems = (
  detail: Pick<ReviewDetailModel, 'reviewStatus' | 'status' | 'processType' | 'user' | 'dept' | 'createTime' | 'updateTime'>
) => {
  const statusMeta = getReviewStatusMeta(detail.reviewStatus ?? detail.status);
  const processTypeMeta = getReviewProcessTypeMeta(detail.processType);

  return [
    {
      label: '审核状态',
      value: statusMeta.label,
      tagType: statusMeta.type
    },
    {
      label: '流程类型',
      value: processTypeMeta.label,
      tagType: processTypeMeta.type
    },
    {
      label: '发起人',
      value: detail.user?.name || '-'
    },
    {
      label: '部门',
      value: detail.dept?.name || '-'
    },
    {
      label: '创建时间',
      value: detail.createTime || '-'
    },
    {
      label: '修改时间',
      value: detail.updateTime || '-'
    }
  ];
};

export const toReviewActionPayload = (form: ReviewFormModel): ReviewActionPayload => {
  const attachmentList = (form.attachmentList ?? [])
    .filter((attachment) => attachment.ossId || attachment.fileUrl || attachment.fileName)
    .map((attachment) => ({
      id: attachment.id ?? null,
      reviewId: attachment.reviewId,
      ossId: attachment.ossId,
      fileName: attachment.fileName,
      fileUrl: attachment.fileUrl,
      fileSize: attachment.fileSize ?? null,
      version: attachment.version,
      uploaderId: attachment.uploaderId,
      uploaderName: attachment.uploaderName,
      createTime: attachment.createTime
    }));
  const latestAttachment = attachmentList.length > 0 ? attachmentList[attachmentList.length - 1] : undefined;

  return {
    id: form.id,
    title: form.title,
    content: form.content,
    deptId: form.deptId,
    status: form.status,
    remark: form.remark,
    attachmentList,
    attachmentOssId: latestAttachment?.ossId,
    attachmentFileName: latestAttachment?.fileName,
    attachmentFileUrl: latestAttachment?.fileUrl,
    attachmentFileSize: latestAttachment?.fileSize ?? null,
    linkList: form.linkList.map((item) => ({
      id: item.id ?? null,
      url: item.url,
      description: item.description
    })),
    flowCode: form.flowCode ?? REVIEW_FLOW_CODE,
    processType: form.processType
  };
};
