import { describe, expect, it } from 'vitest';

import { APPROVE_BLOCKER_MESSAGE, resolveManuscriptApproveAction } from '@/types/manuscript-review/detail';

describe('T08_Frontend_DetailApproveJumpSpec', () => {
  it('returns BPM jump location when formPath and waiting task are both present', () => {
    const result = resolveManuscriptApproveAction(
      '880000230001',
      {
        formPath: '/workflow/approval/detail'
      },
      {
        list: [
          { id: 'task-running', flowStatus: 'WAITING' },
          { id: 'task-history', flowStatus: 'PASS' }
        ]
      }
    );

    expect(result).toEqual({
      kind: 'jump',
      location: {
        path: '/workflow/approval/detail',
        query: {
          id: '880000230001',
          type: 'approval',
          taskId: 'task-running'
        }
      }
    });
  });

  it('returns blocker message when the current task is missing', () => {
    const result = resolveManuscriptApproveAction(
      '880000230001',
      {
        formPath: '/workflow/approval/detail'
      },
      {
        list: []
      }
    );

    expect(result).toEqual({
      kind: 'blocked',
      message: APPROVE_BLOCKER_MESSAGE
    });
  });
});
