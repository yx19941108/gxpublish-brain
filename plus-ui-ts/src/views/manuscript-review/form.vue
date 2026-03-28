<template>
  <div data-testid="manuscript-review-form-page" class="p-2 manuscript-review-form-page">
    <el-card shadow="hover" class="mb-[12px]">
      <template #header>
        <div class="manuscript-review-form-page__hero">
          <div>
            <div class="manuscript-review-form-page__eyebrow">{{ presentation.mode === 'create' ? 'CREATE / SUBMIT ONLY' : 'EDIT / SAVE ONLY' }}</div>
            <h1 class="manuscript-review-form-page__title">{{ presentation.title }}</h1>
            <p class="manuscript-review-form-page__desc">{{ presentation.description }}</p>
          </div>
          <el-tag type="primary" effect="light">{{
            presentation.mode === 'create' ? '冻结口径：新增提交是一体化动作' : '冻结口径：修改保存不推 BPM'
          }}</el-tag>
        </div>
      </template>

      <div v-if="errorMessage" class="mb-[12px]">
        <el-alert :title="errorMessage" type="error" :closable="false" show-icon />
      </div>

      <div v-if="loading" class="manuscript-review-form-page__loading">
        <el-skeleton :rows="8" animated />
      </div>

      <template v-else>
        <el-form ref="formRef" :model="formModel" :rules="rules" label-width="100px" class="manuscript-review-form-page__form">
          <el-card shadow="never" class="mb-[12px]">
            <template #header>
              <div class="manuscript-review-form-page__section-head">
                <span>基础信息区</span>
                <span class="manuscript-review-form-page__section-tip">
                  {{
                    presentation.mode === 'create'
                      ? 'create 模式：提交时整表单 + 暂存资源一次性发送'
                      : 'edit 模式：保存时主表 + 本次追加资源一次性发送'
                  }}
                </span>
              </div>
            </template>

            <el-row :gutter="16">
              <el-col :span="12">
                <el-form-item label="流程类型" prop="processType">
                  <el-select v-model="formModel.processType" placeholder="请选择流程类型" clearable>
                    <el-option v-for="item in processTypeOptions" :key="item.value" :label="item.label" :value="item.value" />
                  </el-select>
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="外部稿件号" prop="externalManuscriptCode">
                  <el-input v-model="formModel.externalManuscriptCode" placeholder="请输入外部稿件号" />
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="标题" prop="title">
                  <el-input v-model="formModel.title" placeholder="请输入标题" />
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="媒体栏目" prop="mediaChannel">
                  <el-input v-model="formModel.mediaChannel" placeholder="请输入媒体栏目" />
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="报送部门" prop="submitDepartment">
                  <el-input v-model="formModel.submitDepartment" placeholder="系统自动带出" readonly />
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="作者" prop="authorName">
                  <el-input v-model="formModel.authorName" placeholder="请输入作者" />
                </el-form-item>
              </el-col>
              <el-col :span="24">
                <el-form-item label="说明" prop="remark">
                  <el-input v-model="formModel.remark" type="textarea" :rows="3" placeholder="请输入说明" />
                </el-form-item>
              </el-col>
              <el-col :span="24">
                <el-form-item label="正文" prop="contentBody">
                  <el-input v-model="formModel.contentBody" type="textarea" :rows="6" placeholder="请输入正文内容" />
                </el-form-item>
              </el-col>
            </el-row>
          </el-card>

          <el-card data-testid="manuscript-review-form-upload-section" shadow="never" class="mb-[12px]">
            <template #header>
              <div class="manuscript-review-form-page__section-head">
                <span>上传暂存区</span>
                <span class="manuscript-review-form-page__section-tip">
                  上传后仅暂存在表单态；未提交附件删除走 deletePendingOssResource / DELETE /resource/oss/{ossId}
                </span>
              </div>
            </template>

            <div class="mb-[12px]">
              <el-upload
                :action="uploadFileUrl"
                :data="buildUploadData"
                :headers="uploadHeaders"
                :show-file-list="false"
                :before-upload="handleBeforeUpload"
                :on-success="handleUploadSuccess"
                :on-error="handleUploadError"
                multiple
              >
                <el-button type="primary" plain>上传附件/视频</el-button>
              </el-upload>
            </div>

            <div v-if="draftUploads.length === 0" class="manuscript-review-form-page__empty">暂无待提交附件，可继续上传。</div>
            <div v-else class="manuscript-review-form-page__draft-list">
              <div v-for="item in draftUploads" :key="item.uid" class="manuscript-review-form-page__draft-item">
                <div>
                  <div class="manuscript-review-form-page__draft-name">{{ item.displayName }}</div>
                  <div class="manuscript-review-form-page__draft-meta">{{ item.resourceType }} / ossId={{ item.ossId }}</div>
                </div>
                <el-button link type="danger" @click="handleDeletePendingUpload(item.ossId)">删除</el-button>
              </div>
            </div>
          </el-card>

          <el-card data-testid="manuscript-review-form-external-links" shadow="never" class="mb-[12px]">
            <template #header>
              <div class="manuscript-review-form-page__section-head">
                <span>外链录入区</span>
                <span class="manuscript-review-form-page__section-tip">外链与附件至少一种，空行不会进入最终 payload。</span>
              </div>
            </template>

            <div class="manuscript-review-form-page__link-list">
              <div v-for="item in draftExternalLinks" :key="item.uid" class="manuscript-review-form-page__link-item">
                <el-input v-model="item.displayName" placeholder="链接标题" />
                <el-input v-model="item.externalUrl" placeholder="https://example.com/ref" />
                <el-button link type="danger" @click="removeExternalLink(item.uid)">移除</el-button>
              </div>
            </div>
            <el-button plain @click="appendExternalLink">新增外链</el-button>
          </el-card>

          <el-card v-if="presentation.mode === 'edit'" data-testid="manuscript-review-form-persisted-resources" shadow="never" class="mb-[12px]">
            <template #header>
              <div class="manuscript-review-form-page__section-head">
                <span>当前已入库资源</span>
                <span class="manuscript-review-form-page__section-tip">edit 模式只展示已入库资源，不把它们混入本次草稿资源。</span>
              </div>
            </template>

            <div v-if="persistedResources.length === 0" class="manuscript-review-form-page__empty">暂无已入库资源。</div>
            <div v-else class="manuscript-review-form-page__draft-list">
              <div v-for="item in persistedResources" :key="item.id" class="manuscript-review-form-page__draft-item">
                <div>
                  <div class="manuscript-review-form-page__draft-name">{{ item.displayName }}</div>
                  <div class="manuscript-review-form-page__draft-meta">
                    {{ item.typeLabel }}<span v-if="item.note"> / {{ item.note }}</span>
                  </div>
                </div>
                <el-tag size="small" type="success" effect="light">已入库</el-tag>
              </div>
            </div>
          </el-card>

          <div class="manuscript-review-form-page__footer">
            <el-button @click="handleBack">返回</el-button>
            <el-button data-testid="manuscript-review-form-primary-action" type="primary" :loading="submitting" @click="handlePrimaryAction">
              {{ presentation.primaryActionLabel }}
            </el-button>
          </div>
        </el-form>
      </template>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { ElMessage, type FormInstance, type FormRules, type UploadProps, type UploadRawFile } from 'element-plus';

