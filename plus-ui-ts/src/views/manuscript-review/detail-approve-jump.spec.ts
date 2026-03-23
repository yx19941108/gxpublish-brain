import { beforeEach, describe, expect, it, vi } from 'vitest';
import { nextTick, proxyRefs } from 'vue';

import DetailView from './detail.vue';
import { APPROVE_BLOCKER_MESSAGE } from '@/types/manuscript-review/detail';

const { routerPush, routerBack, routeState, requestGet, getInfo, flowHisTaskList, mountedCallbacks } = vi.hoisted(() => ({
  routerPush: vi.fn(async () => undefined),
  routerBack: vi.fn(),
  routeState: {
    query: {
      reviewId: '880000230001'
    }
  },
  requestGet: vi.fn(),
  getInfo: vi.fn(),
  flowHisTaskList: vi.fn(),
  mountedCallbacks: [] as Array<() => unknown>
}));

vi.mock('vue', async (importOriginal) => {
  const actual = await importOriginal<typeof import('vue')>();

  return {
    ...actual,
    onMounted: (callback: () => unknown) => {
      mountedCallbacks.push(callback);
    },
    useCssVars: () => undefined,
    useSSRContext: () => ({ modules: new Set<string>() })
  };
});

vi.mock('vue-router', () => ({
  useRoute: () => routeState,
  useRouter: () => ({
    push: routerPush,
    back: routerBack
  })
}));

vi.mock('@/utils/request', () => ({
  default: {
    get: requestGet
  }
}));

vi.mock('@/api/workflow/instance', () => ({
  getInfo,
  flowHisTaskList
}));

const flushAll = async () => {
  await Promise.resolve();
  await Promise.resolve();
  await nextTick();
  await Promise.resolve();
  await nextTick();
};

const createReadableDetailPayload = () => ({
  code: 200,
  data: {
    reviewId: 880000230001,
    summaryCard: {
      flowStatusLabel: '审批中',
      currentNodeLabel: '待二级审批',
      initiatorName: '审校发起人',
      updateTime: '2026-03-23 11:22:33'
    },
    manuscriptCard: {
      processTypeLabel: '审核流程',
      manuscriptCode: 'SH20260323991',
      externalManuscriptCode: 'WX-2026-991',
      title: '审校样例稿件',
      mediaChannelLabel: '广西日报 / 要闻',
      submitterDeptName: '总编室',
      authorNames: '张三、李四',
      note: '供前端跳转验证使用',
      content: '这里是正文内容。'
    },
    actionBar: {
      canModify: true,
      actions: ['修改', '去审批', '返回']
    },
    timeline: [{ createTime: '2026-03-23 11:20:00', text: '审校发起人新增了流程。' }],
    resources: {
      currentAttachments: [],
      currentExternalLinks: [],
      currentVideos: [],
      historyAttachments: [],
      historyExternalLinks: [],
      historyVideoMarkers: []
    }
  }
});

const createComponentState = async () => {
  mountedCallbacks.length = 0;

  const setupState = (DetailView as any).setup({}, { expose: () => undefined });

  for (const callback of mountedCallbacks) {
    await callback();
  }
  await flushAll();

  return setupState;
};

const renderHtml = (setupState: any) => {
  let html = '';
  const exposedState = proxyRefs(setupState);

  (DetailView as any).ssrRender(
    {},
    (chunk: string) => {
      html += chunk;
    },
    null,
    {},
    {},
    exposedState,
    {},
    {}
  );

  return html;
};

describe('T08_Frontend_DetailApproveJumpSpec', () => {
  beforeEach(() => {
    routerPush.mockClear();
    routerBack.mockClear();
    requestGet.mockReset();
    getInfo.mockReset();
    flowHisTaskList.mockReset();
    mountedCallbacks.length = 0;

    requestGet.mockResolvedValue(createReadableDetailPayload());
  });

  it('clicking 去审批 triggers router.push with the BPM approval path string', async () => {
    getInfo.mockResolvedValue({
      data: {
        formPath: '/workflow/approval/detail'
      }
    });
    flowHisTaskList.mockResolvedValue({
      data: {
        list: [
          { id: 'task-running', flowStatus: 'WAITING' },
          { id: 'task-history', flowStatus: 'PASS' }
        ]
      }
    });

    const setupState = await createComponentState();
    const approveAction = setupState.actionBar.value.find((action: { label: string; key: string }) => action.label === '去审批');

    expect(approveAction?.key).toBe('approve');

    await setupState.onAction(approveAction.key);
    await flushAll();

    expect(routerPush).toHaveBeenCalledWith('/workflow/approval/detail?id=880000230001&type=approval&taskId=task-running');
  });

  it('clicking 去审批 renders the blocker message when the current task is missing', async () => {
    getInfo.mockResolvedValue({
      data: {
        formPath: '/workflow/approval/detail'
      }
    });
    flowHisTaskList.mockResolvedValue({
      data: {
        list: []
      }
    });

    const setupState = await createComponentState();
    const approveAction = setupState.actionBar.value.find((action: { label: string; key: string }) => action.label === '去审批');

    expect(approveAction?.key).toBe('approve');

    await setupState.onAction(approveAction.key);
    await flushAll();

    const html = renderHtml(setupState);

    expect(html).toContain(APPROVE_BLOCKER_MESSAGE);
    expect(routerPush).not.toHaveBeenCalled();
  });
});
