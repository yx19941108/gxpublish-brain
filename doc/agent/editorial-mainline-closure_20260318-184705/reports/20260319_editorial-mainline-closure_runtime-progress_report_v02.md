---
Owner: Codex
Task: editorial-mainline-closure
Status: in_progress
Updated: 2026-03-19 09:08 +08:00
---

# 20260319 editorial-mainline-closure runtime progress report v02

## 已确认
- 当前主线仍为 `main @ 30189ad`，禁止触碰的 `doc/agent/reports/20260304_AI_Policy_Review_report_v02.md` 与 `test_qa_flow.py` 未被修改。
- `certified skip` 已确认根因为 `warm-flow 1.8.4` 只在 gateway 节点评估 `skipCondition`；普通节点双 `PASS` 只取第一条。源码 flow 已改为 `applicant-node -> certified-route-node -> first/second-review-node`。
- live DB 已执行 gateway 化 apply，并保留前后快照证据。apply 后 active definition 仍为 `flow_code=editorial_review_flow`、`form_path=/editorial/review/detail`，且路由已变为：
  - `applicant-node -> certified-route-node`
  - `certified-route-node -> first-review-node (ne@@isCertified|true)`
  - `certified-route-node -> second-review-node (eq@@isCertified|true)`
- backend 在 `JDK 17.0.16` 下 fresh 源码运行成功；`Brain/brain-modules/brain-editorial` 当前定向测试为 `11/11` 通过，fresh `GET /auth/code` 返回 `200`。
- fresh full API 回归已从 `49/52` 提升到 `52/52`；`CERT-NOT-FIRST`、`CERT-SECOND-VISIBLE`、`CERT-STATUS-SKIP` 三项已全部转绿。
- fresh `resubmit` 真实 HTTP 已单独补齐：`submit -> back -> resubmit -> waiting -> 一级待办重新可见`，单独探针 `8/8` 通过。
- fresh `TUS finish` 已在 bucket 手动补齐后通过：`POST /tus/upload -> PATCH /tus/upload/{id} -> POST /tus/upload/finish` 返回业务 `code=200`，并生成 `ossId/url`。
- front-end boundary 已由主控独立复核：`plus-ui-ts` 下 `pnpm exec vitest run src/views/editorial/review/integration.spec.ts` fresh `7/7` 通过；`pnpm build:prod` fresh 通过。
- MySQL MCP 当前**不能**按“已指向 gxpublish schema 可用”落盘。主控在本轮直接调用 `mcp__mysql__all_table_names` 与 `mcp__mysql__filter_table_names('editorial')`，返回的是 `nexus-platform/system_*` 表而不是 `gxpublish_brain` 的 `brain_editorial_* / flow_*`。本轮已验证可用的真值源仍是 `local pymysql -> gxpublish_brain`。

## 风险
- `BROWSER_EVIDENCED` 仍未达成：当前 bundle 中没有 fresh 截图、HAR、console、request-response 集合。
- 因浏览器级关键链路证据仍缺，当前仍不能宣称 `可测试`。
- `mysql mcp` 与当前 repo DB 目标仍存在漂移风险；下个会话若要使用 MySQL MCP，必须先重新验证，不可直接假定指向 `gxpublish_brain`。

## 待确认
- 浏览器级登录态、create/detail/approval/history/fallback/upload/TUS finish 的 fresh 证据包。
- 如果新会话要使用 MySQL MCP，需先确认是否已真正切到 `gxpublish_brain`。

## 推进方案
1. 新会话继续采用 in-session child-agent 并行编排。
2. 主控优先推进 `BROWSER_EVIDENCED`：登录、submit、`10 -> 20 -> 30 -> 40`、back、cancel、termination、resubmit、TUS finish 的截图/HAR/console/request-response。
3. 前端线继续只收口 browser 交互与 upload/detail/fallback 细节，不再重复 backend 逻辑排障。
4. QA 线基于现有 fresh HTTP 结果回填浏览器矩阵，并据此判断 `可测试`。
5. 若新会话确实要用 MySQL MCP，先把 `mcp__mysql__all_table_names` / `filter_table_names` 当成 Gate 0，而不是默认信任。

## Decision
- 当前 gate 状态：
  - `SQL_APPLY_READY`: 成立
  - `SQL_APPLIED`: 成立
  - `REAL_HTTP_READY`: 成立
  - `BROWSER_EVIDENCED`: 未成立
  - `可测试`: 未成立

## Artifacts
- [20260319_editorial-mainline-closure_runtime-progress_report_v01.md](/C:/kuguaHome/project/gxpublish-brain/doc/agent/editorial-mainline-closure_20260318-184705/reports/20260319_editorial-mainline-closure_runtime-progress_report_v01.md)
- [20260319_gateway_fix_apply.json](/C:/kuguaHome/project/gxpublish-brain/.agent-temp/editorial-mainline-closure_20260318-184705/mysql/20260319_gateway_fix_apply.json)
- [20260319_resubmit_probe.json](/C:/kuguaHome/project/gxpublish-brain/.agent-temp/editorial-mainline-closure_20260318-184705/api/20260319_resubmit_probe.json)
- [20260319_tus_probe_before_bucket.json](/C:/kuguaHome/project/gxpublish-brain/.agent-temp/editorial-mainline-closure_20260318-184705/api/20260319_tus_probe_before_bucket.json)
- [20260319_tus_probe_after_bucket.json](/C:/kuguaHome/project/gxpublish-brain/.agent-temp/editorial-mainline-closure_20260318-184705/api/20260319_tus_probe_after_bucket.json)
- [20260318_editorial_review_certified_skip_fix.sql](/C:/kuguaHome/project/gxpublish-brain/Brain/script/sql/update/20260318_editorial_review_certified_skip_fix.sql)
- [20260318_editorial_review_certified_skip_fix_rollback.sql](/C:/kuguaHome/project/gxpublish-brain/Brain/script/sql/update/20260318_editorial_review_certified_skip_fix_rollback.sql)

## Next Gate
- 最近目标 gate：`BROWSER_EVIDENCED`
- 精确剩余 blocker：
  1. 浏览器级关键链路尚未形成 fresh 证据包
  2. `mysql mcp` 当前仍未验证为 `gxpublish_brain` 真值源
