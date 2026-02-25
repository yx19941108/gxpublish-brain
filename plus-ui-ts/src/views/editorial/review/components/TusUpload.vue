<template>
  <div class="tus-upload">
    <!-- 上传按钮区域 -->
    <el-upload
      v-if="!hasFile || uploading"
      ref="uploadRef"
      action=""
      :http-request="customUpload"
      :show-file-list="false"
      :accept="accept"
      :disabled="disabled || uploading"
    >
      <el-button type="primary" :disabled="disabled || uploading" :loading="uploading">
        {{ uploading ? (percentage === 100 ? '正在处理...' : `上传中 ${percentage}%`) : '选择并上传文件' }}
      </el-button>
    </el-upload>

    <!-- 上传进度条 -->
    <el-progress v-if="uploading" :percentage="percentage" :status="progressStatus" class="mt-1" />

    <!-- 上传中操作按钮（暂停/取消） -->
    <div v-if="uploading" class="mt-1 flex items-center gap-2">
      <el-button link type="warning" size="small" @click="pauseOrResume">{{ paused ? '继续' : '暂停' }}</el-button>
      <el-button link type="danger" size="small" @click="cancel">取消</el-button>
    </div>

    <!-- 文件回显卡片 -->
    <div v-if="hasFile && !uploading" class="file-card">
      <div class="file-card__info">
        <el-icon :size="20" class="file-card__icon"><Document /></el-icon>
        <div class="file-card__detail">
          <span class="file-card__name" :title="displayFileName">{{ displayFileName }}</span>
          <el-tag v-if="version" size="small" type="info" class="file-card__version">v{{ version }}</el-tag>
        </div>
      </div>
      <div class="file-card__actions">
        <el-button link type="primary" size="small" :disabled="false" @click="handlePreview">
          <el-icon class="mr-1"><View /></el-icon>预览
        </el-button>
        <el-button link type="success" size="small" :disabled="false" @click="handleDownload">
          <el-icon class="mr-1"><Download /></el-icon>下载
        </el-button>
        <el-button v-if="!disabled" link type="danger" size="small" @click="handleRemove">
          <el-icon class="mr-1"><Delete /></el-icon>移除
        </el-button>
      </div>
    </div>

    <div class="text-gray-400 text-xs mt-1" v-if="!disabled && !hasFile && !uploading">支持断点续传，不限大小</div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, watch, onBeforeUnmount } from 'vue';
import * as tus from 'tus-js-client';
import { getToken } from '@/utils/auth';
import { Document, View, Download, Delete } from '@element-plus/icons-vue';

const props = defineProps({
  /** OSS 文件 ID（双向绑定） */
  modelValue: String,
  /** 文件名（双向绑定） */
  fileName: String,
  /** 文件访问 URL（双向绑定），用于回显已有文件 */
  fileUrl: String,
  /** 文件大小（双向绑定） */
  fileSize: Number,
  /** 附件版本号 */
  version: Number,
  /** 文件类型过滤 */
  accept: {
    type: String,
    default: '*'
  },
  /** 是否禁用（查看模式） */
  disabled: Boolean,
  /** TUS 上传端点 */
  endpoint: {
    type: String,
    default: import.meta.env.VITE_APP_BASE_API + '/tus/upload'
  }
});

const emit = defineEmits(['update:modelValue', 'update:fileName', 'update:fileUrl', 'update:fileSize', 'success', 'error']);

// 上传状态
const uploading = ref(false);
const percentage = ref(0);
const progressStatus = ref<"" | "success" | "warning" | "exception">("");
const paused = ref(false);
let upload: tus.Upload | null = null;

// 文件回显状态（上传成功后或外部传入）
const innerFileUrl = ref('');
const innerFileName = ref('');

/** 当前是否有可回显的文件 */
const hasFile = computed(() => {
  return !!(props.modelValue || props.fileUrl || innerFileUrl.value);
});

/** 显示的文件名（优先使用外部 prop，其次内部状态） */
const displayFileName = computed(() => {
  return props.fileName || innerFileName.value || '未知文件';
});

/** 显示的文件 URL（优先使用外部 prop，其次内部状态） */
const displayFileUrl = computed(() => {
  return props.fileUrl || innerFileUrl.value || '';
});

// 监听外部 props 变更以同步内部状态
watch(() => props.fileUrl, (val) => {
  if (val) innerFileUrl.value = val;
});
watch(() => props.fileName, (val) => {
  if (val) innerFileName.value = val;
});

/** 预览文件（通过后端 API 获取，避免预签名 URL 过期） */
const handlePreview = () => {
  const ossId = props.modelValue;
  if (ossId) {
    // 使用后端下载 API 新窗口打开
    const baseURL = import.meta.env.VITE_APP_BASE_API;
    window.open(baseURL + '/resource/oss/download/' + ossId, '_blank');
  } else if (displayFileUrl.value) {
    window.open(displayFileUrl.value, '_blank');
  }
};

