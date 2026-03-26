import { readFileSync } from 'node:fs';
import { resolve } from 'node:path';

import { describe, expect, it } from 'vitest';

describe('T09_Frontend_DetailLayoutSpec', () => {
  it('contains the frozen detail layout sections and action split markers', () => {
    const file = readFileSync(resolve(__dirname, './detail.vue'), 'utf-8');

    expect(file).toContain('data-testid="manuscript-review-summary-cards"');
    expect(file).toContain('data-testid="manuscript-review-basic-info"');
    expect(file).toContain('data-testid="manuscript-review-permission-matrix"');
    expect(file).toContain('data-testid="manuscript-review-resource-shell"');
    expect(file).toContain('data-testid="manuscript-review-history-shell"');
    expect(file).toContain('manuscript-review-detail-shell__action-panel');
    expect(file).toContain('permissionMatrix');
    expect(file).toContain('去审批');
    expect(file).toContain('重新提交');
    expect(file).not.toContain('页内直接审批');
  });
});
