<template>
  <div class="home-page">
    <section class="hero-shell">
      <article class="hero-card">
        <div class="hero-head">
          <div>
            <h1 class="hero-title">广西数智出版传媒有限公司<br />出版业务系统首页</h1>
            <p class="hero-subtitle">
              围绕出版业务协同与稿件审校管理，提供统一、清晰的业务入口，帮助用户快速进入常用工作页面，提升日常处理效率。
            </p>
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
            <span class="entry-ornament"></span>
            <div class="entry-top">
              <div class="entry-icon">{{ module.glyph }}</div>
              <div class="entry-status">{{ module.badge }}</div>
            </div>
            <h3>{{ module.title }}</h3>
            <p>{{ module.description }}</p>
            <div class="entry-meta">
              <el-button class="entry-button" type="primary" round @click="handleEnter(module.routePath)">点击进入</el-button>
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
  --bg: #edf2f8;
  --panel: rgba(255, 255, 255, 0.92);
  --line: #dbe4ef;
  --text: #1b2b3d;
  --text-2: #52657a;
  --brand: #2f74ff;
  --brand-deep: #1658d4;
  --shadow-lg: 0 24px 60px rgba(12, 34, 59, 0.12);
  --shadow-md: 0 12px 28px rgba(16, 33, 57, 0.08);
  --radius-xl: 28px;
  --radius-lg: 20px;
  --sans: 'Microsoft YaHei UI', 'PingFang SC', 'Noto Sans SC', sans-serif;
  --serif: 'Source Han Serif SC', 'Noto Serif SC', 'Songti SC', 'STSong', serif;

  position: relative;
  min-height: calc(100vh - 84px);
  padding: 24px 24px 36px;
  overflow: hidden;
  font-family: var(--sans);
  background:
    radial-gradient(circle at top left, rgba(47, 116, 255, 0.14), transparent 30%),
    radial-gradient(circle at top right, rgba(47, 116, 255, 0.1), transparent 24%),
    linear-gradient(180deg, #edf3fb 0%, #f8fbff 100%);
}

.home-page::before,
.home-page::after {
  position: absolute;
  width: 320px;
  height: 320px;
  border-radius: 50%;
  opacity: 0.28;
  content: '';
  pointer-events: none;
}

.home-page::before {
  top: 88px;
  right: -48px;
  background: radial-gradient(circle, rgba(47, 116, 255, 0.2), transparent 70%);
}

.home-page::after {
  bottom: 12px;
  left: -96px;
  background: radial-gradient(circle, rgba(47, 116, 255, 0.12), transparent 72%);
}

.hero-shell,
.content-grid {
  position: relative;
  z-index: 1;
}

.hero-card,
.surface-card,
.entry-card {
  overflow: hidden;
  border: 1px solid rgba(219, 228, 239, 0.85);
  border-radius: var(--radius-xl);
  background: var(--panel);
  box-shadow: var(--shadow-lg);
  backdrop-filter: blur(14px);
}

.hero-card {
  position: relative;
  min-height: 360px;
  padding: 38px 38px 34px;
  color: #fff;
  background:
    linear-gradient(135deg, rgba(22, 88, 212, 0.94) 0%, rgba(47, 116, 255, 0.9) 56%, rgba(107, 170, 255, 0.84) 100%),
    radial-gradient(circle at 80% 20%, rgba(255, 255, 255, 0.18), transparent 18%);
}

.hero-card::before,
.hero-card::after {
  position: absolute;
  border: 1px solid rgba(255, 255, 255, 0.16);
  content: '';
}

.hero-card::before {
  right: 92px;
  bottom: 30px;
  width: 286px;
  height: 286px;
  border-radius: 32px;
  background: linear-gradient(180deg, rgba(255, 255, 255, 0.18) 0%, rgba(255, 255, 255, 0.06) 100%);
  transform: rotate(-10deg);
}

.hero-card::after {
  top: 52px;
  right: 178px;
  width: 178px;
  height: 178px;
  border-radius: 28px 28px 40px 28px;
  background: linear-gradient(180deg, rgba(255, 255, 255, 0.18) 0%, rgba(255, 255, 255, 0.06) 100%);
  transform: rotate(11deg);
}

.hero-head {
  position: relative;
  z-index: 1;
  display: flex;
  justify-content: space-between;
  gap: 16px;
  align-items: flex-start;
}

