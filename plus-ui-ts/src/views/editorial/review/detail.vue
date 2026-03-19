<template>
  <div class="p-2">
    <el-card shadow="never">
      <approvalButton
        :buttonLoading="buttonLoading"
        :id="String(detail.id || '')"
        :status="detail.reviewStatus || detail.status"
        :pageType="approvalButtonPageType"
        :mode="false"
        @approvalVerifyOpen="approvalVerifyOpen"
        @handleApprovalRecord="handleApprovalRecord"
      >
        <el-button v-if="canCancelProcessApply" type="danger" link @click="handleCancelProcessApply">撤销</el-button>
        <el-button v-if="detail.canEdit && detail.shell.pageType !== 'approval'" type="primary" link @click="goToEdit">继续编辑</el-button>
      </approvalButton>
    </el-card>

    <el-row :gutter="16" class="mt-2">
      <el-col :span="16">
        <el-card shadow="never">
          <el-form v-loading="loading" :model="detail" label-width="100px">
            <ReviewFormFields :form="detail" readonly />
          </el-form>
        </el-card>

        <el-card shadow="never" class="mt-2">
          <template #header>
            <div class="flex items-center justify-between">
              <span>历史记录</span>
              <span class="text-xs text-[var(--el-text-color-secondary)]">detail/approval shell</span>
            </div>
          </template>

          <el-timeline v-if="detail.history.length > 0">
            <el-timeline-item v-for="history in detail.history" :key="history.id" :timestamp="parseTime(history.operateTime)" placement="top">
              <el-card shadow="hover">
                <div class="mb-2 text-sm font-medium">{{ history.operatorName }} 执行了 {{ history.operateType }}</div>
                <DiffViewer v-if="history.fieldDiff" :diffData="history.fieldDiff" />
              </el-card>
            </el-timeline-item>
          </el-timeline>
          <el-empty v-else description="暂无历史记录" />
        </el-card>
      </el-col>

      <el-col :span="8">
        <el-card shadow="never">
          <template #header>
            <span>审批壳上下文</span>
          </template>
          <el-descriptions :column="1" border>
            <el-descriptions-item label="当前入口">{{ shellTypeLabel }}</el-descriptions-item>
            <el-descriptions-item label="审校状态">
              <el-tag :type="statusMeta.type">{{ statusMeta.label }}</el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="流程类型">
              <el-tag :type="processTypeMeta.type">{{ processTypeMeta.label }}</el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="发起人">{{ detail.user.name || '-' }}</el-descriptions-item>
            <el-descriptions-item label="部门">{{ detail.dept.name || '-' }}</el-descriptions-item>
            <el-descriptions-item label="创建时间">{{ parseTime(detail.createTime) || '-' }}</el-descriptions-item>
            <el-descriptions-item label="taskId">{{ detail.shell.taskId || '-' }}</el-descriptions-item>
            <el-descriptions-item label="可编辑">{{ detail.canEdit ? '是' : '否' }}</el-descriptions-item>
          </el-descriptions>
        </el-card>
      </el-col>
    </el-row>

    <submitVerify ref="submitVerifyRef" :task-variables="taskVariables" @submit-callback="submitCallback" />
    <approvalRecord ref="approvalRecordRef" />
  </div>
</template>

<script setup lang="ts">
import { computed, getCurrentInstance, nextTick, onMounted, reactive, ref } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import type { ComponentInternalInstance } from 'vue';

import { getReviewDetail } from '@/api/editorial/review';
import { cancelProcessApply } from '@/api/workflow/instance';
import ApprovalButton from '@/components/Process/approvalButton.vue';
import ApprovalRecord from '@/components/Process/approvalRecord.vue';
import SubmitVerify from '@/components/Process/submitVerify.vue';
import { useUserStore } from '@/store/modules/user';

