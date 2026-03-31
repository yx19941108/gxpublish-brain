import { describe, expect, it } from 'vitest';
import type { RouteRecordRaw } from 'vue-router';
import { collectVisibleRoutePaths, resolveAccessibleHomeModules } from './index-home.modules';

describe('index home modules', () => {
  it('shows only manuscript-review menus that are actually visible in the sidebar', () => {
    const sidebarRoutes = [
      {
        path: '/index',
        meta: { title: '首页' }
      },
      {
        path: 'manuscript',
        meta: { title: '审校管理' },
        children: [
          {
            path: 'review',
            meta: { title: '审校台账' }
          },
          {
            path: 'review/my-document',
            meta: { title: '我发起的' }
          },
          {
            path: 'review/task-waiting',
            hidden: true,
            meta: { title: '我的待办' }
          }
        ]
      }
    ] as RouteRecordRaw[];

    expect(resolveAccessibleHomeModules(sidebarRoutes).map((module) => module.routePath)).toEqual([
      '/manuscript/review',
      '/manuscript/review/my-document'
    ]);
  });

  it('does not surface child menus when the parent sidebar menu itself is hidden', () => {
    const sidebarRoutes = [
      {
        path: 'manuscript',
        hidden: true,
        meta: { title: '审校管理' },
        children: [
          {
            path: 'review/task-finish',
            meta: { title: '我的已办' }
          }
        ]
      }
    ] as RouteRecordRaw[];

    expect(Array.from(collectVisibleRoutePaths(sidebarRoutes))).toEqual([]);
    expect(resolveAccessibleHomeModules(sidebarRoutes)).toEqual([]);
  });
});