import {
  deletePendingOssResource,
  getManuscriptReviewDetail,
  submitAndFlowStartManuscriptReview,
  updateManuscriptReview
} from '@/api/manuscript-review';
import { getInfo as getCurrentUserInfo } from '@/api/login';
import { globalHeaders } from '@/utils/request';

import {
  buildFormPresentation,
  PROCESS_TYPE_OPTIONS,
  buildIntegratedSavePayload,
  buildIntegratedSubmitPayload,
  createDraftStateFromDetail,
  createDraftStateFromPayload,
  readReviewIdFromQuery,
  removeDraftUploadByOssId,
  resolveFormMode,
  type ManuscriptReviewDraftExternalLinkItem,
  type ManuscriptReviewDraftFormModel,
  type ManuscriptReviewDraftUploadItem,
  type ManuscriptReviewPersistedResourceView
} from './components/formState';
import { resolveEditBackTarget } from './detail-navigation';

const route = useRoute();
const router = useRouter();

const mode = computed(() => resolveFormMode(route.query));
const presentation = computed(() => buildFormPresentation(mode.value));
const reviewId = computed(() => readReviewIdFromQuery(route.query));

const formRef = ref<FormInstance>();
const loading = ref(false);
const submitting = ref(false);
const errorMessage = ref('');
const draftUploads = ref<ManuscriptReviewDraftUploadItem[]>([]);
const draftExternalLinks = ref<ManuscriptReviewDraftExternalLinkItem[]>([{ uid: 'link-1', displayName: '', externalUrl: '' }]);
const persistedResources = ref<ManuscriptReviewPersistedResourceView[]>([]);
const pendingVideoDurationSeconds = ref<Record<string, number>>({});