import ReviewFormFields from './components/ReviewFormFields.vue';
import DiffViewer from './components/DiffViewer.vue';
import {
  canCancelReviewProcess,
  createReviewFormLocation,
  getReviewProcessTypeMeta,
  getReviewStatusMeta,
  mapReviewDetailModel,
  resolveReviewApprovalButtonPageType,
  resolveReviewRouteType
} from './integration';
import { createEmptyReviewForm } from './model';

const { proxy } = getCurrentInstance() as ComponentInternalInstance;
const route = useRoute();
const router = useRouter();
const userStore = useUserStore();

const submitVerifyRef = ref<InstanceType<typeof SubmitVerify>>();
const approvalRecordRef = ref<InstanceType<typeof ApprovalRecord>>();

const loading = ref(true);
const buttonLoading = ref(false);
const detail = reactive({
  ...createEmptyReviewForm(),
  canEdit: false,
  user: {
    id: undefined,
    name: undefined
  },
  dept: {
    id: undefined,
    name: undefined
  },
  history: [],
  shell: {
    pageType: 'view',
    taskId: undefined,
    instanceId: undefined,
    canApprove: false,
    canEdit: false,
    fallback: false
  }
});

const taskVariables = ref<Record<string, any>>({});
const routeQuery = computed(() => route.query as Record<string, unknown>);
const routeType = computed(() => resolveReviewRouteType(routeQuery.value));
const statusMeta = computed(() => getReviewStatusMeta(detail.reviewStatus || detail.status));
const processTypeMeta = computed(() => getReviewProcessTypeMeta(detail.processType));
const shellTypeLabel = computed(() => {
  const baseLabel = detail.shell.pageType === 'approval' ? '审批壳' : '详情壳';
  return detail.shell.fallback ? `${baseLabel} / 旧页fallback` : baseLabel;
});
const canCancelProcessApply = computed(() =>
  canCancelReviewProcess({
    pageType: detail.shell.pageType,
    reviewStatus: detail.reviewStatus || detail.status,
    applicantUserId: detail.user.id,
    currentUserId: userStore.userId
  })
);
const approvalButtonPageType = computed(() =>
  resolveReviewApprovalButtonPageType({
    pageType: detail.shell.pageType,
    canApprove: detail.shell.canApprove
  })
);

const loadDetail = async () => {
  const id = routeQuery.value.id;
  if (typeof id !== 'string' || !id) {
    loading.value = false;
    return;
  }

  loading.value = true;
  try {
    const detailRes = await getReviewDetail(id);
    const mapped = mapReviewDetailModel(detailRes.data, routeType.value, routeQuery.value);
    Object.assign(detail, mapped);
  } finally {
    loading.value = false;
  }
};

const approvalVerifyOpen = () => {
  if (!detail.shell.taskId) {
    proxy?.$modal.msgWarning('当前缺少 taskId，无法打开审批弹窗');
    return;
  }
  submitVerifyRef.value?.openDialog(detail.shell.taskId);
};

const handleApprovalRecord = () => {
  if (!detail.id) {
    return;
  }
  approvalRecordRef.value?.init(detail.id);
};

const handleCancelProcessApply = async () => {
  if (!detail.id) {
    return;
  }

  await proxy?.$modal.confirm('是否确认撤销当前单据？');
  buttonLoading.value = true;
  try {
    await cancelProcessApply({
      businessId: detail.id,
      message: '申请人撤销流程！'
    });
    proxy?.$modal.msgSuccess('撤销成功');
    await loadDetail();
  } finally {
    buttonLoading.value = false;
  }
};

const submitCallback = async () => {
  await proxy?.$tab.closePage(proxy.$route);
  router.go(-1);
};

const goToEdit = () => {
  router.push(
    createReviewFormLocation({
      id: detail.id ? String(detail.id) : undefined,
      type: 'update',
      taskId: detail.shell.canApprove ? detail.shell.taskId : undefined,
      instanceId: detail.shell.canApprove ? detail.shell.instanceId : undefined
    })
  );
};

onMounted(() => {
  nextTick(() => {
    loadDetail();
  });
});
</script>
