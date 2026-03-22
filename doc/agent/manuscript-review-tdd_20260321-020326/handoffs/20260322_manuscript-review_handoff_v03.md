Owner: Codex
Task: manuscript-review handoff
Status: Ready for WP7 continuation (SQL apply still not closed loop)
Updated: 2026-03-22 18:31 CST

# Manuscript Review Handoff v03 (Latest)

## 0. 重要声明（续接入口）

- `v02` 已过时，禁止再作为续接入口（历史留档，不再维护）：
  - `/home/kugua/.config/superpowers/worktrees/gxpublish-brain/manuscript-review-tdd-20260321/doc/agent/manuscript-review-tdd_20260321-020326/handoffs/20260322_manuscript-review_handoff_v02.md`
- 本文件 `v03` 为当前唯一续接入口。下一会话必须从 `v03 + 需求真源(PRD)` 恢复上下文。

## 1. 当前 worktree / 分支 / HEAD（可读现状）

Worktree 根路径：
`/home/kugua/.config/superpowers/worktrees/gxpublish-brain/manuscript-review-tdd-20260321`

Git 基线（2026-03-22 18:31 CST 复核）：
- 分支：`manuscript-review-tdd-20260321`
- HEAD：`23428b81a12a6aa7becea6c4657b98703a56cd26`（`23428b8 测试部分修改`）
- 工作树：Dirty（存在未提交修改与未跟踪文件）

关键复核命令：
`cd /home/kugua/.config/superpowers/worktrees/gxpublish-brain/manuscript-review-tdd-20260321 && git status --porcelain=v1`

Dirty 摘要（只列关键面，避免误判“已提交/已闭环”）：
- 已修改：`Brain/pom.xml`、`Brain/brain-modules/pom.xml`、`Brain/brain-admin/pom.xml`（新模块 wiring 已进入变更面）
- 已修改：`plus-ui-ts/package.json`、`plus-ui-ts/src/router/index.ts`（前端路由/依赖面已进入变更）
- 已修改：`plus-ui-ts/.eslintrc-auto-import.json`（高概率为自动生成或格式漂移，后续提交前要单独核对）
- 未跟踪：`Brain/brain-modules/brain-manuscript-review/`（新后端模块主体）
- 未跟踪：`Brain/script/sql/update/20260321_manuscript_review_*_v01.sql`（审校 SQL 草案）
- 未跟踪：`plus-ui-ts/src/views/manuscript-review/**`、`plus-ui-ts/src/types/manuscript-review/**`（前端模块草案）
- 未跟踪：`doc/agent/manuscript-review-tdd_20260321-020326/**`、`doc/manuscript_review/**`（需求/治理类文档）

## 2. 需求真源与硬禁边界

唯一需求真源（PRD，必须优先读）：
`/home/kugua/.config/superpowers/worktrees/gxpublish-brain/manuscript-review-tdd-20260321/doc/manuscript_review/product/20260321_manuscript-review_product-requirements_v01.md`

硬禁边界（禁止旧 editorial 作为真源或兼容基线）：
- 禁止把旧 editorial 业务实现、页面、API、SQL 当作需求真源或兼容基线，包括但不限于：
  - `Brain/brain-modules/brain-editorial/**`
  - `plus-ui-ts/src/views/editorial/review/**`
  - `plus-ui-ts/src/api/editorial/review.ts`
  - 旧 editorial SQL update 脚本

允许复用的仅通用平台能力与官方文档（用于补充/交叉验证，不得反向覆盖 PRD）：
- `Brain/brain-common/**`
- `Brain/brain-modules/brain-workflow/**`
- `Brain/brain-system/**`
- `doc/official_doc/**`

## 3. 已完成 WP1-WP6：结果与关键证据摘要

### WP1（Plan/ADR/模块接线）：已完成

- ADR / Plan 已落地：
  - `doc/agent/manuscript-review-tdd_20260321-020326/decisions/ADR-0001_manuscript-review-module-implementation-strategy.md`
  - `doc/agent/manuscript-review-tdd_20260321-020326/plans/20260321_manuscript-review_tdd-implementation_plan_v01.md`
