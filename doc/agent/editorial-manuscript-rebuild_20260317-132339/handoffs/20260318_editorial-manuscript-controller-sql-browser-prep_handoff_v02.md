Owner: Codex
Task: editorial-manuscript-controller-handoff
Status: SQL_PREP_READY + JOINT_DEBUG_SURFACE_READY
Updated: 2026-03-18

# 审校模块主控 SQL / Browser 准备态 handoff v02

> Supersedes: `20260318_editorial-manuscript-controller-sql-browser-prep_handoff_v01.md`

## 已确认

1. 已完成对以下三份执行线产物的主控审核：
   - `C:\kuguaHome\project\gxpublish-brain\.agent-temp\editorial-manuscript-rebuild\sql-backend\20260318_sql-backend_prep-ready_report_v01.md`
   - `C:\kuguaHome\project\gxpublish-brain\.agent-temp\editorial-manuscript-rebuild\browser-qa\20260318_editorial-manuscript-browser-qa_controller-review-note_v01.md`
   - `C:\kuguaHome\project\gxpublish-brain\.agent-temp\editorial-manuscript-rebuild\browser-qa\20260318_editorial-manuscript-browser-qa_joint-debug-surface-ready_v01.md`
2. 当前主控 live 复核后确认：
   - `brain_editorial_review = 66`
   - `editorial_review_flow` 为 `2 active + 1 deleted`
   - runtime 仍挂在该 flow 上
   - 旧菜单 `reviewEdit` 仍存在
3. SQL/Backend 当前状态仍只到 `SQL_PREP_READY`。
4. Browser/QA 当前状态仍只到 `JOINT_DEBUG_SURFACE_READY`。

## 风险

1. 当前 `20260317_editorial_review_flow_role_seed.sql` 不是 tenant-aware full-rebuild SQL。
2. 当前 repo 仍无独立 editorial `menu/permission` SQL。
3. Browser/QA 虽然已有矩阵和证据目录，但仍缺真实账号包、依赖和 runtime SQL apply 状态。
4. 当前仍禁止把任何状态表述成“可提测”或“运行时闭环通过”。

## 待确认

1. SQL/Backend 何时补齐 tenant-aware flow full-rebuild SQL。
2. SQL/Backend 何时补齐独立 menu/permission SQL。
3. destructive cleanup 是否一并覆盖 runtime 表。
4. Browser/QA 使用哪些真实账号与密码。
5. SQL apply 后 Browser/QA 使用哪组记录和 `taskId/instanceId` 取证。

## 推进方案

1. SQL/Backend 先补 SQL 缺口，不直接 apply。
2. Browser/QA 先补账号包与依赖，不提前宣称浏览器取证已完成。
3. SQL apply 状态明确后，由主控决定是否发出 `GO_BROWSER_RUNTIME`。
4. 一旦 `GO_BROWSER_RUNTIME` 发出，Browser/QA 直接沿用现有 `BR01 -> BR10` 和 evidence 目录规则执行。

## Decision

1. 当前主控批准：
   - 维持 `SQL_PREP_READY`
   - 维持 `JOINT_DEBUG_SURFACE_READY`
2. 当前主控不批准：
   - `SQL_APPLY_READY`
   - `BACKEND_SMOKE_READY`
   - `REAL_HTTP_READY`
   - `BROWSER_EVIDENCED`
   - `可提测`
3. 当前主控补充裁决：
   - `approval-shell deeplink`：批准作为当前运行时验证入口
   - `BR08_CANCEL_PENDING`：分类正确，继续保留在当前目标范围内
   - Browser/QA 现有 evidence 目录拆分与 case id 规则：批准

## Need From SQL/Backend

1. tenant-aware `editorial_review_flow` full-rebuild SQL
2. 独立 editorial `menu/permission` SQL
3. runtime 表 cleanup 最终范围确认
4. apply 后 Browser/QA 可用的记录集与 `taskId/instanceId` 来源

## Need From Browser/QA

1. 真实登录账号包：`tenantId / username / password`
2. 补齐集成树前端依赖后执行 fresh 前端静态验证
3. 在 `GO_BROWSER_RUNTIME` 后按现有矩阵直接执行，不再重写文档

## Artifacts

1. `reports/20260318_editorial-manuscript-controller-sql-browser-audit_report_v02.md`
2. `handoffs/20260318_editorial-manuscript-controller-sql-browser-prep_handoff_v02.md`

## Next Gate

1. `SQL_APPLY_READY`
2. `BACKEND_SMOKE_READY`
3. `REAL_HTTP_READY`
4. `BROWSER_EVIDENCED`

