<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch" label-width="68px">
      <el-form-item label="标题" prop="title">
        <el-input v-model="queryParams.title" placeholder="请输入标题" clearable style="width: 240px" @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item label="状态" prop="status">
        <el-select v-model="queryParams.status" placeholder="请选择状态" clearable style="width: 240px">
          <el-option label="草稿" value="draft" />
          <el-option label="待审批" value="waiting" />
          <el-option label="已通过" value="finish" />
          <el-option label="已驳回" value="back" />
          <el-option label="已撤销" value="cancel" />
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
        <el-button type="danger" plain icon="Delete" :disabled="multiple" @click="handleDelete()" v-hasPermi="['editorial:review:remove']"
          >删除</el-button
        >
      </el-col>
      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table v-loading="loading" :data="reviewList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center" />

      <el-table-column label="标题" align="center" prop="title" :show-overflow-tooltip="true" />
      <el-table-column label="流程类型" align="center" prop="processType">
        <template #default="scope">
          <el-tag v-if="scope.row.processType === 'AUDIT'" type="primary">审核流程</el-tag>
          <el-tag v-else-if="scope.row.processType === 'PROOFREAD'" type="success">校验流程</el-tag>
          <span v-else>-</span>
        </template>
      </el-table-column>
      <el-table-column label="部门" align="center" prop="deptName" />
      <el-table-column label="发起人" align="center" prop="userName" />
      <el-table-column label="状态" align="center" prop="status">
        <template #default="scope">
          <el-tag v-if="scope.row.status === 'finish'" type="success">已通过</el-tag>
          <el-tag v-else-if="scope.row.status === 'back'" type="danger">已驳回</el-tag>
          <el-tag v-else-if="scope.row.status === 'waiting'" type="warning">待审批</el-tag>
          <el-tag v-else-if="scope.row.status === 'cancel'" type="info">已撤销</el-tag>
          <el-tag v-else-if="scope.row.status === 'termination'" type="info">已终止</el-tag>
          <el-tag v-else-if="scope.row.status === 'invalid'" type="info">已作废</el-tag>
          <el-tag v-else type="info">草稿</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="创建时间" align="center" prop="createTime" width="180">
        <template #default="scope">
          <span>{{ parseTime(scope.row.createTime) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="操作" align="center" class-name="small-padding fixed-width">
        <template #default="scope">
          <el-button
            link
            type="primary"
            icon="Edit"
            @click="handleUpdate(scope.row)"
            v-if="scope.row.status === 'draft' || scope.row.status === 'back'"
            v-hasPermi="['editorial:review:edit']"
            >修改</el-button
          >
          <el-button link type="primary" icon="View" @click="handleView(scope.row)">详情</el-button>
          <el-button
            link
            type="primary"
            icon="Delete"
            @click="handleDelete(scope.row)"
            v-if="scope.row.status === 'draft'"
            v-hasPermi="['editorial:review:remove']"
            >删除</el-button
          >
        </template>
      </el-table-column>
    </el-table>

    <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, toRefs, onMounted, onActivated } from 'vue';
import { useRouter } from 'vue-router';
import { ElMessage, ElMessageBox } from 'element-plus';
import { listReview, delReview, type EditorialReviewQuery, type EditorialReviewVo } from '@/api/editorial/review';

const router = useRouter();
const loading = ref(true);
const showSearch = ref(true);
const ids = ref<Array<number | string>>([]);
const single = ref(true);
const multiple = ref(true);
const total = ref(0);
const reviewList = ref<EditorialReviewVo[]>([]);

const queryParams = reactive<EditorialReviewQuery>({
  pageNum: 1,
  pageSize: 10,
  title: undefined,
  status: undefined
});

const getList = async () => {
  loading.value = true;
  const res = await listReview(queryParams);
  reviewList.value = res.rows;
  total.value = res.total;
  loading.value = false;
};

const handleQuery = () => {
  queryParams.pageNum = 1;
  getList();
};

const resetQuery = () => {
  queryParams.title = undefined;
  queryParams.status = undefined;
  handleQuery();
};

const handleSelectionChange = (selection: EditorialReviewVo[]) => {
  ids.value = selection.map((item) => item.id);
  single.value = selection.length !== 1;
  multiple.value = !selection.length;
};

const handleAdd = () => {
  router.push({ path: '/editorial/review/reviewEdit', query: { type: 'add' } });
};

const handleUpdate = (row: EditorialReviewVo) => {
  const id = row.id || ids.value[0];
  router.push({ path: '/editorial/review/reviewEdit', query: { id, type: 'update' } });
};

const handleView = (row: EditorialReviewVo) => {
  router.push({ path: '/editorial/review/reviewEdit', query: { id: row.id, type: 'view' } });
};

const handleDelete = async (row?: EditorialReviewVo) => {
  const deleteIds = row?.id || ids.value;
  ElMessageBox.confirm('是否确认删除选中的数据项？', '警告', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    await delReview(deleteIds);
    getList();
    ElMessage.success('删除成功');
  });
};

onMounted(() => {
  getList();
});

onActivated(() => {
  getList();
});
</script>
