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
        @submitForm="submitForm"
        @handleApprovalRecord="handleApprovalRecord"
      />
    </el-card>

    <el-card shadow="never" class="mt-2">
      <el-form ref="reviewFormRef" v-loading="loading" :model="form" :rules="rules" label-width="100px">
        <ReviewFormFields :form="form" :dept-options="deptOptions" />
      </el-form>
    </el-card>

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
import { createReviewDraft, getReviewDetail, resubmitReviewAction, updateReviewDraft, submitReviewAction } from '@/api/editorial/review';
import ApprovalButton from '@/components/Process/approvalButton.vue';
import ApprovalRecord from '@/components/Process/approvalRecord.vue';
import { useUserStore } from '@/store/modules/user';

import ReviewFormFields from './components/ReviewFormFields.vue';
import { getReviewSubmitLabel, mapReviewFormModel, resolveReviewRouteType, toReviewActionPayload } from './integration';
import { createEmptyReviewForm } from './model';

const { proxy } = getCurrentInstance() as ComponentInternalInstance;
const route = useRoute();
const router = useRouter();
const userStore = useUserStore();

const reviewFormRef = ref<FormInstance>();
const approvalRecordRef = ref<InstanceType<typeof ApprovalRecord>>();

const loading = ref(true);
const buttonLoading = ref(false);
const deptOptions = ref<DeptVO[]>([]);
const form = reactive(createEmptyReviewForm(userStore.deptId));

const routeQuery = computed(() => route.query as Record<string, unknown>);
const routeType = computed(() => resolveReviewRouteType(routeQuery.value));
const buttonPageType = computed(() => (routeType.value === 'update' ? 'update' : 'add'));
const shouldUseResubmit = computed(() => Boolean(form.id) && form.reviewStatus === 'BACK');
const submitLabel = computed(() => getReviewSubmitLabel(form.reviewStatus));

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
  Object.assign(form, mapReviewFormModel(detailRes.data));
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

const doSubmit = async (action: 'draft' | 'submit') => {
  const payload = toReviewActionPayload(form);

  if (action === 'draft') {
    const response = form.id ? await updateReviewDraft(payload) : await createReviewDraft(payload);
    form.id = response.data?.id ?? form.id;
    ElMessage.success('暂存成功');
    await close();
    return;
  }

  if (!form.attachmentOssId && form.linkList.length === 0) {
    ElMessage.warning('提交审批时，附件和关联链接至少需要填写一项');
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

onMounted(() => {
  nextTick(() => {
    initForm();
  });
});
</script>
