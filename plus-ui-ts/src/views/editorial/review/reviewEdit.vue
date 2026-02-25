<template>
  <div class="p-2">
    <el-card shadow="never">
      <!-- 审批按钮组件: 处理提交、通过、驳回、转办等流程操作 -->
      <approvalButton
        @submitForm="submitForm"
        @approvalVerifyOpen="approvalVerifyOpen"
        @handleApprovalRecord="handleApprovalRecord"
        :buttonLoading="buttonLoading"
        :id="form.id"
        :status="form.status"
        :pageType="routeParams.type"
        :mode="true"
      />
    </el-card>

    <el-card shadow="never" style="height: 78vh; overflow-y: auto" class="mt-2">
      <el-form ref="reviewFormRef" v-loading="loading" :disabled="isView" :model="form" :rules="rules" label-width="100px">


        <el-form-item label="流程类型" prop="processType">
          <el-radio-group v-model="form.processType" :disabled="!!form.id || isView">
            <el-radio label="AUDIT">审核流程</el-radio>
            <el-radio label="PROOFREAD">校验流程</el-radio>
          </el-radio-group>
        </el-form-item>

        <el-form-item label="申请标题" prop="title">
          <el-input v-model="form.title" placeholder="请输入标题" />
        </el-form-item>

        <el-form-item label="申请内容" prop="content">
          <el-input type="textarea" v-model="form.content" :rows="6" placeholder="请输入内容" />
        </el-form-item>

        <el-form-item label="部门" prop="deptId">
          <el-tree-select
            v-model="form.deptId"
            :data="deptOptions"
            :props="{ value: 'deptId', label: 'deptName', children: 'children' }"
            value-key="deptId"
            placeholder="请选择部门"
            check-strictly
          />
        </el-form-item>

        <el-form-item label="附件" prop="attachmentOssId">
          <TusUpload
            v-model="form.attachmentOssId"
            v-model:fileName="form.attachmentFileName"
            v-model:fileUrl="attachmentFileUrl"
            v-model:fileSize="attachmentFileSize"
            :version="currentAttachmentVersion"
            :disabled="isView"
          />
        </el-form-item>

        <el-form-item label="关联链接">
          <div class="w-full">
            <div v-for="(link, index) in form.linkList" :key="index" class="flex mb-2 items-center">
              <el-input v-model="link.description" placeholder="描述" class="w-1/3 mr-2" :disabled="isView" />
              <el-input v-model="link.url" placeholder="URL地址 (https://...)" class="w-1/2 mr-2" :disabled="isView" />
              <el-button v-if="!isView" type="danger" icon="Delete" circle @click="removeLink(index)" />
              <el-link v-if="isView" type="primary" :href="link.url" target="_blank" class="ml-2">跳转</el-link>
            </div>
            <el-button v-if="!isView" type="primary" plain icon="Plus" @click="addLink">添加链接</el-button>
          </div>
        </el-form-item>

        <el-form-item label="备注">
          <el-input v-model="form.remark" type="textarea" placeholder="请输入备注" />
        </el-form-item>

        <!-- 历史差异展示 (仅在详情或审批模式显示) -->
        <el-form-item label="历史变更" v-if="isView || routeParams.type === 'approval'">
          <div class="w-full border rounded p-2 bg-gray-50">
            <el-timeline>
              <el-timeline-item v-for="(his, index) in historyList" :key="index" :timestamp="parseTime(his.operateTime)" placement="top">
                <el-card shadow="hover" class="mb-2">
                  <h4>{{ his.operatorName }} 执行了 {{ his.operateType }}</h4>
                  <DiffViewer v-if="his.fieldDiff" :diffData="his.fieldDiff" />
                </el-card>
              </el-timeline-item>
              <div v-if="historyList.length === 0" class="text-center text-gray-400">暂无历史记录</div>
            </el-timeline>
          </div>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 提交验证组件 (选人/变量) -->
    <submitVerify ref="submitVerifyRef" :task-variables="taskVariables" @submit-callback="submitCallback" />
    <!-- 审批记录组件 (时间轴) -->
    <approvalRecord ref="approvalRecordRef" />
  </div>
</template>

<script setup name="ReviewEdit" lang="ts">
import { ref, reactive, toRefs, onMounted, computed, nextTick, getCurrentInstance, ComponentInternalInstance } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { ElMessage } from 'element-plus';
import type { FormInstance, FormRules } from 'element-plus';
import {
  getReview,
  addReview,
  updateReview,
  submitReview,
  getHistory,
  type EditorialReviewForm,
  type EditorialHistoryVo
} from '@/api/editorial/review';
import { listDept } from '@/api/system/dept';
import { startWorkFlow } from '@/api/workflow/task';
import { StartProcessBo } from '@/api/workflow/workflowCommon/types';

import TusUpload from './components/TusUpload.vue';
import DiffViewer from './components/DiffViewer.vue';
import SubmitVerify from '@/components/Process/submitVerify.vue';
import ApprovalRecord from '@/components/Process/approvalRecord.vue';
import ApprovalButton from '@/components/Process/approvalButton.vue';

const { proxy } = getCurrentInstance() as ComponentInternalInstance;
const route = useRoute();
const router = useRouter();

const reviewFormRef = ref<FormInstance>();
const submitVerifyRef = ref<InstanceType<typeof SubmitVerify>>();
const approvalRecordRef = ref<InstanceType<typeof ApprovalRecord>>();

