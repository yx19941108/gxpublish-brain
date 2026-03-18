import { describe, expect, it } from 'vitest';

import {
  createLegacyReviewFallbackLocation,
  getReviewStatusMeta,
  isEditableReviewStatus,
  isWaitingReviewStatus,
  mapReviewDetailModel,
  mapReviewPageItem,
  normalizeReviewStatus
} from './integration';

describe('editorial review integration', () => {
  it('does not infer canEdit from ownership when page contract omits it', () => {
    const item = mapReviewPageItem({
      id: 101,
      title: '显式台账契约',
      status: 'BACK',
      reviewStatus: 'BACK',
      processType: 'AUDIT',
      user: {
        id: 9,
        name: '申请人'
      },
      dept: {
        id: 3,
        name: '编辑部'
      },
      createTime: '2026-03-17 14:00:00'
    } as any);

    expect(item.reviewStatus).toBe('BACK');
    expect(item.canEdit).toBe(false);
    expect(item.user.name).toBe('申请人');
    expect(item.dept.name).toBe('编辑部');
  });

  it('recognizes both legacy and staged waiting statuses for approval shell', () => {
    expect(isWaitingReviewStatus('waiting')).toBe(true);
    expect(isWaitingReviewStatus('WAITING_SECOND')).toBe(true);
    expect(isWaitingReviewStatus('APPROVED')).toBe(false);
  });

  it('maps detail contract history and approval shell context explicitly', () => {
    const detail = mapReviewDetailModel(
      {
        id: 12,
        title: '审批详情壳',
        status: 'WAITING_FINAL',
        reviewStatus: 'WAITING_FINAL',
        canEdit: false,
        processType: 'PROOFREAD',
        createTime: '2026-03-17 15:20:00',
        user: {
          id: 7,
          name: '申请人'
        },
        dept: {
          id: 2,
          name: '校对室'
        },
        content: 'detail content',
        remark: 'detail remark',
        attachment: {
          id: 1,
          reviewId: 12,
          fileName: 'proofread.docx',
          ossId: 'oss-1'
        },
        linkList: [],
        historyList: [
          {
            id: 88,
            reviewId: 12,
            operatorId: 9,
            operatorName: '终审人',
            operateTime: '2026-03-17 15:21:00',
            operateType: 'approve',
            fieldDiff: {
              reviewStatus: ['WAITING_SECOND', 'WAITING_FINAL']
            }
          }
        ],
        approvalContext: {
          taskId: 'task-1',
          instanceId: 'instance-1',
          canApprove: true
        }
      },
      'approval',
      {
        id: '12',
        type: 'approval',
        taskId: 'legacy-task',
        fallback: 'legacy'
      }
    );

    expect(detail.history).toHaveLength(1);
    expect(detail.shell.taskId).toBe('task-1');
    expect(detail.shell.instanceId).toBe('instance-1');
    expect(detail.shell.canApprove).toBe(true);
    expect(detail.shell.fallback).toBe(true);
    expect(detail.processType).toBe('PROOFREAD');
  });

  it('redirects legacy reviewEdit entry to the new shell route with fallback marker', () => {
    expect(createLegacyReviewFallbackLocation({ id: '12', type: 'view' })).toEqual({
      path: '/editorial/review/detail',
      query: {
        id: '12',
        type: 'view',
        fallback: 'legacy'
      }
    });

    expect(createLegacyReviewFallbackLocation({ id: '12', type: 'update' })).toEqual({
      path: '/editorial/review/form',
      query: {
        id: '12',
        type: 'update',
        fallback: 'legacy'
      }
    });
  });

  it('surfaces staged review labels once backend starts returning them', () => {
    expect(normalizeReviewStatus('finish')).toBe('APPROVED');
    expect(getReviewStatusMeta('WAITING_FINAL').label).toBe('待终审');
    expect(getReviewStatusMeta('TERMINATED').type).toBe('info');
  });

  it('keeps only BACK as resubmit-editable status', () => {
    expect(isEditableReviewStatus('BACK')).toBe(true);
    expect(isEditableReviewStatus('CANCELED')).toBe(false);
  });
});
