<template>
  <section class="manuscript-review-detail-shell">
    <header class="manuscript-review-detail-shell__header">
      <div>
        <h1>审校详情</h1>
        <p>当前壳层先锁定详情动作分流与业务可读展示，后续再接入真实接口与历史、资源区域。</p>
      </div>
      <nav class="manuscript-review-detail-shell__actions" aria-label="详情动作栏">
        <button
          v-for="action in actionBar"
          :key="action.key"
          type="button"
          class="manuscript-review-detail-shell__action"
        >
          {{ action.label }}
        </button>
      </nav>
    </header>

    <dl class="manuscript-review-detail-shell__summary">
      <div
        v-for="item in summaryItems"
        :key="item.label"
        class="manuscript-review-detail-shell__summary-row"
      >
        <dt>{{ item.label }}</dt>
        <dd>{{ item.value }}</dd>
      </div>
    </dl>
  </section>
</template>

<script setup lang="ts">
import { computed } from 'vue';

import {
  buildDetailActionBar,
  buildReadableSummary,
  manuscriptReviewDetailShellDemo
} from './detail.contract';

const actionBar = computed(() => buildDetailActionBar('CURRENT_APPROVER'));
const summaryItems = computed(() => buildReadableSummary(manuscriptReviewDetailShellDemo));
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

.manuscript-review-detail-shell__header p {
  margin: 0;
  max-width: 720px;
  color: var(--el-text-color-secondary);
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
</style>