const buttonLoading = ref(false);
const loading = ref(true);
const routeParams = ref<Record<string, any>>({});
const deptOptions = ref([]);
const historyList = ref<EditorialHistoryVo[]>([]);
const currentAttachmentVersion = ref(0);
const attachmentFileUrl = ref('');
const attachmentFileSize = ref<number | null>(null);

const flowCode = ref('editorial_review_flow'); // 默认流程编码

const form = reactive<EditorialReviewForm>({
  title: '',
  content: '',
  linkList: [],
  remark: '',
  status: 'DRAFT',
  processType: 'AUDIT' // 默认值
});

const rules = reactive<FormRules>({
  title: [{ required: true, message: '请输入标题', trigger: 'blur' }],
  content: [{ required: true, message: '请输入内容', trigger: 'blur' }],
  deptId: [{ required: true, message: '请选择部门', trigger: 'change' }],
  processType: [{ required: true, message: '请选择流程类型', trigger: 'change' }]
});

const submitFormData = ref<StartProcessBo>({
  businessId: '',
  flowCode: '',
  variables: {},
  bizExt: {}
});
const taskVariables = ref<Record<string, any>>({});
const bizExt = ref<Record<string, any>>({});

const isView = computed(() => routeParams.value.type === 'view' || routeParams.value.type === 'approval');

import { useUserStore } from '@/store/modules/user';
import { DeptVO } from '@/api/system/dept/types';

const getDeptTree = async () => {
  const res = await listDept({} as any);
  const data = proxy?.handleTree<DeptVO>(res.data, 'deptId');
  if (data) {
    deptOptions.value = data;
  }
};

const initData = async () => {
  await getDeptTree();
  const id = routeParams.value.id;
  if (!id && routeParams.value.type === 'add') {
    // 自动带出当前用户部门
    const userStore = useUserStore();
    if (userStore.deptId) {
      form.deptId = userStore.deptId as any;
    }
  }
  if (id) {
    const res = await getReview(id);
    Object.assign(form, res.data);

    if (res.data.attachment) {
      form.attachmentOssId = res.data.attachment.ossId;
      form.attachmentFileName = res.data.attachment.fileName;
      attachmentFileUrl.value = res.data.attachment.fileUrl || '';
      currentAttachmentVersion.value = res.data.attachment.version;
    }
    if (res.data.linkList) {
      form.linkList = res.data.linkList.map((item) => ({
        id: item.id,
        url: item.url,
        description: item.description
      }));
    }

    // 加载历史记录
    const hisRes = await getHistory(id);
    historyList.value = hisRes.data;
  }
};

// 提交/保存逻辑
// mode: true=后端直接发起流程(非草稿), false=保存/修改
const submitForm = async (status: string, mode: boolean) => {
  if (!reviewFormRef.value) return;

  const doSubmit = async () => {
    buttonLoading.value = true;
    try {
      // 将附件的fileUrl和fileSize合入form提交到后端
      (form as any).attachmentFileUrl = attachmentFileUrl.value || '';
      (form as any).attachmentFileSize = attachmentFileSize.value || null;
      (form as any).flowCode = flowCode.value;

      if (status === 'draft') {
        let res;
        if (form.id) {
          res = await updateReview(form);
        } else {
          res = await addReview(form);
        }
        form.id = res.data.id; // 回填ID
        
        ElMessage.success('暂存成功');
        close();
      } else {
        // 提交校验必填项
        if (!form.attachmentOssId && (!form.linkList || form.linkList.length === 0)) {
          ElMessage.warning('提交审批时，附件和关联链接至少需要填写一项');
          buttonLoading.value = false;
          return;
        }

        // 发起流程 (利用后端直接发起的集成接口)
        const res = await submitReview(form);
        form.id = res.data.id;
        ElMessage.success('提交并启动流程成功');
        close();
      }
    } catch (e) {
      console.error(e);
    } finally {
      buttonLoading.value = false;
    }
  };

  if (status === 'draft') {
    // 保存草稿无需触发表单必填校验
    await doSubmit();
  } else {
    // 提交流程需严格校验表单必填
    await reviewFormRef.value.validate(async (valid) => {
      if (valid) {
        await doSubmit();
      }
    });
  }
};

const handleStartWorkFlow = async (data: any) => {
  submitFormData.value.flowCode = flowCode.value;
  submitFormData.value.businessId = data.id;
  // 设置流程变量 (如有)
  taskVariables.value = {
    // example: deptId: data.deptId
  };
  bizExt.value = {
    businessTitle: `审校申请: ${data.title}`,
    businessCode: data.applyCode // 假设后端生成了 applyCode
  };
  submitFormData.value.variables = taskVariables.value;
  submitFormData.value.bizExt = bizExt.value;

  const resp = await startWorkFlow(submitFormData.value);
  if (submitVerifyRef.value) {
    submitVerifyRef.value.openDialog(resp.data.taskId);
  }
};

const approvalVerifyOpen = () => {
  if (submitVerifyRef.value) {
    submitVerifyRef.value.openDialog(routeParams.value.taskId);
  }
};

const handleApprovalRecord = () => {
  if (approvalRecordRef.value) {
    approvalRecordRef.value.init(form.id);
  }
};

const submitCallback = () => {
  close();
};

const close = () => {
  proxy?.$tab.closePage(proxy.$route);
  proxy?.$router.go(-1);
};

const addLink = () => {
  if (!form.linkList) form.linkList = [];
  form.linkList.push({ url: '', description: '' });
};

const removeLink = (index: number) => {
  form.linkList?.splice(index, 1);
};

onMounted(() => {
  nextTick(() => {
    routeParams.value = proxy?.$route.query || {};
    initData();
    loading.value = false;
  });
});
</script>