- Maven wiring 已进入变更面（worktree dirty 中可见）：`Brain/pom.xml`、`Brain/brain-modules/pom.xml`、`Brain/brain-admin/pom.xml`

### WP2（RED tests + 前端壳层 contract）：已完成并有可复核证据

- 后端单测证据（Surefire 报告可读）：
  - `.../target/surefire-reports/com.gxpublish.brain.manuscript.review.service.ManuscriptReviewServiceTest.txt`
    - 摘要：`Tests run: 11, Failures: 0, Errors: 0, Skipped: 0`
  - `.../target/surefire-reports/com.gxpublish.brain.manuscript.review.service.ManuscriptReviewDetailPermissionPolicyTest.txt`
    - 摘要：`Tests run: 1, Failures: 0, Errors: 0, Skipped: 0`
- 前端 contract 级验证口径：仍以 `route shell + detail readable display` spec 为基线（需在下一会话按提示词复跑）

### WP3（Green slice 1）：已完成（unit scope）

- 已存在核心 service 与命令模型（示例证据路径）：
  - `Brain/brain-modules/brain-manuscript-review/.../service/ManuscriptReviewService.java`
  - `Brain/brain-modules/brain-manuscript-review/.../domain/command/CreateManuscriptReviewCommand.java`
  - `Brain/brain-modules/brain-manuscript-review/.../domain/command/ResubmitManuscriptReviewCommand.java`
- 说明：当前实现仍是“domain/service contract 级”，尚未进入持久化与真实 BPM 流转闭环

### WP4（DDL/DML/Rollback 脚本起草）：已完成（脚本层）

- 脚本已存在（当前为未跟踪状态，后续提交前需确认号段/多库一致性/回滚可重复性）：
  - `Brain/script/sql/update/20260321_manuscript_review_ddl_v01.sql`
  - `Brain/script/sql/update/20260321_manuscript_review_dml_v01.sql`
  - `Brain/script/sql/update/20260321_manuscript_review_rollback_v01.sql`

### WP5（后端扩展下一步的最小 contract）：已完成（但仍未落库/未接 BPM）

- 已确认：`ResubmitManuscriptReviewCommand` 存在，且 `ManuscriptReviewService#resubmit(...)` 有“必须退回发起人才能再次提交”的 domain 级约束（见 `RESUBMIT_NOT_RETURNED_MESSAGE`）。
- 明确边界：该约束当前仍处于“信任输入”的范畴（详见下文风险），必须在接入真实流程状态后改为可信闭环。

### WP6（数据库现状收敛与号段预检查）：已完成（结论为“未闭环 apply”）

- MySQL 现状复核结论（2026-03-22 18:31 CST）：
  - 当前仅存在 `brain_manuscript_review_serial`（0 行）；其余 `brain_manuscript_review*` 表不存在
  - `brain_manuscript_review_flow_config` 表不存在（`0`）
  - 预留号段 `60000-65001` 在当前环境计数为 `0`（仍要求 apply 前备份与“全库唯一”复核）
- 该结论的直接含义：
  - WP4 的 SQL 文件“已写出”，但**SQL apply 未闭环**；后续任何依赖落库/工作流联动的实现都必须把“先闭环 apply 再推进”作为硬门槛。

## 4. 当前高风险 / 待确认项（必须显式带上）

- `resubmit` 信任边界（高风险）：
  - 当前 `ResubmitManuscriptReviewCommand.returnedToInitiator` 为调用方输入；`ManuscriptReviewService#resubmit(...)` 以此判断是否允许再次提交。
  - 这在真实联调时不可接受：必须改为从工作流实例真实状态/当前节点/操作者上下文推导，而不是信任前端传参。
  - 同类风险：`applicantUserId` 当前亦为输入，后续需切换为登录态/流程实例关联的可信身份。
- worktree dirty（高风险）：
  - 当前变更面跨后端 pom、新模块、SQL、前端路由与页面/types；若继续堆叠，回滚与定位成本急剧上升。
  - 下一会话要先决定：先收敛最小可提交集，还是继续在 dirty 上推进（建议先收敛）。
