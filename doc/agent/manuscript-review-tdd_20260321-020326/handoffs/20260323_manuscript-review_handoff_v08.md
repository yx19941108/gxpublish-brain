Owner: Codex
Task: manuscript-review test-ready handoff
Status: Current
Updated: 2026-03-23 09:53:28 +08:00

# Manuscript Review Handoff v08 (Current, Test-Ready Entry)

## 0. 唯一续接入口

从本文件开始，`v07` 降级为历史入口。后续会话默认只从以下材料恢复：

1. `doc/agent/manuscript-review-tdd_20260321-020326/handoffs/20260323_manuscript-review_handoff_v08.md`
2. `doc/manuscript_review/product/20260321_manuscript-review_product-requirements_v01.md`
3. `doc/agent/manuscript-review-tdd_20260321-020326/reports/20260323_manuscript-review_test-ready_report_v01.md`

如需补读上一轮 push checkpoint 与中断会话总结，再读取：

4. `doc/agent/manuscript-review-tdd_20260321-020326/handoffs/20260323_manuscript-review_handoff_v07.md`
5. `doc/agent/manuscript-review-tdd_20260321-020326/reports/20260323_manuscript-review_019d1619-session-summary_report_v01.md`

## 1. 当前状态

### 1.1 已确认

- 当前研发线仍是 clean-slate `manuscript_review`；旧 `brain-editorial` 业务实现/页面/API/SQL 仍禁止作为需求或兼容真源。
- 当前阶段已从“push checkpoint”推进到“可测试阶段”：
  - dev-only 登录补丁已落地
  - readable HTTP 样例 fixture 已落地
  - `去审批` 已推进到前端可测
- 当前关键增量：
  - `Brain/script/sql/update/20260323_manuscript_review_dev_login_seed_v01.sql`
  - `Brain/script/sql/update/20260323_manuscript_review_readable_http_fixture_v01.sql`
  - `plus-ui-ts/src/views/manuscript-review/detail.vue`
  - `plus-ui-ts/src/views/manuscript-review/detail-approve-jump.spec.ts`
  - `plus-ui-ts/src/api/workflow/instance/index.ts`
  - `plus-ui-ts/src/api/workflow/instance/types.ts`
  - `plus-ui-ts/src/types/manuscript-review/detail.ts`
- dev-only 登录信息：
  - 账号范围：既有 5 个 `mr_*` 用户
  - dev-only 明文口令：`666666`
- readable HTTP 高位样例：
  - `review_id=880000230001`
  - `manuscript_code=SH20260323991`
- `去审批` 当前路由规则：
  - 成功分支：`/workflow/approval/detail?id=<reviewId>&type=approval&taskId=<taskId>`
  - 缺实例/待办 blocker：
    - `当前流程尚未生成可办理待办，请先确认流程实例与办理页配置已完成。`
  - API 加载失败：
    - `去审批入口加载失败，请稍后重试。`

### 1.2 Fresh 已验证

- `.\.agent-temp\manuscript-review-dev-login-guard.ps1`
  - 结果：PASS
- `.\.agent-temp\manuscript-review-readable-http-fixture-guard.ps1`
  - 结果：PASS
- `pnpm exec vitest run src/views/manuscript-review/detail-readable-display.spec.ts src/views/manuscript-review/detail-approve-jump.spec.ts`
  - 结果：`Tests 8 passed`
- `pnpm exec eslint src/views/manuscript-review/detail.vue src/views/manuscript-review/detail-approve-jump.spec.ts src/api/workflow/instance/index.ts src/api/workflow/instance/types.ts src/types/manuscript-review/detail.ts`
  - 结果：退出码 `0`
- `pnpm run build:prod`
  - 结果：退出码 `0`

### 1.3 仍未闭环

- 真实登录证据未落盘。
- `/manuscript-review/readable/ledger` 与 `/manuscript-review/readable/detail` 的真实 HTTP 证据未落盘。
- 浏览器级证据未落盘。
- `去审批 -> BPM 办理页` 真实运行时闭环未落盘。

## 2. 当前风险

- 当前阶段只是“可测试”，不能误报成真实 HTTP、真实 BPM、浏览器联调或运行时闭环已通过。
- 所有运行时链路都依赖手工 apply SQL。未 apply 前，不要做真实性判断。
- `去审批` 成功分支依赖数据库里已存在该 `reviewId` 对应的 workflow instance 与 `formPath` 配置；否则应进入 blocker 分支，而不是判失败实现。
- backend 旧绿结论仍来自 v07 之前的验证，本轮未 fresh 重跑 backend 定向测试或 package。

