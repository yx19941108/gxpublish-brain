import { readFileSync } from 'node:fs';
import { resolve } from 'node:path';

import { describe, expect, it } from 'vitest';

describe('logout redirect boundary', () => {
  it('does not carry redirect when the user actively logs out', () => {
    const navbar = readFileSync(resolve(__dirname, '../../layout/components/Navbar.vue'), 'utf-8');

    expect(navbar).not.toContain("login?redirect=");
    expect(navbar).toContain("location.href = import.meta.env.VITE_APP_CONTEXT_PATH + 'login'");
  });

  it('keeps unauthenticated redirect flow for guarded routes', () => {
    const login = readFileSync(resolve(__dirname, '../login.vue'), 'utf-8');
    const permission = readFileSync(resolve(__dirname, '../../permission.ts'), 'utf-8');

    expect(login).toContain('newRoute.query.redirect');
    expect(permission).toContain('next(`/login?redirect=${redirect}`)');
  });
});