const formModel = reactive<ManuscriptReviewDraftFormModel>({
  processType: '',
  externalManuscriptCode: '',
  title: '',
  mediaChannel: '',
  submitDepartment: '',
  authorName: '',
  remark: '',
  contentBody: ''
});

const uploadFileUrl = `${import.meta.env.VITE_APP_BASE_API}/resource/oss/upload`;
const uploadHeaders = globalHeaders();
const processTypeOptions = PROCESS_TYPE_OPTIONS;

const rules: FormRules<ManuscriptReviewDraftFormModel> = {
  processType: [{ required: true, message: '请选择流程类型', trigger: 'change' }],
  title: [{ required: true, message: '请输入标题', trigger: 'blur' }],
  mediaChannel: [{ required: true, message: '请输入媒体栏目', trigger: 'blur' }],
  contentBody: [{ required: true, message: '请输入正文内容', trigger: 'blur' }]
};

const resetDraftState = () => {
  formModel.processType = '';
  formModel.externalManuscriptCode = '';
  formModel.title = '';
  formModel.mediaChannel = '';
  formModel.submitDepartment = '';
  formModel.authorName = '';
  formModel.remark = '';
  formModel.contentBody = '';
  draftUploads.value = [];
  draftExternalLinks.value = [{ uid: `link-${Date.now()}`, displayName: '', externalUrl: '' }];
  persistedResources.value = [];
  pendingVideoDurationSeconds.value = {};
};

const ensureAtLeastOneLink = () => {
  if (draftExternalLinks.value.length === 0) {
    draftExternalLinks.value = [{ uid: `link-${Date.now()}`, displayName: '', externalUrl: '' }];
  }
};

const appendExternalLink = () => {
  draftExternalLinks.value.push({ uid: `link-${Date.now()}-${draftExternalLinks.value.length + 1}`, displayName: '', externalUrl: '' });
};

const removeExternalLink = (uid: string) => {
  draftExternalLinks.value = draftExternalLinks.value.filter((item) => item.uid !== uid);
  ensureAtLeastOneLink();
};

const applyEditDraftState = async () => {
  if (!reviewId.value) {
    errorMessage.value = 'edit 模式缺少 reviewId，无法回填表单。';
    return;
  }

  loading.value = true;
  errorMessage.value = '';

  try {
    const detail = await getManuscriptReviewDetail(reviewId.value);
    const draftState = createDraftStateFromPayload(detail);
    Object.assign(formModel, draftState.form);
    persistedResources.value = draftState.persistedResources;
    draftUploads.value = draftState.draftUploads;
    draftExternalLinks.value =
      draftState.draftExternalLinks.length > 0 ? draftState.draftExternalLinks : [{ uid: `link-${Date.now()}`, displayName: '', externalUrl: '' }];
  } catch {
    errorMessage.value = '详情回填失败，请从详情页重新进入修改。';
  } finally {
    loading.value = false;
  }
};

