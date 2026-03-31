<template>
  <section class="manuscript-review-detail-shell">
    <header class="manuscript-review-detail-shell__header">
      <div class="manuscript-review-detail-shell__headline">
        <p class="manuscript-review-detail-shell__eyebrow">{{ isApprovalPage ? '审校审批页' : '审校详情页' }}</p>
        <h1>{{ isApprovalPage ? '审校审批' : '审校详情' }}</h1>
        <p class="manuscript-review-detail-shell__description">
          {{
            isApprovalPage
              ? '审批页承接流程办理动作，并保留审校单的状态摘要、基本信息、当前有效资源和统一时间线。'
              : '详情页统一承载状态摘要、基本信息、当前有效资源和统一时间线，并提供与当前业务状态匹配的后置动作入口。'
          }}
        </p>
      </div>
      <nav
        data-testid="manuscript-review-action-panel"
        class="manuscript-review-detail-shell__actions manuscript-review-detail-shell__action-panel"
        aria-label="详情动作栏"
      >
        <button
          v-for="action in actionBar"
          :key="action.key"
          type="button"
          class="manuscript-review-detail-shell__action"
          :class="`manuscript-review-detail-shell__action--${action.key}`"
          :disabled="actionPendingKey === action.key"
          @click="onAction(action.key)"
        >
          {{ action.label }}
        </button>
      </nav>
    </header>

    <span
      data-testid="manuscript-review-hidden-reviewId"
      aria-hidden="true"
      :data-review-id="reviewId"
      class="manuscript-review-detail-shell__review-id-anchor"
    />

    <p v-if="errorMessage" class="manuscript-review-detail-shell__alert" role="status">
      {{ errorMessage }}
    </p>

    <section data-testid="manuscript-review-summary-cards" class="manuscript-review-detail-shell__summary-cards">
      <article v-for="card in summaryCards" :key="card.label" class="manuscript-review-detail-shell__summary-card">
        <p class="manuscript-review-detail-shell__summary-label">{{ card.label }}</p>
        <p class="manuscript-review-detail-shell__summary-value">
          <template v-if="loading">加载中…</template>
          <template v-else>{{ card.value }}</template>
        </p>
        <p class="manuscript-review-detail-shell__summary-meta">{{ loading ? '请稍候…' : card.meta }}</p>
      </article>
    </section>

    <div class="manuscript-review-detail-shell__content">
      <div class="manuscript-review-detail-shell__main">
        <section data-testid="manuscript-review-basic-info" class="manuscript-review-detail-shell__section">
          <header class="manuscript-review-detail-shell__section-header">
            <div>
              <h2 class="manuscript-review-detail-shell__section-title">基本信息与状态摘要</h2>
              <p class="manuscript-review-detail-shell__section-desc">
                当前页用于查看业务信息、资源与历史，并承接修改、去审批、重新提交等后置动作入口。
              </p>
            </div>
          </header>
          <div class="manuscript-review-detail-shell__section-body">
            <div class="manuscript-review-detail-shell__info-grid">
              <div v-for="item in basicInfoItems" :key="item.label" class="manuscript-review-detail-shell__info-field">
                <p class="manuscript-review-detail-shell__info-label">{{ item.label }}</p>
                <div class="manuscript-review-detail-shell__info-box">
                  {{ loading ? '加载中…' : item.value }}
                </div>
              </div>
            </div>
          </div>
        </section>

        <section data-testid="manuscript-review-resource-shell" class="manuscript-review-detail-shell__section">
          <header class="manuscript-review-detail-shell__section-header">
            <div>
              <h2 class="manuscript-review-detail-shell__section-title">当前有效资源区</h2>
              <p class="manuscript-review-detail-shell__section-desc">资源按附件、外链、视频、视频标注分组展示；当前有效资源与历史追溯分开表达。</p>
            </div>
          </header>
          <div class="manuscript-review-detail-shell__section-body manuscript-review-detail-shell__section-body--resource">
            <div class="manuscript-review-detail-shell__video-panel" data-testid="manuscript-review-video-panel">
              <template v-if="loading">
                <p class="manuscript-review-detail-shell__section-hint">正在加载视频数据…</p>
              </template>
              <template v-else-if="!activeVideo">
                <div class="manuscript-review-detail-shell__video-placeholder">
                  <strong>视频播放器区</strong>
                  <span>当前暂无可播放视频，待视频资源入库后在此承载“视频列表 + 单播放器”的详情联动。</span>
                </div>
              </template>
              <template v-else>
                <div class="manuscript-review-detail-shell__video-stage">
                  <video
                    ref="videoPlayerRef"
                    :key="activeVideo.id"
                    data-testid="manuscript-review-video-player"
                    class="manuscript-review-detail-shell__video-player"
                    :src="activeVideo.resourceUrl"
                    controls
                    preload="metadata"
                    @loadedmetadata="onVideoLoadedMetadata"
                  />
                  <div class="manuscript-review-detail-shell__video-meta">
                    <div>
                      <p class="manuscript-review-detail-shell__video-title">{{ activeVideo.name }}</p>
                      <p class="manuscript-review-detail-shell__video-note">当前命中 {{ activeVideoMarks.length }} 条有效标注</p>
                    </div>
                  </div>
                </div>

                <div
                  v-if="videoPlaybackItems.length > 1"
                  class="manuscript-review-detail-shell__video-switcher"
                  data-testid="manuscript-review-video-switcher"
                >
                  <button
                    v-for="item in videoPlaybackItems"
                    :key="item.id"
                    type="button"
                    class="manuscript-review-detail-shell__video-switch"
                    :class="{ 'manuscript-review-detail-shell__video-switch--active': item.id === activeVideo.id }"
                    @click="selectVideo(item.id)"
                  >
                    <span>{{ item.name }}</span>
                    <span>{{ item.marks.length }} 条标注</span>
                  </button>
                </div>

                <div class="manuscript-review-detail-shell__video-marks" data-testid="manuscript-review-video-mark-jump-list">
                  <p class="manuscript-review-detail-shell__video-marks-title">当前视频标注</p>
                  <p v-if="activeVideoMarks.length === 0" class="manuscript-review-detail-shell__section-hint">当前视频暂无有效标注。</p>
                  <div v-else class="manuscript-review-detail-shell__video-mark-list">
                    <button
                      v-for="item in activeVideoMarks"
                      :key="item.id"
                      type="button"
                      class="manuscript-review-detail-shell__video-mark"
                      @click="jumpToVideoMark(item.resourceId, item.startSeconds)"
                    >
                      <span class="manuscript-review-detail-shell__video-mark-time">
                        {{ item.startTimeText }}<template v-if="item.endTimeText"> - {{ item.endTimeText }}</template>
                      </span>
                      <span class="manuscript-review-detail-shell__video-mark-content">{{ item.markContent }}</span>
                    </button>
                  </div>
                </div>

                <div
                  v-if="canManageResources && videoPlaybackItems.length > 0"
                  class="manuscript-review-detail-shell__video-mark-create"
                  data-testid="manuscript-review-video-mark-create"
                >
                  <div class="manuscript-review-detail-shell__video-mark-create-header">
                    <div>
                      <p class="manuscript-review-detail-shell__video-marks-title">新增视频标注</p>
                      <p class="manuscript-review-detail-shell__section-hint">
                        标注能力与详情页“修改”权限保持一致，提交成功后会刷新当前标注列表与时间线。
                      </p>
                    </div>
                    <button
                      type="button"
                      class="manuscript-review-detail-shell__video-mark-submit"
                      :disabled="videoMarkSubmitting"
                      @click="handleAddVideoMark"
                    >
                      {{ videoMarkSubmitting ? '提交中…' : '提交新增标注' }}
                    </button>
                  </div>
                  <div class="manuscript-review-detail-shell__video-mark-form">
                    <label class="manuscript-review-detail-shell__video-mark-field">
                      <span>当前视频</span>
                      <select v-model="pendingVideoMarkResourceId">
                        <option v-for="item in videoPlaybackItems" :key="item.id" :value="item.id">
                          {{ item.name }}
                        </option>
                      </select>
                    </label>
                    <label class="manuscript-review-detail-shell__video-mark-field">
                      <span>开始时间</span>
                      <input v-model.trim="pendingVideoMarkStartTime" type="text" placeholder="00:00:05" />
                    </label>
                    <label class="manuscript-review-detail-shell__video-mark-field">
                      <span>结束时间</span>
                      <input v-model.trim="pendingVideoMarkEndTime" type="text" placeholder="00:00:12" />
                    </label>
                    <label class="manuscript-review-detail-shell__video-mark-field manuscript-review-detail-shell__video-mark-field--full">
                      <span>标注内容</span>
                      <textarea v-model.trim="pendingVideoMarkContent" rows="3" maxlength="500" placeholder="请输入需要记录的问题或说明" />
                    </label>
                  </div>
                  <p
                    v-if="videoMarkFormError"
                    data-testid="manuscript-review-video-mark-form-error"
                    class="manuscript-review-detail-shell__video-mark-form-error"
                    role="alert"
                  >
                    {{ videoMarkFormError }}
                  </p>
                </div>
              </template>
            </div>

            <div class="manuscript-review-detail-shell__resource-groups">
              <article v-for="group in resourceGroups" :key="group.key" class="manuscript-review-detail-shell__resource-group">
                <h3 class="manuscript-review-detail-shell__resource-group-title">{{ group.title }}</h3>
                <p v-if="loading" class="manuscript-review-detail-shell__section-hint">正在加载资源数据…</p>
                <p v-else-if="group.items.length === 0" class="manuscript-review-detail-shell__section-hint">当前暂无{{ group.title }}数据。</p>
                <ul v-else class="manuscript-review-detail-shell__resource-list" :aria-label="`${group.title}列表`">
                  <li v-for="item in group.items" :key="item.id" class="manuscript-review-detail-shell__resource-item">
                    <div class="manuscript-review-detail-shell__resource-main">
                      <a
                        v-if="item.href"
                        class="manuscript-review-detail-shell__resource-name manuscript-review-detail-shell__resource-link"
                        :href="item.href"
                        target="_blank"
                        rel="noopener noreferrer"
                      >
                        {{ item.name }}
                      </a>
                      <button
                        v-else-if="item.resourceType === 'VIDEO_MARK' && item.resourceId && item.startTimeText"
                        type="button"
                        class="manuscript-review-detail-shell__resource-name manuscript-review-detail-shell__timeline-link"
                        @click="jumpToVideoMark(item.resourceId, parseVideoMarkStartSeconds(item))"
                      >
                        {{ item.name }}
                      </button>
                      <span v-else class="manuscript-review-detail-shell__resource-name">{{ item.name }}</span>
                      <span class="manuscript-review-detail-shell__resource-status">{{ item.statusLabel }}</span>
                    </div>
                    <div
                      v-for="line in item.metaLines ?? []"
                      :key="`${item.id}-${line}`"
                      class="manuscript-review-detail-shell__resource-note manuscript-review-detail-shell__resource-meta"
                    >
                      {{ line }}
                    </div>
                    <div v-if="item.note" class="manuscript-review-detail-shell__resource-note">{{ item.note }}</div>
                    <div v-if="!loading" class="manuscript-review-detail-shell__resource-actions">
                      <button
                        v-for="action in resolveResourceActions(item)"
                        :key="`${item.id}-${action.key}`"
                        type="button"
                        class="manuscript-review-detail-shell__resource-action"
                        :disabled="resourcePendingKey === `${item.id}:${action.key}`"
                        @click="onResourceAction(item, action.key)"
                      >
                        {{ action.label }}
                      </button>
                    </div>
                  </li>
                </ul>
              </article>
            </div>
          </div>
        </section>

        <section data-testid="manuscript-review-history-shell" class="manuscript-review-detail-shell__section">
          <header class="manuscript-review-detail-shell__section-header">
            <div>
              <h2 class="manuscript-review-detail-shell__section-title">统一时间线</h2>
              <p class="manuscript-review-detail-shell__section-desc">时间线采用统一自然语言可读文案，整合业务动作、系统判定与审批动作。</p>
            </div>
          </header>
          <div class="manuscript-review-detail-shell__section-body">
            <p v-if="loading" class="manuscript-review-detail-shell__section-hint">正在加载历史数据…</p>
            <p v-else-if="historyItems.length === 0" class="manuscript-review-detail-shell__section-hint">暂无可展示历史数据。</p>
            <ol v-else class="manuscript-review-detail-shell__timeline" aria-label="流程历史时间线">
              <li v-for="item in historyItems" :key="item.id" class="manuscript-review-detail-shell__timeline-item">
                <div class="manuscript-review-detail-shell__timeline-time">{{ item.timeLabel }}</div>
                <div class="manuscript-review-detail-shell__timeline-content">
                  <div class="manuscript-review-detail-shell__timeline-title">
                    <template v-if="item.actionLinkLabel && (item.actionLinkHref || item.actionLinkOssId)">
                      {{ item.actionPrefix }}
                      <button type="button" class="manuscript-review-detail-shell__timeline-link" @click="onHistoryAction(item)">
                        {{ item.actionLinkLabel }}
                      </button>
                      {{ item.actionSuffix }}
                    </template>
                    <template v-else>{{ item.actionLabel }}</template>
                  </div>
                  <div v-if="item.operatorName" class="manuscript-review-detail-shell__timeline-operator">
                    {{ item.operatorName }}
                  </div>
                  <div v-if="item.remark" class="manuscript-review-detail-shell__timeline-remark">{{ item.remark }}</div>
                  <div v-if="!loading && resolveHistoryActions(item).length > 0" class="manuscript-review-detail-shell__timeline-actions">
                    <button
                      v-for="action in resolveHistoryActions(item)"
                      :key="`${item.id}-${action.key}`"
                      type="button"
                      class="manuscript-review-detail-shell__resource-action"
                      :disabled="resourcePendingKey === `${item.id}:${action.key}`"
                      @click="onHistorySecondaryAction(item, action.key)"
                    >
                      {{ action.label }}
                    </button>
                  </div>
                </div>
              </li>
            </ol>
          </div>
        </section>
      </div>
    </div>
    <submitVerify ref="submitVerifyRef" :task-variables="{}" @submit-callback="handleApprovalSubmit" />
  </section>
