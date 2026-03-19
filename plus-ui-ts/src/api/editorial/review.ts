import request from '@/utils/request';

export interface ReviewUserResp {
  id?: number | string;
  name?: string;
}

export interface ReviewDeptResp {
  id?: number | string;
  name?: string;
}

export interface ReviewAttachmentResp {
  id?: number | null;
  reviewId?: number;
  fileName?: string;
  ossId?: string;
  fileUrl?: string;
  fileSize?: number | null;
  version?: number;
  uploaderId?: number;
  uploaderName?: string;
  createTime?: string;
}

export interface ReviewLinkResp {
  id?: number | null;
  reviewId?: number;
  url: string;
  description: string;
  createTime?: string;
}

export interface ReviewHistoryResp {
  id: number;
  reviewId: number;
  operatorId: number;
  operatorName: string;
  operateTime: string;
  operateType: string;
  fieldDiff: Record<string, any>;
}

export interface ReviewApprovalContextResp {
  canApprove?: boolean;
  taskId?: string;
  instanceId?: string;
  canEdit?: boolean;
}

export interface ReviewPageItemResp {
  id: number;
  title: string;
  status: string;
  reviewStatus: string;
  canEdit: boolean;
  canApprove?: boolean;
  taskId?: string;
  instanceId?: string;
  processType: string;
  createTime: string;
  user: ReviewUserResp;
  dept: ReviewDeptResp;
}

export interface ReviewDetailResp {
  id: number;
  title: string;
  status: string;
  reviewStatus: string;
  canEdit: boolean;
  processType: string;
  createTime: string;
  user: ReviewUserResp;
  dept: ReviewDeptResp;
  content?: string;
  applyCode?: string;
  remark?: string;
  attachmentList?: ReviewAttachmentResp[];
  attachment?: ReviewAttachmentResp;
  linkList?: ReviewLinkResp[];
  historyList?: ReviewHistoryResp[];
  approvalContext?: ReviewApprovalContextResp;
}

export interface ReviewActionPayload {
  id?: number;
  title: string;
  content: string;
  deptId?: number;
  status?: string;
  remark?: string;
  attachmentList?: ReviewAttachmentResp[];
  attachmentOssId?: string;
  attachmentFileName?: string;
  attachmentFileUrl?: string;
  attachmentFileSize?: number | null;
  linkList?: ReviewLinkResp[];
  flowCode?: string;
  processType?: string;
}

export interface ReviewPageQuery extends PageQuery {
  title?: string;
  status?: string;
  reviewStatus?: string;
  processType?: string;
  userId?: number;
}

export interface ReviewListResponse {
  rows: ReviewPageItemResp[];
  total: number;
}

export interface ReviewDataResponse<T> {
  data: T;
}

/**
 * 查询审校台账列表
 * 兼容期继续复用旧 `/list`，由前端 integration 层映射到 page contract。
 */
export const listReviewPage = (query: ReviewPageQuery): Promise<ReviewListResponse> => {
  return request({
    url: '/editorial/review/list',
    method: 'get',
    params: query
  });
};

/**
 * 查询审校详情
 * 兼容期继续复用旧详情接口，由前端拆分 detail shell 所需字段。
 */
export const getReviewDetail = (id: number | string): Promise<ReviewDataResponse<ReviewDetailResp>> => {
  return request({
    url: '/editorial/review/' + id,
    method: 'get'
  });
};

/**
 * 保存草稿
 */
export const createReviewDraft = (data: ReviewActionPayload): Promise<ReviewDataResponse<ReviewDetailResp>> => {
  return request({
    url: '/editorial/review',
    method: 'post',
    data
  });
};

/**
 * 更新草稿/退回稿
 */
export const updateReviewDraft = (data: ReviewActionPayload): Promise<ReviewDataResponse<ReviewDetailResp>> => {
  return request({
    url: '/editorial/review',
    method: 'put',
    data
  });
};

/**
 * 提交审校申请并由后端开启流程
 */
export const submitReviewAction = (data: ReviewActionPayload): Promise<ReviewDataResponse<ReviewDetailResp>> => {
  return request({
    url: '/editorial/review/submit',
    method: 'post',
    data
  });
};

/**
 * 退回后重新提交
 */
export const resubmitReviewAction = (data: ReviewActionPayload): Promise<ReviewDataResponse<ReviewDetailResp>> => {
  return request({
    url: '/editorial/review/resubmit',
    method: 'post',
    data
  });
};

/**
 * 删除审校申请
 */
export const deleteReview = (ids: string | number | Array<string | number>) => {
  return request({
    url: '/editorial/review/' + ids,
    method: 'delete'
  });
};

/**
 * 获取历史记录
 */
export const getReviewHistory = (id: number | string): Promise<ReviewDataResponse<ReviewHistoryResp[]>> => {
  return request({
    url: '/editorial/review/history/' + id,
    method: 'get'
  });
};

// 兼容旧导出，便于 fallback 期间平滑切换。
export type EditorialReviewQuery = ReviewPageQuery;
export type EditorialReviewVo = ReviewDetailResp;
export type EditorialReviewForm = ReviewActionPayload;
export type EditorialAttachmentVo = ReviewAttachmentResp;
export type EditorialLinkVo = ReviewLinkResp;
export type EditorialLinkBo = ReviewLinkResp;
export type EditorialHistoryVo = ReviewHistoryResp;

export const listReview = listReviewPage;
export const getReview = getReviewDetail;
export const addReview = createReviewDraft;
export const updateReview = updateReviewDraft;
export const submitReview = submitReviewAction;
export const delReview = deleteReview;
export const getHistory = getReviewHistory;
