import { readFileSync } from 'node:fs';
import { resolve } from 'node:path';
import { describe, expect, it } from 'vitest';

describe('manuscript review approval route shell', () => {
  it('registers the frozen approval carrier route and uses taskId to open BPM approval handling', () => {
    const routerSource = readFileSync(resolve(__dirname, '../../router/index.ts'), 'utf-8');
    const detailSource = readFileSync(resolve(__dirname, './detail.vue'), 'utf-8');

    expect(routerSource).toContain("path: 'review/approval'");
    expect(routerSource).toContain("name: 'ManuscriptReviewApproval'");
    expect(detailSource).toContain('const isApprovalPage = computed(() => route.path === \'/manuscript/review/approval\')');
    expect(detailSource).toContain('const routeTaskId = computed(() =>');
    expect(detailSource).toContain("submitVerifyRef.value?.openDialog(String(routeTaskId.value))");
    expect(detailSource).toContain('<submitVerify ref="submitVerifyRef"');
  });
});
