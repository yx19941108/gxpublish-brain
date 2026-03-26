<template>
  <div class="p-2 manuscript-review-ledger-page">
    <transition :enter-active-class="proxy?.animate.searchAnimate.enter" :leave-active-class="proxy?.animate.searchAnimate.leave">
      <div v-show="showSearch" class="mb-[10px]">
        <el-card shadow="hover">
          <el-form ref="queryFormRef" :model="queryParams" :inline="true">
            <el-form-item label="关键词" prop="keyword">
              <el-input
                v-model="queryParams.keyword"
                placeholder="稿件号 / 标题 / 发起人"
                clearable
                style="width: 220px"
                @keyup.enter="handleQuery"
              />
            </el-form-item>
            <el-form-item label="流程类型" prop="processType">
              <el-select v-model="queryParams.processType" placeholder="请选择流程类型" clearable style="width: 180px">
                <el-option v-for="item in processTypeOptions" :key="item.value" :label="item.label" :value="item.value" />
              </el-select>
            </el-form-item>
            <el-form-item label="媒体栏目" prop="mediaChannel">
              <el-select v-model="queryParams.mediaChannel" placeholder="请选择媒体栏目" clearable style="width: 200px">
                <el-option v-for="item in mediaChannelOptions" :key="item.value" :label="item.label" :value="item.value" />
              </el-select>
            </el-form-item>
            <el-form-item label="业务状态" prop="businessStatus">
              <el-select v-model="queryParams.businessStatus" placeholder="请选择业务状态" clearable style="width: 160px">
                <el-option v-for="item in businessStatusOptions" :key="item.value" :label="item.label" :value="item.value" />
              </el-select>
            </el-form-item>
            <el-form-item label="当前节点" prop="currentNodeCode">
              <el-select v-model="queryParams.currentNodeCode" placeholder="请选择当前节点" clearable style="width: 180px">
                <el-option v-for="item in currentNodeOptions" :key="item.value" :label="item.label" :value="item.value" />
              </el-select>
            </el-form-item>
            <el-form-item>
              <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
              <el-button icon="Refresh" @click="resetQuery">重置</el-button>
            </el-form-item>
          </el-form>
        </el-card>
      </div>
    </transition>

    <el-alert v-if="errorMessage" :title="errorMessage" type="error" show-icon class="mb-[10px]" />

    <el-card shadow="hover">
      <template #header>
        <el-row :gutter="10" class="mb8">
          <el-col :span="1.5">
            <el-button type="primary" plain icon="Plus" @click="handleCreate">新增</el-button>
          </el-col>
          <right-toolbar v-model:show-search="showSearch" @query-table="fetchLedger" />
        </el-row>
      </template>

      <el-table v-loading="loading" border :data="rows" row-key="id">
        <el-table-column label="系统稿件号" align="center" prop="manuscriptCode" min-width="160" :show-overflow-tooltip="true" />
        <el-table-column label="标题" align="left" prop="title" min-width="260" :show-overflow-tooltip="true" />
        <el-table-column label="流程类型" align="center" prop="processTypeLabel" min-width="140" :show-overflow-tooltip="true" />
        <el-table-column label="媒体栏目" align="center" prop="mediaChannel" min-width="180" :show-overflow-tooltip="true" />
        <el-table-column label="业务状态" align="center" min-width="120">
          <template #default="scope">
            <el-tag :type="resolveStatusTagType(scope.row.businessStatusLabel)" effect="light">
              {{ scope.row.businessStatusLabel }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="当前节点" align="center" prop="currentNodeLabel" min-width="150" :show-overflow-tooltip="true" />
        <el-table-column label="发起人" align="center" prop="initiatorName" min-width="120" :show-overflow-tooltip="true" />
        <el-table-column label="最近更新时间" align="center" prop="updateTime" min-width="180" />
        <el-table-column label="操作" align="center" width="100" fixed="right" class-name="small-padding fixed-width">
          <template #default="scope">
            <el-tooltip content="详情" placement="top">
              <el-button link type="primary" @click="goDetail(String(scope.row.id))">详情</el-button>
            </el-tooltip>
          </template>
        </el-table-column>

        <template #empty>
          <el-empty description="暂无审校台账数据" />
        </template>
      </el-table>

      <pagination
        v-show="total > 0"
        v-model:page="queryParams.pageNum"
        v-model:limit="queryParams.pageSize"
        :total="total"
        @pagination="fetchLedger"
      />
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { getCurrentInstance, onMounted, reactive, ref } from 'vue';
import type { ComponentInternalInstance } from 'vue';
import { useRouter } from 'vue-router';

import { listManuscriptReview } from '@/api/manuscript-review';

import { normalizeLedgerRows, type ManuscriptReviewLedgerRow } from './detail.contract';

interface LedgerQueryParams {
  pageNum: number;
  pageSize: number;
  keyword?: string;
  processType?: string;
  mediaChannel?: string;
  businessStatus?: string;
  currentNodeCode?: string;
}

interface OptionItem {
  label: string;
  value: string;
}

const { proxy } = getCurrentInstance() as ComponentInternalInstance;
const router = useRouter();

const loading = ref(false);
const total = ref(0);
const rows = ref<ManuscriptReviewLedgerRow[]>([]);
const errorMessage = ref('');
const showSearch = ref(true);

const queryFormRef = ref<ElFormInstance>();
const queryParams = reactive<LedgerQueryParams>({
  pageNum: 1,
  pageSize: 10,
  keyword: undefined,
  processType: undefined,
  mediaChannel: undefined,
  businessStatus: undefined,
  currentNodeCode: undefined
});

const processTypeOptions: OptionItem[] = [
  { label: '稿件审校', value: 'AUDIT' },
  { label: '校对审校', value: 'PROOFREAD' },
  { label: '视频审校', value: 'VIDEO' }
];

const mediaChannelOptions: OptionItem[] = [
  { label: '客户端头条', value: 'CLIENT_HEADLINE' },
  { label: '视频中心', value: 'VIDEO_CENTER' },
  { label: '融媒专题', value: 'INTEGRATED_TOPIC' }
];

const businessStatusOptions: OptionItem[] = [
  { label: '审批中', value: 'IN_PROGRESS' },
  { label: '已退回', value: 'BACK' },
  { label: '已完成', value: 'DONE' },
  { label: '已撤销', value: 'CANCELLED' }
];

const currentNodeOptions: OptionItem[] = [
  { label: '一级审批', value: 'LEVEL_1' },
  { label: '二级审批', value: 'LEVEL_2' },
  { label: '三级审批', value: 'LEVEL_3' },
  { label: '待发起人处理', value: 'RETURN_TO_INITIATOR' }
];

const resolveLedgerTotal = (payload: unknown): number => {
  if (typeof payload !== 'object' || payload === null) {
    return 0;
  }

  const source = payload as Record<string, unknown>;
  const directTotal = source.total;
  if (typeof directTotal === 'number') {
    return directTotal;
  }

  const data = source.data;
  if (typeof data === 'object' && data !== null && typeof (data as Record<string, unknown>).total === 'number') {
    return (data as Record<string, number>).total;
  }

  return rows.value.length;
};

const resolveStatusTagType = (statusLabel: string) => {
  if (statusLabel.includes('完成')) {
    return 'success';
  }
  if (statusLabel.includes('退回')) {
    return 'danger';
  }
  if (statusLabel.includes('审批')) {
    return 'warning';
  }
  return 'info';
};

const goDetail = (reviewId: string) => {
  router.push({ path: '/manuscript/review/detail', query: { reviewId } });
};

const handleCreate = () => {
  router.push({ path: '/manuscript/review/form', query: { mode: 'create' } });
};

const fetchLedger = async () => {
  loading.value = true;
  errorMessage.value = '';

  try {
    const payload = await listManuscriptReview({ ...queryParams });
    rows.value = normalizeLedgerRows(payload);
    total.value = resolveLedgerTotal(payload);
  } catch {
    rows.value = [];
    total.value = 0;
    errorMessage.value = '审校台账数据加载失败，请稍后重试。';
  } finally {
    loading.value = false;
  }
};

const handleQuery = () => {
  queryParams.pageNum = 1;
  void fetchLedger();
};

const resetQuery = () => {
  queryFormRef.value?.resetFields();
  queryParams.pageNum = 1;
  queryParams.pageSize = 10;
  void fetchLedger();
};

onMounted(() => {
  void fetchLedger();
});
</script>

<style scoped lang="scss">
.manuscript-review-ledger-page {
  :deep(.el-card__header) {
    padding-bottom: 18px;
  }

  :deep(.el-table .cell) {
    word-break: break-word;
  }
}
</style>
