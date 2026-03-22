<template>
  <section class="manuscript-review-ledger-shell">
    <header class="manuscript-review-ledger-shell__header">
      <h1>审校台账</h1>
    </header>

    <p v-if="errorMessage" class="manuscript-review-ledger-shell__alert" role="status">
      {{ errorMessage }}
    </p>

    <div class="manuscript-review-ledger-shell__table-wrap" role="region" aria-label="审校台账列表">
      <table class="manuscript-review-ledger-shell__table">
        <thead>
          <tr>
            <th scope="col">系统稿件号</th>
            <th scope="col">标题</th>
            <th scope="col">流程类型</th>
            <th scope="col">所属媒体/栏目</th>
            <th scope="col" data-testid="ledger-col-flowStatus">流程状态</th>
            <th scope="col" data-testid="ledger-col-currentNode">当前节点</th>
            <th scope="col">发起人</th>
            <th scope="col">最近更新时间</th>
            <th scope="col">操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-if="loading" class="manuscript-review-ledger-shell__empty">
            <td colspan="9">正在加载台账数据…</td>
          </tr>
          <tr v-else-if="rows.length === 0" class="manuscript-review-ledger-shell__empty">
            <td colspan="9">暂无数据（WP10 readable 合同待对接）。</td>
          </tr>
          <template v-else>
            <tr v-for="row in rows" :key="row.reviewId">
              <td>{{ row.manuscriptCode }}</td>
              <td class="manuscript-review-ledger-shell__title">{{ row.title }}</td>
              <td>{{ row.processTypeLabel }}</td>
              <td>{{ row.mediaChannelLabel }}</td>
              <td>{{ row.flowStatusLabel }}</td>
              <td>{{ row.currentNodeLabel }}</td>
              <td>{{ row.initiatorName }}</td>
              <td>{{ row.latestUpdatedAt }}</td>
              <td>
                <button
                  type="button"
                  class="manuscript-review-ledger-shell__link"
                  @click="goDetail(row.reviewId)"
                >
                  详情
                </button>
              </td>
            </tr>
          </template>
        </tbody>
      </table>
    </div>
  </section>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue';
import { useRouter } from 'vue-router';

import request from '@/utils/request';

import { normalizeLedgerRows, type ManuscriptReviewLedgerRow } from './detail.contract';

const router = useRouter();

const loading = ref(false);
const rows = ref<ManuscriptReviewLedgerRow[]>([]);
const errorMessage = ref('');

const goDetail = (reviewId: string) => {
  router.push({ path: '/manuscript/review/detail', query: { reviewId } });
};

const fetchLedger = async () => {
  loading.value = true;
  errorMessage.value = '';

  try {
    const payload = await request.get('/manuscript-review/readable/ledger');
    rows.value = normalizeLedgerRows(payload);
  } catch {
    rows.value = [];
    errorMessage.value = '台账数据加载失败，请稍后重试。';
  } finally {
    loading.value = false;
  }
};

onMounted(() => {
  void fetchLedger();
});
</script>

<style scoped>
.manuscript-review-ledger-shell {
  display: grid;
  gap: 16px;
  padding: 24px;
}

.manuscript-review-ledger-shell__header h1 {
  margin: 0;
}

.manuscript-review-ledger-shell__alert {
  margin: 0;
  padding: 12px 16px;
  border: 1px solid var(--el-border-color);
  border-radius: 12px;
  background: var(--el-color-danger-light-9);
  color: var(--el-color-danger);
}

.manuscript-review-ledger-shell__table-wrap {
  overflow-x: auto;
  border: 1px solid var(--el-border-color-light);
  border-radius: 16px;
  background: var(--el-bg-color);
}

.manuscript-review-ledger-shell__table {
  width: 100%;
  border-collapse: collapse;
  min-width: 960px;
}

.manuscript-review-ledger-shell__table th,
.manuscript-review-ledger-shell__table td {
  padding: 12px 16px;
  border-bottom: 1px solid var(--el-border-color-lighter);
  text-align: left;
  white-space: nowrap;
}

.manuscript-review-ledger-shell__table th {
  color: var(--el-text-color-secondary);
  font-weight: 600;
}

.manuscript-review-ledger-shell__title {
  max-width: 320px;
  white-space: normal;
  word-break: break-word;
}

.manuscript-review-ledger-shell__empty td {
  color: var(--el-text-color-secondary);
  text-align: center;
  white-space: normal;
}

.manuscript-review-ledger-shell__link {
  padding: 6px 10px;
  border: 1px solid var(--el-border-color);
  border-radius: 999px;
  background: var(--el-bg-color);
  color: var(--el-color-primary);
  font: inherit;
  cursor: pointer;
}
</style>
