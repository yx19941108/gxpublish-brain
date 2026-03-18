Owner: Codex
Task: editorial-manuscript-controller-runtime-dispatch
Status: Worker-A Round-03 Dispatched + Polling Active
Updated: 2026-03-18

# 2026-03-18 主控轮询与协议修复报告 v02

> Supersedes: `20260318_editorial-manuscript-controller-runtime-dispatch_report_v01.md`

## 范围

本报告记录 controller 在 `round-02` 后的新事实、`worker-a` 协议阻断的根因、以及 controller 采取的保守修复动作。

## 已确认

1. `worker-b round-02` 成功完成，并已把 Browser/QA 两份 direct outputs 落到约定路径：
   - `.agent-temp/editorial-manuscript-rebuild/browser-qa/20260318_editorial-manuscript-browser-qa_runtime-pack_v01.md`
   - `.agent-temp/editorial-manuscript-rebuild/browser-qa/20260318_editorial-manuscript-browser-qa_runtime-evidence-index_v01.md`
2. 上述两份 Browser/QA direct outputs 已达成本轮主控目标：
   - 固定真实账号包字段结构
   - 固定 `GO_BROWSER_RUNTIME` 前后边界
   - 固定 `BR01 -> BR10` evidence index
   - 保留 `BR08_CANCEL_PENDING`
3. `worker-a round-02` 不是技术失败，而是协议阻断：
   - `worker-a_bootstrap_v01.md` 原始写边界只允许 `orchestration/worker-a/**`
   - controller 的 `worker-a_round-02_task_v01.md` 却要求产出到 `sql-backend/**`
   - `worker-a_round-02_blocker_v01.md` 已把该冲突写明
4. controller 已完成根因裁决：
   - 问题是文件协议冲突
   - 不是 SQL 方案能力不足
   - 不是 destructive SQL 执行失败
5. controller 已采取保守修复：
   - 不放宽 `worker-a` 原始写边界
   - 下发 `worker-a_round-03_task_v01.md`
   - 要求 `worker-a` 先把三份 SQL/Backend 草案写入 `orchestration/worker-a/**`
6. controller 已启动 120 秒后台轮询脚本，持续盯协议文件变化：
   - `.agent-temp/editorial-manuscript-rebuild/controller/controller_protocol_poll_v01.ps1`
7. 当前总状态仍只到：
   - `SQL_PREP_READY`
   - `JOINT_DEBUG_SURFACE_READY`

## 风险

1. 若 controller 为了追求路径统一而直接放宽 `worker-a` 写入 `sql-backend/**`，会破坏原始 worker 写边界协议。
2. 若 controller 不处理该冲突而继续只盯 `sql-backend/**`，会把“协议阻断”误判成“worker-a 不执行”。
3. 当前仍没有 SQL/Backend 草案 direct outputs、没有 SQL apply 证据、没有真实 HTTP 和浏览器证据，任何更高级 gate 都不能宣称成立。

## 待确认

1. `worker-a round-03` 是否能在其原始可写边界内完整产出三份草案。
2. controller 审核通过后，是否需要将 `worker-a round-03` 草案镜像到 `sql-backend/**` 作为统一 direct output。
3. SQL/Backend 草案是否足以支撑下一步 `SQL_APPLY_READY` 预审。

## 推进方案

1. 保持 `worker-a` 原始协议不变，避免再制造权限漂移。
2. 等待并轮询 `worker-a round-03` 的 3 份草案和结果文件。
3. Browser/QA 线当前不再追加新任务，只保持其 direct outputs 作为后续 runtime 输入模板。
4. 待 `worker-a round-03` 草案生成后，由 controller 继续审核：
   - 若达标，则进入 `SQL_APPLY_READY` 预审，并决定是否镜像到 `sql-backend/**`
   - 若不达标或再出 blocker，则继续走 controller 文件协议修正

## Decision

1. 接受 `worker-b round-02` 成功结果。
2. 接受 `worker-a round-02` blocker，并定性为协议阻断。
3. controller 不放宽 `worker-a` 原始写边界。
4. controller 改以 `worker-a round-03` 在原始可写边界内产出草案。
5. 当前不升级到：
   - `SQL_APPLY_READY`
   - `BACKEND_SMOKE_READY`
   - `REAL_HTTP_READY`
   - `BROWSER_EVIDENCED`
   - `可提测`

## Need From SQL/Backend

1. `orchestration/worker-a/worker-a_round-03_tenant-aware-flow-sql_v01.md`
2. `orchestration/worker-a/worker-a_round-03_menu-permission-sql_v01.md`
3. `orchestration/worker-a/worker-a_round-03_apply-ready-report_v01.md`
4. `orchestration/worker-a/worker-a_round-03_result_v01.md`

## Need From Browser/QA

1. 当前无新增写作任务。
2. 保持现有 `runtime-pack` 与 `runtime-evidence-index` 作为后续 runtime 输入模板。
3. 在 `GO_BROWSER_RUNTIME` 前继续维持 `PENDING_INPUT` 口径。

## Artifacts

1. `.agent-temp/editorial-manuscript-rebuild/orchestration/controller/worker-a_round-03_task_v01.md`
2. `.agent-temp/editorial-manuscript-rebuild/controller/controller_protocol_poll_v01.ps1`
3. `.agent-temp/editorial-manuscript-rebuild/controller/20260318_editorial-manuscript-next-session-prompt-pack_v03.md`
4. `doc/agent/editorial-manuscript-rebuild_20260317-132339/reports/20260318_editorial-manuscript-controller-runtime-dispatch_report_v02.md`
5. `doc/agent/editorial-manuscript-rebuild_20260317-132339/handoffs/20260318_editorial-manuscript-controller-runtime-dispatch_handoff_v02.md`

## Next Gate

1. `worker-a round-03` 三份草案全部生成并经 controller 审核达标
2. Browser/QA direct outputs 继续作为有效输入保持不变
3. 达标后再决定是否推进到：
   - `SQL_APPLY_READY`
   - `BACKEND_SMOKE_READY`
   - `GO_BROWSER_RUNTIME`
