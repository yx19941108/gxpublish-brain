<template>
  <section class="manuscript-review-detail-shell">
    <header class="manuscript-review-detail-shell__header">
      <div>
        <h1>审校详情</h1>
      </div>
      <nav class="manuscript-review-detail-shell__actions" aria-label="详情动作栏">
        <button
          v-for="action in actionBar"
          :key="action.key"
          type="button"
          class="manuscript-review-detail-shell__action"
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

    <dl class="manuscript-review-detail-shell__summary">
      <template v-if="loading">
        <div class="manuscript-review-detail-shell__summary-row">
          <dt>加载状态</dt>
          <dd>正在加载详情数据…</dd>
        </div>
      </template>
      <template v-else>
        <div v-for="item in summaryItems" :key="item.label" class="manuscript-review-detail-shell__summary-row">
          <dt>{{ item.label }}</dt>
          <dd>{{ item.value }}</dd>
        </div>
      </template>
    </dl>

    <section data-testid="manuscript-review-history-shell" class="manuscript-review-detail-shell__section">
      <header class="manuscript-review-detail-shell__section-header">
        <h2 class="manuscript-review-detail-shell__section-title">流程历史</h2>
      </header>
      <div class="manuscript-review-detail-shell__section-body">
        <p v-if="loading" class="manuscript-review-detail-shell__section-hint">正在加载历史数据…</p>
        <p v-else-if="historyItems.length === 0" class="manuscript-review-detail-shell__section-hint">暂无可展示历史数据。</p>
        <ol v-else class="manuscript-review-detail-shell__timeline" aria-label="流程历史时间线">
          <li v-for="item in historyItems" :key="item.id" class="manuscript-review-detail-shell__timeline-item">
            <div class="manuscript-review-detail-shell__timeline-main">
              <span class="manuscript-review-detail-shell__timeline-action">{{ item.actionLabel }}</span>
              <span class="manuscript-review-detail-shell__timeline-operator">{{ item.operatorName }}</span>
            </div>
            <div class="manuscript-review-detail-shell__timeline-sub">
              <span class="manuscript-review-detail-shell__timeline-time">{{ item.timeLabel }}</span>
              <span v-if="item.remark" class="manuscript-review-detail-shell__timeline-remark">{{ item.remark }}</span>
            </div>
          </li>
        </ol>
      </div>
    </section>

    <section data-testid="manuscript-review-resource-shell" class="manuscript-review-detail-shell__section">
      <header class="manuscript-review-detail-shell__section-header">
        <h2 class="manuscript-review-detail-shell__section-title">资源区</h2>
      </header>
      <div class="manuscript-review-detail-shell__section-body">
        <p v-if="loading" class="manuscript-review-detail-shell__section-hint">正在加载资源数据…</p>
        <p v-else-if="resourceItems.length === 0" class="manuscript-review-detail-shell__section-hint">暂无可展示资源数据。</p>
        <ul v-else class="manuscript-review-detail-shell__resource-list" aria-label="资源列表">
          <li v-for="item in resourceItems" :key="item.id" class="manuscript-review-detail-shell__resource-item">
            <div class="manuscript-review-detail-shell__resource-main">
              <span class="manuscript-review-detail-shell__resource-type">{{ item.typeLabel }}</span>
              <span class="manuscript-review-detail-shell__resource-name">{{ item.name }}</span>
            </div>
            <div class="manuscript-review-detail-shell__resource-sub">
              <span v-if="item.statusLabel" class="manuscript-review-detail-shell__resource-status">{{ item.statusLabel }}</span>
              <span v-if="item.note" class="manuscript-review-detail-shell__resource-note">{{ item.note }}</span>
            </div>
          </li>
        </ul>
      </div>
    </section>
  </section>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue';
import { useRoute, useRouter } from 'vue-router';

import { flowHisTaskList, getInfo } from '@/api/workflow/instance';
import request from '@/utils/request';
import { resolveManuscriptApproveAction, type ManuscriptReviewApproveActionResult } from '@/types/manuscript-review/detail';

import {
  buildDetailActionBar,
  buildReadableSummary,
  normalizeDetailViewModel,
  type ManuscriptReviewDetailReadableViewModel,
  type ManuscriptReviewHistoryItem,
  type ManuscriptReviewResourceItem
} from './detail.contract';

const route = useRoute();
const router = useRouter();

const reviewId = computed(() => {
  const value = route.query.reviewId;
  return typeof value === 'string' ? value : Array.isArray(value) ? value[0] : '';
});

const loading = ref(false);
const errorMessage = ref('');
const viewModel = ref<ManuscriptReviewDetailReadableViewModel | null>(null);

const actionBar = computed(() => buildDetailActionBar(viewModel.value?.actionRole ?? 'HISTORY_PARTICIPANT'));
const summaryItems = computed(() => (viewModel.value ? buildReadableSummary(viewModel.value.detail) : []));
const historyItems = computed<ManuscriptReviewHistoryItem[]>(() => viewModel.value?.historyItems ?? []);
const resourceItems = computed<ManuscriptReviewResourceItem[]>(() => viewModel.value?.resourceItems ?? []);

const fetchDetail = async () => {
  if (!reviewId.value) {
    errorMessage.value = '缺少必要的定位信息，请从台账进入详情页。';
    viewModel.value = null;
    return;
  }

  loading.value = true;
  errorMessage.value = '';

  try {
    const payload = await request.get('/manuscript-review/readable/detail', {
      params: { reviewId: reviewId.value }
    });
    viewModel.value = normalizeDetailViewModel(payload);
  } catch {
    viewModel.value = null;
    errorMessage.value = '详情数据加载失败，请稍后重试。';
  } finally {
    loading.value = false;
  }
};

