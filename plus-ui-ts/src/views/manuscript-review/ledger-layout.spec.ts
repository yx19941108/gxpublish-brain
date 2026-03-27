import { readFileSync } from 'node:fs';
import { resolve } from 'node:path';

import { describe, expect, it } from 'vitest';

const ledgerViewPath = resolve(__dirname, 'index.vue');
const ledgerViewSource = readFileSync(ledgerViewPath, 'utf8');

describe('manuscript review ledger layout shell', () => {
  it('uses the shared manuscript-review api instead of ad-hoc readable request routes', () => {
    expect(ledgerViewSource).toContain('@/api/manuscript-review');
    expect(ledgerViewSource).toContain('listManuscriptReview');
    expect(ledgerViewSource).not.toContain('/manuscript-review/readable/ledger');
  });

  it('renders a management-page search shell with right-toolbar and pagination', () => {
    expect(ledgerViewSource).toContain('<el-form');
    expect(ledgerViewSource).toContain('right-toolbar');
    expect(ledgerViewSource).toContain('<pagination');
    expect(ledgerViewSource).toContain('showSearch');
  });

  it('keeps query conditions aligned to frozen ledger filters and removes media channel selector', () => {
    expect(ledgerViewSource).toContain('label="流程类型"');
    expect(ledgerViewSource).toContain('label="业务状态"');
    expect(ledgerViewSource).toContain('BUSINESS_STATUS_OPTIONS');
    expect(ledgerViewSource).toContain('PROCESS_TYPE_OPTIONS');
    expect(ledgerViewSource).not.toContain('<el-form-item label="媒体栏目"');
    expect(ledgerViewSource).not.toContain('IN_PROGRESS');
    expect(ledgerViewSource).not.toContain('VIDEO');
  });

  it('keeps detail as the only row-level business action', () => {
    expect(ledgerViewSource).toContain('详情');
    expect(ledgerViewSource).not.toContain('去审批');
    expect(ledgerViewSource).not.toContain('重新提交');
    expect(ledgerViewSource).not.toContain('>撤销<');
    expect(ledgerViewSource).not.toContain('>修改<');
    expect(ledgerViewSource).toContain('goDetail(String(scope.row.id))');
  });

  it('keeps create entry in the header while preserving detail as the only row-level business action', () => {
    expect(ledgerViewSource).toContain('icon="Plus"');
    expect(ledgerViewSource).toContain('handleCreate');
    expect(ledgerViewSource).toContain('新增');
    expect(ledgerViewSource).toContain("path: '/manuscript/review/form'");
    expect(ledgerViewSource).toContain("query: { mode: 'create' }");
    expect(ledgerViewSource).not.toContain('去审批');
    expect(ledgerViewSource).not.toContain('重新提交');
    expect(ledgerViewSource).not.toContain('>撤销<');
    expect(ledgerViewSource).not.toContain('>修改<');
  });
});
