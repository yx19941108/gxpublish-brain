Owner: Codex
Task: manuscript-review test-ready checkpoint report
Status: Current
Updated: 2026-03-23 09:53:28 +08:00

# Manuscript Review Test-Ready Checkpoint Report v01

## 1. Summary

本报告记录 `gxpublish-brain` clean-slate `manuscript_review` 研发线在 2026-03-23 达到“可测试阶段”的实际收口状态。

这里的“可测试阶段”仅指：

- 本地验证账号准备已落地。
- readable HTTP 取证样例数据已落地。
- `去审批` 已推进到前端可测。
- fresh 定向验证已补齐。

本报告不把当前状态误报为真实 HTTP、真实 BPM 办理页、浏览器联调或运行时闭环已通过。

## 2. 已确认

### 2.1 本轮新增成果

- 新增 dev-only 登录补丁：
  - `Brain/script/sql/update/20260323_manuscript_review_dev_login_seed_v01.sql`
  - 作用：仅更新既有 5 个 `mr_*` 账号的 `sys_user.password`
  - dev-only 明文口令：`666666`
- 新增 readable HTTP 样例 fixture：
  - `Brain/script/sql/update/20260323_manuscript_review_readable_http_fixture_v01.sql`
  - 关键样例：
    - `review_id=880000230001`
    - `manuscript_code=SH20260323991`
  - 作用：仅服务于 `/manuscript-review/readable/ledger` 与 `/manuscript-review/readable/detail` 取证
  - 边界：`flow_instance_id = NULL`，未伪造 BPM 上下文
- 前端 `去审批` 已从固定报错推进到前端可测：
  - `plus-ui-ts/src/views/manuscript-review/detail.vue`
  - `plus-ui-ts/src/views/manuscript-review/detail-approve-jump.spec.ts`
  - `plus-ui-ts/src/api/workflow/instance/index.ts`
  - `plus-ui-ts/src/api/workflow/instance/types.ts`
  - `plus-ui-ts/src/types/manuscript-review/detail.ts`
- 跳转规则：
  - 成功分支：
    - `/workflow/approval/detail?id=<reviewId>&type=approval&taskId=<taskId>`
  - blocker 分支：
    - `当前流程尚未生成可办理待办，请先确认流程实例与办理页配置已完成。`
  - 接口失败分支：
    - `去审批入口加载失败，请稍后重试。`

### 2.2 Fresh Verification

本轮由 controller 在当前会话 fresh 补齐以下验证：

- SQL guard:
  - `.\.agent-temp\manuscript-review-dev-login-guard.ps1`
  - 结果：`manuscript-review-dev-login-guard: PASS`
- SQL guard:
  - `.\.agent-temp\manuscript-review-readable-http-fixture-guard.ps1`
  - 结果：`manuscript-review-readable-http-fixture-guard: PASS`
- 前端定向测试：
  - `pnpm exec vitest run src/views/manuscript-review/detail-readable-display.spec.ts src/views/manuscript-review/detail-approve-jump.spec.ts`
  - 结果：`Test Files 2 passed`，`Tests 8 passed`
- 边界内 lint：
  - `pnpm exec eslint src/views/manuscript-review/detail.vue src/views/manuscript-review/detail-approve-jump.spec.ts src/api/workflow/instance/index.ts src/api/workflow/instance/types.ts src/types/manuscript-review/detail.ts`
  - 结果：退出码 `0`
- 前端生产构建：
  - `pnpm run build:prod`
  - 结果：退出码 `0`

## 3. 当前风险

- 当前 SQL 仍未 apply；未 apply 前不能宣称真实登录或真实 readable HTTP 已具备。
- `去审批` 当前只证明“前端可测”，真实成功跳进 BPM 办理页仍依赖数据库里已有该 `reviewId` 对应的 workflow instance 与 `formPath` 配置。
- 本轮没有 fresh 重跑 backend 定向测试或 `brain-admin package`；相关 backend 绿结论仍沿用此前 handoff v07 的已验证状态，不应与本轮 fresh 证据混写。
- 真实 HTTP、浏览器级证据仍未落盘。

## 4. Git Submission Boundary

本轮应提交的边界内文件：

- `Brain/script/sql/update/20260323_manuscript_review_dev_login_seed_v01.sql`
- `Brain/script/sql/update/20260323_manuscript_review_readable_http_fixture_v01.sql`
- `plus-ui-ts/src/api/workflow/instance/index.ts`
- `plus-ui-ts/src/api/workflow/instance/types.ts`
- `plus-ui-ts/src/types/manuscript-review/detail.ts`
- `plus-ui-ts/src/views/manuscript-review/detail.vue`
- `plus-ui-ts/src/views/manuscript-review/detail-approve-jump.spec.ts`
- `doc/agent/manuscript-review-tdd_20260321-020326/reports/20260323_manuscript-review_test-ready_report_v01.md`
- `doc/agent/manuscript-review-tdd_20260321-020326/handoffs/20260323_manuscript-review_handoff_v08.md`

本轮不应提交的噪声或边界外文件：

- `plus-ui-ts/.eslintrc-auto-import.json`
  - 当前仅见 `LF -> CRLF` 告警，未见功能内容 diff
- `test_qa_flow.py`
  - 边界外未跟踪文件

## 5. Next Gate

下一阶段不再是“补代码准备”，而是“运行时取证计划 + 执行前收口”：

1. 在计划模式下锁定 apply 顺序、账号矩阵、HTTP 链路、浏览器链路、证据落盘路径和 gate 判定标准。
2. 执行前先确认：
   - DDL / DML / dev-login-seed / readable-fixture 的手工 apply 顺序
   - `mr_* / 666666` 的登录矩阵
   - `review_id=880000230001` 的 HTTP 取证路径
   - `mr_approver_l2` 的动作栏与 `去审批` 成功/阻断分支
3. 然后再进入真实登录、真实 HTTP、浏览器证据采集。