## 3. 下一阶段建议

下一阶段会话不要直接开始执行 apply、HTTP、浏览器。先进入计划模式，把运行时取证阶段的细节敲定，然后再执行：

1. 锁定 SQL apply 顺序与执行责任。
2. 锁定登录账号矩阵与取证顺序。
3. 锁定 HTTP 取证链路、headers、目标字段与成功标准。
4. 锁定浏览器取证链路、账号、页面路径与截图/日志证据面。
5. 锁定 `去审批` 的成功分支与 blocker 分支判定标准。
6. 锁定证据落盘路径、命名和阶段 gate。

## 4. 执行前已知信息

### 4.1 建议 apply 顺序

1. `20260321_manuscript_review_ddl_v01.sql`
2. `20260321_manuscript_review_dml_v01.sql`
3. `20260323_manuscript_review_dev_login_seed_v01.sql`
4. `20260323_manuscript_review_readable_http_fixture_v01.sql`

### 4.2 建议运行时起点

- 登录账号：
  - `mr_initiator / 666666`
  - `mr_approver_l2 / 666666`
- HTTP 样例：
  - `review_id=880000230001`
  - `manuscript_code=SH20260323991`
- `去审批` 重点验证角色：
  - `mr_approver_l2`

## 5. 下阶段会话提示词

以下提示词用于“计划模式”新会话，目标不是立刻执行，而是先把运行时取证阶段的所有细节敲定：

```text
你继续接管 `gxpublish-brain` 仓库里的 clean-slate `manuscript_review` 研发线。

先不要读旧聊天；只从仓库内以下文件恢复上下文：
1. `doc/agent/manuscript-review-tdd_20260321-020326/handoffs/20260323_manuscript-review_handoff_v08.md`
2. `doc/manuscript_review/product/20260321_manuscript-review_product-requirements_v01.md`
3. `doc/agent/manuscript-review-tdd_20260321-020326/reports/20260323_manuscript-review_test-ready_report_v01.md`

当前阶段不是补代码，而是进入“运行时取证阶段”的计划模式。

先输出：
- 已确认
- 风险
- 待确认
- 运行时取证计划

硬约束继续有效：
- 这是 clean-slate `manuscript_review` 线；绝对不要把旧 `brain-editorial` 业务实现/页面/API/SQL 当需求或兼容基线。
- 主控默认只负责编排/调度/监控；除非用户明确批准，否则不要本地越权直接执行 apply、真实 HTTP 或浏览器操作。
- 不要把当前“可测试阶段”误报成真实 HTTP / 真实 BPM / 浏览器联调 / 运行时闭环已通过。
- 任何代码或持久化文件改动前，先给 `Goal / Affected Files / Risk / Verification / Rollback`。
- 展示必须业务可读：不要在 UI 里直出数据库 ID、枚举码、账号、role key、permission flag；`reviewId` 仅允许用于隐式定位。

当前已确认状态：
- dev-only 登录补丁已落地：`20260323_manuscript_review_dev_login_seed_v01.sql`
- readable HTTP 高位样例 fixture 已落地：`20260323_manuscript_review_readable_http_fixture_v01.sql`
- `去审批` 已推进到前端可测，成功分支会跳 `/workflow/approval/detail?id=<reviewId>&type=approval&taskId=<taskId>`，取不到实例/待办时会展示 blocker 文案。
- fresh 验证已补齐：
  - 两个 SQL guard PASS
  - 前端定向测试 8/8 绿
  - 边界内 eslint 绿
  - fresh `build:prod` 绿

当前未闭环项：
1. 真实登录证据未落盘。
2. `/manuscript-review/readable/ledger` 与 `/manuscript-review/readable/detail` 的真实 HTTP 证据未落盘。
3. `去审批 -> BPM 办理页` 的真实运行时闭环未落盘。
4. 浏览器级证据未落盘。

本会话目标：
1. 在计划模式下把运行时取证阶段的所有细节敲定。
2. 明确 SQL apply 顺序、账号矩阵、HTTP 链路、浏览器链路、证据落盘路径和 gate 判定标准。
3. 如果需要执行，再把执行前的最小检查清单写清楚，但不要直接开始跑。
```
