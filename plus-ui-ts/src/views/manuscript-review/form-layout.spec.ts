import { readFileSync } from 'node:fs';
import { resolve } from 'node:path';

import { describe, expect, it } from 'vitest';

describe('manuscript review form layout shell', () => {
  it('contains the frozen create-edit form sections and isolated primary action', () => {
    const file = readFileSync(resolve(__dirname, './form.vue'), 'utf-8');

    expect(file).toContain('data-testid="manuscript-review-form-page"');
    expect(file).toContain('data-testid="manuscript-review-form-primary-action"');
    expect(file).toContain('data-testid="manuscript-review-form-upload-section"');
    expect(file).toContain('data-testid="manuscript-review-form-external-links"');
    expect(file).toContain('data-testid="manuscript-review-form-persisted-resources"');
    expect(file).toContain('buildFormPresentation');
    expect(file).toContain('deletePendingOssResource');
    expect(file).not.toContain('去审批');
    expect(file).not.toContain('重新提交');
  });
});
