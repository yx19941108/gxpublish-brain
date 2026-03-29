import type { LocationQuery, RouteLocationRaw } from 'vue-router';

const MANUSCRIPT_REVIEW_LEDGER_PATH = '/manuscript/review';
const MANUSCRIPT_REVIEW_DETAIL_PATH = '/manuscript/review/detail';
const MANUSCRIPT_REVIEW_APPROVAL_PATH = '/manuscript/review/approval';
const MANUSCRIPT_REVIEW_FORM_PATH = '/manuscript/review/form';
const WORKFLOW_TASK_WAITING_PATH = '/workflow/task/taskWaiting';

const readQueryText = (value: LocationQuery[string]): string | undefined => {
  if (Array.isArray(value)) {
    return value[0] ? String(value[0]) : undefined;
  }
  if (value == null) {
    return undefined;
  }
  return String(value);
};

const parseRouteLocation = (target: string): RouteLocationRaw => {
  const [path, queryString = ''] = target.split('?');
  if (!queryString) {
    return { path };
  }
  return {
    path,
    query: Object.fromEntries(new URLSearchParams(queryString))
  };
};

export const isManuscriptReviewFormPath = (formPath?: string | null): boolean =>
  typeof formPath === 'string' && formPath.trim() === MANUSCRIPT_REVIEW_APPROVAL_PATH;

export const buildWorkflowViewDetailRoute = (
  reviewId: string,
  taskId?: string | number,
  returnTo?: string
): RouteLocationRaw => ({
  path: MANUSCRIPT_REVIEW_DETAIL_PATH,
  query: {
    reviewId,
    type: 'view',
    ...(taskId != null ? { taskId: String(taskId) } : {}),
    ...(returnTo ? { returnTo } : {})
  }
});

export const buildEditRouteLocation = (reviewId: string, returnTo?: string): RouteLocationRaw => ({
  path: MANUSCRIPT_REVIEW_FORM_PATH,
  query: {
    reviewId,
    mode: 'edit',
    ...(returnTo ? { returnTo } : {})
  }
});

export const resolveEditBackTarget = (query: LocationQuery, reviewId?: string): RouteLocationRaw => {
  const returnTo = readQueryText(query.returnTo);
  if (returnTo) {
    return parseRouteLocation(returnTo);
  }
  if (reviewId) {
    return { path: MANUSCRIPT_REVIEW_DETAIL_PATH, query: { reviewId } };
  }
  return { path: MANUSCRIPT_REVIEW_LEDGER_PATH };
};

export const buildApprovalRouteLocation = (path: string, query: Record<string, string>, returnTo?: string): RouteLocationRaw => ({
  path,
  query: {
    ...query,
    ...(returnTo ? { returnTo } : {})
  }
});

export const resolveApprovalBackTarget = (query: LocationQuery): RouteLocationRaw => {
  const returnTo = readQueryText(query.returnTo);
  if (returnTo) {
    return parseRouteLocation(returnTo);
  }
  return { path: WORKFLOW_TASK_WAITING_PATH };
};

export const resolveDetailBackTarget = (query: LocationQuery): RouteLocationRaw => {
  const returnTo = readQueryText(query.returnTo);
  if (returnTo) {
    return parseRouteLocation(returnTo);
  }
  const entryType = readQueryText(query.type);
  const taskId = readQueryText(query.taskId);
  if (entryType === 'view' && taskId) {
    return { path: WORKFLOW_TASK_WAITING_PATH };
  }
  return { path: MANUSCRIPT_REVIEW_LEDGER_PATH };
};

export const resolveApprovalSuccessTarget = (query?: LocationQuery): RouteLocationRaw => {
  const returnTo = query ? readQueryText(query.returnTo) : undefined;
  if (returnTo && !returnTo.startsWith(MANUSCRIPT_REVIEW_DETAIL_PATH)) {
    return parseRouteLocation(returnTo);
  }
  return {
    path: MANUSCRIPT_REVIEW_LEDGER_PATH
  };
};