</template>

<script setup lang="ts">
import { computed, getCurrentInstance, nextTick, onMounted, ref, watch } from 'vue';
import { useRoute, useRouter } from 'vue-router';

import {
  addManuscriptReviewVideoMark,
  disableManuscriptReviewResource,
  disableManuscriptReviewVideoMark,
  enableManuscriptReviewResource,
  enableManuscriptReviewVideoMark,
  getManuscriptReviewDetail,
  getManuscriptReviewPreviewTicket,
  resubmitManuscriptReview
} from '@/api/manuscript-review';
import { flowHisTaskList, getInfo } from '@/api/workflow/instance';
import { resolveManuscriptApproveAction, type ManuscriptReviewApproveActionResult } from '@/types/manuscript-review/detail';
import SubmitVerify from '@/components/Process/submitVerify.vue';

import {
  applyVideoSelection,
  buildVideoPlaybackItems,
  buildDetailActionBar,
  normalizeDetailViewModel,
  parseVideoTimeTextToSeconds,
  reconcileVideoSelectionState,
  validatePendingVideoMarkDraft,
  type ManuscriptReviewDetailReadableViewModel,
  type ManuscriptReviewHistoryItem,
  type ManuscriptReviewResourceItem,
  type ManuscriptReviewVideoPlaybackItem,
  type ManuscriptReviewVideoPlaybackMarkItem
} from './detail.contract';
import {
  buildApprovalRouteLocation,
  buildEditRouteLocation,
  resolveApprovalBackTarget,
  resolveApprovalSuccessTarget,
  resolveDetailBackTarget
} from './detail-navigation';

