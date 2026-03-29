import { describe, expect, it } from 'vitest';

import {
  buildEditRouteLocation,
  buildWorkflowViewDetailRoute,
  resolveApprovalBackTarget,
  resolveApprovalSuccessTarget,
  resolveDetailBackTarget
} from './detail-navigation';

describe('T10_ManuscriptReview_DetailNavigationSpec', () => {
  it('routes detail back to waiting-task list when the page is opened from workflow waiting entry', () => {
    expect(resolveDetailBackTarget({ type: 'view', taskId: 'task-1001', id: 'review-1001' })).toEqual({
      path: '/workflow/task/taskWaiting'
    });
  });

  it('routes detail back to the captured workflow list when the page is opened with returnTo', () => {
    expect(
      resolveDetailBackTarget({
        reviewId: 'review-1001',
        type: 'view',
        taskId: 'task-1001',
        returnTo: '/workflow/task/taskFinish'
      })
    ).toEqual({
      path: '/workflow/task/taskFinish'
    });
  });

  it('routes detail back to manuscript-review ledger when the page is opened from ledger entry', () => {
    expect(resolveDetailBackTarget({ reviewId: 'review-1002' })).toEqual({
      path: '/manuscript/review'
    });
  });

  it('keeps edit back/save target on the current detail route', () => {
    expect(buildEditRouteLocation('review-1003', '/manuscript/review/detail?reviewId=review-1003&type=view&taskId=task-1003')).toEqual({
      path: '/manuscript/review/form',
      query: {
        reviewId: 'review-1003',
        mode: 'edit',
        returnTo: '/manuscript/review/detail?reviewId=review-1003&type=view&taskId=task-1003'
      }
    });
  });

  it('routes approval back to the captured detail entry when approval is opened from detail page', () => {
    expect(
      resolveApprovalBackTarget({
        taskId: 'task-2001',
        type: 'approval',
        returnTo: '/manuscript/review/detail?reviewId=review-2001&type=view&taskId=task-2001'
      })
    ).toEqual({
      path: '/manuscript/review/detail',
      query: {
        reviewId: 'review-2001',
        type: 'view',
        taskId: 'task-2001'
      }
    });
  });

  it('routes approval back to waiting-task list when approval is opened from workflow waiting entry', () => {
    expect(resolveApprovalBackTarget({ taskId: 'task-2002', type: 'approval', id: 'review-2002' })).toEqual({
      path: '/workflow/task/taskWaiting'
    });
  });

  it('routes approval success to manuscript-review ledger instead of reloading protected detail', () => {
    expect(resolveApprovalSuccessTarget({ returnTo: '/manuscript/review/detail?reviewId=review-2001&type=view' })).toEqual({
      path: '/manuscript/review'
    });
  });

  it('routes approval success back to the dedicated waiting page when returnTo points there', () => {
    expect(resolveApprovalSuccessTarget({ returnTo: '/manuscript/review/task-waiting' })).toEqual({
      path: '/manuscript/review/task-waiting'
    });
  });

  it('builds workflow manuscript-review view route with return target', () => {
    expect(buildWorkflowViewDetailRoute('review-3001', 'task-3001', '/workflow/task/myDocument')).toEqual({
      path: '/manuscript/review/detail',
      query: {
        reviewId: 'review-3001',
        type: 'view',
        taskId: 'task-3001',
        returnTo: '/workflow/task/myDocument'
      }
    });
  });

});
