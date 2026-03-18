export const REVIEW_INDEX_ROUTE = '/editorial/review';
export const REVIEW_FORM_ROUTE = '/editorial/review/form';
export const REVIEW_DETAIL_ROUTE = '/editorial/review/detail';
export const REVIEW_LEGACY_ROUTE = '/editorial/review/reviewEdit';
export const REVIEW_FLOW_CODE = 'editorial_review_flow';

export type ReviewRouteType = 'add' | 'update' | 'view' | 'approval';
export type ReviewProcessType = 'AUDIT' | 'PROOFREAD' | string;

export interface ReviewRouteQuery {
  id?: string;
  type?: string;
  taskId?: string;
  instanceId?: string;
  processType?: string;
  fallback?: string;
}

export interface ReviewUserSummary {
  id?: number | string;
  name?: string;
}

export interface ReviewDeptSummary {
  id?: number | string;
  name?: string;
}

export interface ReviewLinkFormItem {
  id?: number | null;
  url: string;
  description: string;
}

export interface ReviewHistoryItem {
  id: number;
  reviewId: number;
  operatorId: number;
  operatorName: string;
  operateTime: string;
  operateType: string;
  fieldDiff: Record<string, any>;
}

export interface ReviewFormModel {
  id?: number;
  title: string;
  content: string;
  deptId?: number;
  status?: string;
  reviewStatus?: string;
  canEdit?: boolean;
  remark?: string;
  attachmentOssId?: string;
  attachmentFileName?: string;
  attachmentFileUrl?: string;
  attachmentFileSize?: number | null;
  attachmentVersion?: number;
  linkList: ReviewLinkFormItem[];
  flowCode?: string;
  processType: ReviewProcessType;
  applyCode?: string;
  userId?: number;
  userName?: string;
  deptName?: string;
  createTime?: string;
}

export interface ReviewPageItem {
  id: number;
  title: string;
  status?: string;
  reviewStatus: string;
  canEdit: boolean;
  processType: ReviewProcessType;
  createTime?: string;
  user: ReviewUserSummary;
  dept: ReviewDeptSummary;
}

export interface ReviewDetailShellContext {
  pageType: string;
  taskId?: string;
  instanceId?: string;
  canApprove: boolean;
  canEdit: boolean;
  fallback: boolean;
}

export interface ReviewDetailModel extends ReviewFormModel {
  user: ReviewUserSummary;
  dept: ReviewDeptSummary;
  history: ReviewHistoryItem[];
  shell: ReviewDetailShellContext;
}

export interface ReviewVerificationScenario {
  key: string;
  title: string;
  route: string;
  evidence: string;
  notes: string;
}

export const createEmptyReviewForm = (defaultDeptId?: number): ReviewFormModel => ({
  title: '',
  content: '',
  deptId: defaultDeptId,
  status: 'DRAFT',
  reviewStatus: 'DRAFT',
  canEdit: true,
  remark: '',
  attachmentOssId: undefined,
  attachmentFileName: undefined,
  attachmentFileUrl: '',
  attachmentFileSize: null,
  attachmentVersion: 0,
  linkList: [],
  flowCode: REVIEW_FLOW_CODE,
  processType: 'AUDIT',
  applyCode: undefined,
  userId: undefined,
  userName: undefined,
  deptName: undefined,
  createTime: undefined
});
