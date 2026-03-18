Owner: Codex
Task: editorial-manuscript-rebuild
Status: Ready For Parallel Coding
Updated: 2026-03-17

# `gxpublish-brain` 审校模块替换式重构 Implementation Plan

> For Claude: REQUIRED SUB-SKILL: Use `superpowers:executing-plans` to implement this plan task-by-task.

**Goal:** 在不引入 `pub-nexus` 运行时依赖、且允许不兼容旧流程实例的前提下，把 `gxpublish-brain` 审校模块从“单体页 + 粗粒度契约 + 漂移 flow/sql 基线”重构为“可分层、可验证、可回滚”的新实现。

**Architecture:** 保留 `brain-editorial`、现有四张业务表主干、TUS 上传、workflow 事件接入与权限框架；替换页面 IA、API 契约、flow 节点语义和 SQL 迁移方式。`pub-nexus` 只提供结构原则和交互约束，不作为代码复制源。

**Tech Stack:** Spring Boot 3 / MyBatis-Plus / 自定义 workflow JSON / Vue 3 / TypeScript / Element Plus / TUS upload / MySQL 增量 SQL。

**Execution Rule:** 本计划已按 `ADR-0002_editorial-manuscript-rebuild-6gate-baseline.md` 收口。若计划条目与 ADR 冲突，以 ADR 为准；本计划不再重开 6-gate 讨论。

---

## Phase 0: 基线收口与契约冻结

**Goal:** 把已生效的 `ADR-0002` 转成可执行口径；本阶段由主控完成，Worker A / B 只读不改。

**Status:** Completed By Controller

**Affected Files:**
- Read Only: `doc/agent/editorial-manuscript-rebuild_20260317-132339/decisions/ADR-0002_editorial-manuscript-rebuild-6gate-baseline.md`
- Read Only: `doc/agent/editorial-manuscript-rebuild_20260317-132339/handoffs/20260317_editorial-manuscript-dual-codex-shared_handoff_v01.md`
- Modify: `doc/agent/editorial-manuscript-rebuild_20260317-132339/plans/20260317_editorial-manuscript-rebuild-implementation_plan_v01.md`

**Risk:**
- 若不先收口 plan，worker 会沿用过期 draft，把已锁定 gate 重新带回实现阶段。
- 若仍把内置 `editorial_review.sql` 当迁移事实源，会和 ADR 的增量 SQL 入口发生冲突。

**Verification:**
- 静态核对本计划与 `ADR-0002`：单 `flowCode + processType`、freeze 仅锁 hook 边界、`Brain/script/sql/update/` 为唯一迁移入口、`reviewEdit.vue` 仅作 fallback。
- 确认 worker 启动顺序与共享 handoff 一致。

**Rollback:**
- 回退本计划到收口前版本。

## Phase 1: Backend 契约拆分与查询模型重构

**Goal:** 把现有单一 `EditorialReviewVo` 和粗粒度接口拆成台账契约、详情契约和显式动作契约。

**Affected Files:**
- Modify: `Brain/brain-modules/brain-editorial/src/main/java/com/gxpublish/brain/editorial/controller/EditorialReviewController.java`
- Modify: `Brain/brain-modules/brain-editorial/src/main/java/com/gxpublish/brain/editorial/service/IEditorialReviewService.java`
- Modify: `Brain/brain-modules/brain-editorial/src/main/java/com/gxpublish/brain/editorial/service/impl/EditorialReviewServiceImpl.java`
- Modify: `Brain/brain-modules/brain-editorial/src/main/resources/mapper/editorial/EditorialReviewMapper.xml`
- Modify: `Brain/brain-modules/brain-editorial/src/main/java/com/gxpublish/brain/editorial/domain/bo/EditorialReviewBo.java`
- Modify: `Brain/brain-modules/brain-editorial/src/main/java/com/gxpublish/brain/editorial/domain/vo/EditorialReviewVo.java`
- Create: `Brain/brain-modules/brain-editorial/src/main/java/com/gxpublish/brain/editorial/domain/vo/EditorialReviewPageItemVo.java`
- Create: `Brain/brain-modules/brain-editorial/src/main/java/com/gxpublish/brain/editorial/domain/vo/EditorialReviewDetailVo.java`
- Create: `Brain/brain-modules/brain-editorial/src/main/java/com/gxpublish/brain/editorial/domain/bo/EditorialReviewResubmitBo.java`
- Create: `Brain/brain-modules/brain-editorial/src/main/java/com/gxpublish/brain/editorial/domain/bo/EditorialReviewFreezeBo.java`

