Owner: Codex
Task: editorial-manuscript-controller-runtime-dispatch
Status: Round-02 Dispatched
Updated: 2026-03-18

# 2026-03-18 主控轮询与 round-02 派发报告 v01

## 范围

本报告只记录 controller 按既有文件协议完成的一次真实轮询与下一轮派发，不重开 ADR-0002，不把静态结论升级成运行时结论。

本轮输入来自：

1. `doc/agent/editorial-manuscript-rebuild_20260317-132339/reports/20260318_editorial-manuscript-controller-sql-browser-audit_report_v02.md`
2. `doc/agent/editorial-manuscript-rebuild_20260317-132339/handoffs/20260318_editorial-manuscript-controller-sql-browser-prep_handoff_v02.md`
3. `doc/agent/editorial-manuscript-rebuild_20260317-132339/handoffs/20260318_editorial-manuscript-next-session-multicli_handoff_v01.md`
4. `.agent-temp/editorial-manuscript-rebuild/controller/20260318_editorial-manuscript-next-session-prompt-pack_v01.md`
5. `.agent-temp/editorial-manuscript-rebuild/orchestration/worker-a/worker-a_round-01_result_v01.md`
6. `.agent-temp/editorial-manuscript-rebuild/orchestration/worker-b/worker-b_round-01_result_v01.md`

## 已确认

1. controller 已成功轮询到两条执行线的 `round-01` 结果文件：
   - `.agent-temp/editorial-manuscript-rebuild/orchestration/worker-a/worker-a_round-01_result_v01.md`
   - `.agent-temp/editorial-manuscript-rebuild/orchestration/worker-b/worker-b_round-01_result_v01.md`
2. worker-a `round-01` 已把 SQL 缺口审阅清楚：
   - 现有 `20260317_editorial_review_flow_role_seed.sql` 不满足 tenant-aware full-rebuild
   - repo 当前无独立 editorial `menu/permission` SQL
   - runtime cleanup 四表应纳入 apply-ready 草案范围
3. worker-b `round-01` 已把 Browser/QA 前置条件审阅清楚：
   - 进入 `GO_BROWSER_RUNTIME` 前仍缺真实账号包、apply 后记录集、`taskId/instanceId`、runtime SQL apply 证据
   - `node_modules` 缺失只允许继续准备态整理，不允许升级成 runtime-ready
   - 现有 evidence 目录、命名规则和 `BR01 -> BR10` 仍可直接沿用
4. 主控约定的直接产物路径本轮仍全部缺失：
   - `.agent-temp/editorial-manuscript-rebuild/sql-backend/20260318_sql-backend_tenant-aware-flow-sql_v01.md`
   - `.agent-temp/editorial-manuscript-rebuild/sql-backend/20260318_sql-backend_menu-permission-sql_v01.md`
   - `.agent-temp/editorial-manuscript-rebuild/sql-backend/20260318_sql-backend_apply-ready_report_v01.md`
   - `.agent-temp/editorial-manuscript-rebuild/browser-qa/20260318_editorial-manuscript-browser-qa_runtime-pack_v01.md`
   - `.agent-temp/editorial-manuscript-rebuild/browser-qa/20260318_editorial-manuscript-browser-qa_runtime-evidence-index_v01.md`
5. 当前总状态仍只到：
   - `SQL_PREP_READY`
   - `JOINT_DEBUG_SURFACE_READY`

## 风险

1. 如果 controller 停在 `round-01` 审阅结论，不继续派发 `round-02`，多 CLI 文件协议会退化成“只有分析没有落盘产物”。
2. 若 worker-a 在没有 controller 约束的情况下直接推进 destructive SQL，会越过当前 gate。
3. 若 worker-b 在没有 controller 约束的情况下把 runtime-pack 缺件写成“已就绪”，会把准备态误写成运行态。
4. 当前仍没有真实账号包、真实 SQL apply 证据、真实 HTTP、真实浏览器证据，任何“可提测”表述都不成立。

## 待确认

