Owner: Codex
Task: editorial-manuscript-controller-runtime-dispatch
Status: Ready For Next Session Progress Report
Updated: 2026-03-18

# 审校模块下一会话交接 handoff v03

> Supersedes: `20260318_editorial-manuscript-controller-runtime-dispatch_handoff_v02.md`

## 已确认

1. 当前后端/前端主线编码都已推进到“主干落地 + 剩余 SQL/runtime 收口”阶段：
   - backend/workflow 主线与 `resubmit` 已落地
   - frontend IA/contract 主线与 fallback 策略已落地
2. Worker A 当前已生成 3 份 SQL/Backend 草案与结果文件：
   - `.agent-temp/editorial-manuscript-rebuild/orchestration/worker-a/worker-a_round-03_tenant-aware-flow-sql_v01.md`
   - `.agent-temp/editorial-manuscript-rebuild/orchestration/worker-a/worker-a_round-03_menu-permission-sql_v01.md`
   - `.agent-temp/editorial-manuscript-rebuild/orchestration/worker-a/worker-a_round-03_apply-ready-report_v01.md`
   - `.agent-temp/editorial-manuscript-rebuild/orchestration/worker-a/worker-a_round-03_result_v01.md`
3. Worker B 当前已生成 2 份 Browser/QA direct outputs：
   - `.agent-temp/editorial-manuscript-rebuild/browser-qa/20260318_editorial-manuscript-browser-qa_runtime-pack_v01.md`
   - `.agent-temp/editorial-manuscript-rebuild/browser-qa/20260318_editorial-manuscript-browser-qa_runtime-evidence-index_v01.md`
4. 当前 controller 后台轮询仍在运行，日志已记录 `worker-a round-03` 新产物。
5. 当前只成立：
   - `SQL_PREP_READY`
   - `JOINT_DEBUG_SURFACE_READY`

## 风险

1. 当前 Worker A 三份 SQL/Backend 产物仍是草案，不是正式 `SQL_APPLY_READY` 包。
2. 当前 Browser/QA 仍没有真实账号包、`node_modules`、记录集、`taskId/instanceId`、SQL apply 证据。
3. 当前没有任何 destructive SQL / backend smoke / 真实 HTTP / 浏览器证据，因此不得在下个会话里把“代码进度高”误报成“可提测 / 可上线”。

## 待确认

1. controller 是否接受 Worker A 的保守 SQL 策略：
   - 目标租户 `099142`
   - 保留 approver role rows 和 `sys_user_role`
   - 保留 `reviewEdit` hidden fallback
2. controller 是否需要把 Worker A 三份草案镜像到 `sql-backend/**`
3. Browser/QA 所需真实账号包、依赖、记录集和 `taskId/instanceId` 何时可得

## 推进方案

### 下个会话第一步必须做什么

下个会话开始后，**先汇报研发进度**，不要先上 agent 编排。

建议汇报结构：

1. 编码进度
2. 测试进度
3. 当前成立的 gate
4. 当前不成立的 gate
5. 到“可提测 / 最终验收上线前置完成”的 ETA 区间与假设

建议口径：

1. 编码进度：`75% - 85%`
2. 测试进度：`30% - 40%`
3. 到“可提测”：`1 - 2 个工作日`
4. 到“最终验收上线前置完成”：`2 - 4 个工作日`

说明：

1. 这些是基于当前证据的估算，不是承诺
2. 若账号包 / 依赖 / apply 权限继续缺失，ETA 需要被重新下调

### 下个会话第二步做什么

先由主控本地完成一轮 blocking review：

1. 审核 Worker A 三份草案
2. 判断是否达到 `SQL_APPLY_READY` 预审入口
3. 决定是否镜像到 `sql-backend/**`

### 下个会话第三步再考虑什么

如果 blocking review 通过，再考虑并行提效。

推荐并行 bundle：

1. `senior_developer`
   - 责任边界：正式化 SQL/Backend direct outputs / apply-ready package
2. `business_qa_lead`
   - 责任边界：收口 Browser/QA runtime 输入与验证前置，不提前跑正式浏览器取证
3. `project_reviewer` 或 `architect`
   - 责任边界：独立审查 `SQL_APPLY_READY` 包与剩余上线风险

## 子 Agent 边界

1. 用户已经要求下个会话根据进度考虑多子 agent 并行提效。
2. 但全局 AGENTS 仍要求：**实际 spawning 前必须先做 justification + boundary + expected artifact，并向用户确认。**
3. 所以下个会话允许：
   - 先给出并行方案
   - 再请求确认
   - 确认后再实际 spawn

## 下一会话先读

1. `doc/agent/editorial-manuscript-rebuild_20260317-132339/decisions/ADR-0002_editorial-manuscript-rebuild-6gate-baseline.md`
2. `doc/agent/editorial-manuscript-rebuild_20260317-132339/plans/20260317_editorial-manuscript-rebuild-implementation_plan_v01.md`
3. `doc/agent/editorial-manuscript-rebuild_20260317-132339/reports/20260318_editorial-manuscript-controller-runtime-dispatch_report_v03.md`
4. `doc/agent/editorial-manuscript-rebuild_20260317-132339/handoffs/20260318_editorial-manuscript-controller-runtime-dispatch_handoff_v03.md`
5. `.agent-temp/editorial-manuscript-rebuild/controller/20260318_editorial-manuscript-next-session-prompt-pack_v04.md`

## 下一会话优先读取的工作产物

1. `.agent-temp/editorial-manuscript-rebuild/orchestration/worker-a/worker-a_round-03_tenant-aware-flow-sql_v01.md`
2. `.agent-temp/editorial-manuscript-rebuild/orchestration/worker-a/worker-a_round-03_menu-permission-sql_v01.md`
3. `.agent-temp/editorial-manuscript-rebuild/orchestration/worker-a/worker-a_round-03_apply-ready-report_v01.md`
4. `.agent-temp/editorial-manuscript-rebuild/orchestration/worker-a/worker-a_round-03_result_v01.md`
5. `.agent-temp/editorial-manuscript-rebuild/browser-qa/20260318_editorial-manuscript-browser-qa_runtime-pack_v01.md`
6. `.agent-temp/editorial-manuscript-rebuild/browser-qa/20260318_editorial-manuscript-browser-qa_runtime-evidence-index_v01.md`
