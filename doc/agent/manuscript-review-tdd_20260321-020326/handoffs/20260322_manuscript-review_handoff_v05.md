Owner: Codex
Task: manuscript-review handoff
Status: WP9 completed; SQL apply closed loop; next focus WP10-WP11
Updated: 2026-03-22 20:37:54 CST

# Manuscript Review Handoff v05 (Current, Only Entry)

## 0. 重要声明（唯一续接入口）

v04 已过时，禁止再作为续接入口（历史留档，不再维护）：
- `/home/kugua/.config/superpowers/worktrees/gxpublish-brain/manuscript-review-tdd-20260321/doc/agent/manuscript-review-tdd_20260321-020326/handoffs/20260322_manuscript-review_handoff_v04.md`

本文件 v05 为当前唯一续接入口。下一会话必须从 `v05 + 需求真源(PRD)` 恢复上下文。

## 1. 本轮新增事实

### WP9 已完成：SQL apply 已闭环

已确认：
- `gxpublish_brain` 库内 manuscript_review DDL + DML 已 apply。
- `brain_manuscript_review*` 表面已形成 7 表：
- `brain_manuscript_review`
- `brain_manuscript_review_serial`
- `brain_manuscript_review_flow_config`
- `brain_manuscript_review_attachment`
- `brain_manuscript_review_external_link`
- `brain_manuscript_review_video_marker`
- `brain_manuscript_review_history`

已确认 seeds 计数：
- `dict_type = 4`
- `dict_data = 18`
- `menu = 8`
- `role = 5`
- `role_menu = 70`
- `flow_config = 2`
- `user = 5`
- `user_role = 5`

关键口径已锁定：
- 一期无 `draft/delete/recycle`。
- workflow 菜单按 `11618 + 11619 + 11632` / `11618 + 11629` 拆分。
- 不授予 `11633`。

定向测试复核已确认：
- 后端 Maven 定向测试：`15 tests` 全绿，时间 `2026-03-22 20:04:11 +08:00`。
- 前端 Vitest 定向测试：`4 tests` 全绿。

## 2. 当前阶段结论

已确认：
- WP1-WP8 的需求/测试/TDD 基线仍成立。
- WP9 不再是 blocker；SQL 脚本“已写出未落库”的旧结论已失效。
- 后续推进默认从 `WP10 API 合同` 和 `WP11 历史审计/联调 gate` 接续。

风险：
- 虽然 SQL apply 已闭环，但这不等于 API/前端/BPM 联调闭环已完成。
- 一期口径已锁死“无 draft/delete/recycle”，后续接口、按钮、错误文案若回流出这些语义，属于需求回退。
- workflow 菜单授权边界已给出；若实现或联调中重新暴露 `11633`，会形成权限面偏差。

待确认：
- WP10 API 合同的最终业务口径是否已完全收敛到“业务可读字段”，并彻底阻断 raw ID / raw enum / 账号 / `roleKey/permissionFlag` 外泄。
- WP11 是否已形成“历史追加、不物理覆盖”的统一审计链，以及 BPM 待办/已办/办理页的真实联调证据。
- 前端页面是否仍严格遵守一期范围，不额外引入草稿箱、回收站、删除态入口。

## 3. 剩余工作包

### WP10（API 合同）

目标：
- 落地台账、详情、历史、资源区 API 合同。
- 对齐后端 DTO/VO、前端 `src/api`、前端 `types`。
- 硬拦技术标识外泄，只保留业务可读字段与中文口径。

完成判据：
- 合同字段与 PRD 一致。
- 无 raw ID / raw code / 登录账号 / 权限标识直出。
- 定向后端测试与前端 contract/type 验证通过。

### WP11（历史审计 / 联调 gate）

目标：
- 形成“追加历史 + 当前可停用”的资源生命周期。
- 形成统一时间线审计。
- 完成 BPM 待办/已办与办理页的联调证据包。

完成判据：
- 历史链可追溯且不被覆盖。
- 页面动作、流程状态、历史记录三者口径一致。
- 至少具备真实 HTTP / 页面侧联调证据，而不仅是单测通过。

## 4. 下一会话建议起点

先输出：
- 已确认
- 风险
- 待确认
- 推进方案

然后只围绕以下两项推进：
1. WP10 API 合同收敛与定向验证。
2. WP11 历史审计/BPM 联调证据闭环。

硬约束继续有效：
- 不读取旧 editorial 业务实现作为真源或兼容基线。
- 任何代码或持久化文件改动前，先给 `Goal / Affected Files / Risk / Verification / Rollback`。
- 不把 “SQL apply 已闭环” 误报成 “整体提测完成”。