/** 下载文件（通过后端 API） */
const handleDownload = () => {
  const ossId = props.modelValue;
  if (ossId) {
    const baseURL = import.meta.env.VITE_APP_BASE_API;
    const a = document.createElement('a');
    a.href = baseURL + '/resource/oss/download/' + ossId;
    a.download = displayFileName.value;
    a.target = '_blank';
    document.body.appendChild(a);
    a.click();
    document.body.removeChild(a);
  }
};

/** 移除当前文件（允许重新上传） */
const handleRemove = () => {
  innerFileUrl.value = '';
  innerFileName.value = '';
  emit('update:modelValue', '');
  emit('update:fileName', '');
  emit('update:fileUrl', '');
  emit('update:fileSize', null);
};

/** 自定义上传逻辑（TUS 断点续传） */
const customUpload = (options: any): Promise<unknown> => {
  return new Promise((resolve, reject) => {
    const file = options.file;
    uploading.value = true;
    percentage.value = 0;
    progressStatus.value = '';
    paused.value = false;

    upload = new tus.Upload(file, {
      endpoint: props.endpoint,
      retryDelays: [0, 3000, 5000, 10000, 20000],
      headers: {
        Authorization: 'Bearer ' + getToken(),
        clientid: import.meta.env.VITE_APP_CLIENT_ID
      },
      metadata: {
        filename: file.name,
        filetype: file.type
      },
      onError: (error) => {
        console.error('上传失败: ' + error);
        progressStatus.value = 'exception';
        uploading.value = false;
        emit('error', error);
        reject(error);
      },
      onProgress: (bytesUploaded, bytesTotal) => {
        const p = ((bytesUploaded / bytesTotal) * 100).toFixed(2);
        percentage.value = Number(p);
      },
      onAfterResponse: (req, res) => {
        // 修正 Vite 代理导致 Location 丢失前缀的问题
        if (upload && upload.url && upload.url.indexOf(import.meta.env.VITE_APP_BASE_API) === -1) {
          const urlSegments = upload.url.split('/');
          const id = urlSegments[urlSegments.length - 1];
          if (id && id.length > 5) {
            upload.url = props.endpoint + '/' + id;
          }
        }
      },
      onSuccess: async () => {
        const uploadUrl = upload!.url;
        try {
          const response = await fetch(import.meta.env.VITE_APP_BASE_API + '/tus/upload/finish', {
            method: 'POST',
            headers: {
              'Content-Type': 'application/json',
              Authorization: 'Bearer ' + getToken(),
              clientid: import.meta.env.VITE_APP_CLIENT_ID
            },
            body: JSON.stringify({
              uploadUrl: uploadUrl,
              originalName: file.name,
              contentType: file.type
            })
          });
          const resData = await response.json();
          if (resData.code !== 200) {
            throw new Error(resData.msg || '上传完成回调失败');
          }
          uploading.value = false;
          progressStatus.value = 'success';

          const ossId = String(resData.data.ossId);
          const url = resData.data.url || '';
          const size = resData.data.size || resData.data.fileSize || file.size || null;

          // 更新内部回显状态
          innerFileUrl.value = url;
          innerFileName.value = file.name;

          // 向父组件同步数据
          emit('success', { ossId, fileName: file.name, url, size });
          emit('update:fileName', file.name);
          emit('update:modelValue', ossId);
          emit('update:fileUrl', url);
          emit('update:fileSize', size);

          resolve(resData.data);
        } catch (e) {
          uploading.value = false;
          console.error('上传完成回调出错:', e);
          progressStatus.value = 'exception';
          emit('error', e);
          reject(e);
        }
      }
    });

    // 尝试恢复此前的上传
    upload.findPreviousUploads().then(function (previousUploads) {
      if (previousUploads.length) {
        upload!.resumeFromPreviousUpload(previousUploads[0]);
      }
      upload!.start();
    });
  });
};

/** 暂停或继续上传 */
const pauseOrResume = () => {
  if (!upload) return;
  if (paused.value) {
    upload.start();
    paused.value = false;
  } else {
    upload.abort();
    paused.value = true;
  }
};

/** 取消上传 */
const cancel = () => {
  if (upload) {
    upload.abort();
    upload = null;
  }
  uploading.value = false;
  percentage.value = 0;
};

onBeforeUnmount(() => {
  cancel();
});
</script>

<style scoped>
.tus-upload {
  width: 100%;
}

.file-card {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 10px 14px;
  margin-top: 8px;
  background: var(--el-fill-color-lighter, #f5f7fa);
  border: 1px solid var(--el-border-color-lighter, #e4e7ed);
  border-radius: 6px;
  transition: box-shadow 0.2s;
}
.file-card:hover {
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
}

.file-card__info {
  display: flex;
  align-items: center;
  gap: 8px;
  overflow: hidden;
  flex: 1;
  min-width: 0;
}

.file-card__icon {
  color: var(--el-color-primary);
  flex-shrink: 0;
}

.file-card__detail {
  display: flex;
  align-items: center;
  gap: 6px;
  overflow: hidden;
}

.file-card__name {
  font-size: 14px;
  color: var(--el-text-color-regular);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.file-card__version {
  flex-shrink: 0;
}

.file-card__actions {
  display: flex;
  align-items: center;
  gap: 4px;
  flex-shrink: 0;
  margin-left: 12px;
}
</style>
