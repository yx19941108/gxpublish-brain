---
Owner: Codex
Task: editorial-mainline-closure
Status: in_progress
Updated: 2026-03-19 08:31 +08:00
---

# 20260319 editorial-mainline-closure runtime progress report v01

## 已确认
- 当前主线仍为 `main @ 30189ad`，禁止触碰的 `doc/agent/reports/20260304_AI_Policy_Review_report_v02.md` 与 `test_qa_flow.py` 未被修改。
- backend 在 `JDK 17.0.16` 下完成 fresh 源码运行：`Brain/brain-modules/brain-editorial` 模块测试 `9/9` 通过，重新 `mvn install` 成功；`Brain/brain-admin` 源码 `spring-boot:run` fresh 启动成功。
- fresh `GET /auth/code` 返回 `200`，服务端日志记录了真实请求命中 `/auth/code`。
- fresh API 回归重新执行 `.agent-temp/editorial_fulltest_api.py`，结果仍为 `52 total / 49 passed / 3 failed`；失败项精确为 `CERT-NOT-FIRST`、`CERT-SECOND-VISIBLE`、`CERT-STATUS-SKIP`。
- live DB 真值已确认：
  - `flow_definition.flow_code='editorial_review_flow'` 的活跃定义存在，`form_path=/editorial/review/detail`。
  - `flow_skip` 当前 applicant 两条路由条件分别为 `ne@@isCertified|true` 和 `eq@@isCertified|true`，更新时间 `2026-03-18 17:12:38`。
- cert 失败已收敛到 runtime：最新认证作者稿件 `brain_editorial_review.id=2034425352831672322` 仍为 `review_status=10,status=waiting`；其 `flow_instance.variable` 明确带有 `"isCertified":true`，但实例落点是 `first-review-node`，并生成了 `first-review-node` waiting task。
- fresh `resubmit` 真实 HTTP 已单独补齐：`submit -> back -> resubmit` 全链路返回 `200/200`，并重新进入 `reviewStatus=10,status=waiting`，一级审批待办重新可见。证据见 `.agent-temp/editorial_resubmit_probe_result.json`。
- front-end boundary 由主控独立复核：`plus-ui-ts` 下 `pnpm exec vitest run src/views/editorial/review/integration.spec.ts` fresh `7/7` 通过；`pnpm build:prod` fresh 通过。
- fresh TUS 纯 HTTP 探针已补齐：`POST /tus/upload -> PATCH /tus/upload/{id} -> POST /tus/upload/finish` 路由全通，但 `finish` 返回业务失败 `code=500`，根因为对象存储 `NoSuchBucketException`。证据见 `.agent-temp/editorial_tus_probe_result.json`。

## 风险
- `CERT-*` 三项失败在 fresh restart 后依然复现，说明不是旧 jar、旧缓存或 SQL 未 apply 的假象，而是 workflow runtime / 路由判定层面的真实 blocker。
- `TUS finish` 当前不是前端调用链断裂，而是 backend 存储最终落库/落 OSS 的 runtime 配置问题；在 bucket 未修复前不能宣称 upload 链路 ready。
- `BROWSER_EVIDENCED` 仍未达成：当前 bundle 中没有 fresh 截图 / HAR / console / request-response 集合。

## 待确认
- certified skip 的最小正确修复点：editorial 变量类型适配、workflow runtime 条件判定、或 warm-flow skip 选择逻辑中的哪一层。
- 是否允许在主控本地直接落一个 editorial 边界内最小 workaround（例如 `isCertified` 变量类型调整）并用 TDD + fresh HTTP 验证。
- TUS `NoSuchBucketException` 是测试环境配置缺失还是代码/策略绑定错误。

## 推进方案
1. Backend：围绕 cert skip 做最小修复设计并经确认后实施；优先验证 `isCertified` 变量类型与 skip 条件比较是否失配。
2. Backend/Infra：定位并修复 TUS finish 依赖的 bucket 配置，使 `POST /tus/upload/finish` 返回 `200/code=200`。
3. QA/Browser：在 fresh runtime 基础上补浏览器级登录、submit、10->20->30->40、back、cancel、termination、resubmit、TUS finish 的截图/HAR/console/request-response。
4. 汇总：拿到 cert skip + TUS finish + browser evidence 后，再判断 `REAL_HTTP_READY` / `BROWSER_EVIDENCED` / `可测试`。

## Decision
- 当前 gate 状态：
  - `SQL_APPLY_READY`: 基本具备，且 applicant skip 条件已在 live DB 真值中确认。
  - `SQL_APPLIED`: 就 cert skip 微修而言已在 live DB 生效，但缺整包 apply/rollback 归档证据。
  - `REAL_HTTP_READY`: 未通过，精确阻塞在 `CERT-*` 与 `TUS finish`。
  - `BROWSER_EVIDENCED`: 未开始。
  - `可测试`: 不成立。

## Artifacts
- `.agent-temp/editorial_api_results.json`
- `.agent-temp/editorial_resubmit_probe_result.json`
- `.agent-temp/editorial_tus_probe_result.json`
- `.agent-temp/editorial-mainline-closure_20260318-184705/mysql/20260318_certified_skip_before.tsv`
- `Brain/script/sql/update/20260318_editorial_review_certified_skip_fix.sql`
- `Brain/script/sql/update/20260318_editorial_review_certified_skip_fix_rollback.sql`

## Next Gate
- 最近阻塞 gate：`REAL_HTTP_READY`
- 精确 blocker：
  1. cert applicant submit 仍落到 `first-review-node`
  2. `/tus/upload/finish` 仍报 `NoSuchBucketException`
  3. browser evidence 尚未采集