const applyCreateDefaultSubmitDepartment = async () => {
  if (mode.value !== 'create' || formModel.submitDepartment.trim()) {
    return;
  }

  try {
    const payload = await getCurrentUserInfo();
    const deptName = payload?.data?.user?.deptName ?? payload?.user?.deptName ?? '';
    if (typeof deptName === 'string' && deptName.trim()) {
      formModel.submitDepartment = deptName.trim();
    }
  } catch {
    // keep empty and rely on validation/runtime feedback if current user info is unavailable
  }
};

const isVideoUpload = (rawFile: UploadRawFile) => {
  const mimeType = String(rawFile.type ?? '').toLowerCase();
  if (mimeType.startsWith('video/')) {
    return true;
  }
  return rawFile.name.toLowerCase().endsWith('.mp4');
};

const resolveVideoDurationSeconds = (rawFile: UploadRawFile): Promise<number | undefined> =>
  new Promise((resolve) => {
    const objectUrl = URL.createObjectURL(rawFile);
    const video = document.createElement('video');
    const cleanup = () => {
      video.removeAttribute('src');
      video.load();
      URL.revokeObjectURL(objectUrl);
    };

    video.preload = 'metadata';
    video.onloadedmetadata = () => {
      const duration = Number.isFinite(video.duration) ? Math.ceil(video.duration) : NaN;
      cleanup();
      resolve(duration > 0 ? duration : undefined);
    };
    video.onerror = () => {
      cleanup();
      resolve(undefined);
    };
    video.src = objectUrl;
  });

const handleBeforeUpload: UploadProps['beforeUpload'] = async (rawFile) => {
  if (rawFile.name.includes(',')) {
    ElMessage.error('文件名不能包含英文逗号。');
    return false;
  }

  delete pendingVideoDurationSeconds.value[String(rawFile.uid)];
  if (!isVideoUpload(rawFile)) {
    return true;
  }

  const durationSeconds = await resolveVideoDurationSeconds(rawFile);
  if (!durationSeconds) {
    ElMessage.error('无法识别视频总时长，请更换视频文件后重试。');
    return false;
  }
  pendingVideoDurationSeconds.value[String(rawFile.uid)] = durationSeconds;
  return true;
};

const buildUploadData = async (rawFile: UploadRawFile) => {
  const durationSeconds = pendingVideoDurationSeconds.value[String(rawFile.uid)];
  return durationSeconds ? { videoDurationSeconds: String(durationSeconds) } : {};
};

const handleUploadSuccess: UploadProps['onSuccess'] = (response, uploadFile) => {
  delete pendingVideoDurationSeconds.value[String(uploadFile.uid)];
  if (response?.code !== 200 || !response?.data?.ossId) {
    ElMessage.error(response?.msg ?? '上传失败，请重试。');
    return;
  }

  const rawType = String(uploadFile.raw?.type ?? '').toLowerCase();
  const resourceType = rawType.startsWith('video/') ? 'VIDEO' : 'ATTACHMENT';

  draftUploads.value.push({
    uid: String(uploadFile.uid),
    displayName: response.data.fileName ?? uploadFile.name,
    ossId: response.data.ossId,
    url: response.data.url,
    resourceType
  });
};

const handleUploadError: UploadProps['onError'] = (_, uploadFile) => {
  delete pendingVideoDurationSeconds.value[String(uploadFile.uid)];
  ElMessage.error('上传失败，请重试。');
};

const handleDeletePendingUpload = async (ossId: string | number) => {
  try {
    await deletePendingOssResource(ossId);
    draftUploads.value = removeDraftUploadByOssId(draftUploads.value, ossId);
    ElMessage.success('已删除暂存附件。');
  } catch {
    ElMessage.error('删除暂存附件失败，请稍后重试。');
  }
};

const handleBack = () => {
  if (mode.value === 'edit' && reviewId.value) {
    void router.push(resolveEditBackTarget(route.query, reviewId.value));
    return;
  }
  void router.push({ path: '/manuscript/review' });
};