type ActionKey = 'edit' | 'approve' | 'resubmit' | 'back' | '';
type ResourceActionKey = 'disable' | 'preview' | 'download' | 'open';
type HistoryActionKey = 'enable';

const props = defineProps<{
  pageMode: 'detail' | 'approval';
}>();

const { proxy } = getCurrentInstance() as ComponentInternalInstance;
const route = useRoute();
const router = useRouter();

const EMPTY_TEXT = '--';

const reviewId = computed(() => {
  const value = route.query.reviewId ?? route.query.id;
  return typeof value === 'string' ? value : Array.isArray(value) ? value[0] : '';
});
const isApprovalPage = computed(() => props.pageMode === 'approval');
const routeTaskId = computed(() => {
  const value = route.query.taskId;
  return typeof value === 'string' ? value : Array.isArray(value) ? value[0] : '';
});

const loading = ref(false);
const actionPendingKey = ref<ActionKey>('');
const resourcePendingKey = ref('');
const errorMessage = ref('');
const viewModel = ref<ManuscriptReviewDetailReadableViewModel | null>(null);
const submitVerifyRef = ref<InstanceType<typeof SubmitVerify>>();
const videoPlayerRef = ref<HTMLVideoElement | null>(null);
const activeVideoId = ref('');
const pendingSeekSeconds = ref<number | null>(null);
const pendingVideoMarkResourceId = ref('');
const pendingVideoMarkStartTime = ref('');
const pendingVideoMarkEndTime = ref('');
const pendingVideoMarkContent = ref('');
const videoMarkFormError = ref('');
const videoMarkSubmitting = ref(false);

const detailSource = computed(() => viewModel.value?.detail ?? null);
const actionBar = computed(() => {
  if (isApprovalPage.value) {
    return [
      { key: 'approve', label: '审批办理' },
      { key: 'back', label: '返回' }
    ];
  }

  return buildDetailActionBar(viewModel.value?.actionRole ?? 'HISTORY_PARTICIPANT');
});
const historyItems = computed<ManuscriptReviewHistoryItem[]>(() => viewModel.value?.historyItems ?? []);
const resourceItems = computed<ManuscriptReviewResourceItem[]>(() => viewModel.value?.resourceItems ?? []);
const videoPlaybackItems = computed(() => buildVideoPlaybackItems(detailSource.value ?? {}));
const activeVideo = computed<ManuscriptReviewVideoPlaybackItem | undefined>(() => {
  const currentId = activeVideoId.value.trim();
  if (currentId) {
    return videoPlaybackItems.value.find((item) => item.id === currentId);
  }
  return videoPlaybackItems.value[0];
});
const activeVideoMarks = computed<ManuscriptReviewVideoPlaybackMarkItem[]>(() => activeVideo.value?.marks ?? []);
const extractPreviewTicketUrl = (response: Awaited<ReturnType<typeof getManuscriptReviewPreviewTicket>>): string | undefined => {
  const payload = response?.data as
    | {
        data?: {
          resourceUrl?: string;
        };
        resourceUrl?: string;
      }
    | undefined;
  const resourceUrl = payload?.data?.resourceUrl ?? payload?.resourceUrl;
  return typeof resourceUrl === 'string' && resourceUrl.trim() ? resourceUrl.trim() : undefined;
};

