---
Owner: Codex
Task: editorial-mainline-closure
Status: in_progress
Updated: 2026-03-19 09:55 +08:00
---

# 20260319 editorial-mainline-closure runtime progress report v03

## 已确认
- 当前主线仍为 `main`；你要求不要碰的 `doc/agent/reports/20260304_AI_Policy_Review_report_v02.md` 与 `test_qa_flow.py` 未被触碰。
- MySQL MCP 已在本会话重新验真为 `gxpublish_brain`：
  - `SELECT DATABASE()` 返回 `gxpublish_brain`
  - `mcp__mysql__all_table_names` 返回 `brain_editorial_*`、`flow_*` 等表
- frontend/browser 侧新增两条已验证修补：
  - `plus-ui-ts/src/views/editorial/review/integration.ts` 增补 numeric `reviewStatus` 映射：`10/20/30/40/50/60 -> WAITING_FIRST/WAITING_SECOND/WAITING_FINAL/APPROVED/BACK/TERMINATED`
  - `plus-ui-ts/src/views/editorial/review/detail.vue` / `integration.ts` 继续保留前端 approval button 降级逻辑，避免 `type=approval` 但 `canApprove=false` 时误显“审批”
- backend/browser support 侧新增一条已验证修补：
  - `Brain/brain-modules/brain-editorial/src/main/java/com/gxpublish/brain/editorial/enums/ReviewStatusEnum.java` 已让 `BACK` 对 applicant/applicant_has_certificate/admin 返回 `canEdit=true`
  - 在 fresh 运行面定向复核中，`review_after_back.canEdit=true`、`approvalContext.canEdit=true`
- fresh browser/runtime evidence 已补齐以下最低覆盖：
  - frontend 登录态建立
  - submit
  - TUS finish
  - `10 -> 20 -> 30 -> 40`
  - back
  - cancel
  - termination
  - resubmit
  - legacy fallback 到 `/editorial/review/detail?...&fallback=legacy`
- fresh browser harness 汇总文件已落盘，且最后一轮 `failures=[]`。

## 风险
- 本轮 browser 证据达成后，仍有一个非 gate 风险未收口：detail/list contract 里的 `user.name` 与 `dept.name` 在 live response 中仍为 `null`；这不再阻塞当前 gate，但仍建议后续单独清理。
- 主列表真实浏览器入口不是直接 `/editorial/review`，而是菜单父路径拼接后的 `/workflow/editorial/review`；后续脚本或人工验证若还用旧路径，会再次命中前端 404。
- 当前 `Brain/brain-admin` 运行日志里仍有 Snail-Job / Spring Boot Admin 外围告警，但未阻断 editorial browser 证据链。

## 待确认
- 若要继续向更高层验收推进，下一层更像是“UI 文案/展示完整性”而非“可测试性”：例如 `user/dept` 名称为空、部分历史 `operateType` 显示乱码。

## 推进方案
1. 当前阶段先以 `BROWSER_EVIDENCED=yes` 收口，不再把 fresh browser 证据和 build/HTTP 混写。
2. 下一阶段若继续推进，优先整理 detail/list contract 显示完整性与运行时展示噪声。
3. 若再开新会话，直接从本报告 v03 与 handoff v03 恢复，不再回退到 v02 的 `BROWSER_EVIDENCED=no`。

## Decision
- 当前 gate 状态：
  - `SQL_APPLY_READY`: yes
  - `SQL_APPLIED`: yes
  - `REAL_HTTP_READY`: yes
  - `BROWSER_EVIDENCED`: yes
  - `可测试`: yes

## Need From Backend
- 当前 gate 已不再阻塞。
- 后续优化项：补齐 detail/list contract 中 `user.name`、`dept.name` 的 live 展示值，清理个别 `operateType` 文本乱码。

## Need From Frontend
- 当前 gate 已不再阻塞。
- 后续优化项：继续把 numeric `reviewStatus`、主菜单真实路由 `/workflow/editorial/review` 这些运行面约束固化进后续自动化与文档。

## Need From QA/Browser
- 当前 gate 已不再阻塞。
- 后续若做更高层验收，直接复用 `.agent-temp` 中现有 evidence 目录即可，无需从零重录。

## Artifacts
- `.agent-temp/editorial-mainline-closure_20260318-184705/browser/editorial_browser_evidence_summary.json`
- `.agent-temp/editorial-mainline-closure_20260318-184705/browser/browser_back_resubmit_runtime_refresh.json`
- `.agent-temp/editorial-mainline-closure_20260318-184705/browser/evidence/submit_and_fallback/*`
- `.agent-temp/editorial-mainline-closure_20260318-184705/browser/evidence/cancel/*`
- `.agent-temp/editorial-mainline-closure_20260318-184705/browser/evidence/progress_first/*`
- `.agent-temp/editorial-mainline-closure_20260318-184705/browser/evidence/progress_second/*`
- `.agent-temp/editorial-mainline-closure_20260318-184705/browser/evidence/progress_third/*`
- `.agent-temp/editorial-mainline-closure_20260318-184705/browser/evidence/progress_detail_history/*`
- `.agent-temp/editorial-mainline-closure_20260318-184705/browser/evidence/back_resubmit/*`
- `.agent-temp/editorial-mainline-closure_20260318-184705/browser/evidence/resubmit/*`
- `.agent-temp/editorial-mainline-closure_20260318-184705/browser/evidence/termination/*`

## Next Gate
- 最近目标 gate：`BROWSER_EVIDENCED`
- 结果：已达成
- 当前下一层判断：`可测试`
- 结果：已达成