- SQL apply 未闭环（高风险）：
  - 数据库仅存在 `brain_manuscript_review_serial`，其余业务表与 seeds 未落库；
  - 在未形成“备份 + 清理策略 + 可重复 apply/rollback + 查询证据”的闭环前，不要推进依赖落库的实现与前端真实联调。

## 5. 后续 WP7-WP11 顺序与一句话目标

1. WP7：在 TDD 下扩后端到“持久化 + 工作流启动/再次提交/撤销/驳回/退回”，并把权限与状态机口径固化为可测规则。
2. WP8：落地台账/详情/历史/资源区的业务可读 API 契约，硬拦技术 ID、账号、原始码值、`roleKey/permissionFlag` 外泄。
3. WP9：把前端 `index/form/detail` 与 BPM “去审批”跳转接到新 API，保持 `无草稿/无删除` 与“修改/再次提交”“修改/去审批”拆步口径。
4. WP10：做集成与浏览器级验收，覆盖 API 可读性、统一时间线、资源追加历史、多视频标注与 BPM 分流闭环，并沉淀证据包。
5. WP11：仓库治理与交付收尾（build/lint/敏感信息/临时文件），输出 report + handoff，并准备合入主线的最小安全变更集。

## 6. 子 agent 工作协议（必须遵守）

- 主控职责：只编排/监控/收敛事实，不把“需求真源判断”和“硬禁边界执行”外包给子 agent。
- 10 分钟轻量 ping 规则：只等完成通知；10 分钟无完成通知再轻量 ping；只要状态、阻塞、下一步，不刷长日志。
- 变更前硬门槛：任何代码或持久化文件变更前必须先写清：
  - Goal
  - Affected Files
  - Risk
  - Verification
  - Rollback
- 禁止旧 editorial：子 agent 不得以旧 editorial 代码/SQL/API 作为方案输入或兼容基线。

## 7. 下一会话可直接粘贴的提示词（不要读旧聊天）

```text
你负责继续 gxpublish-brain 的 manuscript_review 线（clean-slate）。

不要读旧聊天；只从以下两类材料恢复上下文：
1) /home/kugua/.config/superpowers/worktrees/gxpublish-brain/manuscript-review-tdd-20260321/doc/agent/manuscript-review-tdd_20260321-020326/handoffs/20260322_manuscript-review_handoff_v03.md
2) /home/kugua/.config/superpowers/worktrees/gxpublish-brain/manuscript-review-tdd-20260321/doc/manuscript_review/product/20260321_manuscript-review_product-requirements_v01.md

先输出：
- 已确认
- 风险
- 待确认
- 推进方案

硬约束：
- 不要把旧 editorial 业务实现、页面、API、SQL 当真源或兼容基线；只允许复用通用平台能力与官方文档。
- 任何代码或持久化文件改动前，先给 Goal / Affected Files / Risk / Verification / Rollback。
- 若启用子 agent：主控只编排/监控；默认只等完成通知；10 分钟无完成通知再轻量 ping。

从以下“现状事实”开始复核（不要扩大范围）：
1) worktree 基线与 dirty：
cd /home/kugua/.config/superpowers/worktrees/gxpublish-brain/manuscript-review-tdd-20260321
git branch --show-current
git rev-parse HEAD
git status --porcelain=v1

2) 数据库现状（MySQL）：
SELECT table_name, table_rows FROM information_schema.tables WHERE table_schema = 'gxpublish_brain' AND table_name LIKE 'brain_manuscript_review%' ORDER BY table_name;
SELECT COUNT(*) AS flow_config_table_exists FROM information_schema.tables WHERE table_schema = 'gxpublish_brain' AND table_name = 'brain_manuscript_review_flow_config';

推进顺序：WP7 -> WP11（但任何依赖落库/工作流的实现前，先把 SQL apply 闭环作为硬门槛处理）。
特别注意：resubmit 目前存在“returnedToInitiator 由入参决定”的信任边界，高风险，必须改为可信状态推导。
```