const onAction = async (key: string) => {
  if (key === 'back') {
    router.back();
    return;
  }

  if (!reviewId.value) {
    errorMessage.value = '缺少必要的定位信息，请从台账进入详情页。';
    return;
  }

  if (key === 'approve') {
    try {
      const [instanceResponse, taskResponse] = await Promise.all([getInfo(reviewId.value), flowHisTaskList(reviewId.value)]);
      const outcome: ManuscriptReviewApproveActionResult = resolveManuscriptApproveAction(reviewId.value, instanceResponse?.data, taskResponse?.data);

      if (outcome.kind === 'jump') {
        const approvalQuery = new URLSearchParams({
          id: outcome.location.query.id,
          type: outcome.location.query.type,
          taskId: String(outcome.location.query.taskId)
        });
        await router.push(`${outcome.location.path}?${approvalQuery.toString()}`);
        return;
      }

      errorMessage.value = outcome.message;
    } catch {
      errorMessage.value = '去审批入口加载失败，请稍后重试。';
    }
    return;
  }

  if (key === 'edit') {
    errorMessage.value = '修改入口暂未开放，请稍后重试。';
    return;
  }

  if (key === 'resubmit') {
    errorMessage.value = '再次提交入口暂未开放，请稍后重试。';
  }
};

onMounted(() => {
  void fetchDetail();
});
</script>

<style scoped>
.manuscript-review-detail-shell {
  display: grid;
  gap: 20px;
  padding: 24px;
}

.manuscript-review-detail-shell__header {
  display: flex;
  flex-wrap: wrap;
  gap: 16px;
  justify-content: space-between;
  align-items: flex-start;
}

.manuscript-review-detail-shell__header h1 {
  margin: 0 0 8px;
}

.manuscript-review-detail-shell__actions {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
}

.manuscript-review-detail-shell__action {
  min-width: 88px;
  padding: 10px 16px;
  border: 1px solid var(--el-border-color);
  border-radius: 999px;
  background: var(--el-bg-color);
  color: var(--el-text-color-primary);
  font: inherit;
  cursor: pointer;
}

.manuscript-review-detail-shell__review-id-anchor {
  display: none;
}

.manuscript-review-detail-shell__alert {
  margin: 0;
  padding: 12px 16px;
  border: 1px solid var(--el-border-color);
  border-radius: 12px;
  background: var(--el-color-danger-light-9);
  color: var(--el-color-danger);
}

.manuscript-review-detail-shell__summary {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(240px, 1fr));
  gap: 16px;
  margin: 0;
}

.manuscript-review-detail-shell__summary-row {
  padding: 16px;
  border: 1px solid var(--el-border-color-light);
  border-radius: 16px;
  background: var(--el-fill-color-extra-light);
}

.manuscript-review-detail-shell__summary-row dt {
  margin-bottom: 8px;
  color: var(--el-text-color-secondary);
}

.manuscript-review-detail-shell__summary-row dd {
  margin: 0;
  color: var(--el-text-color-primary);
  word-break: break-word;
}

.manuscript-review-detail-shell__section {
  padding: 16px;
  border: 1px solid var(--el-border-color-light);
  border-radius: 16px;
  background: var(--el-bg-color);
}

.manuscript-review-detail-shell__section-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 12px;
}

.manuscript-review-detail-shell__section-title {
  margin: 0;
  font-size: 16px;
}

.manuscript-review-detail-shell__section-body {
  display: grid;
  gap: 8px;
}

.manuscript-review-detail-shell__section-hint {
  margin: 0;
  color: var(--el-text-color-secondary);
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
  gap: 4px;
  padding: 12px;
  border: 1px solid var(--el-border-color-lighter);
  border-radius: 12px;
}

.manuscript-review-detail-shell__timeline-main {
  display: flex;
  flex-wrap: wrap;
  gap: 8px 12px;
  align-items: center;
}

.manuscript-review-detail-shell__timeline-action {
  font-weight: 600;
}

.manuscript-review-detail-shell__timeline-operator {
  color: var(--el-text-color-secondary);
}

.manuscript-review-detail-shell__timeline-sub {
  display: flex;
  flex-wrap: wrap;
  gap: 8px 12px;
  color: var(--el-text-color-secondary);
}

.manuscript-review-detail-shell__timeline-remark {
  max-width: 720px;
  white-space: normal;
}

.manuscript-review-detail-shell__resource-list {
  display: grid;
  gap: 12px;
  margin: 0;
  padding: 0;
  list-style: none;
}

.manuscript-review-detail-shell__resource-item {
  display: grid;
  gap: 4px;
  padding: 12px;
  border: 1px solid var(--el-border-color-lighter);
  border-radius: 12px;
}

.manuscript-review-detail-shell__resource-main {
  display: flex;
  flex-wrap: wrap;
  gap: 8px 12px;
  align-items: center;
}

.manuscript-review-detail-shell__resource-type {
  display: inline-flex;
  padding: 2px 8px;
  border: 1px solid var(--el-border-color);
  border-radius: 999px;
  font-size: 12px;
  color: var(--el-text-color-secondary);
}

.manuscript-review-detail-shell__resource-name {
  font-weight: 600;
}

.manuscript-review-detail-shell__resource-sub {
  display: flex;
  flex-wrap: wrap;
  gap: 8px 12px;
  color: var(--el-text-color-secondary);
}

.manuscript-review-detail-shell__resource-note {
  max-width: 720px;
  white-space: normal;
}
</style>
