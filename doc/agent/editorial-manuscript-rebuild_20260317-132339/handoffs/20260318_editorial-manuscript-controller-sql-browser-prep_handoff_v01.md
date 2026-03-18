Owner: Codex
Task: editorial-manuscript-controller-handoff
Status: SQL_PREP_READY + JOINT_DEBUG_SURFACE_READY
Updated: 2026-03-18

# 审校模块主控 SQL / Browser 准备态 handoff

说明：

1. 设计与架构基线继续继承 `ADR-0002 + implementation plan v01 + shared handoff v01`。
2. 本 handoff 只更新 2026-03-18 的 live 状态与下一 gate 需求，不重开 ADR-0002。

## 已确认

1. 集成树同时具备 backend `POST /editorial/review/resubmit` 与 frontend `resubmitReviewAction -> /editorial/review/resubmit`。
2. backend 最小静态验证已 fresh 通过：`Tests run: 9, Failures: 0, Errors: 0, Skipped: 0`。
3. 目标库 `gxpublish_brain` 存在，`brain_editorial_review / flow_definition / flow_node / flow_skip / sys_role / sys_user / sys_user_role` 均存在。
4. 当前 live 数据不是“空白重建态”：
   - `brain_editorial_review` 当前 63 行
   - `editorial_review_flow` 当前有两条 `del_flag=0` definition
   - runtime 仍挂在两个 definition 上
5. Browser/QA 所需 applicant / approver 用户样本已存在，但未确认凭据。
6. 集成树当前 `plus-ui-ts/node_modules` 不存在，因此 Browser/QA 还未具备本树直接跑前端命令的依赖条件。

## 风险

1. 现有 `20260317_editorial_review_flow_role_seed.sql` 是“补缺式修正”，不是“全量清旧后的唯一 active flow 重建”；直接执行会留下旧 active definition、旧 UUID 节点和额外历史节点。
2. runtime 表真实有数据；如果只改 `flow_definition/node/skip`，会把新 flow 结构和旧 runtime/task 数据混在一起。
3. 当前 seed 脚本使用 `tenant_id='000000'`，而 live editorial flow / role / runtime 当前 tenant 是 `099142`。
4. Browser/QA 如果没有真实账号、真实 taskId 和项目本地依赖，只能停留在静态矩阵，无法形成浏览器证据。
5. 本轮仍禁止把任何状态表述成“可提测”或“运行时闭环通过”。

## 待确认

1. SQL/Backend 是否清理全部旧 `editorial_review_flow` definition，只保留一条目标 active definition。
2. SQL/Backend 如何处理当前 `WAITING_*` 与 `BACK` 的业务行及其 runtime 解绑。
3. SQL/Backend 的 destructive SQL 执行器到底是 `mysql.exe` 还是其他明确入口。
4. Browser/QA 使用哪些真实登录账号，如何拿到凭据。
5. Browser/QA 在集成树补依赖后的前端 fresh 验证命令与浏览器证据落盘路径。

## 推进方案

1. 先由 SQL/Backend 线补齐备份语句、清理范围、tenant 口径、apply 顺序和 backend smoke 方案。
2. 在未拿到上述清单前，不允许执行 destructive SQL。
3. Browser/QA 线先锁定账号矩阵、样本记录、taskId 来源与依赖补齐方案。
4. SQL apply 完成且 backend smoke 通过后，再进入真实 HTTP 与浏览器联调。

## Decision

1. 当前主控状态批准推进到 `SQL_PREP_READY + JOINT_DEBUG_SURFACE_READY`。
2. 当前主控状态不批准推进到 `SQL_APPLIED / REAL_HTTP_READY / BROWSER_EVIDENCED / 可提测`。

## Need From SQL/Backend

1. 明确受影响表：
   - `brain_editorial_review`
   - `flow_definition`
   - `flow_node`
   - `flow_skip`
   - `sys_role`
   - `flow_instance`
   - `flow_task`
   - `flow_his_task`
   - `flow_instance_biz_ext`
   - `sys_user_role`（若 role id 要删除重建）
2. 提供上述表的备份动作与过滤条件。
3. 提供旧 definition / node / skip / runtime 的 pre-clean SQL。
4. 明确 `WAITING_*` / `BACK` 业务行的复位策略。
5. 明确 tenant 口径与实际 SQL 执行器。
6. 提供 apply 后 backend real HTTP smoke 清单。

## Need From Browser/QA

1. 提供 applicant / certified applicant / approver 账号矩阵。
2. 提供凭据来源或重置方案。
3. 在集成树补齐 `node_modules` 后执行 fresh frontend 静态命令。
4. 提供 6 条浏览器关键链路矩阵：
   - applicant submit
   - certified applicant submit
   - BACK resubmit
   - first approval
   - second/final approval
   - legacy fallback
5. 产出截图、HAR、console、网络证据并写回报告。

## Artifacts

1. `reports/20260318_editorial-manuscript-controller-sql-browser-audit_report_v01.md`
2. `handoffs/20260318_editorial-manuscript-controller-sql-browser-prep_handoff_v01.md`

## Next Gate

1. `SQL_APPLY_READY`
2. `BACKEND_SMOKE_READY`
3. `REAL_HTTP_READY`
4. `BROWSER_EVIDENCED`

