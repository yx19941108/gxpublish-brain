import { describe, expect, it, vi } from 'vitest';

vi.mock('@/layout/index.vue', () => ({
  default: {}
}));

vi.mock('vue-router', async (importOriginal) => {
  const actual = await importOriginal<typeof import('vue-router')>();

  return {
    ...actual,
    createWebHistory: vi.fn(() => ({})),
    createRouter: vi.fn((options) => options)
  };
});

const { constantRoutes } = await import('@/router');

describe('manuscript review route shell', () => {
  it('registers ledger, form, and detail routes under the manuscript namespace', () => {
    const manuscriptRoute = constantRoutes.find((route) => route.path === '/manuscript');

    expect(manuscriptRoute).toBeDefined();

    const childPaths = manuscriptRoute?.children?.map((route) => route.path) ?? [];

    expect(childPaths).toEqual(expect.arrayContaining(['review', 'review/form', 'review/detail']));
    expect(childPaths).not.toContain('review/delete');
  });
});