**Risk:**
- 若 page/detail 不拆分，前端 IA 重构会继续受旧 VO 污染。
- 若一次性大改所有接口命名，容易造成前后端同步窗口过大。

**Verification:**
- Controller/VO/Mapper 静态走查，确认列表接口不再返回详情负载。
- 模块编译通过。
- 新旧接口迁移表在任务包 report 中可追踪。

**Rollback:**
- 先保留旧接口路径与旧 VO，逐步切换；如出问题可退回到旧 controller/service 方法。

## Phase 2: Backend 业务动作与 workflow 同步重构

**Goal:** 重建 `submit / resubmit / history / freezeHook / process sync` 的职责边界，使 flow 节点与状态机一一对应。

**Affected Files:**
- Modify: `Brain/brain-modules/brain-editorial/src/main/java/com/gxpublish/brain/editorial/service/impl/EditorialReviewServiceImpl.java`
- Modify: `Brain/brain-modules/brain-editorial/src/main/java/com/gxpublish/brain/editorial/enums/ReviewStatusEnum.java`
- Modify: `Brain/brain-modules/brain-editorial/src/main/resources/flow/editorial_review_flow.json`
- Optional Create: `Brain/brain-modules/brain-editorial/src/main/java/com/gxpublish/brain/editorial/service/EditorialReviewWorkflowSyncService.java`
- Optional Create: `Brain/brain-modules/brain-editorial/src/main/java/com/gxpublish/brain/editorial/service/EditorialReviewFreezeHookService.java`

**Risk:**
- 若把 freeze 扩写成独立快照持久化首批硬目标，会重新偏离 `ADR-0002`。
- 若 workflow 事件和业务历史混写在同一个方法里，替换式重构后仍会留下旧耦合。

**Verification:**
- 目标模块单测覆盖：
  - 提交初始化
  - `10 -> 20 -> 30 -> 40`
  - 退回 / 撤销 / 终止
  - resubmit
  - freezeHook 前置调用
- 针对 `ProcessEvent` / `ProcessTaskEvent` 增加事件级断言。

**Rollback:**
- 新增 service 采用增量接线，出现问题可切回旧 `ServiceImpl` 逻辑。
- `freezeHook` 作为可回退边界，如不稳定可先降级为最小 hook 实现。

## Phase 3: Frontend 信息架构拆分

**Goal:** 从单体 `reviewEdit.vue` 切换到“列表台账 + 业务表单页 + 详情审批壳”的前端结构。

**Affected Files:**
- Modify: `plus-ui-ts/src/views/editorial/review/index.vue`
- Modify: `plus-ui-ts/src/router/index.ts`
- Modify: `plus-ui-ts/src/api/editorial/review.ts`
- Create: `plus-ui-ts/src/views/editorial/review/create.vue`
- Create: `plus-ui-ts/src/views/editorial/review/detail.vue`
- Create: `plus-ui-ts/src/views/editorial/review/components/ReviewFormFields.vue`
- Create: `plus-ui-ts/src/views/editorial/review/model.ts`
- Create: `plus-ui-ts/src/views/editorial/review/integration.ts`
- Retire later: `plus-ui-ts/src/views/editorial/review/reviewEdit.vue`

**Risk:**
- 若直接在旧页上叠新交互，后续难以切断旧路径。
- 若过早物理删除 `reviewEdit.vue`，调试与回滚窗口会太小。

**Verification:**
- 页面路由静态检查通过。
- create/detail 页面都能独立获取数据并渲染。
- 定向 `eslint` / `build:prod` 通过。

**Rollback:**
- 先保留旧 `reviewEdit.vue` 路由为内部兜底入口。
- 只有当新 `create/detail` 稳定后，再移除旧页。

## Phase 4: 前端审批前 hook 与共享流程组件改造

**Goal:** 在不迁入 `pub-nexus` 大体量审批按钮实现的前提下，让本仓共享流程组件支持业务前置 hook。

**Affected Files:**
- Modify: `plus-ui-ts/src/components/Process/approvalButton.vue`
- Modify: `plus-ui-ts/src/components/Process/submitVerify.vue`
- Modify: `plus-ui-ts/src/views/editorial/review/detail.vue`
- Modify: `plus-ui-ts/src/views/editorial/review/integration.ts`
- Modify: `plus-ui-ts/src/api/editorial/review.ts`

