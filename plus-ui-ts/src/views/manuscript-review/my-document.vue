<template>
  <div class="p-2 manuscript-review-task-page">
    <el-card shadow="hover">
      <template #header>
        <el-row :gutter="10" class="mb8">
          <el-col :span="1.5">
            <el-button type="primary" plain icon="Plus" @click="handleCreate">新增</el-button>
          </el-col>
          <right-toolbar v-model:show-search="showSearch" @query-table="fetchList" />
        </el-row>
      </template>

      <el-table v-loading="loading" border :data="rows" row-key="id">
        <el-table-column label="业务编码" align="center" prop="businessCode" min-width="160" :show-overflow-tooltip="true" />
        <el-table-column label="业务标题" align="left" prop="businessTitle" min-width="260" :show-overflow-tooltip="true" />
        <el-table-column label="流程状态" align="center" min-width="120">
          <template #default="scope">
            <dict-tag :options="wf_business_status" :value="scope.row.flowStatus" />
          </template>
        </el-table-column>
        <el-table-column label="当前任务" align="center" min-width="140" :show-overflow-tooltip="true">
          <template #default="scope">
            {{ scope.row.flowTaskList?.[0]?.nodeName || '--' }}
          </template>
        </el-table-column>
        <el-table-column label="启动时间" align="center" prop="createTime" min-width="180" />
        <el-table-column label="操作" align="center" width="220" fixed="right">
          <template #default="scope">
            <el-button link type="primary" @click="handleView(scope.row)">查看</el-button>
            <el-button v-if="isManuscriptReviewTaskEditable(scope.row.flowStatus)" link type="primary" @click="handleEdit(scope.row)">编辑</el-button>
            <el-button v-if="isManuscriptReviewTaskCancellable(scope.row.flowStatus)" link type="danger" @click="handleCancel(scope.row)">撤销</el-button>
          </template>
        </el-table-column>

        <template #empty>
          <el-empty description="暂无三审三校我发起数据" />
        </template>
      </el-table>

      <pagination
        v-show="total > 0"
        v-model:page="queryParams.pageNum"
        v-model:limit="queryParams.pageSize"
        :total="total"
        @pagination="fetchList"
      />
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { getCurrentInstance, onMounted, reactive, ref, toRefs } from 'vue';
import type { ComponentInternalInstance } from 'vue';
import { useRouter } from 'vue-router';

import { cancelManuscriptReviewProcess } from '@/api/manuscript-review';
import { pageByCurrent } from '@/api/workflow/instance';
import type { FlowInstanceQuery, FlowInstanceVO } from '@/api/workflow/instance/types';

import {
  buildMyDocumentEditRoute,
  buildMyDocumentViewRoute,
  isManuscriptReviewTaskCancellable,
  isManuscriptReviewTaskEditable,
  MANUSCRIPT_REVIEW_TASK_FLOW_CODE
} from './task-center';

const { proxy } = getCurrentInstance() as ComponentInternalInstance;
const { wf_business_status } = toRefs<any>(proxy?.useDict('wf_business_status'));
const router = useRouter();

const showSearch = ref(true);
const loading = ref(false);
const total = ref(0);
const rows = ref<FlowInstanceVO[]>([]);

const queryParams = reactive<FlowInstanceQuery>({
  pageNum: 1,
  pageSize: 10,
  flowCode: MANUSCRIPT_REVIEW_TASK_FLOW_CODE
});

const fetchList = async () => {
  loading.value = true;
  try {
    const resp = await pageByCurrent({ ...queryParams });
    rows.value = resp.rows ?? [];
    total.value = resp.total ?? rows.value.length;
  } finally {
    loading.value = false;
  }
};

const handleCreate = () => {
  void router.push({ path: '/manuscript/review/form', query: { mode: 'create' } });
};

const handleView = (row: FlowInstanceVO) => {
  void router.push(buildMyDocumentViewRoute(String(row.businessId), row.id));
};

const handleEdit = (row: FlowInstanceVO) => {
  void router.push(buildMyDocumentEditRoute(String(row.businessId)));
};

const handleCancel = async (row: FlowInstanceVO) => {
  await proxy?.$modal.confirm('是否确认撤销当前单据？');
  await cancelManuscriptReviewProcess({
    id: row.businessId,
    reason: '申请人撤销流程！'
  });
  proxy?.$modal.msgSuccess('撤销成功');
  await fetchList();
};

onMounted(() => {
  void fetchList();
});
</script>
