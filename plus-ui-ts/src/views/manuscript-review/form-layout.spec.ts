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
    expect(file).toContain('manuscript-review-form-page__link-preview');
    expect(file).toContain('isClickableExternalUrl');
    expect(file).toContain('buildFormPresentation');
    expect(file).toContain('deletePendingOssResource');
    expect(file).not.toContain('去审批');
    expect(file).not.toContain('重新提交');
    expect(file).not.toContain('CREATE / SUBMIT ONLY');
    expect(file).not.toContain('EDIT / SAVE ONLY');
  });

  it('keeps submitDepartment readonly and keeps create/edit as submit-save only actions', () => {
    const file = readFileSync(resolve(__dirname, './form.vue'), 'utf-8');

    expect(file).toContain('v-model="formModel.submitDepartment"');
    expect(file).toContain('readonly');
    expect(file).toContain("presentation.mode === 'create'");
    expect(file).toContain("presentation.mode === 'edit'");
    expect(file).not.toContain('保存并提交');
    expect(file).not.toContain('保存草稿');
    expect(file).not.toContain('草稿按钮');
    expect(file).not.toContain('create 模式');
    expect(file).not.toContain('edit 模式');
    expect(file).not.toContain('ossId={{ item.ossId }}');
    expect(file).not.toContain('DELETE /resource/oss/{ossId}');
  });

  it('freezes processType on edit and does not require submitDepartment', () => {
    const file = readFileSync(resolve(__dirname, './form.vue'), 'utf-8');

    expect(file).toContain('<el-select v-model="formModel.processType"');
    expect(file).toContain('processTypeOptions');
    expect(file).toContain(':disabled="presentation.mode === \'edit\'"');
    expect(file).toContain('target="_blank"');
    expect(file).toContain('rel="noopener noreferrer"');
    expect(file).not.toContain('placeholder="请输入流程类型"');
    expect(file).not.toContain('submitDepartment: [{ required: true');
  });

  it('exposes author validation and upload progress blocking hooks in the form shell', () => {
    const file = readFileSync(resolve(__dirname, './form.vue'), 'utf-8');

    expect(file).toContain('<el-form-item label="作者" prop="authorName">');
    expect(file).toContain('data-testid="manuscript-review-form-upload-progress-list"');
    expect(file).toContain(':on-progress="handleUploadProgress"');
    expect(file).toContain(':disabled="hasUploadingInFlight"');
    expect(file).toContain('ElMessageBox.confirm');
  });
});
