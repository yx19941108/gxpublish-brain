import { readFileSync } from 'node:fs';
import { resolve } from 'node:path';
import { describe, expect, it } from 'vitest';

describe('manuscript review approval route shell', () => {
  it('registers the frozen approval carrier route as an independent page file', () => {
    const routerSource = readFileSync(resolve(__dirname, '../../router/index.ts'), 'utf-8');
    const detailSource = readFileSync(resolve(__dirname, './detail.vue'), 'utf-8');
    const approvalSource = readFileSync(resolve(__dirname, './approval.vue'), 'utf-8');

    expect(routerSource).toContain("path: 'review/approval'");
    expect(routerSource).toContain("name: 'ManuscriptReviewApproval'");
    expect(routerSource).toContain("component: () => import('@/views/manuscript-review/approval.vue')");
    expect(detailSource).toContain("page-mode=\"detail\"");
    expect(approvalSource).toContain("page-mode=\"approval\"");
    expect(detailSource).toContain("import ManuscriptReviewPageShell from './detail-page-shell.vue'");
    expect(approvalSource).toContain("import ManuscriptReviewPageShell from './detail-page-shell.vue'");
  });
});