.hero-title {
  margin: 24px 0 16px;
  font-family: var(--serif);
  font-size: 54px;
  line-height: 1.1;
  font-weight: 700;
  letter-spacing: 0.02em;
}

.hero-subtitle {
  max-width: 860px;
  margin: 0;
  color: rgba(255, 255, 255, 0.84);
  font-size: 20px;
  line-height: 1.75;
}

.meta-pill {
  position: relative;
  z-index: 1;
  display: inline-flex;
  align-items: center;
  min-height: 46px;
  padding: 0 18px;
  border: 1px solid rgba(255, 255, 255, 0.18);
  border-radius: 999px;
  background: rgba(255, 255, 255, 0.12);
  font-size: 15px;
  font-weight: 700;
}

.content-grid {
  margin-top: 24px;
}

.surface-card {
  padding: 28px 28px 42px;
}

.section-head {
  display: flex;
  justify-content: space-between;
  gap: 12px;
  align-items: flex-start;
  margin-bottom: 26px;
}

.section-title {
  margin: 0;
  color: var(--text);
  font-size: 32px;
  font-weight: 800;
}

.section-desc {
  margin: 8px 0 0;
  color: var(--text-2);
  font-size: 14px;
  line-height: 1.75;
}

.entry-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 20px;
}

.entry-card {
  position: relative;
  display: flex;
  flex-direction: column;
  min-height: 310px;
  padding: 28px 24px 26px;
  background: linear-gradient(180deg, rgba(255, 255, 255, 0.97) 0%, rgba(250, 252, 255, 0.94) 100%);
}

.entry-top {
  display: flex;
  justify-content: space-between;
  gap: 12px;
  align-items: flex-start;
}

.entry-icon {
  display: inline-flex;
  width: 62px;
  height: 62px;
  justify-content: center;
  align-items: center;
  border-radius: 20px;
  color: #fff;
  font-size: 28px;
  font-weight: 900;
  background: linear-gradient(135deg, #2f74ff 0%, #2f74ff 100%);
  box-shadow: var(--shadow-md);
}

.entry-status {
  display: none;
}

.entry-card h3 {
  margin: 26px 0 14px;
  color: var(--text);
  font-size: 32px;
  font-weight: 800;
}

.entry-card p {
  min-height: 100px;
  margin: 0;
  color: var(--text-2);
  font-size: 18px;
  line-height: 1.85;
}

.entry-meta {
  display: flex;
  margin-top: auto;
  padding-top: 28px;
}

.entry-button {
  min-height: 48px;
  padding: 0 22px;
  border: 1px solid rgba(47, 116, 255, 0.14);
  background: linear-gradient(180deg, #2f74ff 0%, #1f66f2 100%);
  box-shadow: 0 10px 24px rgba(47, 116, 255, 0.18);
}

.entry-ornament {
  position: absolute;
  top: 20px;
  right: 20px;
  width: 66px;
  height: 66px;
  opacity: 0.14;
}

.entry-ornament::before,
.entry-ornament::after {
  position: absolute;
  border: 2px solid var(--brand);
  border-radius: 14px;
  content: '';
}

.entry-ornament::before {
  inset: 8px 12px 10px 8px;
  transform: rotate(8deg);
}

.entry-ornament::after {
  inset: 0;
  transform: rotate(-8deg);
}

.empty-state {
  min-height: 260px;
  border: 1px dashed var(--line);
  border-radius: var(--radius-lg);
  background: rgba(255, 255, 255, 0.68);
}

@media (max-width: 1366px) {
  .entry-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }

  .hero-title {
    font-size: 44px;
  }

  .hero-subtitle {
    max-width: 720px;
    font-size: 18px;
  }
}

@media (max-width: 960px) {
  .home-page {
    min-height: auto;
    padding: 18px 16px 28px;
  }

  .hero-card {
    min-height: auto;
    padding: 28px 22px;
  }

  .hero-card::before,
  .hero-card::after {
    display: none;
  }

  .hero-head {
    flex-direction: column;
  }

  .hero-title {
    margin-top: 0;
    font-size: 34px;
  }

  .hero-subtitle {
    font-size: 16px;
    line-height: 1.8;
  }

  .surface-card {
    padding: 22px 18px 26px;
  }

  .entry-grid {
    grid-template-columns: 1fr;
  }

  .entry-card {
    min-height: 0;
  }

  .entry-card h3 {
    font-size: 26px;
  }

  .entry-card p {
    min-height: 0;
    font-size: 16px;
  }
}
</style>
