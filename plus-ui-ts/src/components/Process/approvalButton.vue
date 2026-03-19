<template>
  <div style="display: flex; justify-content: space-between">
    <div>
      <el-button v-if="draftButtonShow" :loading="props.buttonLoading" type="info" @click="submitForm('draft', mode)">暂存</el-button>
      <el-button v-if="submitButtonShow" :loading="props.buttonLoading" type="primary" @click="submitForm('submit', mode)">{{
        props.submitLabel
      }}</el-button>
      <el-button v-if="approvalButtonShow" :loading="props.buttonLoading" type="primary" @click="approvalVerifyOpen">审批</el-button>
      <el-button v-if="props.id && props.status?.toLowerCase() !== 'draft'" type="primary" @click="handleApprovalRecord">流程进度</el-button>
      <slot />
    </div>
    <div>
      <el-button style="float: right" @click="goBack()">返回</el-button>
    </div>
  </div>
</template>
<script setup lang="ts">
import { propTypes } from '@/utils/propTypes';
const { proxy } = getCurrentInstance() as ComponentInternalInstance;
const props = defineProps({
  status: propTypes.string.def(''),
  pageType: propTypes.string.def(''),
  buttonLoading: propTypes.bool.def(false),
  id: propTypes.string.def('') || propTypes.number.def(),
  mode: propTypes.bool.def(false),
  submitLabel: propTypes.string.def('提交'),
  showDraftButton: {
    type: Boolean,
    default: undefined
  },
  showSubmitButton: {
    type: Boolean,
    default: undefined
  }
});
const emits = defineEmits(['submitForm', 'approvalVerifyOpen', 'handleApprovalRecord']);
//暂存，提交
const submitForm = async (type, mode) => {
  emits('submitForm', type, mode);
};
//审批
const approvalVerifyOpen = async () => {
  emits('approvalVerifyOpen');
};
//审批记录
const handleApprovalRecord = () => {
  emits('handleApprovalRecord');
};

const normalizedStatus = computed(() => {
  return String(props.status || '')
    .trim()
    .toUpperCase();
});

const isWaitingStatus = computed(() => {
  return normalizedStatus.value === 'WAITING' || normalizedStatus.value.startsWith('WAITING_');
});

//校验提交按钮是否显示
const defaultSubmitButtonShow = computed(() => {
  return props.pageType === 'add' || (props.pageType === 'update' && (normalizedStatus.value === 'DRAFT' || normalizedStatus.value === 'BACK'));
});

const draftButtonShow = computed(() => {
  if (typeof props.showDraftButton === 'boolean') {
    return props.showDraftButton;
  }
  return defaultSubmitButtonShow.value;
});

const submitButtonShow = computed(() => {
  if (typeof props.showSubmitButton === 'boolean') {
    return props.showSubmitButton;
  }
  return defaultSubmitButtonShow.value;
});

//校验审批按钮是否显示
const approvalButtonShow = computed(() => {
  return props.pageType === 'approval' && isWaitingStatus.value;
});

//返回
const goBack = () => {
  proxy.$tab.closePage(proxy.$route);
  proxy.$router.go(-1);
};
</script>