const resolveSuccessReviewId = (payload: unknown): string => {
  if (typeof payload === 'object' && payload !== null) {
    const maybeId = (payload as { id?: string | number }).id;
    if (maybeId != null) {
      return String(maybeId);
    }
    const nestedId = (payload as { data?: { id?: string | number } }).data?.id;
    if (nestedId != null) {
      return String(nestedId);
    }
  }
  return reviewId.value;
};

const handlePrimaryAction = async () => {
  const valid = await formRef.value?.validate().catch(() => false);
  if (!valid) {
    return;
  }

  submitting.value = true;
  errorMessage.value = '';

  try {
    if (mode.value === 'edit') {
      if (!reviewId.value) {
        throw new Error('MISSING_REVIEW_ID');
      }
      await updateManuscriptReview(buildIntegratedSavePayload(reviewId.value, formModel, draftUploads.value, draftExternalLinks.value));
      ElMessage.success(presentation.value.successToast);
      await router.push(resolveEditBackTarget(route.query, reviewId.value));
      return;
    }

    const result = await submitAndFlowStartManuscriptReview(buildIntegratedSubmitPayload(formModel, draftUploads.value, draftExternalLinks.value));
    const nextReviewId = resolveSuccessReviewId(result);
    ElMessage.success(presentation.value.successToast);
    if (nextReviewId) {
      await router.push({ path: '/manuscript/review/detail', query: { reviewId: nextReviewId } });
      return;
    }
    await router.push({ path: '/manuscript/review' });
  } catch (error) {
    if (error instanceof Error && error.message === 'MISSING_REVIEW_ID') {
      errorMessage.value = 'edit 模式缺少 reviewId，无法执行保存。';
    } else {
      errorMessage.value = `${presentation.value.primaryActionLabel}失败，请根据提示修正后重试。`;
    }
  } finally {
    submitting.value = false;
  }
};

onMounted(() => {
  resetDraftState();
  if (mode.value === 'edit') {
    void applyEditDraftState();
    return;
  }
  void applyCreateDefaultSubmitDepartment();
});
</script>

<style scoped lang="scss">
.manuscript-review-form-page {
  &__hero {
    display: flex;
    align-items: flex-start;
    justify-content: space-between;
    gap: 16px;
  }

  &__eyebrow {
    margin-bottom: 8px;
    color: var(--el-color-primary);
    font-size: 12px;
    font-weight: 700;
    letter-spacing: 0.08em;
  }

  &__title {
    margin: 0 0 8px;
    font-size: 28px;
    line-height: 1.2;
  }

  &__desc {
    margin: 0;
    color: var(--el-text-color-secondary);
    line-height: 1.7;
  }

  &__section-head {
    display: flex;
    align-items: center;
    justify-content: space-between;
    gap: 12px;
    font-weight: 600;
  }

  &__section-tip {
    color: var(--el-text-color-secondary);
    font-size: 12px;
    font-weight: 400;
  }

  &__footer {
    display: flex;
    justify-content: flex-end;
    gap: 12px;
    padding-top: 4px;
  }

  &__empty {
    color: var(--el-text-color-secondary);
    font-size: 13px;
    line-height: 1.8;
  }

  &__draft-list,
  &__link-list {
    display: grid;
    gap: 12px;
  }

  &__draft-item {
    display: flex;
    align-items: center;
    justify-content: space-between;
    gap: 16px;
    padding: 12px 14px;
    border: 1px solid var(--el-border-color-light);
    border-radius: 12px;
    background: var(--el-fill-color-extra-light);
  }

  &__draft-name {
    font-weight: 600;
  }

  &__draft-meta {
    margin-top: 4px;
    color: var(--el-text-color-secondary);
    font-size: 12px;
  }

  &__link-item {
    display: grid;
    grid-template-columns: minmax(180px, 240px) 1fr auto;
    gap: 12px;
    align-items: center;
  }

  &__loading {
    padding: 4px 0 8px;
  }
}
</style>
