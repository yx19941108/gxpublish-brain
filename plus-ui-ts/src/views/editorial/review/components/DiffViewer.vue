<template>
  <div class="diff-viewer">
    <div v-for="(diff, field) in diffData" :key="field" class="diff-item mb-4">
      <div class="field-label font-bold text-gray-700 mb-1">{{ getFieldLabel(field) }}</div>
      <div class="diff-content flex items-start text-sm">
        <div class="old-val w-1/2 p-2 bg-red-50 text-red-700 rounded mr-2 break-all">
          <span class="tag">变更前：</span>
          <template v-if="isAttachmentCollection(diff.old)">
            <div v-for="(attachment, index) in normalizeAttachmentCollection(diff.old)" :key="attachment.ossId ?? attachment.fileUrl ?? index" class="mb-2">
              <div>{{ attachment.fileName || '(空)' }}</div>
              <el-button link type="primary" class="px-0" @click="handleDownload(attachment)">下载</el-button>
            </div>
          </template>
          <template v-else>
            {{ formatDiffValue(diff.old) }}
          </template>
        </div>
        <div class="new-val w-1/2 p-2 bg-green-50 text-green-700 rounded break-all">
          <span class="tag">变更后：</span>
          <template v-if="isAttachmentCollection(diff.new)">
            <div v-for="(attachment, index) in normalizeAttachmentCollection(diff.new)" :key="attachment.ossId ?? attachment.fileUrl ?? index" class="mb-2">
              <div>{{ attachment.fileName || '(空)' }}</div>
              <el-button link type="primary" class="px-0" @click="handleDownload(attachment)">下载</el-button>
            </div>
          </template>
          <template v-else>
            {{ formatDiffValue(diff.new) }}
          </template>
        </div>
      </div>
    </div>
    <div v-if="Object.keys(diffData).length === 0" class="text-gray-400 text-center">无内容变更</div>
  </div>
</template>

<script setup lang="ts">
import { ElMessage } from 'element-plus';

import { fetchAttachmentBlob, openBlobDownload } from './attachmentUtils';

const props = defineProps({
  diffData: {
    type: Object,
    default: () => ({})
  }
});

const fieldMap: Record<string, string> = {
  title: '标题',
  content: '内容',
  attachment: '附件',
  linkList: '关联链接'
};

const getFieldLabel = (field: string) => {
  return fieldMap[field] || field;
};

const formatDiffValue = (value: unknown) => {
  if (value === undefined || value === null || value === '') {
    return '(空)';
  }
  if (typeof value === 'string' || typeof value === 'number' || typeof value === 'boolean') {
    return String(value);
  }
  try {
    return JSON.stringify(value, null, 2);
  } catch (error) {
    return String(value);
  }
};

const normalizeAttachmentCollection = (value: unknown) => {
  if (Array.isArray(value)) {
    return value as Array<Record<string, any>>;
  }
  if (value && typeof value === 'object' && ('ossId' in (value as Record<string, any>) || 'fileName' in (value as Record<string, any>) || 'fileUrl' in (value as Record<string, any>))) {
    return [value as Record<string, any>];
  }
  return [];
};

const isAttachmentCollection = (value: unknown) => normalizeAttachmentCollection(value).length > 0;

const handleDownload = async (attachment: Record<string, any>) => {
  try {
    const blob = await fetchAttachmentBlob(attachment);
    openBlobDownload(blob, attachment.fileName || 'attachment');
  } catch (error) {
    console.error('历史附件下载失败', error);
    if (attachment.fileUrl) {
      window.open(attachment.fileUrl, '_blank');
      return;
    }
    ElMessage.warning('历史附件下载失败，请稍后重试');
  }
};
</script>

<style scoped>
.tag {
  font-weight: bold;
  display: block;
  margin-bottom: 4px;
  opacity: 0.7;
}
</style>
