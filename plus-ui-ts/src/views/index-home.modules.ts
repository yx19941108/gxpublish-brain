import { getNormalPath } from '@/utils/ruoyi';
import type { RouteRecordRaw } from 'vue-router';

export interface HomeModuleConfig {
  key: 'ledger' | 'myDocument' | 'taskWaiting' | 'taskFinish';
  title: string;
  description: string;
  badge: string;
  glyph: string;
  routePath: string;
}

export const HOME_MODULES: HomeModuleConfig[] = [
  {
    key: 'ledger',
    title: '审校台账',
    description: '查看审校流程进度，按标题、稿件号和状态进行检索，并进入详情页。',
    badge: '台账入口',
    glyph: '台',
    routePath: '/manuscript/review'
  },
  {
    key: 'myDocument',
    title: '我发起的',
    description: '查看本人发起的流程记录，继续跟进当前状态和后续处理。',
    badge: '发起入口',
    glyph: '发',
    routePath: '/manuscript/review/my-document'
  },
  {
    key: 'taskWaiting',
    title: '我的待办',
    description: '进入待办任务页面，集中处理当前待审批的业务事项。',
    badge: '待办入口',
    glyph: '待',
    routePath: '/manuscript/review/task-waiting'
  },
  {
    key: 'taskFinish',
    title: '我的已办',
    description: '查看已处理事项与结果记录，便于后续回看和追踪。',
    badge: '已办入口',
    glyph: '已',
    routePath: '/manuscript/review/task-finish'
  }
];

const EXTERNAL_PATH_PATTERN = /^(https?:|mailto:|tel:)/i;

const normalizeRoutePath = (path: string): string => {
  if (!path) {
    return '';
  }
  return getNormalPath(path.startsWith('/') ? path : `/${path}`);
};

const joinRoutePath = (parentPath: string, currentPath: string): string => {
  if (!currentPath) {
    return normalizeRoutePath(parentPath);
  }
  if (EXTERNAL_PATH_PATTERN.test(currentPath)) {
    return currentPath;
  }
  if (currentPath.startsWith('/')) {
    return normalizeRoutePath(currentPath);
  }
  const basePath = normalizeRoutePath(parentPath);
  if (!basePath) {
    return normalizeRoutePath(currentPath);
  }
  return getNormalPath(`${basePath}/${currentPath}`);
};

export const collectVisibleRoutePaths = (routes: RouteRecordRaw[]): Set<string> => {
  const visiblePaths = new Set<string>();

  const visitRoutes = (items: RouteRecordRaw[], parentPath = '') => {
    items.forEach((route) => {
      if (route.hidden) {
        return;
      }

      const fullPath = joinRoutePath(parentPath, String(route.path ?? ''));

      if (route.meta?.title && fullPath && !EXTERNAL_PATH_PATTERN.test(fullPath)) {
        visiblePaths.add(fullPath);
      }

      if (route.children?.length) {
        visitRoutes(route.children, fullPath);
      }
    });
  };

  visitRoutes(routes);

  return visiblePaths;
};

export const resolveAccessibleHomeModules = (routes: RouteRecordRaw[]): HomeModuleConfig[] => {
  const visiblePaths = collectVisibleRoutePaths(routes);
  return HOME_MODULES.filter((module) => visiblePaths.has(module.routePath));
};
