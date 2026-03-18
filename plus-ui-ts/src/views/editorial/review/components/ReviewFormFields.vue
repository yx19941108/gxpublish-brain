<template>
  <el-form-item label="流程类型" prop="processType">
    <el-radio-group v-if="!readonly" v-model="form.processType" :disabled="Boolean(form.id)">
      <el-radio label="AUDIT">审核流程</el-radio>
      <el-radio label="PROOFREAD">校验流程</el-radio>
    </el-radio-group>
    <el-tag v-else :type="processTypeMeta.type">{{ processTypeMeta.label }}</el-tag>
  </el-form-item>

  <el-form-item label="申请标题" prop="title">
    <el-input v-if="!readonly" v-model="form.title" placeholder="请输入标题" />
    <div v-else class="text-sm leading-6 text-[var(--el-text-color-primary)]">{{ form.title || '-' }}</div>
  </el-form-item>

  <el-form-item label="申请内容" prop="content">
    <el-input v-if="!readonly" v-model="form.content" type="textarea" :rows="6" placeholder="请输入内容" />
    <div v-else class="whitespace-pre-wrap text-sm leading-6 text-[var(--el-text-color-primary)]">{{ form.content || '-' }}</div>
  </el-form-item>

  <el-form-item label="部门" prop="deptId">
    <el-tree-select
      v-if="!readonly"
      v-model="form.deptId"
      :data="deptOptions"
      :props="{ value: 'deptId', label: 'deptName', children: 'children' }"
      value-key="deptId"
      placeholder="请选择部门"
      check-strictly
    />
    <div v-else class="text-sm leading-6 text-[var(--el-text-color-primary)]">{{ form.deptName || '-' }}</div>
  </el-form-item>

  <el-form-item label="附件" prop="attachmentOssId">
    <TusUpload
      v-model="form.attachmentOssId"
      v-model:fileName="form.attachmentFileName"
      v-model:fileUrl="form.attachmentFileUrl"
      v-model:fileSize="form.attachmentFileSize"
      :version="form.attachmentVersion || 0"
      :disabled="readonly"
    />
  </el-form-item>

  <el-form-item label="关联链接">
    <div class="w-full">
      <template v-if="form.linkList && form.linkList.length > 0">
        <div v-for="(link, index) in form.linkList" :key="link.id ?? index" class="mb-2 flex items-center">
          <el-input v-if="!readonly" v-model="link.description" placeholder="描述" class="mr-2 w-1/3" />
          <div v-else class="mr-2 w-1/3 text-sm text-[var(--el-text-color-primary)]">{{ link.description || '-' }}</div>

          <el-input v-if="!readonly" v-model="link.url" placeholder="URL地址 (https://...)" class="mr-2 w-1/2" />
          <div v-else class="mr-2 w-1/2 truncate text-sm text-[var(--el-text-color-primary)]">{{ link.url || '-' }}</div>

          <el-button v-if="!readonly" type="danger" icon="Delete" circle @click="removeLink(index)" />
          <el-link v-else type="primary" :href="link.url" target="_blank" :underline="false">打开</el-link>
        </div>
      </template>
      <div v-else class="text-sm text-[var(--el-text-color-secondary)]">暂无关联链接</div>

      <el-button v-if="!readonly" type="primary" plain icon="Plus" @click="addLink">添加链接</el-button>
    </div>
  </el-form-item>

  <el-form-item label="备注">
    <el-input v-if="!readonly" v-model="form.remark" type="textarea" placeholder="请输入备注" />
    <div v-else class="whitespace-pre-wrap text-sm leading-6 text-[var(--el-text-color-primary)]">{{ form.remark || '-' }}</div>
  </el-form-item>
</template>

<script setup lang="ts">
import { computed, type PropType } from 'vue';

import type { DeptVO } from '@/api/system/dept/types';

import { getReviewProcessTypeMeta } from '../integration';
import type { ReviewFormModel } from '../model';
import TusUpload from './TusUpload.vue';

const props = defineProps({
  form: {
    type: Object as PropType<ReviewFormModel>,
    required: true
  },
  readonly: {
    type: Boolean,
    default: false
  },
  deptOptions: {
    type: Array as PropType<DeptVO[]>,
    default: () => []
  }
});

const form = props.form;

const processTypeMeta = computed(() => getReviewProcessTypeMeta(form.processType));

const addLink = () => {
  if (!form.linkList) {
    form.linkList = [];
  }
  form.linkList.push({
    id: null,
    url: '',
    description: ''
  });
};

const removeLink = (index: number) => {
  form.linkList.splice(index, 1);
};
</script>
