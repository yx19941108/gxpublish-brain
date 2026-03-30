import { readFileSync } from 'node:fs';
import { resolve } from 'node:path';

import { describe, expect, it } from 'vitest';

const ledgerViewSource = readFileSync(resolve(__dirname, 'index.vue'), 'utf8');
const myDocumentViewSource = readFileSync(resolve(__dirname, 'my-document.vue'), 'utf8');
const formViewSource = readFileSync(resolve(__dirname, 'form.vue'), 'utf8');
const routerSource = readFileSync(resolve(__dirname, '../../router/index.ts'), 'utf8');

describe('manuscript review first-round access control', () => {
  it('gates create entry on the submit permission in ledger and initiator task pages', () => {
    expect(ledgerViewSource).toMatch(/v-if="canCreate"/);
    expect(ledgerViewSource).toMatch(/checkPermi\(\['manuscript:review:submit'\]\)/);
    expect(myDocumentViewSource).toMatch(/v-if="canCreate"/);
    expect(myDocumentViewSource).toMatch(/checkPermi\(\['manuscript:review:submit'\]\)/);
  });

  it('declares manuscript form route permissions explicitly for create and edit boundaries', () => {
    expect(routerSource).toMatch(
      /permissions:\s*\[\s*'manuscript:review:submit'[\s\S]*'manuscript:review:edit'[\s\S]*'manuscript:review:resubmit'[\s\S]*'manuscript:review:goApprove'[\s\S]*\]/
    );
  });

  it('guards create mode inside the shared form shell before submission', () => {
    expect(formViewSource).toMatch(/mode\.value === 'create'/);
    expect(formViewSource).toMatch(/checkPermi\(\['manuscript:review:submit'\]\)/);
    expect(formViewSource).toMatch(/router\.replace\(\{ path: '\/manuscript\/review'/);
  });
});