const applyPreviewTicketUrls = async (
  currentViewModel: ManuscriptReviewDetailReadableViewModel
): Promise<ManuscriptReviewDetailReadableViewModel> => {
  const previewableIds = [
    ...(currentViewModel.detail.attachmentList ?? []).map((item) => String(item.id)),
    ...(currentViewModel.detail.videoList ?? []).map((item) => String(item.id))
  ];
  if (previewableIds.length === 0) {
    return currentViewModel;
  }

  const previewEntries = await Promise.all(
    previewableIds.map(async (resourceId) => {
      try {
        const response = await getManuscriptReviewPreviewTicket(resourceId);
        return [resourceId, extractPreviewTicketUrl(response)] as const;
      } catch {
        return [resourceId, undefined] as const;
      }
    })
  );

  const previewUrlMap = new Map(previewEntries.filter((entry): entry is readonly [string, string] => Boolean(entry[1])));
  if (previewUrlMap.size === 0) {
    return currentViewModel;
  }

  return normalizeDetailViewModel({
    data: {
      ...currentViewModel.detail,
      attachmentList: (currentViewModel.detail.attachmentList ?? []).map((item) => ({
        ...item,
        resourceUrl: previewUrlMap.get(String(item.id)) ?? item.resourceUrl
      })),
      videoList: (currentViewModel.detail.videoList ?? []).map((item) => ({
        ...item,
        resourceUrl: previewUrlMap.get(String(item.id)) ?? item.resourceUrl
      }))
    }
  });
};

const summaryCards = computed(() => {
  const detail = detailSource.value;

  return [
    {
      label: '业务状态',
      value: detail?.businessStatusLabel ?? EMPTY_TEXT,
      meta: detail?.currentNodeLabel ?? EMPTY_TEXT
    },
    {
      label: '当前节点',
      value: detail?.currentNodeLabel ?? EMPTY_TEXT,
      meta: detail?.updateTime ?? EMPTY_TEXT
    },
    {
      label: '流程类型',
      value: detail?.processTypeLabel ?? EMPTY_TEXT,
      meta: detail?.manuscriptCode ?? EMPTY_TEXT
    }
  ];
});

const basicInfoItems = computed(() => {
  const detail = detailSource.value;

  return [
    { label: '系统稿件号', value: detail?.manuscriptCode ?? EMPTY_TEXT },
    { label: '标题', value: detail?.title ?? EMPTY_TEXT },
    { label: '流程类型', value: detail?.processTypeLabel ?? EMPTY_TEXT },
    { label: '媒体栏目', value: detail?.mediaChannel ?? EMPTY_TEXT },
    { label: '报送部门', value: detail?.submitDepartment ?? EMPTY_TEXT },
    { label: '发起人', value: detail?.initiatorName ?? EMPTY_TEXT },
    { label: '作者', value: detail?.authorName ?? EMPTY_TEXT },
    { label: '说明', value: detail?.remark ?? EMPTY_TEXT }
  ];
});

const resourceGroups = computed(() => {
  const groups = [
    { key: 'attachment', title: '附件', items: [] as ManuscriptReviewResourceItem[] },
    { key: 'link', title: '外链', items: [] as ManuscriptReviewResourceItem[] },
    { key: 'video', title: '视频', items: [] as ManuscriptReviewResourceItem[] },
    { key: 'mark', title: '视频标注', items: [] as ManuscriptReviewResourceItem[] }
  ];

  for (const item of resourceItems.value) {
    const typeLabel = item.typeLabel ?? '';

    if (typeLabel.includes('标注')) {
      groups[3].items.push(item);
    } else if (typeLabel.includes('视频')) {
      groups[2].items.push(item);
    } else if (typeLabel.includes('外链')) {
      groups[1].items.push(item);
    } else {
      groups[0].items.push(item);
    }
  }

  return groups;
});

const canManageResources = computed(() => Boolean(detailSource.value?.permissionMatrix?.canEdit));

watch(
  videoPlaybackItems,
  (items) => {
    const selectionState = reconcileVideoSelectionState(items, activeVideoId.value, pendingVideoMarkResourceId.value);
    activeVideoId.value = selectionState.activeVideoId;
    pendingVideoMarkResourceId.value = selectionState.pendingVideoMarkResourceId;
    if (items.length === 0) {
      pendingSeekSeconds.value = null;
    }
  },
  { immediate: true }
);

watch([pendingVideoMarkResourceId, pendingVideoMarkStartTime, pendingVideoMarkContent], () => {
  if (videoMarkFormError.value) {
    videoMarkFormError.value = '';
  }
});

const resolveResourceActions = (item: ManuscriptReviewResourceItem) => {
  const actions: Array<{ key: ResourceActionKey; label: string }> = [];

  if (item.resourceType === 'EXTERNAL_LINK') {
    if (canManageResources.value) {
      actions.push({ key: 'disable', label: '停用' });
    }
    if (item.href) {
      actions.push({ key: 'open', label: '打开链接' });
    }
    return actions;
  }

  if (item.resourceType === 'VIDEO_MARK') {
    if (canManageResources.value) {
      actions.push({ key: 'disable', label: '停用' });
    }
    return actions;
  }

  if (canManageResources.value) {
    actions.push({ key: 'disable', label: '停用' });
  }
  if (item.resourceUrl) {
    actions.push({ key: 'preview', label: '预览' });
  }
  if (item.ossId) {
    actions.push({ key: 'download', label: '下载' });
  }
  return actions;
};