1. worker-a `round-02` 写出的 tenant-aware flow SQL 草案，是否能在不破坏现有角色绑定的前提下闭环。
2. worker-a `round-02` 写出的 menu/permission SQL 草案，是否采用“收口现有菜单树”即可闭环，还是必须删后重种。
3. worker-b `round-02` 写出的 runtime-pack，是否已经把所有 runtime 前置输入列成可执行清单。
4. worker-b `round-02` 写出的 runtime-evidence-index，是否已把 `BR01 -> BR10` 的当前状态和证据路径固化完整。

## 推进方案

1. controller 接受两条执行线的 `round-01` 结果为当前事实输入，不再要求用户手工搬运聊天内容。
2. controller 已向两条执行线下发 `round-02` 任务，要求把结构化审阅结果落成直接产物文件：
   - `orchestration/controller/worker-a_round-02_task_v01.md`
   - `orchestration/controller/worker-b_round-02_task_v01.md`
3. worker-a `round-02` 目标：
   - 产出 tenant-aware flow SQL 草案
   - 产出独立 menu/permission SQL 草案
   - 产出 apply-ready 报告草案
   - 继续明确“未执行 destructive SQL”
4. worker-b `round-02` 目标：
   - 产出 runtime-pack
   - 产出 runtime-evidence-index
   - 继续明确“未执行正式浏览器取证”
5. controller 下一轮继续只按文件协议轮询，不改回聊天同步。

## Decision

1. 接受 worker-a / worker-b `round-01` 结果作为当前事实源。
2. 当前不升级到：
   - `SQL_APPLY_READY`
   - `BACKEND_SMOKE_READY`
   - `REAL_HTTP_READY`
   - `BROWSER_EVIDENCED`
   - `可提测`
3. controller 本轮补充裁决：
   - worker-a `round-02` 仅写草案，不执行 destructive SQL
   - worker-a `round-02` 默认把 runtime 四表纳入 cleanup 草案范围
   - worker-a `round-02` 默认优先保留现有 approver 角色与 `sys_user_role` 绑定
   - worker-a `round-02` 默认优先收口现有 editorial 菜单树，而不是先删后重种
   - worker-b `round-02` 不发 `GO_BROWSER_RUNTIME`
   - worker-b `round-02` 允许用待补件模板固化 runtime-pack，但不得夸大为“已具备”
   - `BR08_CANCEL_PENDING` 继续保留

## Need From SQL/Backend

1. `20260318_sql-backend_tenant-aware-flow-sql_v01.md`
2. `20260318_sql-backend_menu-permission-sql_v01.md`
3. `20260318_sql-backend_apply-ready_report_v01.md`
4. 在 `round-02` 结果里明确：
   - `Changed Files`
   - `Verification`
   - `Need From Other Worker`

## Need From Browser/QA

1. `20260318_editorial-manuscript-browser-qa_runtime-pack_v01.md`
2. `20260318_editorial-manuscript-browser-qa_runtime-evidence-index_v01.md`
3. 在 `round-02` 结果里明确：
   - `Changed Files`
   - `Verification`
   - `Need From Other Worker`

## Artifacts

1. `.agent-temp/editorial-manuscript-rebuild/orchestration/controller/worker-a_round-02_task_v01.md`
2. `.agent-temp/editorial-manuscript-rebuild/orchestration/controller/worker-b_round-02_task_v01.md`
3. `.agent-temp/editorial-manuscript-rebuild/controller/20260318_editorial-manuscript-next-session-prompt-pack_v02.md`
4. `doc/agent/editorial-manuscript-rebuild_20260317-132339/reports/20260318_editorial-manuscript-controller-runtime-dispatch_report_v01.md`
5. `doc/agent/editorial-manuscript-rebuild_20260317-132339/handoffs/20260318_editorial-manuscript-controller-runtime-dispatch_handoff_v01.md`

## Next Gate

1. worker-a direct outputs全部生成并经 controller 审核达标
2. worker-b direct outputs全部生成并经 controller 审核达标
3. 仍保持：
   - `SQL_PREP_READY`
   - `JOINT_DEBUG_SURFACE_READY`
4. 达标后再判断是否推进到：
   - `SQL_APPLY_READY`
   - `BACKEND_SMOKE_READY`
   - `GO_BROWSER_RUNTIME`
