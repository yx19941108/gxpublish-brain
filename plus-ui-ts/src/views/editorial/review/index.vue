<template>
  <div class="app-container">
    <el-form ref="queryRef" :model="queryParams" :inline="true" label-width="68px" v-show="showSearch">
      <el-form-item label="标题" prop="title">
        <el-input v-model="queryParams.title" placeholder="请输入标题" clearable style="width: 240px" @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item label="审校状态" prop="reviewStatus">
        <el-select v-model="queryParams.reviewStatus" placeholder="请选择审校状态" clearable style="width: 240px">
          <el-option label="草稿" value="DRAFT" />
          <el-option label="待一审" value="WAITING_FIRST" />
          <el-option label="待二审" value="WAITING_SECOND" />
          <el-option label="待终审" value="WAITING_FINAL" />
          <el-option label="已通过" value="APPROVED" />
          <el-option label="已退回" value="BACK" />
          <el-option label="已撤销" value="CANCELED" />
          <el-option label="已终止" value="TERMINATED" />
        </el-select>
      </el-form-item>
      <el-form-item label="流程类型" prop="processType">
        <el-select v-model="queryParams.processType" placeholder="请选择流程类型" clearable style="width: 240px">
          <el-option label="审核流程" value="AUDIT" />
          <el-option label="校验流程" value="PROOFREAD" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
        <el-button icon="Refresh" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button type="primary" plain icon="Plus" @click="handleAdd" v-hasPermi="['editorial:review:add']">新增</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="danger" plain icon="Delete" :disabled="multiple" @click="handleDelete()" v-hasPermi="['editorial:review:remove']">
          删除
        </el-button>
      </el-col>
      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList" />
    </el-row>

    <el-table v-loading="loading" :data="reviewList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="标题" align="center" prop="title" :show-overflow-tooltip="true" min-width="260" />
      <el-table-column label="流程类型" align="center" min-width="120">
        <template #default="{ row }">
          <el-tag :type="getReviewProcessTypeMeta(row.processType).type">
            {{ getReviewProcessTypeMeta(row.processType).label }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="发起人" align="center" min-width="120">
        <template #default="{ row }">{{ row.user.name || '-' }}</template>
      </el-table-column>
      <el-table-column label="部门" align="center" min-width="140">
        <template #default="{ row }">{{ row.dept.name || '-' }}</template>
      </el-table-column>
      <el-table-column label="审校状态" align="center" min-width="120">
        <template #default="{ row }">
          <el-tag :type="getReviewStatusMeta(row.reviewStatus).type">
            {{ getReviewStatusMeta(row.reviewStatus).label }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="可编辑" align="center" width="90">
        <template #default="{ row }">
          <el-tag :type="row.canEdit ? 'success' : 'info'">{{ row.canEdit ? '是' : '否' }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="创建时间" align="center" prop="createTime" width="180">
        <template #default="{ row }">
          <span>{{ parseTime(row.createTime) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="操作" align="center" class-name="small-padding fixed-width" width="220">
        <template #default="{ row }">
          <el-button link type="primary" icon="Edit" @click="handleUpdate(row)" v-if="row.canEdit" v-hasPermi="['editorial:review:edit']"
            >修改</el-button
          >
          <el-button link type="primary" icon="View" @click="handleView(row)">详情</el-button>
          <el-button link type="primary" icon="Delete" @click="handleDelete(row)" v-if="row.canEdit" v-hasPermi="['editorial:review:remove']">
            删除
          </el-button>
        </template>
      </el-table-column>
    </el-table>

    <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />
  </div>
</template>

<script setup lang="ts">
import { onActivated, onMounted, reactive, ref } from 'vue';
import { useRouter } from 'vue-router';
import { ElMessage, ElMessageBox } from 'element-plus';

import { deleteReview, listReviewPage, type ReviewPageQuery } from '@/api/editorial/review';

import {
  createReviewDetailLocation,
  createReviewFormLocation,
  getReviewProcessTypeMeta,
  getReviewStatusMeta,
  mapReviewPageItem
} from './integration';
import type { ReviewPageItem } from './model';

const router = useRouter();

const loading = ref(true);
const showSearch = ref(true);
const ids = ref<Array<number | string>>([]);
const multiple = ref(true);
const total = ref(0);
const reviewList = ref<ReviewPageItem[]>([]);

const queryParams = reactive<ReviewPageQuery>({
  pageNum: 1,
  pageSize: 10,
  title: undefined,
  reviewStatus: undefined,
  processType: undefined
});

const getList = async () => {
  loading.value = true;
  try {
    const response = await listReviewPage(queryParams);
    reviewList.value = (response.rows || []).map((item) => mapReviewPageItem(item));
    total.value = response.total || 0;
  } finally {
    loading.value = false;
  }
};

const handleQuery = () => {
  queryParams.pageNum = 1;
  getList();
};

const resetQuery = () => {
  queryParams.title = undefined;
  queryParams.reviewStatus = undefined;
  queryParams.processType = undefined;
  handleQuery();
};

const handleSelectionChange = (selection: ReviewPageItem[]) => {
  ids.value = selection.map((item) => item.id);
  multiple.value = !selection.length;
};

const handleAdd = () => {
  router.push(createReviewFormLocation({ type: 'add' }));
};

const handleUpdate = (row: ReviewPageItem) => {
  router.push(
    createReviewFormLocation({
      id: String(row.id),
      type: 'update'
    })
  );
};

const handleView = (row: ReviewPageItem) => {
  router.push(
    createReviewDetailLocation({
      id: String(row.id),
      type: 'view'
    })
  );
};

const handleDelete = async (row?: ReviewPageItem) => {
  const deleteIds = row?.id || ids.value;
  await ElMessageBox.confirm('是否确认删除选中的数据项？', '警告', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  });
  await deleteReview(deleteIds);
  ElMessage.success('删除成功');
  await getList();
};

onMounted(() => {
  getList();
});

onActivated(() => {
  getList();
});
</script>
