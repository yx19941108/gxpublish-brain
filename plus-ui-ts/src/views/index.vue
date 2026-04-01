<template>
  <div class="home-page">
    <section class="hero-shell">
      <article class="hero-card">
        <div class="hero-head">
          <div>
            <p class="hero-kicker">业务工作台</p>
            <h1 class="hero-title">广西数智出版传媒有限公司出版业务系统首页</h1>
            <p class="hero-subtitle">围绕出版业务协同与稿件审校管理，提供统一、清晰的业务入口，帮助用户快速进入常用工作页面，提升日常处理效率。</p>
          </div>
          <div class="meta-pill">首页</div>
        </div>
      </article>
    </section>

    <section class="content-grid">
      <article class="surface-card">
        <div class="section-head">
          <div>
            <h2 class="section-title">业务模块</h2>
            <p class="section-desc">根据当前账号实际拥有的审校菜单展示业务模块，点击后直接进入对应菜单页面。</p>
          </div>
        </div>

        <div v-if="visibleModules.length > 0" class="entry-grid">
          <article v-for="module in visibleModules" :key="module.key" class="entry-card">
            <div class="entry-top">
              <div class="entry-icon">{{ module.glyph }}</div>
              <div class="entry-status">{{ module.badge }}</div>
            </div>
            <h3>{{ module.title }}</h3>
            <p>{{ module.description }}</p>
            <div class="entry-meta">
              <el-button class="entry-button" type="primary" @click="handleEnter(module.routePath)">进入模块</el-button>
            </div>
          </article>
        </div>

        <el-empty v-else class="empty-state" description="当前账号暂无可访问的审校业务模块，请联系管理员授权相关菜单。" />
      </article>
    </section>
  </div>
</template>

<script setup name="Index" lang="ts">
import { computed } from 'vue';
import { useRouter } from 'vue-router';
import { usePermissionStore } from '@/store/modules/permission';
import { resolveAccessibleHomeModules } from './index-home.modules';

const router = useRouter();
const permissionStore = usePermissionStore();

const visibleModules = computed(() => resolveAccessibleHomeModules(permissionStore.getSidebarRoutes()));

const handleEnter = (path: string) => {
  void router.push({ path });
};
</script>