const resolveHistoryActions = (item: ManuscriptReviewHistoryItem) => {
  const actions: Array<{ key: HistoryActionKey; label: string }> = [];
  if (!canManageResources.value) {
    return actions;
  }
  if (!item.relatedResourceId || !item.relatedResourceType) {
    return actions;
  }
  if (item.actionType === 'RESOURCE_DISABLE' || item.actionType === 'VIDEO_MARK_DISABLE') {
    actions.push({ key: 'enable', label: '启用' });
  }
  return actions;
};

const fetchDetail = async () => {
  if (!reviewId.value) {
    errorMessage.value = '缺少必要的定位信息，请从台账进入详情页。';
    viewModel.value = null;
    return;
  }

  loading.value = true;
  errorMessage.value = '';

  try {
    const payload = await getManuscriptReviewDetail(reviewId.value);
    const normalizedViewModel = normalizeDetailViewModel(payload);
    viewModel.value = await applyPreviewTicketUrls(normalizedViewModel);
  } catch {
    viewModel.value = null;
    errorMessage.value = '详情数据加载失败，请稍后重试。';
  } finally {
    loading.value = false;
  }
};

const onAction = async (key: string) => {
  if (key === 'back') {
    await router.push(isApprovalPage.value ? resolveApprovalBackTarget(route.query) : resolveDetailBackTarget(route.query));
    return;
  }

  if (!reviewId.value) {
    errorMessage.value = '缺少必要的定位信息，请从台账进入详情页。';
    return;
  }

  if (key === 'edit') {
    await router.push(buildEditRouteLocation(reviewId.value, route.fullPath));
    return;
  }

  if (key === 'approve') {
    if (isApprovalPage.value) {
      if (!routeTaskId.value) {
        errorMessage.value = '当前缺少 taskId，无法打开审批办理弹窗。';
        return;
      }

      submitVerifyRef.value?.openDialog(String(routeTaskId.value));
      return;
    }

    actionPendingKey.value = 'approve';

    try {
      const [instanceResponse, taskResponse] = await Promise.all([getInfo(reviewId.value), flowHisTaskList(reviewId.value)]);
      const outcome: ManuscriptReviewApproveActionResult = resolveManuscriptApproveAction(reviewId.value, instanceResponse?.data, taskResponse?.data);

      if (outcome.kind === 'jump') {
        await router.push(
          buildApprovalRouteLocation(
            outcome.location.path,
            {
              id: outcome.location.query.id,
              type: outcome.location.query.type,
              taskId: String(outcome.location.query.taskId)
            },
            route.fullPath
          )
        );
        return;
      }

      errorMessage.value = outcome.message;
    } catch {
      errorMessage.value = '去审批入口加载失败，请稍后重试。';
    } finally {
      actionPendingKey.value = '';
    }

    return;
  }

  if (key === 'resubmit') {
    actionPendingKey.value = 'resubmit';

    try {
      await resubmitManuscriptReview({ id: reviewId.value });
      await fetchDetail();
    } catch {
      errorMessage.value = '重新提交失败，请稍后重试。';
    } finally {
      actionPendingKey.value = '';
    }
  }
};

const openExternalTarget = (href?: string) => {
  if (!href) {
    errorMessage.value = '当前资源缺少可访问地址。';
    return;
  }

  window.open(href, '_blank', 'noopener,noreferrer');
};

const parseVideoMarkStartSeconds = (item: ManuscriptReviewResourceItem): number | undefined => parseVideoTimeTextToSeconds(item.startTimeText);

const applyPendingVideoSeek = () => {
  const player = videoPlayerRef.value;
  if (!player || pendingSeekSeconds.value == null) {
    return;
  }

  player.currentTime = pendingSeekSeconds.value;
  pendingSeekSeconds.value = null;
};

const onVideoLoadedMetadata = () => {
  applyPendingVideoSeek();
};

const selectVideo = (videoId: string) => {
  const selectionState = applyVideoSelection(videoId);
  activeVideoId.value = selectionState.activeVideoId;
  pendingVideoMarkResourceId.value = selectionState.pendingVideoMarkResourceId;
  videoMarkFormError.value = '';
};

const resetPendingVideoMarkForm = () => {
  pendingVideoMarkResourceId.value = activeVideo.value?.id ?? videoPlaybackItems.value[0]?.id ?? '';
  pendingVideoMarkStartTime.value = '';
  pendingVideoMarkEndTime.value = '';
  pendingVideoMarkContent.value = '';
  videoMarkFormError.value = '';
};

const handleAddVideoMark = async () => {
  const validation = validatePendingVideoMarkDraft({
    reviewId: reviewId.value,
    activeVideoId: activeVideo.value?.id,
    pendingVideoMarkResourceId: pendingVideoMarkResourceId.value,
    startTimeText: pendingVideoMarkStartTime.value,
    markContent: pendingVideoMarkContent.value
  });
  if (!validation.ok) {
    videoMarkFormError.value = validation.formError ?? '';
    errorMessage.value = validation.pageError ?? '';
    return;
  }

  videoMarkSubmitting.value = true;
  errorMessage.value = '';
  videoMarkFormError.value = '';
  activeVideoId.value = validation.resourceId ?? '';

  try {
    await addManuscriptReviewVideoMark({
      reviewId: reviewId.value,
      resourceId: validation.resourceId,
      startTimeText: validation.startTimeText,
      endTimeText: pendingVideoMarkEndTime.value.trim() || undefined,
      markContent: validation.markContent
    });
    resetPendingVideoMarkForm();
    await fetchDetail();
    proxy?.$modal.msgSuccess('新增视频标注成功');
  } catch {
    errorMessage.value = '新增视频标注失败，请稍后重试。';
  } finally {
    videoMarkSubmitting.value = false;
  }
};

const jumpToVideoMark = async (videoId: string, seconds?: number) => {
  if (!videoId || seconds == null) {
    errorMessage.value = '当前标注缺少有效时间信息，无法跳转。';
    return;
  }

  errorMessage.value = '';
  pendingSeekSeconds.value = seconds;
  activeVideoId.value = videoId;
  await nextTick();

  const player = videoPlayerRef.value;
  if (player && player.readyState >= 1) {
    applyPendingVideoSeek();
  }
};

