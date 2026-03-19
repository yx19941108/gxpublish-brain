<template>
  <div class="p-2">
    <el-card shadow="never">
      <approvalButton
        :buttonLoading="buttonLoading"
        :id="String(form.id || '')"
        :status="form.reviewStatus || form.status"
        :pageType="buttonPageType"
        :submit-label="submitLabel"
        :mode="true"
        :show-draft-button="false"
        :show-submit-button="canFormSubmit"
        @submitForm="submitForm"
        @handleApprovalRecord="handleApprovalRecord"
      />
    </el-card>

    <el-card shadow="never" class="mt-2">
      <el-form ref="reviewFormRef" v-loading="loading" :model="form" :rules="rules" label-width="100px">
        <ReviewFormFields :form="form" :dept-options="deptOptions" />
      </el-form>
    </el-card>

    <submitVerify ref="submitVerifyRef" :task-variables="taskVariables" @submit-callback="submitCallback" />
    <approvalRecord ref="approvalRecordRef" />
  </div>
</template>

<script setup lang="ts">
import { computed, getCurrentInstance, nextTick, onMounted, reactive, ref } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { ElMessage } from 'element-plus';
import type { ComponentInternalInstance } from 'vue';
import type { FormInstance, FormRules } from 'element-plus';

import { listDept } from '@/api/system/dept';
import type { DeptVO } from '@/api/system/dept/types';
import { createReviewDraft, getReviewDetail, resubmitReviewAction, submitReviewAction, updateReviewDraft } from '@/api/editorial/review';
import ApprovalButton from '@/components/Process/approvalButton.vue';
import ApprovalRecord from '@/components/Process/approvalRecord.vue';
import SubmitVerify from '@/components/Process/submitVerify.vue';
import { useUserStore } from '@/store/modules/user';

import ReviewFormFields from './components/ReviewFormFields.vue';
import { getReviewSubmitLabel, mapReviewFormModel, resolveReviewFormPageType, resolveReviewRouteType, toReviewActionPayload } from './integration';
import { createEmptyReviewForm } from './model';

const { proxy } = getCurrentInstance() as ComponentInternalInstance;
const route = useRoute();
const router = useRouter();
const userStore = useUserStore();

const reviewFormRef = ref<FormInstance>();
const approvalRecordRef = ref<InstanceType<typeof ApprovalRecord>>();
const submitVerifyRef = ref<InstanceType<typeof SubmitVerify>>();

const loading = ref(true);
const buttonLoading = ref(false);
const deptOptions = ref<DeptVO[]>([]);
const taskVariables = ref<Record<string, any>>({});
const form = reactive(createEmptyReviewForm(userStore.deptId));

const routeQuery = computed(() => route.query as Record<string, unknown>);
const routeType = computed(() => resolveReviewRouteType(routeQuery.value));
const buttonPageType = computed(() =>
  resolveReviewFormPageType({
    routeType: routeType.value,
    reviewStatus: form.reviewStatus,
    taskId: form.taskId,
    canApprove: form.canApprove
  })
);
const isApprovalEdit = computed(() => buttonPageType.value === 'approval');
const shouldUseResubmit = computed(() => Boolean(form.id) && form.reviewStatus === 'BACK');
const canFormSubmit = computed(() => buttonPageType.value === 'add' || buttonPageType.value === 'update' || buttonPageType.value === 'approval');
const submitLabel = computed(() => (isApprovalEdit.value ? '保存并审批' : getReviewSubmitLabel(form.reviewStatus)));

const rules = reactive<FormRules>({
  title: [{ required: true, message: '请输入标题', trigger: 'blur' }],
  content: [{ required: true, message: '请输入内容', trigger: 'blur' }],
  deptId: [{ required: true, message: '请选择部门', trigger: 'change' }],
  processType: [{ required: true, message: '请选择流程类型', trigger: 'change' }]
});

const close = async () => {
  await proxy?.$tab.closePage(proxy.$route);
  router.go(-1);
};

const getDeptTree = async () => {
  const res = await listDept({} as any);
  const data = proxy?.handleTree<DeptVO>(res.data, 'deptId');
  deptOptions.value = data || [];
};

const loadDetail = async (id: number | string) => {
  const detailRes = await getReviewDetail(id);
  Object.assign(form, mapReviewFormModel(detailRes.data, routeQuery.value));
};

const initForm = async () => {
  loading.value = true;
  await getDeptTree();

  if (routeType.value === 'add') {
    Object.assign(form, createEmptyReviewForm(userStore.deptId));
    loading.value = false;
    return;
  }

  const id = routeQuery.value.id;
  if (typeof id === 'string' && id) {
    await loadDetail(id);
  }
  loading.value = false;
};

const validateAttachmentOrLink = () => {
  const hasAttachment = form.attachmentList.some((attachment) => attachment.ossId);
  const hasLink = form.linkList.some((link) => link.url?.trim());
  if (!hasAttachment && !hasLink) {
    ElMessage.warning('提交审批时，附件和关联链接至少需要填写一项');
    return false;
  }
  return true;
};

const openApprovalDialogAfterSave = () => {
  if (!form.taskId) {
    proxy?.$modal.msgWarning('当前缺少 taskId，无法继续审批');
    return;
  }
  submitVerifyRef.value?.openDialog(form.taskId);
};

const doSubmit = async (action: 'draft' | 'submit') => {
  const payload = toReviewActionPayload(form);

  if (action === 'draft') {
    const response = form.id ? await updateReviewDraft(payload) : await createReviewDraft(payload);
    form.id = response.data?.id ?? form.id;
    ElMessage.success('暂存成功');
    await close();
    return;
  }

  if (!validateAttachmentOrLink()) {
    return;
  }

  if (isApprovalEdit.value) {
    const response = await updateReviewDraft(payload);
    Object.assign(form, mapReviewFormModel(response.data, routeQuery.value));
    ElMessage.success('修改已保存，准备进入审批');
    openApprovalDialogAfterSave();
    return;
  }

  const response = shouldUseResubmit.value ? await resubmitReviewAction(payload) : await submitReviewAction(payload);
  form.id = response.data?.id ?? form.id;
  ElMessage.success(shouldUseResubmit.value ? '重新提交成功' : '提交成功');
  await close();
};

const submitForm = async (action: string) => {
  if (!reviewFormRef.value) {
    return;
  }

  buttonLoading.value = true;
  try {
    if (action === 'draft') {
      await doSubmit('draft');
      return;
    }

    const valid = await reviewFormRef.value.validate().catch(() => false);
    if (!valid) {
      return;
    }
    await doSubmit('submit');
  } finally {
    buttonLoading.value = false;
  }
};

const handleApprovalRecord = () => {
  if (!form.id) {
    return;
  }
  approvalRecordRef.value?.init(form.id);
};

const submitCallback = async () => {
  await close();
};

onMounted(() => {
  nextTick(() => {
    initForm();
  });
});
</script>
