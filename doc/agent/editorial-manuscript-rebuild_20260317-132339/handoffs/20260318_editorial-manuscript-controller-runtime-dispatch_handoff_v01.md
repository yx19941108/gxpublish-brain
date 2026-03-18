Owner: Codex
Task: editorial-manuscript-controller-runtime-dispatch
Status: Round-02 Dispatched
Updated: 2026-03-18

# 审校模块主控 round-02 派发 handoff v01

## 已确认

1. controller 已按文件协议轮询到两条执行线的 `round-01` 结果：
   - `.agent-temp/editorial-manuscript-rebuild/orchestration/worker-a/worker-a_round-01_result_v01.md`
   - `.agent-temp/editorial-manuscript-rebuild/orchestration/worker-b/worker-b_round-01_result_v01.md`
2. 当前主控约定的直接产物仍未生成：
   - `sql-backend/20260318_sql-backend_tenant-aware-flow-sql_v01.md`
   - `sql-backend/20260318_sql-backend_menu-permission-sql_v01.md`
   - `sql-backend/20260318_sql-backend_apply-ready_report_v01.md`
   - `browser-qa/20260318_editorial-manuscript-browser-qa_runtime-pack_v01.md`
   - `browser-qa/20260318_editorial-manuscript-browser-qa_runtime-evidence-index_v01.md`
3. controller 已下发：
   - `.agent-temp/editorial-manuscript-rebuild/orchestration/controller/worker-a_round-02_task_v01.md`
   - `.agent-temp/editorial-manuscript-rebuild/orchestration/controller/worker-b_round-02_task_v01.md`
4. 当前总状态仍只到：
   - `SQL_PREP_READY`
   - `JOINT_DEBUG_SURFACE_READY`

## 风险

1. 若下一会话不先轮询 `round-02` 结果文件，就会重复 round-01 已完成的分析。
2. 若只盯 orchestration 结果、不盯直接产物路径，可能遗漏 worker 已经把正式输出写到目标目录。
3. 当前没有真实账号包、没有真实 SQL apply 证据、没有真实浏览器取证，不能越 gate。

## 待确认

1. worker-a `round-02` 是否把 tenant-aware flow SQL、menu/permission SQL、apply-ready 报告三件套都落齐。
2. worker-b `round-02` 是否把 runtime-pack、runtime-evidence-index 两件套都落齐。
3. 两条执行线的 `Changed Files / Verification / Need From Other Worker` 是否达标。

## 推进方案

1. 下一会话主控继续按文件协议轮询，不回退到聊天同步。
2. 先轮询 orchestration 结果文件，再同步核对直接产物路径。
3. 若 direct outputs 已生成：
   - 立即读取
   - 审核是否满足当前 gate 的最低达标线
   - 决定是继续 round-03，还是进入 `SQL_APPLY_READY` / `GO_BROWSER_RUNTIME` 预审
4. 若 only result files 生成但 direct outputs 缺失：
   - 直接判定为“结果未落成正式产物”
   - 继续派发补写
5. 若有 blocker 文件生成：
   - 先读 blocker
   - 再决定是否需要新的 controller 裁决或用户确认

## Decision

1. worker-a `round-02` 仅允许写草案，不执行 destructive SQL。
2. worker-a `round-02` 默认把 runtime 四表纳入 cleanup 草案范围。
3. worker-a `round-02` 默认优先保留现有 approver 角色与 `sys_user_role` 绑定，并优先收口现有 editorial 菜单树。
4. worker-b `round-02` 不发 `GO_BROWSER_RUNTIME`。
5. worker-b `round-02` 允许用待补件模板固化 runtime-pack 与 runtime-evidence-index。
6. `BR08_CANCEL_PENDING` 继续保留。

## 下一会话先读

1. `doc/agent/editorial-manuscript-rebuild_20260317-132339/decisions/ADR-0002_editorial-manuscript-rebuild-6gate-baseline.md`
2. `doc/agent/editorial-manuscript-rebuild_20260317-132339/plans/20260317_editorial-manuscript-rebuild-implementation_plan_v01.md`
3. `doc/agent/editorial-manuscript-rebuild_20260317-132339/reports/20260318_editorial-manuscript-controller-sql-browser-audit_report_v02.md`
4. `doc/agent/editorial-manuscript-rebuild_20260317-132339/reports/20260318_editorial-manuscript-controller-runtime-dispatch_report_v01.md`
5. `doc/agent/editorial-manuscript-rebuild_20260317-132339/handoffs/20260318_editorial-manuscript-controller-runtime-dispatch_handoff_v01.md`
6. `.agent-temp/editorial-manuscript-rebuild/controller/20260318_editorial-manuscript-next-session-prompt-pack_v02.md`

## 下一会话优先轮询文件

1. `.agent-temp/editorial-manuscript-rebuild/orchestration/worker-a/worker-a_round-02_result_v01.md`
2. `.agent-temp/editorial-manuscript-rebuild/orchestration/worker-a/worker-a_round-02_blocker_v01.md`
3. `.agent-temp/editorial-manuscript-rebuild/orchestration/worker-b/worker-b_round-02_result_v01.md`
4. `.agent-temp/editorial-manuscript-rebuild/orchestration/worker-b/worker-b_round-02_blocker_v01.md`
5. `.agent-temp/editorial-manuscript-rebuild/sql-backend/20260318_sql-backend_tenant-aware-flow-sql_v01.md`
6. `.agent-temp/editorial-manuscript-rebuild/sql-backend/20260318_sql-backend_menu-permission-sql_v01.md`
7. `.agent-temp/editorial-manuscript-rebuild/sql-backend/20260318_sql-backend_apply-ready_report_v01.md`
8. `.agent-temp/editorial-manuscript-rebuild/browser-qa/20260318_editorial-manuscript-browser-qa_runtime-pack_v01.md`
9. `.agent-temp/editorial-manuscript-rebuild/browser-qa/20260318_editorial-manuscript-browser-qa_runtime-evidence-index_v01.md`
