<template>
  <div class="w-full">
    <template v-if="readonly">
      <div v-if="attachments.length > 0" class="space-y-3">
        <div v-for="(attachment, index) in attachments" :key="attachment.id ?? attachment.ossId ?? index" class="attachment-card">
          <div class="attachment-card__meta">
            <div class="font-medium">{{ attachment.fileName || '未命名文件' }}</div>
            <div class="text-xs text-[var(--el-text-color-secondary)]">
              <span v-if="attachment.version">v{{ attachment.version }}</span>
              <span v-if="attachment.createTime" class="ml-2">{{ attachment.createTime }}</span>
            </div>
          </div>
          <div class="attachment-card__actions">
            <el-button link type="primary" @click="handlePreview(attachment)">预览</el-button>
            <el-button link type="success" @click="handleDownload(attachment)">下载</el-button>
          </div>
        </div>
      </div>
      <div v-else class="text-sm text-[var(--el-text-color-secondary)]">暂无附件</div>
    </template>

    <template v-else>
      <div v-if="attachments.length > 0" class="space-y-3">
        <div v-for="(attachment, index) in attachments" :key="attachment.id ?? attachment.ossId ?? index" class="attachment-edit-row">
          <TusUpload
            :model-value="attachment.ossId"
            :file-name="attachment.fileName"
            :file-url="attachment.fileUrl"
            :file-size="attachment.fileSize ?? undefined"
            :version="attachment.version"
            @update:modelValue="updateAttachment(index, 'ossId', $event)"
            @update:fileName="updateAttachment(index, 'fileName', $event)"
            @update:fileUrl="updateAttachment(index, 'fileUrl', $event)"
            @update:fileSize="updateAttachment(index, 'fileSize', $event)"
          />
          <el-button type="danger" plain @click="removeAttachment(index)">移除</el-button>
        </div>
      </div>
      <div v-else class="text-sm text-[var(--el-text-color-secondary)]">暂无附件，可按需添加多个附件</div>

      <el-button type="primary" plain icon="Plus" class="mt-3" @click="addAttachment">添加附件</el-button>
    </template>

    <el-dialog v-model="previewVisible" :title="previewTitle" width="70%" destroy-on-close>
      <template v-if="previewSource">
        <img v-if="previewKind === 'image'" :src="previewSource" class="mx-auto max-h-[70vh] max-w-full object-contain" />
        <iframe v-else-if="previewKind === 'pdf'" :src="previewSource" class="h-[70vh] w-full border-0" />
        <video v-else-if="previewKind === 'video'" :src="previewSource" controls class="mx-auto max-h-[70vh] max-w-full" />
        <div v-else class="py-6 text-center">
          当前文件类型暂不支持页内预览，请使用下载或新窗口打开。
        </div>
      </template>
      <template #footer>
        <el-button @click="previewVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ElMessage } from 'element-plus';
import { computed, ref, watch, type PropType } from 'vue';

import type { ReviewAttachmentFormItem } from '../model';
import { fallbackPreviewUrl, fetchAttachmentBlob, openBlobDownload, resolveAttachmentPreviewKind, type AttachmentPreviewKind } from './attachmentUtils';
import TusUpload from './TusUpload.vue';

const props = defineProps({
  modelValue: {
    type: Array as PropType<ReviewAttachmentFormItem[]>,
    default: () => []
  },
  readonly: {
    type: Boolean,
    default: false
  }
});

const emit = defineEmits(['update:modelValue']);

const previewVisible = ref(false);
const previewSource = ref('');
const previewTitle = ref('');
const previewKind = ref<AttachmentPreviewKind>('other');
let previewObjectUrl: string | null = null;

const attachments = computed(() => props.modelValue ?? []);

const emitAttachments = (nextAttachments: ReviewAttachmentFormItem[]) => {
  emit('update:modelValue', nextAttachments);
};

const addAttachment = () => {
  emitAttachments([
    ...attachments.value,
    {
      id: null,
      ossId: '',
      fileName: '',
      fileUrl: '',
      fileSize: null,
      version: 0
    }
  ]);
};

const removeAttachment = (index: number) => {
  const nextAttachments = [...attachments.value];
  nextAttachments.splice(index, 1);
  emitAttachments(nextAttachments);
};

const updateAttachment = (index: number, field: keyof ReviewAttachmentFormItem, value: unknown) => {
  const nextAttachments = [...attachments.value];
  const current = nextAttachments[index] ?? {
    id: null,
    ossId: '',
    fileName: '',
    fileUrl: '',
    fileSize: null,
    version: 0
  };

  nextAttachments[index] = {
    ...current,
    [field]: value
  };
  emitAttachments(nextAttachments);
};

const cleanupPreviewUrl = () => {
  if (previewObjectUrl) {
    URL.revokeObjectURL(previewObjectUrl);
    previewObjectUrl = null;
  }
};

watch(previewVisible, (visible) => {
  if (!visible) {
    cleanupPreviewUrl();
    previewSource.value = '';
  }
});

const handlePreview = async (attachment: ReviewAttachmentFormItem) => {
  previewTitle.value = attachment.fileName || '附件预览';
  previewKind.value = resolveAttachmentPreviewKind(attachment);

  if (previewKind.value === 'other') {
    const fallbackUrl = fallbackPreviewUrl(attachment);
    if (fallbackUrl) {
      window.open(fallbackUrl, '_blank');
      return;
    }
    ElMessage.warning('当前文件缺少可预览地址');
    return;
  }

  try {
    cleanupPreviewUrl();
    const blob = await fetchAttachmentBlob(attachment);
    previewObjectUrl = URL.createObjectURL(blob);
    previewSource.value = previewObjectUrl;
    previewVisible.value = true;
  } catch (error) {
    console.error('附件预览失败', error);
    ElMessage.warning('附件预览失败，请稍后重试');
  }
};

const handleDownload = async (attachment: ReviewAttachmentFormItem) => {
  try {
    const blob = await fetchAttachmentBlob(attachment);
    openBlobDownload(blob, attachment.fileName || 'download');
  } catch (error) {
    console.error('附件下载失败', error);
    const fallbackUrl = fallbackPreviewUrl(attachment);
    if (fallbackUrl) {
      window.open(fallbackUrl, '_blank');
      return;
    }
    ElMessage.warning('附件下载失败，请稍后重试');
  }
};
</script>

<style scoped>
.attachment-card,
.attachment-edit-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  padding: 12px;
  border: 1px solid var(--el-border-color-lighter);
  border-radius: 8px;
  background: var(--el-fill-color-lighter);
}

.attachment-card__meta {
  min-width: 0;
  flex: 1;
}

.attachment-card__actions {
  display: flex;
  align-items: center;
  gap: 8px;
}

.attachment-edit-row :deep(.tus-upload) {
  flex: 1;
}
</style>
