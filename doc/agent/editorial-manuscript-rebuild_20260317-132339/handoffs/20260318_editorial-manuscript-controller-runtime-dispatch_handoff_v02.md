Owner: Codex
Task: editorial-manuscript-controller-runtime-dispatch
Status: Worker-A Round-03 Dispatched + Polling Active
Updated: 2026-03-18

# 审校模块主控协议修复 handoff v02

> Supersedes: `20260318_editorial-manuscript-controller-runtime-dispatch_handoff_v01.md`

## 已确认

1. Browser/QA 线的 direct outputs 已生成：
   - `.agent-temp/editorial-manuscript-rebuild/browser-qa/20260318_editorial-manuscript-browser-qa_runtime-pack_v01.md`
   - `.agent-temp/editorial-manuscript-rebuild/browser-qa/20260318_editorial-manuscript-browser-qa_runtime-evidence-index_v01.md`
2. `worker-a round-02` 的 blocker 根因已确认是文件协议写边界冲突。
3. controller 已下发：
   - `.agent-temp/editorial-manuscript-rebuild/orchestration/controller/worker-a_round-03_task_v01.md`
4. controller 已启动后台 120 秒轮询脚本：
   - `.agent-temp/editorial-manuscript-rebuild/controller/controller_protocol_poll_v01.ps1`
5. 当前总状态仍只到：
   - `SQL_PREP_READY`
   - `JOINT_DEBUG_SURFACE_READY`

## 风险

1. 若下一会话忘记 `worker-a round-02` 的 blocker 根因，又重新要求其写 `sql-backend/**`，会重复同一协议错误。
2. 若只盯 direct outputs、不盯 `worker-a round-03` 自己目录下的草案，会错过 SQL/Backend 新产物。
3. Browser/QA 虽已形成 direct outputs，但仍不是 runtime 证据。

## 待确认

1. `worker-a round-03` 是否能在原始边界内落齐 3 份草案。
2. controller 是否需要在审核后把这些草案镜像到 `sql-backend/**`。

## 推进方案

1. 先轮询 `worker-a round-03` 的 3 份草案和结果/阻断文件。
2. 同时保留对 Browser/QA 两份 direct outputs 的存在性轮询，但当前不再给 `worker-b` 新任务。
3. 若 `worker-a round-03` 草案生成：
   - 立即读取
   - 审核是否达到 `SQL_APPLY_READY` 预审最低线
   - 再决定是否镜像到 `sql-backend/**`
4. 若 `worker-a round-03` 再次 blocker：
   - 先读 blocker
   - 再决定是否需要新的 controller 裁决

## Decision

1. `worker-b round-02` 保持成功结论。
2. `worker-a round-02` 保持协议阻断结论。
3. `worker-a round-03` 继续在原始可写边界内推进。
4. 当前不发出 `GO_BROWSER_RUNTIME`。

## 下一会话先读

1. `doc/agent/editorial-manuscript-rebuild_20260317-132339/reports/20260318_editorial-manuscript-controller-runtime-dispatch_report_v02.md`
2. `doc/agent/editorial-manuscript-rebuild_20260317-132339/handoffs/20260318_editorial-manuscript-controller-runtime-dispatch_handoff_v02.md`
3. `.agent-temp/editorial-manuscript-rebuild/controller/20260318_editorial-manuscript-next-session-prompt-pack_v03.md`

## 下一会话优先轮询文件

1. `.agent-temp/editorial-manuscript-rebuild/orchestration/worker-a/worker-a_round-03_tenant-aware-flow-sql_v01.md`
2. `.agent-temp/editorial-manuscript-rebuild/orchestration/worker-a/worker-a_round-03_menu-permission-sql_v01.md`
3. `.agent-temp/editorial-manuscript-rebuild/orchestration/worker-a/worker-a_round-03_apply-ready-report_v01.md`
4. `.agent-temp/editorial-manuscript-rebuild/orchestration/worker-a/worker-a_round-03_result_v01.md`
5. `.agent-temp/editorial-manuscript-rebuild/orchestration/worker-a/worker-a_round-03_blocker_v01.md`
6. `.agent-temp/editorial-manuscript-rebuild/browser-qa/20260318_editorial-manuscript-browser-qa_runtime-pack_v01.md`
7. `.agent-temp/editorial-manuscript-rebuild/browser-qa/20260318_editorial-manuscript-browser-qa_runtime-evidence-index_v01.md`