const onHistoryAction = (item: ManuscriptReviewHistoryItem) => {
  if (item.actionLinkOssId) {
    proxy?.$download.oss(item.actionLinkOssId);
    return;
  }
  if (item.actionLinkHref) {
    openExternalTarget(item.actionLinkHref);
  }
};

const onHistorySecondaryAction = async (item: ManuscriptReviewHistoryItem, key: HistoryActionKey) => {
  if (key !== 'enable' || !item.relatedResourceId || !item.relatedResourceType) {
    return;
  }

  resourcePendingKey.value = `${item.id}:${key}`;

  try {
    await proxy?.$modal.confirm(item.relatedResourceType === 'VIDEO_MARK' ? '确定启用该视频标注吗？' : '确定启用该资源吗？');
    if (item.relatedResourceType === 'VIDEO_MARK') {
      await enableManuscriptReviewVideoMark({ markId: item.relatedResourceId });
    } else {
      await enableManuscriptReviewResource({ resourceId: item.relatedResourceId });
    }
    proxy?.$modal.msgSuccess('启用成功');
    await fetchDetail();
  } catch (error) {
    if (error !== 'cancel') {
      errorMessage.value = item.relatedResourceType === 'VIDEO_MARK' ? '启用视频标注失败，请稍后重试。' : '启用资源失败，请稍后重试。';
    }
  } finally {
    resourcePendingKey.value = '';
  }
};

const onResourceAction = async (item: ManuscriptReviewResourceItem, key: ResourceActionKey) => {
  if (key === 'preview') {
    openExternalTarget(item.resourceUrl);
    return;
  }

  if (key === 'download') {
    if (!item.ossId) {
      errorMessage.value = '当前资源缺少 ossId，无法发起下载。';
      return;
    }
    proxy?.$download.oss(item.ossId);
    return;
  }

  if (key === 'open') {
    openExternalTarget(item.href);
    return;
  }

  if (key !== 'disable') {
    return;
  }

  resourcePendingKey.value = `${item.id}:${key}`;

  try {
    await proxy?.$modal.confirm(item.resourceType === 'VIDEO_MARK' ? '确定停用该视频标注吗？' : '确定停用该资源吗？');
    if (item.resourceType === 'VIDEO_MARK') {
      await disableManuscriptReviewVideoMark({ markId: item.id });
    } else {
      await disableManuscriptReviewResource({ resourceId: item.id });
    }
    proxy?.$modal.msgSuccess('停用成功');
    await fetchDetail();
  } catch (error) {
    if (error !== 'cancel') {
      errorMessage.value = item.resourceType === 'VIDEO_MARK' ? '停用视频标注失败，请稍后重试。' : '停用资源失败，请稍后重试。';
    }
  } finally {
    resourcePendingKey.value = '';
  }
};

const handleApprovalSubmit = async () => {
  await router.replace(resolveApprovalSuccessTarget(route.query));
};

onMounted(() => {
  resetPendingVideoMarkForm();
  void fetchDetail();
});
</script>

<style scoped>
.manuscript-review-detail-shell {
  display: grid;
  gap: 20px;
  padding: 24px;
  background: linear-gradient(180deg, #f6f9ff 0%, #f1f5fb 100%);
}

.manuscript-review-detail-shell__header {
  display: flex;
  flex-wrap: wrap;
  justify-content: space-between;
  align-items: flex-start;
  gap: 16px;
}

.manuscript-review-detail-shell__headline {
  display: grid;
  gap: 8px;
}

.manuscript-review-detail-shell__eyebrow {
  margin: 0;
  color: var(--el-color-primary);
  font-size: 13px;
  font-weight: 700;
  letter-spacing: 0.08em;
}

.manuscript-review-detail-shell__headline h1 {
  margin: 0;
  font-size: 32px;
}

.manuscript-review-detail-shell__description {
  margin: 0;
  max-width: 880px;
  color: var(--el-text-color-secondary);
  line-height: 1.6;
}

.manuscript-review-detail-shell__actions {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
}

.manuscript-review-detail-shell__action {
  min-width: 92px;
  padding: 10px 18px;
  border: 1px solid var(--el-border-color);
  border-radius: 999px;
  background: var(--el-bg-color);
  color: var(--el-text-color-primary);
  font: inherit;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s ease;
}

.manuscript-review-detail-shell__action:hover:enabled {
  transform: translateY(-1px);
  box-shadow: 0 8px 20px rgb(31 45 61 / 10%);
}

.manuscript-review-detail-shell__action:disabled {
  cursor: not-allowed;
  opacity: 0.7;
}

.manuscript-review-detail-shell__action--approve,
.manuscript-review-detail-shell__action--resubmit {
  background: var(--el-color-primary);
  border-color: var(--el-color-primary);
  color: var(--el-color-white);
}

.manuscript-review-detail-shell__review-id-anchor {
  display: none;
}

.manuscript-review-detail-shell__alert {
  margin: 0;
  padding: 12px 16px;
  border: 1px solid var(--el-color-danger-light-5);
  border-radius: 12px;
  background: var(--el-color-danger-light-9);
  color: var(--el-color-danger);
}

.manuscript-review-detail-shell__summary-cards {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 16px;
}

.manuscript-review-detail-shell__summary-card,
.manuscript-review-detail-shell__section {
  border: 1px solid rgb(214 224 237 / 80%);
  border-radius: 18px;
  background: rgb(255 255 255 / 92%);
  box-shadow: 0 12px 28px rgb(31 45 61 / 8%);
}

.manuscript-review-detail-shell__summary-card {
  padding: 16px 18px;
}

.manuscript-review-detail-shell__summary-label {
  margin: 0;
  color: var(--el-text-color-secondary);
  font-size: 12px;
  font-weight: 700;
}

.manuscript-review-detail-shell__summary-value {
  margin: 10px 0 6px;
  font-size: 22px;
  font-weight: 700;
  color: var(--el-text-color-primary);
}

.manuscript-review-detail-shell__summary-meta {
  margin: 0;
  color: var(--el-text-color-secondary);
  line-height: 1.6;
}

.manuscript-review-detail-shell__content {
  display: grid;
  grid-template-columns: minmax(0, 1fr) 320px;
  gap: 20px;
}

.manuscript-review-detail-shell__main,
.manuscript-review-detail-shell__aside {
  display: grid;
  gap: 20px;
}

.manuscript-review-detail-shell__section--tight {
  align-content: start;
}

.manuscript-review-detail-shell__section-header {
  display: flex;
  flex-wrap: wrap;
  align-items: flex-start;
  justify-content: space-between;
  gap: 12px;
  padding: 18px 20px 0;
}

.manuscript-review-detail-shell__section-title {
  margin: 0;
  font-size: 18px;
}

.manuscript-review-detail-shell__section-desc {
  margin: 6px 0 0;
  color: var(--el-text-color-secondary);
  font-size: 13px;
  line-height: 1.6;
}

.manuscript-review-detail-shell__section-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.manuscript-review-detail-shell__tag {
  display: inline-flex;
  align-items: center;
  padding: 6px 12px;
  border-radius: 999px;
  font-size: 12px;
  font-weight: 700;
}

.manuscript-review-detail-shell__tag--primary {
  background: var(--el-color-primary-light-9);
  color: var(--el-color-primary);
}

.manuscript-review-detail-shell__tag--warning {
  background: var(--el-color-warning-light-9);
  color: var(--el-color-warning-dark-2);
}

.manuscript-review-detail-shell__section-body {
  display: grid;
  gap: 16px;
  padding: 18px 20px 20px;
}

.manuscript-review-detail-shell__section-body--resource {
  gap: 18px;
}

.manuscript-review-detail-shell__section-hint {
  margin: 0;
  color: var(--el-text-color-secondary);
}

.manuscript-review-detail-shell__info-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 14px 16px;
}

