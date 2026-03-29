<template>
  <div class="p-2 manuscript-review-task-page">
    <el-card shadow="hover">
      <template #header>
        <el-row :gutter="10" class="mb8">
          <right-toolbar v-model:show-search="showSearch" @query-table="fetchList" />
        </el-row>
      </template>

      <el-table v-loading="loading" border :data="rows" row-key="id">
        <el-table-column label="业务编码" align="center" prop="businessCode" min-width="160" :show-overflow-tooltip="true" />
        <el-table-column label="业务标题" align="left" prop="businessTitle" min-width="260" :show-overflow-tooltip="true" />
        <el-table-column label="任务名称" align="center" prop="nodeName" min-width="140" :show-overflow-tooltip="true" />
        <el-table-column label="申请人" align="center" prop="createByName" min-width="120" :show-overflow-tooltip="true" />
        <el-table-column label="流程状态" align="center" min-width="120">
          <template #default="scope">
            <dict-tag :options="wf_business_status" :value="scope.row.flowStatus" />
          </template>
        </el-table-column>
        <el-table-column label="创建时间" align="center" prop="createTime" min-width="180" />
        <el-table-column label="操作" align="center" width="180" fixed="right">
          <template #default="scope">
            <el-button link type="primary" @click="handleView(scope.row)">查看</el-button>
            <el-button link type="primary" @click="handleApprove(scope.row)">办理</el-button>
          </template>
        </el-table-column>

        <template #empty>
          <el-empty description="暂无三审三校待办数据" />
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

import { pageByTaskWait } from '@/api/workflow/task';
import type { FlowTaskVO, TaskQuery } from '@/api/workflow/task/types';

import {
  buildTaskApprovalRoute,
  buildTaskWaitingViewRoute,
  MANUSCRIPT_REVIEW_TASK_FLOW_CODE
} from './task-center';

const { proxy } = getCurrentInstance() as ComponentInternalInstance;
const { wf_business_status } = toRefs<any>(proxy?.useDict('wf_business_status'));
const router = useRouter();

const showSearch = ref(true);
const loading = ref(false);
const total = ref(0);
const rows = ref<FlowTaskVO[]>([]);

const queryParams = reactive<TaskQuery>({
  pageNum: 1,
  pageSize: 10,
  flowCode: MANUSCRIPT_REVIEW_TASK_FLOW_CODE
});

const fetchList = async () => {
  loading.value = true;
  try {
    const resp = await pageByTaskWait({ ...queryParams });
    rows.value = resp.rows ?? [];
    total.value = resp.total ?? rows.value.length;
  } finally {
    loading.value = false;
  }
};

const handleView = (row: FlowTaskVO) => {
  void router.push(buildTaskWaitingViewRoute(String(row.businessId), row.id));
};

const handleApprove = (row: FlowTaskVO) => {
  void router.push(buildTaskApprovalRoute(String(row.businessId), row.id));
};

onMounted(() => {
  void fetchList();
});
</script>