**Risk:**
- 共享组件改造会影响其他 workflow 页。
- 若 hook 设计成 editorial 专属硬编码，会污染通用组件。

**Verification:**
- 以可选回调或可选 promise hook 的方式接入，确保非 editorial 页面不受影响。
- 本模块场景验证：
  - 提交前 hook
  - 审批前 hook
  - 退回前 hook

**Rollback:**
- 通用组件 hook 设计为可选；如不稳定，可关闭 editorial 调用方接线，不必回退整个组件。

## Phase 5: SQL / 菜单 / 权限收口

**Goal:** 让数据库与权限配置跟上新模块边界，但保持 `Brain/script/sql/update/` 为当前唯一迁移事实入口，并避免把未拍板内容混写进业务表 DDL。

**Affected Files:**
- Create: `Brain/script/sql/update/20260317_editorial_review_rebuild.sql`
- Create: `Brain/script/sql/update/20260317_editorial_review_rebuild_rollback.sql`
- Create: `Brain/script/sql/update/20260317_editorial_review_menu.sql`
- Create: `Brain/script/sql/update/20260317_editorial_review_menu_rollback.sql`
- Modify: permission annotations in `EditorialReviewController.java`

**Risk:**
- 菜单 / 权限若提前和业务 DDL 混写，后续回滚和审计会混乱。
- 若前端页面路径未最终锁定就先写菜单 SQL，会形成“菜单存在但页面未就绪”的假完成。
- 若把模块内置 `editorial_review.sql` 当本轮事实源，会破坏迁移入口唯一性。

**Verification:**
- 菜单 / 权限 SQL 与业务表 SQL 独立文件存在。
- Controller 权限注解与前端按钮权限一致。
- 页面路径与菜单 component/path 一致。
- 内置 `editorial_review.sql` 不作为本轮必须同步项；仅在增量脚本稳定后再回刷样例。

**Rollback:**
- 独立回滚菜单 SQL，不影响业务表结构。
- 业务表与菜单脚本分开回退。

## Phase 6: 定向验证与运行时闭环

**Goal:** 用“目标模块定向验证 + 仓库 baseline 噪声隔离 + 运行时闭环证据”完成验收。

**Affected Files:**
- Modify: `doc/agent/editorial-manuscript-rebuild_20260317-132339/reports/20260317_editorial-manuscript-rebuild-verification_report_v01.md`
- Reuse: `.agent-temp/` 下的 API / 浏览器取证产物

**Risk:**
- 如果只跑仓库全量检查，会被既有噪声淹没。
- 如果没有浏览器级证据，容易再次把“代码 ready”误报为“真实联调 ready”。

**Verification:**
- Backend:
  - 定向单测 / 编译
  - 提交、审批、退回、撤销、终止、resubmit、freeze 链路
- Frontend:
  - 定向 `eslint`
  - 定向构建
  - 列表 / create / detail / approval 壳交互
- Runtime:
  - backend ready
  - 真实 HTTP
  - frontend 登录态
  - 浏览器级操作证据

**Rollback:**
- 以 phase 为单位回退，不做一次性全量回滚。
- 优先回退最近切入的契约/页面/flow 改动。

## 实施顺序要求

1. Phase 0 已完成；worker 从 Phase 1 以后开始执行。
2. Phase 1 与 Phase 2 完成前，不建议开始前端路由切换。
3. Phase 3 完成后先保留旧 `reviewEdit.vue` 作为过渡入口，不要立即删除。
4. Phase 4 完成后再落菜单 / 权限 SQL。
5. 只有拿到 Phase 6 的运行时闭环证据，才能宣称本轮替换式重构“完成”。
6. Worker 不得在 `main` 或脏共享工作树上直接执行本计划；必须先由主控创建隔离 worktree。

## 当前推荐执行口径

1. 优先级最高：
   - Flow / 状态 / SQL 基线收口
   - Backend 契约拆分
2. 第二优先级：
   - Frontend 页面分层
   - 审批前 hook
3. 最后执行：
   - 菜单 / 权限 SQL
   - 全链路验收

## 备注

1. 本计划默认继续使用 `gxpublish-brain` 现有 `workflow` 与 `editorial` 技术基线。
2. 本计划默认不兼容旧流程实例，不再为旧 nodeCode / 旧页面状态兜底。
3. 若后续会话直接执行本计划，建议先从本任务包 handoff 恢复，再逐 phase 推进。 