.manuscript-review-detail-shell__info-field {
  display: grid;
  gap: 8px;
}

.manuscript-review-detail-shell__info-field--full {
  grid-column: 1 / -1;
}

.manuscript-review-detail-shell__info-label {
  margin: 0;
  color: var(--el-text-color-secondary);
  font-size: 12px;
  font-weight: 700;
}

.manuscript-review-detail-shell__info-box {
  min-height: 46px;
  padding: 12px 14px;
  border: 1px solid var(--el-border-color);
  border-radius: 12px;
  background: var(--el-fill-color-extra-light);
  color: var(--el-text-color-primary);
  line-height: 1.5;
}

.manuscript-review-detail-shell__info-box--multiline,
.manuscript-review-detail-shell__video-placeholder {
  min-height: 92px;
}

.manuscript-review-detail-shell__video-panel {
  border: 1px solid var(--el-border-color);
  border-radius: 16px;
  background: linear-gradient(180deg, #fbfdff 0%, #f5f8fd 100%);
  padding: 18px;
  display: grid;
  gap: 16px;
}

.manuscript-review-detail-shell__video-placeholder {
  display: grid;
  gap: 10px;
  place-content: center;
  text-align: left;
  color: var(--el-text-color-secondary);
}

.manuscript-review-detail-shell__video-placeholder strong {
  color: var(--el-text-color-primary);
  font-size: 16px;
}

.manuscript-review-detail-shell__video-stage {
  display: grid;
  gap: 12px;
}

.manuscript-review-detail-shell__video-player {
  width: 100%;
  max-height: 420px;
  border-radius: 14px;
  background: #0f172a;
}

.manuscript-review-detail-shell__video-meta {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  justify-content: space-between;
  gap: 10px;
}

.manuscript-review-detail-shell__video-title,
.manuscript-review-detail-shell__video-note,
.manuscript-review-detail-shell__video-marks-title {
  margin: 0;
}

.manuscript-review-detail-shell__video-title {
  font-size: 16px;
  font-weight: 700;
  color: var(--el-text-color-primary);
}

.manuscript-review-detail-shell__video-note {
  margin-top: 4px;
  color: var(--el-text-color-secondary);
  font-size: 12px;
}

.manuscript-review-detail-shell__video-switcher,
.manuscript-review-detail-shell__video-mark-list {
  display: grid;
  gap: 10px;
}

.manuscript-review-detail-shell__video-mark-create {
  display: grid;
  gap: 16px;
  padding: 18px;
  border: 1px solid rgb(214 224 237 / 80%);
  border-radius: 16px;
  background: linear-gradient(180deg, rgb(245 249 255 / 95%) 0%, rgb(255 255 255 / 92%) 100%);
}

.manuscript-review-detail-shell__video-mark-create-header {
  display: flex;
  flex-wrap: wrap;
  justify-content: space-between;
  gap: 12px;
}

.manuscript-review-detail-shell__video-mark-form {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 12px;
}

.manuscript-review-detail-shell__video-mark-form-error {
  margin: 0;
  color: var(--el-color-danger);
  font-size: 13px;
  font-weight: 600;
}

.manuscript-review-detail-shell__video-mark-field {
  display: grid;
  gap: 8px;
  color: var(--el-text-color-secondary);
  font-size: 13px;
}

.manuscript-review-detail-shell__video-mark-field span {
  color: var(--el-text-color-primary);
  font-weight: 600;
}

.manuscript-review-detail-shell__video-mark-field select,
.manuscript-review-detail-shell__video-mark-field input,
.manuscript-review-detail-shell__video-mark-field textarea {
  width: 100%;
  padding: 10px 12px;
  border: 1px solid var(--el-border-color);
  border-radius: 12px;
  background: var(--el-bg-color);
  color: var(--el-text-color-primary);
  font: inherit;
}

.manuscript-review-detail-shell__video-mark-field textarea {
  resize: vertical;
  min-height: 88px;
}

.manuscript-review-detail-shell__video-mark-field--full {
  grid-column: 1 / -1;
}

.manuscript-review-detail-shell__video-mark-submit {
  align-self: start;
  min-width: 132px;
  padding: 10px 18px;
  border: 1px solid var(--el-color-primary);
  border-radius: 999px;
  background: var(--el-color-primary);
  color: var(--el-color-white);
  font: inherit;
  font-weight: 600;
  cursor: pointer;
}

.manuscript-review-detail-shell__video-mark-submit:disabled {
  cursor: not-allowed;
  opacity: 0.7;
}

.manuscript-review-detail-shell__video-switch {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  padding: 10px 12px;
  border: 1px solid var(--el-border-color);
  border-radius: 12px;
  background: #fff;
  color: var(--el-text-color-primary);
  font: inherit;
  cursor: pointer;
  text-align: left;
}

.manuscript-review-detail-shell__video-switch--active {
  border-color: var(--el-color-primary);
  background: var(--el-color-primary-light-9);
}

.manuscript-review-detail-shell__video-marks {
  display: grid;
  gap: 10px;
}

.manuscript-review-detail-shell__video-mark {
  display: grid;
  gap: 4px;
  padding: 10px 12px;
  border: 1px solid rgba(29, 78, 216, 0.16);
  border-radius: 12px;
  background: #fff;
  color: var(--el-text-color-primary);
  font: inherit;
  cursor: pointer;
  text-align: left;
}

.manuscript-review-detail-shell__video-mark:hover,
.manuscript-review-detail-shell__video-switch:hover,
.manuscript-review-detail-shell__resource-action:hover:not(:disabled) {
  background: rgba(59, 130, 246, 0.08);
}

.manuscript-review-detail-shell__video-mark-time {
  color: #1d4ed8;
  font-size: 12px;
  font-weight: 700;
}

.manuscript-review-detail-shell__video-mark-content {
  line-height: 1.5;
}

.manuscript-review-detail-shell__resource-groups {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 16px;
}

.manuscript-review-detail-shell__resource-group {
  display: grid;
  gap: 12px;
  padding: 16px;
  border: 1px solid var(--el-border-color);
  border-radius: 16px;
  background: var(--el-color-white);
}

.manuscript-review-detail-shell__resource-group-title {
  margin: 0;
  font-size: 16px;
}

.manuscript-review-detail-shell__resource-list {
  display: grid;
  gap: 10px;
  margin: 0;
  padding: 0;
  list-style: none;
}

.manuscript-review-detail-shell__resource-item {
  display: grid;
  gap: 6px;
  padding: 12px 14px;
  border: 1px solid var(--el-border-color-lighter);
  border-radius: 12px;
  background: var(--el-fill-color-blank);
}

.manuscript-review-detail-shell__resource-main {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  justify-content: space-between;
  gap: 8px 12px;
}

.manuscript-review-detail-shell__resource-name {
  font-weight: 600;
  color: var(--el-text-color-primary);
}

.manuscript-review-detail-shell__resource-status {
  display: inline-flex;
  align-items: center;
  padding: 4px 10px;
  border-radius: 999px;
  background: var(--el-color-success-light-9);
  color: var(--el-color-success-dark-2);
  font-size: 12px;
  font-weight: 700;
}

.manuscript-review-detail-shell__resource-note {
  color: var(--el-text-color-secondary);
  font-size: 12px;
  line-height: 1.6;
  word-break: break-word;
}

.manuscript-review-detail-shell__resource-meta {
  color: var(--el-text-color-regular);
}

.manuscript-review-detail-shell__resource-actions {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.manuscript-review-detail-shell__resource-action {
  padding: 6px 12px;
  border: 1px solid rgba(59, 130, 246, 0.16);
  border-radius: 999px;
  background: var(--el-color-white);
  color: #1d4ed8;
  font: inherit;
  cursor: pointer;
  transition: all 0.2s ease;
}

.manuscript-review-detail-shell__resource-action:disabled {
  cursor: not-allowed;
  opacity: 0.6;
}

.manuscript-review-detail-shell__timeline {
  display: grid;
  gap: 12px;
  margin: 0;
  padding: 0;
  list-style: none;
}

.manuscript-review-detail-shell__timeline-item {
  display: grid;
  grid-template-columns: 144px minmax(0, 1fr);
  gap: 14px;
  padding: 14px 16px;
  border: 1px solid var(--el-border-color);
  border-radius: 14px;
  background: linear-gradient(180deg, #fff 0%, #fbfdff 100%);
}

.manuscript-review-detail-shell__timeline-time {
  color: var(--el-text-color-secondary);
  font-size: 12px;
  font-weight: 700;
}

.manuscript-review-detail-shell__timeline-content {
  display: grid;
  gap: 6px;
}

.manuscript-review-detail-shell__timeline-title {
  font-weight: 600;
  color: var(--el-text-color-primary);
}

.manuscript-review-detail-shell__timeline-link {
  padding: 0;
  border: 0;
  background: transparent;
  color: var(--el-color-primary);
  font: inherit;
  font-weight: 700;
  cursor: pointer;
}

.manuscript-review-detail-shell__timeline-link:hover {
  text-decoration: underline;
}

.manuscript-review-detail-shell__timeline-operator,
.manuscript-review-detail-shell__timeline-remark {
  color: var(--el-text-color-secondary);
  font-size: 12px;
  line-height: 1.6;
}

.manuscript-review-detail-shell__permission-list,
.manuscript-review-detail-shell__guidance-list {
  display: grid;
  gap: 12px;
  margin: 0;
  padding: 0;
  list-style: none;
}

.manuscript-review-detail-shell__permission-item,
.manuscript-review-detail-shell__guidance-card {
  display: grid;
  gap: 6px;
  padding: 14px 16px;
  border: 1px solid var(--el-border-color);
  border-radius: 14px;
  background: linear-gradient(180deg, #fff 0%, #fbfdff 100%);
}

.manuscript-review-detail-shell__permission-item {
  grid-template-columns: 1fr auto;
  align-items: center;
}

.manuscript-review-detail-shell__guidance-card h3 {
  margin: 0;
  font-size: 15px;
}

.manuscript-review-detail-shell__guidance-card p {
  margin: 0;
  color: var(--el-text-color-secondary);
  font-size: 12px;
  line-height: 1.7;
}

.manuscript-review-detail-shell__guidance-card--primary {
  background: var(--el-color-primary-light-9);
}

.manuscript-review-detail-shell__guidance-card--warning {
  background: var(--el-color-warning-light-9);
}

@media (max-width: 1360px) {
  .manuscript-review-detail-shell__summary-cards,
  .manuscript-review-detail-shell__content,
  .manuscript-review-detail-shell__resource-groups,
  .manuscript-review-detail-shell__info-grid {
    grid-template-columns: 1fr;
  }

  .manuscript-review-detail-shell__video-mark-form {
    grid-template-columns: 1fr;
  }

  .manuscript-review-detail-shell__timeline-item {
    grid-template-columns: 1fr;
  }
}
</style>
