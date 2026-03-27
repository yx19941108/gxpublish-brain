import { readFileSync } from 'node:fs';
import { resolve } from 'node:path';

import { describe, expect, it } from 'vitest';

describe('T09_Frontend_DetailLayoutSpec', () => {
  it('contains the frozen detail layout sections and action split markers', () => {
    const file = readFileSync(resolve(__dirname, './detail-page-shell.vue'), 'utf-8');

    expect(file).toContain('data-testid="manuscript-review-summary-cards"');
    expect(file).toContain('data-testid="manuscript-review-basic-info"');
    expect(file).toContain('data-testid="manuscript-review-resource-shell"');
    expect(file).toContain('data-testid="manuscript-review-history-shell"');
    expect(file).toContain('manuscript-review-detail-shell__action-panel');
    expect(file).toContain('去审批');
    expect(file).toContain('重新提交');
    expect(file).not.toContain('页内直接审批');
  });

  it('uses frozen workflow detail API and keeps approve/resubmit split in detail', () => {
    const file = readFileSync(resolve(__dirname, './detail-page-shell.vue'), 'utf-8');

    expect(file).toContain('getManuscriptReviewDetail(reviewId.value)');
    expect(file).not.toContain("request.get('/manuscript-review/readable/detail'");
    expect(file).toContain("if (key === 'approve')");
    expect(file).toContain("if (key === 'resubmit')");
  });

  it('does not expose debug-only permission or matrix copy in the user-facing detail page', () => {
    const file = readFileSync(resolve(__dirname, './detail-page-shell.vue'), 'utf-8');

    expect(file).not.toContain('permissionMatrix 动作矩阵');
    expect(file).not.toContain('data-testid="manuscript-review-permission-matrix"');
    expect(file).not.toContain('动作来源：permissionMatrix');
    expect(file).not.toContain('当前节点说明');
    expect(file).not.toContain('按钮分流板');
  });
});
