import { describe, expect, it } from 'vitest';

import {
  buildMyDocumentEditRoute,
  buildMyDocumentViewRoute,
  buildTaskApprovalRoute,
  isManuscriptReviewTaskCancellable,
  isManuscriptReviewTaskEditable
} from './task-center';

describe('T10_ManuscriptReview_TaskCenterSpec', () => {
  it('marks waiting and back rows as cancellable', () => {
    expect(isManuscriptReviewTaskCancellable('waiting')).toBe(true);
    expect(isManuscriptReviewTaskCancellable('back')).toBe(true);
    expect(isManuscriptReviewTaskCancellable('finish')).toBe(false);
  });

  it('marks only back rows as editable', () => {
    expect(isManuscriptReviewTaskEditable('back')).toBe(true);
    expect(isManuscriptReviewTaskEditable('waiting')).toBe(false);
  });

  it('builds my-document detail and edit routes with dedicated return targets', () => {
    expect(buildMyDocumentViewRoute('review-1', 'task-1')).toEqual({
      path: '/manuscript/review/detail',
      query: {
        reviewId: 'review-1',
        type: 'view',
        taskId: 'task-1',
        returnTo: '/manuscript/review/my-document'
      }
    });
    expect(buildMyDocumentEditRoute('review-1')).toEqual({
      path: '/manuscript/review/form',
      query: {
        reviewId: 'review-1',
        mode: 'edit',
        returnTo: '/manuscript/review/my-document'
      }
    });
  });

  it('builds approval route with dedicated waiting return target', () => {
    expect(buildTaskApprovalRoute('review-2', 'task-2')).toEqual({
      path: '/manuscript/review/approval',
      query: {
        id: 'review-2',
        type: 'approval',
        taskId: 'task-2',
        returnTo: '/manuscript/review/task-waiting'
      }
    });
  });
});
