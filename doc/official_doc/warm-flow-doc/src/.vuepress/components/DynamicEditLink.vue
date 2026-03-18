<template xmlns="http://www.w3.org/1999/html">
  <el-link
      :href="dynamicHref"
      target="_blank"
      class="warm-edit removeAfter"
  >
    <el-icon><img src="/icons/gitee_home.svg" alt="编辑图标"></el-icon>
    <span>编辑此页</span>
  </el-link>
</template>

<script setup>
import { computed } from 'vue';

// 获取当前页面路径并替换 .html => .md
const currentPageUrl = computed(() => {
  if (typeof window === 'undefined') return '';
  const url = window.location.href;
  const baseUrl = url.split('#')[0]; // 去掉锚点
  const path = new URL(baseUrl, location.origin).pathname; // 获取路径部分
  return path.replace(/\.html$/, '.md'); // 替换 .html => .md
});

// 构建最终链接
const dynamicHref = computed(() => {
  const baseHref = 'https://gitee.com/warm_4/warm-flow-doc/edit/main/src';
  if (!currentPageUrl.value) return baseHref;
  return baseHref + currentPageUrl.value;
});
</script>

<style scoped>
.warm-edit {
  display: inline-flex;
  align-items: center;
  color: #1E90FF; /* 链接颜色 */
  text-decoration: none;
  padding: 5px 10px;
  border-radius: 4px;
  transition: background-color 0.3s, color 0.3s;
  margin-top: 20px;
}

.warm-edit:hover {
  color: indianred;
}

.warm-edit img {
  margin-right: 5px;
  width: 16px;
  height: 16px;
}

.removeAfter::after {
  content: none !important; /* 移除伪元素内容 */
}
</style>
