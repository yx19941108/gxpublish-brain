import type { RouteLocationRaw } from 'vue-router';

import { buildEditRouteLocation, buildWorkflowViewDetailRoute } from './detail-navigation';

export const MANUSCRIPT_REVIEW_TASK_FLOW_CODE = 'manuscript_review_';
export const MANUSCRIPT_REVIEW_MY_DOCUMENT_PATH = '/manuscript/review/my-document';
export const MANUSCRIPT_REVIEW_TASK_WAITING_PATH = '/manuscript/review/task-waiting';
export const MANUSCRIPT_REVIEW_TASK_FINISH_PATH = '/manuscript/review/task-finish';

export const isManuscriptReviewTaskCancellable = (flowStatus?: string | null): boolean => {
  const normalized = typeof flowStatus === 'string' ? flowStatus.trim() : '';
  return normalized === 'waiting' || normalized === 'back';
};

export const isManuscriptReviewTaskEditable = (flowStatus?: string | null): boolean => {
  const normalized = typeof flowStatus === 'string' ? flowStatus.trim() : '';
  return normalized === 'back';
};

export const buildMyDocumentViewRoute = (reviewId: string, taskId?: string | number): RouteLocationRaw =>
  buildWorkflowViewDetailRoute(reviewId, taskId, MANUSCRIPT_REVIEW_MY_DOCUMENT_PATH);

export const buildTaskWaitingViewRoute = (reviewId: string, taskId?: string | number): RouteLocationRaw =>
  buildWorkflowViewDetailRoute(reviewId, taskId, MANUSCRIPT_REVIEW_TASK_WAITING_PATH);

export const buildTaskFinishViewRoute = (reviewId: string, taskId?: string | number): RouteLocationRaw =>
  buildWorkflowViewDetailRoute(reviewId, taskId, MANUSCRIPT_REVIEW_TASK_FINISH_PATH);

export const buildMyDocumentEditRoute = (reviewId: string): RouteLocationRaw =>
  buildEditRouteLocation(reviewId, MANUSCRIPT_REVIEW_MY_DOCUMENT_PATH);

export const buildTaskApprovalRoute = (reviewId: string, taskId: string | number, returnTo = MANUSCRIPT_REVIEW_TASK_WAITING_PATH): RouteLocationRaw => ({
  path: '/manuscript/review/approval',
  query: {
    id: reviewId,
    type: 'approval',
    taskId: String(taskId),
    returnTo
  }
});
