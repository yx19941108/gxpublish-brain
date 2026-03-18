<template>
  <div v-show="isVisible" class="wwads-cn wwads-horizontal" data-id="349"></div>
  <div v-show="isVisible" class="between-header">
    <a class="removeAfter" href="https://gitee.com/dromara/warm-flow">
      <img src="/ggw/bewteenone.png" alt="warm-flow Logo">
    </a>
  </div>

  <div v-show="isVisible" style="position: relative; display: flex;">
    <!-- 左侧图片 -->
    <div class="between-left">
      <a class="removeAfter" href="https://gitee.com/dromara/warm-flow">
        <img src="/ggw/bewteentwo.png" alt="warm-flow Logo">
      </a>
    </div>
    <!-- 右侧图片 -->
    <div class="between-right" style="display: flex; align-items: center; justify-content: flex-end;">
      <a class="removeAfter" href="https://gitee.com/dromara/warm-flow">
        <img src="/ggw/bewteentwo.png" alt="warm-flow Logo">
      </a>
      <el-link
          :href="dynamicHref"
          target="_blank"
          class="warm-edit removeAfter"
      >
        <el-icon><img src="/icons/gitee_home.svg" alt="编辑图标"></el-icon>
        <span style="font-size: 18px">编辑此页</span>
      </el-link>
    </div>
  </div>
</template>
<script setup>
import {computed, ref} from 'vue';

const isVisible = ref(true);

function hideBanner() {
  isVisible.value = false
}

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

<style lang="scss">
/* 定义样式 */
.between-header {
  margin-bottom: 5px;
  display: flex;
  align-items: center;
}

.between-header img {
  height: 40px;
  border-radius: 4px;
}

.between-left img, .between-right img {
  height: 40px;
  margin-right: 1px;
  border-radius: 4px;
}

.between-right {
  display: flex;
  align-items: center;
  justify-content: flex-end;
  gap: 10px; /* 图片和按钮之间的间距 */
}

.wwads-horizontal {
  max-width: 793px;
  padding: 0 !important;
  min-height: 0 !important;
  align-items: center;
  .wwads-content {
    .wwads-text {
      min-height: 50px;
      display: block;
      padding: 5px;
    }
  }
  .wwads-logo {
    width: 0 !important;
  }
  .wwads-img {
    margin: 0px !important;
    height: 70px;
    img {
      width: 90px !important;
    }
  }
  .wwads-poweredby {
    width: 40px;
    position: absolute;
    right: 25px;
    bottom: 3px;
  }
  .wwads-logo-text {
      font-size: 12px !important;
  }
}

.removeAfter::after {
  content: none !important; /* 移除伪元素内容 */
}

.warm-edit {
  display: inline-flex;
  align-items: center;
  color: #1E90FF; /* 链接颜色 */
  text-decoration: none;
  padding: 5px 10px;
  border-radius: 4px;
  transition: background-color 0.3s, color 0.3s;
  margin-top: 0;
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
