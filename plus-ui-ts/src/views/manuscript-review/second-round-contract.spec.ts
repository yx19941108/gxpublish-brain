import { readFileSync } from 'node:fs';
import { resolve } from 'node:path';

import { describe, expect, it } from 'vitest';

describe('T10_ManuscriptReview_SecondRoundContractSpec', () => {
  it('routes pending upload deletion through manuscript-review module endpoint instead of global oss delete', () => {
    const apiFile = readFileSync(resolve(__dirname, '../../api/manuscript-review/index.ts'), 'utf-8');
    const formFile = readFileSync(resolve(__dirname, './form.vue'), 'utf-8');

    expect(apiFile).toContain('deletePendingManuscriptReviewResource');
    expect(apiFile).toContain('/workflow/manuscript-review/resource/pending/');
    expect(apiFile).not.toContain('url: `/resource/oss/${ossId}`');
    expect(formFile).toContain('deletePendingManuscriptReviewResource');
    expect(formFile).not.toContain('deletePendingOssResource');
  });

  it('exposes round-2 video mark creation area and refreshes detail after creation', () => {
    const detailFile = readFileSync(resolve(__dirname, './detail-page-shell.vue'), 'utf-8');

    expect(detailFile).toContain('新增视频标注');
    expect(detailFile).toContain('addManuscriptReviewVideoMark');
    expect(detailFile).toContain('await addManuscriptReviewVideoMark');
    expect(detailFile).toContain('await fetchDetail()');
  });
});
