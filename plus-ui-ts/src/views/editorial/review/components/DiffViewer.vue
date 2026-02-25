<template>
  <div class="diff-viewer">
    <div v-for="(diff, field) in diffData" :key="field" class="diff-item mb-4">
      <div class="field-label font-bold text-gray-700 mb-1">{{ getFieldLabel(field) }}</div>
      <div class="diff-content flex items-start text-sm">
        <div class="old-val w-1/2 p-2 bg-red-50 text-red-700 rounded mr-2 break-all">
          <span class="tag">变更前：</span>
          {{ diff.old || '(空)' }}
        </div>
        <div class="new-val w-1/2 p-2 bg-green-50 text-green-700 rounded break-all">
          <span class="tag">变更后：</span>
          {{ diff.new || '(空)' }}
        </div>
      </div>
    </div>
    <div v-if="Object.keys(diffData).length === 0" class="text-gray-400 text-center">无内容变更</div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue';

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
</script>

<style scoped>
.tag {
  font-weight: bold;
  display: block;
  margin-bottom: 4px;
  opacity: 0.7;
}
</style>
