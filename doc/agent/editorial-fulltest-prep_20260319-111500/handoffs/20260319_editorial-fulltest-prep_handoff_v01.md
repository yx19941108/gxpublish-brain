---
Owner: Codex
Task: editorial-fulltest-prep
Status: handoff
Updated: 2026-03-19 10:40:37 +08:00
---

# 审校主线全量测试阶段 handoff v01

## Resume Entry

下一会话从以下顺序恢复：

1. `doc/agent/editorial-fulltest-prep_20260319-111500/plans/20260319_editorial-fulltest-prep_plan_v02.md`
2. `doc/agent/editorial-fulltest-prep_20260319-111500/reviews/20260319_editorial-fulltest-prep_review_v01.md`
3. `doc/agent/editorial-mainline-closure_20260318-184705/reports/20260319_editorial-mainline-closure_runtime-progress_report_v03.md`
4. `doc/agent/editorial-mainline-closure_20260318-184705/handoffs/20260319_editorial-mainline-closure_runtime-progress_handoff_v03.md`
5. `.agent-temp/editorial-mainline-closure_20260318-184705/browser/**`

## Current State

- `SQL_APPLY_READY=yes`
- `SQL_APPLIED=yes`
- `REAL_HTTP_READY=yes`
- `BROWSER_EVIDENCED=yes`
- `可测试=yes`
- 测试方案状态：`reviewed_pending_input`

## Missing Inputs Before Execution

下会话正式开跑前，仍需先锁定以下输入：

1. 发起人、持证发起人、一级审批、二级审批、三级审批 5 类账号。
2. 每类账号的登录方式、所属租户、关键权限。
3. 是否按角色隔离浏览器上下文。
4. 每条主链的 `reviewId / applyCode / attachmentId / evidence 路径` 绑定。

## Next-Session Prompt

```text
游工要求你在当前会话接管 `C:\kuguaHome\project\gxpublish-brain` 的 `main` 分支，进入 editorial 全量测试阶段。

先读且只读以下工件：
1. `C:\kuguaHome\project\gxpublish-brain\doc\agent\editorial-fulltest-prep_20260319-111500\plans\20260319_editorial-fulltest-prep_plan_v02.md`
2. `C:\kuguaHome\project\gxpublish-brain\doc\agent\editorial-fulltest-prep_20260319-111500\reviews\20260319_editorial-fulltest-prep_review_v01.md`
3. `C:\kuguaHome\project\gxpublish-brain\doc\agent\editorial-mainline-closure_20260318-184705\reports\20260319_editorial-mainline-closure_runtime-progress_report_v03.md`
4. `C:\kuguaHome\project\gxpublish-brain\doc\agent\editorial-mainline-closure_20260318-184705\handoffs\20260319_editorial-mainline-closure_runtime-progress_handoff_v03.md`

当前已确认：
- `SQL_APPLIED=yes`
- `REAL_HTTP_READY=yes`
- `BROWSER_EVIDENCED=yes`
- `可测试=yes`
- 但 `plan_v02` 仍是 `reviewed_pending_input`，不能跳过输入锁定直接开跑

本会话目标：
1. 先补齐 5 类账号、登录方式、所属租户、关键权限
2. 先过 `G-00 ~ G-06`
3. 仅在门禁全部通过后，按 `plan_v02` 的顺序执行全量测试
4. 所有“最高”优先级用例必须留 fresh 证据，旧 evidence 只可作为 baseline 参考

最低执行纪律：
- 不要把 build/lint/minimal HTTP 混写成“测试通过”
- 不要拿旧截图或旧 JSON 充当本会话通过证据
- 不要在账号、样本、证据路径未绑定前扩大执行
- 若任一“最高”优先级用例失败，立即停在阻断结论，不继续扩散

输出结构使用：
- 已确认
- 风险
- 待确认
- 推进方案
- Decision
- Need From Backend
- Need From Frontend
- Need From QA/Browser
- Artifacts
- Next Gate
```

## Context Warning

若下一会话仍要继续扩展 child-agent 编排、账号锁定、浏览器取证和 SQL 核验，建议直接从本 handoff v01 启动，不要再回退到聊天窗口里的零散结论。