<style lang="scss" scoped>
.home-page {
  --panel: rgba(255, 255, 255, 0.96);
  --line: #dbe6f4;
  --text: #23364d;
  --text-2: #627891;
  --brand: #2f74ff;
  --brand-deep: #2e68da;
  --brand-soft: #eef4ff;
  --shadow-lg: 0 18px 48px rgba(23, 53, 95, 0.12);
  --shadow-md: 0 12px 28px rgba(25, 54, 96, 0.08);
  --radius-xl: 26px;
  --radius-lg: 18px;
  --sans: 'Microsoft YaHei UI', 'Microsoft YaHei', 'PingFang SC', 'Noto Sans SC', sans-serif;

  position: relative;
  display: flex;
  flex-direction: column;
  box-sizing: border-box;
  height: calc(100vh - 84px);
  padding: 18px 20px 28px;
  overflow: hidden;
  font-family: var(--sans);
  background: linear-gradient(180deg, #eef4fc 0%, #f6f9fe 100%);
}

.home-page::before,
.home-page::after {
  position: absolute;
  width: 300px;
  height: 300px;
  border-radius: 50%;
  opacity: 0.2;
  content: '';
  pointer-events: none;
}

.home-page::before {
  top: -64px;
  right: -120px;
  background: radial-gradient(circle, rgba(47, 116, 255, 0.16), transparent 72%);
}

.home-page::after {
  bottom: -120px;
  left: -90px;
  background: radial-gradient(circle, rgba(47, 116, 255, 0.12), transparent 72%);
}

.hero-shell,
.content-grid {
  position: relative;
  z-index: 1;
  width: 100%;
  max-width: 1520px;
  margin: 0 auto;
}

.hero-card,
.surface-card,
.entry-card {
  border: 1px solid var(--line);
  border-radius: var(--radius-xl);
  background: var(--panel);
  box-shadow: var(--shadow-lg);
}

.hero-card {
  position: relative;
  min-height: 304px;
  padding: 28px 34px 30px;
  overflow: hidden;
  color: #fff;
  background: linear-gradient(135deg, #2d65d6 0%, #4d84f3 60%, #82b2fa 100%);
}

.hero-card::before,
.hero-card::after {
  position: absolute;
  pointer-events: none;
  border: 1px solid rgba(255, 255, 255, 0.12);
  border-radius: 26px;
  content: '';
}

.hero-card::before {
  right: 82px;
  top: 26px;
  width: 134px;
  height: 134px;
  background: linear-gradient(180deg, rgba(255, 255, 255, 0.12) 0%, rgba(255, 255, 255, 0.05) 100%);
  transform: rotate(10deg);
}

.hero-card::after {
  right: 28px;
  top: 42px;
  width: 168px;
  height: 220px;
  background: linear-gradient(180deg, rgba(255, 255, 255, 0.13) 0%, rgba(255, 255, 255, 0.04) 100%);
  transform: rotate(-11deg);
}

.hero-head {
  position: relative;
  z-index: 1;
  display: flex;
  justify-content: space-between;
  gap: 16px;
  align-items: flex-start;
}

.hero-kicker {
  display: none;
}

.hero-title {
  max-width: 620px;
  margin: 16px 0 24px;
  color: #fff;
  font-size: 40px;
  line-height: 1.24;
  font-weight: 800;
  letter-spacing: 0.01em;
}

.hero-subtitle {
  max-width: 700px;
  margin: 0;
  color: rgba(255, 255, 255, 0.86);
  font-size: 16px;
  line-height: 1.75;
}

.meta-pill {
  position: relative;
  z-index: 1;
  display: inline-flex;
  align-items: center;
  min-height: 40px;
  padding: 0 16px;
  border: 1px solid rgba(255, 255, 255, 0.18);
  border-radius: 999px;
  background: rgba(255, 255, 255, 0.14);
  color: #fff;
  font-size: 14px;
  font-weight: 700;
}

.content-grid {
  display: flex;
  flex: 1;
  min-height: 0;
  margin-top: 18px;
}

.surface-card {
  display: flex;
  flex: 1;
  flex-direction: column;
  min-height: 0;
  padding: 20px 20px 12px;
  background: linear-gradient(180deg, rgba(255, 255, 255, 0.98) 0%, rgba(252, 253, 255, 0.98) 100%);
}

.section-head {
  display: flex;
  align-items: flex-start;
  margin-bottom: 18px;
}

.section-title {
  margin: 0;
  color: var(--text);
  font-size: 26px;
  font-weight: 800;
}

.section-desc {
  margin: 6px 0 0;
  color: var(--text-2);
  font-size: 14px;
  line-height: 1.75;
}

.entry-grid {
  display: grid;
  flex: 1;
  min-height: 0;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 12px;
  align-content: start;
  overflow: auto;
  padding-right: 6px;
}

.entry-grid::-webkit-scrollbar {
  width: 8px;
}

.entry-grid::-webkit-scrollbar-thumb {
  border-radius: 999px;
  background: rgba(47, 116, 255, 0.22);
}

.entry-grid::-webkit-scrollbar-track {
  background: transparent;
}

.entry-card {
  position: relative;
  display: flex;
  flex-direction: column;
  min-height: 252px;
  padding: 16px 16px 14px;
  overflow: hidden;
  background: linear-gradient(180deg, #ffffff 0%, #fbfcff 100%);
  box-shadow: var(--shadow-md);
}

.entry-card::before,
.entry-card::after {
  position: absolute;
  top: 12px;
  right: 16px;
  width: 38px;
  height: 38px;
  border: 2px solid rgba(47, 116, 255, 0.15);
  border-radius: 12px;
  content: '';
}

.entry-card::before {
  transform: rotate(7deg);
}

.entry-card::after {
  top: 16px;
  right: 22px;
  width: 32px;
  height: 32px;
  transform: rotate(-8deg);
}

.entry-top {
  display: flex;
  justify-content: space-between;
  gap: 8px;
  align-items: flex-start;
}

.entry-icon {
  display: inline-flex;
  width: 46px;
  height: 46px;
  justify-content: center;
  align-items: center;
  border-radius: 16px;
  color: #fff;
  font-size: 24px;
  font-weight: 700;
  background: linear-gradient(180deg, #3577ff 0%, #2f6df0 100%);
  box-shadow: 0 12px 24px rgba(47, 116, 255, 0.2);
}

.entry-status {
  display: none;
}

.entry-card h3 {
  margin: 20px 0 10px;
  color: var(--text);
  font-size: 22px;
  font-weight: 800;
  line-height: 1.4;
}

.entry-card p {
  min-height: 76px;
  margin: 0;
  color: var(--text-2);
  font-size: 13px;
  line-height: 1.75;
}

.entry-meta {
  display: flex;
  margin-top: auto;
  padding-top: 18px;
}

.entry-button {
  min-height: 38px;
  padding: 0 18px;
  border: 1px solid rgba(47, 116, 255, 0.12);
  border-radius: 999px;
  background: linear-gradient(180deg, #2f74ff 0%, #2267ef 100%);
  box-shadow: 0 10px 20px rgba(47, 116, 255, 0.18);
}

.empty-state {
  min-height: 220px;
  border: 1px dashed var(--line);
  border-radius: var(--radius-lg);
  background: rgba(255, 255, 255, 0.68);
}

@media (max-width: 1366px) {
  .hero-title {
    max-width: 560px;
    font-size: 34px;
    margin-bottom: 20px;
  }

  .hero-subtitle {
    max-width: 580px;
    font-size: 15px;
  }
}

@media (max-width: 960px) {
  .home-page {
    height: calc(100vh - 84px);
    padding: 16px;
  }

  .hero-card {
    min-height: auto;
    padding: 22px 20px;
  }

  .hero-card::before,
  .hero-card::after {
    display: none;
  }

  .hero-head {
    flex-direction: column;
  }

  .hero-title {
    max-width: none;
    margin-top: 0;
    font-size: 24px;
  }

  .hero-subtitle {
    font-size: 13px;
  }

  .surface-card {
    padding: 18px 16px 14px;
  }

  .entry-grid {
    grid-template-columns: 1fr;
  }

  .entry-card {
    min-height: 220px;
  }

  .entry-card h3 {
    font-size: 22px;
  }

  .entry-card p {
    min-height: 0;
    font-size: 14px;
  }
}
</style>
