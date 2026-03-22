Owner: Codex
Task: manuscript-review handoff
Status: WP7 completed; WP8 in progress (external link validation); SQL apply not closed loop
Updated: 2026-03-22 19:49 CST

# Manuscript Review Handoff v04 (Latest, Only Entry)

## 0. 重要声明（唯一续接入口）

v03 已过时，禁止再作为续接入口（历史留档，不再维护）：
- `/home/kugua/.config/superpowers/worktrees/gxpublish-brain/manuscript-review-tdd-20260321/doc/agent/manuscript-review-tdd_20260321-020326/handoffs/20260322_manuscript-review_handoff_v03.md`

本文件 v04 为当前唯一续接入口。下一会话必须从 `v04 + 需求真源(PRD)` 恢复上下文。

## 1. 当前 worktree / 分支 / HEAD / dirty（可读现状）

Worktree 根路径：
`/home/kugua/.config/superpowers/worktrees/gxpublish-brain/manuscript-review-tdd-20260321`

Git 基线（2026-03-22 19:49 CST 复核）：
- 分支：`manuscript-review-tdd-20260321`
- HEAD：`23428b81a12a6aa7becea6c4657b98703a56cd26`（`23428b8 2026-03-19T18:00:55+08:00 测试部分修改`）
- 工作树：Dirty

`git status -sb` 摘要（避免误判“已提交/已闭环”，仅列关键面）：
- `## manuscript-review-tdd-20260321`
- `M Brain/pom.xml`
- `M Brain/brain-modules/pom.xml`
- `M Brain/brain-admin/pom.xml`
- `M plus-ui-ts/package.json`
- `M plus-ui-ts/src/router/index.ts`
- `M plus-ui-ts/.eslintrc-auto-import.json`（高概率自动生成漂移，提交前需单独核对）
- `?? Brain/brain-modules/brain-manuscript-review/`
- `?? Brain/script/sql/update/20260321_manuscript_review_*_v01.sql`
- `?? doc/agent/manuscript-review-tdd_20260321-020326/`
- `?? doc/manuscript_review/`
- `?? plus-ui-ts/src/types/manuscript-review/`
- `?? plus-ui-ts/src/views/manuscript-review/`

## 2. 需求真源 + 硬禁边界（禁止旧 editorial）

唯一需求真源（PRD，必须优先读）：
`/home/kugua/.config/superpowers/worktrees/gxpublish-brain/manuscript-review-tdd-20260321/doc/manuscript_review/product/20260321_manuscript-review_product-requirements_v01.md`

硬禁边界（禁止旧 editorial 作为真源或兼容基线）：
- 禁止把旧 editorial 业务实现、页面、API、SQL 当作需求真源或兼容基线，包括但不限于：
- `Brain/brain-modules/brain-editorial/**`
- `plus-ui-ts/src/views/editorial/review/**`
- `plus-ui-ts/src/api/editorial/review.ts`
- 旧 editorial SQL update 脚本

允许复用的仅通用平台能力与官方文档（用于补充/交叉验证，不得反向覆盖 PRD）：
- `Brain/brain-common/**`
- `Brain/brain-modules/brain-workflow/**`
- `Brain/brain-system/**`
- `doc/official_doc/**`

## 3. 已完成 WP1-WP7：结果与关键证据摘要

### WP1（Plan/ADR/模块接线）：已完成

结果：
- 形成 Accepted ADR 与可执行 Plan；完成新模块 `brain-manuscript-review` 的 Maven 接线面进入变更集。

关键证据：
- ADR：`doc/agent/manuscript-review-tdd_20260321-020326/decisions/ADR-0001_manuscript-review-module-implementation-strategy.md`（Accepted）
- Plan：`doc/agent/manuscript-review-tdd_20260321-020326/plans/20260321_manuscript-review_tdd-implementation_plan_v01.md`
- Maven wiring 变更面（dirty 可见）：`Brain/pom.xml`、`Brain/brain-modules/pom.xml`、`Brain/brain-admin/pom.xml`

### WP2（RED tests：领域规则先行）：已完成

结果：
- 核心领域规则已被测试锁定（标题/正文必填、附件或外链至少一种、外链协议/重复、审批链配置缺口拦截、持证跳一级等）。

关键证据（Surefire 报告）：
- `Brain/brain-modules/brain-manuscript-review/target/surefire-reports/com.gxpublish.brain.manuscript.review.service.ManuscriptReviewServiceTest.txt`
- `Brain/brain-modules/brain-manuscript-review/target/surefire-reports/com.gxpublish.brain.manuscript.review.service.ManuscriptReviewDetailPermissionPolicyTest.txt`

### WP3（Green slice 1：最小可验证实现）：已完成

结果：
- `ManuscriptReviewService` 形成可测的 create/submit/resubmit 领域契约；未引入旧 editorial 草稿/删除语义。

关键证据（代码落点）：
- `Brain/brain-modules/brain-manuscript-review/src/main/java/com/gxpublish/brain/manuscript/review/service/ManuscriptReviewService.java`
- `Brain/brain-modules/brain-manuscript-review/src/main/java/com/gxpublish/brain/manuscript/review/domain/command/CreateManuscriptReviewCommand.java`
- `Brain/brain-modules/brain-manuscript-review/src/main/java/com/gxpublish/brain/manuscript/review/domain/command/ResubmitManuscriptReviewCommand.java`

### WP4（SQL 起草：DDL/DML/Rollback）：已完成（脚本已生成，但 apply 未闭环）

结果：
- SQL 脚本已落在 `Brain/script/sql/update/`，具备 apply 与 rollback 草案。

关键证据（脚本落点）：
- `Brain/script/sql/update/20260321_manuscript_review_ddl_v01.sql`
- `Brain/script/sql/update/20260321_manuscript_review_dml_v01.sql`
- `Brain/script/sql/update/20260321_manuscript_review_rollback_v01.sql`

### WP5（数据库现状/号段预检查）：已完成（结论为“未闭环 apply”）

结果：
- 当前环境尚未形成 manuscript-review 业务表面；任何依赖落库/流程联动的实现，必须先把 SQL apply 闭环作为硬门槛。

关键证据（v03 已记录的 MySQL 复核结论，且仍未被新的 apply 证据覆盖）：
- 仅存在 `brain_manuscript_review_serial`；其余 `brain_manuscript_review*` 表不存在。
- `brain_manuscript_review_flow_config` 表不存在。

### WP6（外链校验规则拆解与测试对齐）：已完成（规则已被测试锁定）

结果：
- PRD 外链校验规则已进入单测与领域校验的共同基线。

关键证据：
- PRD（外链校验）：同一流程内 URL 不重复；只允许 `http://`/`https://`（见 PRD “（三）外链校验”）。
- 单测名称可读证据：`TEST-com.gxpublish.brain.manuscript.review.service.ManuscriptReviewServiceTest.xml` 中包含：
- `shouldRejectSubmitWhenExternalLinkUrlProtocolInvalid`
- `shouldRejectSubmitWhenExternalLinkUrlsDuplicateWithinFlow`

### WP7（修复 resubmit 信任边界）：已完成

结果（关键点）：
- resubmit 不再信任 `ResubmitManuscriptReviewCommand.returnedToInitiator` 入参，而是改走 `ManuscriptReviewResubmitGateway` 的可信推导口径。

关键证据：
- 代码：`ManuscriptReviewService#resubmit(...)` 使用 `resubmitGateway.isReturnedToInitiator(reviewId, applicantUserId)`（见 `Brain/brain-modules/brain-manuscript-review/src/main/java/com/gxpublish/brain/manuscript/review/service/ManuscriptReviewService.java`）。
- 测试：`TEST-com.gxpublish.brain.manuscript.review.service.ManuscriptReviewServiceTest.xml` 含用例
- `shouldRejectResubmitWhenGatewayReportsNotReturnedEvenIfCommandClaimsReturnedToInitiator`
- Maven 证据（游工口径要求固化）：2026-03-22 18:54:33+08:00 定向执行 `mvn ... test`，`BUILD SUCCESS`，且当时输出为 `Tests run: 13, Failures: 0, Errors: 0, Skipped: 0`。
- 当前可复核的 Surefire 证据（最近一次报告文件时间为 2026-03-22 19:36）：`ManuscriptReviewServiceTest` 为 `Tests run: 14`，`ManuscriptReviewDetailPermissionPolicyTest` 为 `Tests run: 1`，均为 0 失败/错误。

## 4. WP8 当前目标与进行中状态（外链校验）

目标（来自 PRD 的硬规则）：
- 外链只允许合法的 `http://` 或 `https://` 协议。
- 同一流程内 URL 不允许重复。

进行中状态（为什么仍算 WP8 推进中，而非“已闭环完成”）：
- 已完成：领域校验已落在 `ManuscriptReviewService#validateExternalLinkUrls(...)`，且单测已覆盖协议非法与同流程 URL 重复场景。
- 未闭环：外链校验仍停留在 domain/service 层；尚未形成“HTTP API 入参校验 + 错误码/错误文案策略 + 持久化/历史审计 + 联调证据”的闭环。
- 需求点提醒：PRD 还要求“对外链做联网可达性探测”（是否属于 WP8 范围需要在下一 gate 明确；涉及 SSRF/超时/重试/异步化策略，不能草率直连）。

定向命令（下一会话复核/推进用）：
- 读 PRD 外链校验条款：`rg -n \"外链校验|同一流程内 URL 不允许重复|http://|https://\" doc/manuscript_review/product/20260321_manuscript-review_product-requirements_v01.md -S`
- 定位当前领域实现：`rg -n \"validateExternalLinkUrls|EXTERNAL_LINK_\" Brain/brain-modules/brain-manuscript-review/src/main/java/com/gxpublish/brain/manuscript/review/service/ManuscriptReviewService.java -S`
- 定向跑后端单测（以 Plan 的显式 JDK/Maven 路径为准）：
  - `cd /home/kugua/.config/superpowers/worktrees/gxpublish-brain/manuscript-review-tdd-20260321/Brain`
  - `JAVA_HOME=/home/kugua/.local/share/mise/installs/java/temurin-17.0.18+8 /home/kugua/.local/share/mise/installs/maven/3.9.13/apache-maven-3.9.13/bin/mvn -pl brain-modules/brain-manuscript-review -am -DskipTests=false -Dtest=ManuscriptReviewServiceTest test`

## 5. 当前高风险/待确认项（最关键 5-8 条）

- worktree dirty 且跨面过大：后端 pom + 新模块 + SQL + 前端 router/types/views + docs 同时处于未提交状态，必须尽快收敛最小可提交集与回滚边界。
- SQL apply 未闭环：脚本“已写出”不等于“已落库”；未形成备份/清理/重复 apply/rollback/查询证据闭环前，不要推进依赖落库的联调实现。
- `applicantUserId` 权威来源：当前 domain/service 仍信任 command 输入；后续必须切到登录态/流程实例关联的可信身份，并避免对业务端泄露 raw userId。
- `ManuscriptReviewResubmitGateway` 实现位置：当前仅有 interface（单测使用 mock）；需要确定由哪个模块承接（workflow 查询/持久化查询），以及 Spring 注入 wiring 的最小闭环方式。
- 错误文案/错误码策略：当前使用 `ServiceException` + 纯消息字符串；需要明确全局错误码映射、前端展示口径、以及是否要求 i18n/字段级定位。
- `ResubmitManuscriptReviewCommand.returnedToInitiator` 字段仍存在：虽然 WP7 已不再信任，但字段存在会诱发未来误用；需要决定是否删除、废弃或仅保留用于兼容但强制忽略。
- 外链“联网可达性探测”策略未定：需要显式安全策略（SSRF 防护、域名/协议白名单、超时/重试、异步化），否则不要直接在请求链路内探测。
- `plus-ui-ts/.eslintrc-auto-import.json` 变更风险：高概率为自动生成/换行漂移；提交前必须确认是否应纳入版本控制，避免无意义噪声污染。

## 6. WP9-WP11 一句话目标（后续 gate）

1. WP9（SQL apply 闭环）：形成可重复的 apply/rollback 闭环与查询证据，并完成最小 seeds（租户/角色/菜单/字典）对齐。
2. WP10（API 合同）：落地台账/详情/历史/资源区的业务可读 API 契约，硬拦技术 ID、账号、原始码值、`roleKey/permissionFlag` 外泄，并与前端 types/api 对齐。
3. WP11（历史审计/联调 gate）：完成“追加历史 + 当前可停用”的资源生命周期、统一时间线审计，以及 BPM 待办/已办与办理页联调证据包。

## 7. 子 agent 协议（主控编排）

- 主控只做编排/监控/收敛事实，不把“需求真源判断”和“硬禁边界执行”外包给子 agent。
- 默认只等“完成通知”；10 分钟无完成通知再轻量 ping；只要状态、阻塞、下一步，不刷长日志。
- 任何代码或持久化文件变更前必须先写清：Goal / Affected Files / Risk / Verification / Rollback。
- 禁止旧 editorial：子 agent 不得以旧 editorial 代码/SQL/API 作为方案输入或兼容基线。

## 8. 下一会话可直接粘贴提示词（不要读旧聊天，只从 v04 + PRD 恢复）

```text
你负责继续 gxpublish-brain 的 manuscript_review 线（clean-slate）。

不要读旧聊天；只从以下两类材料恢复上下文：
1) /home/kugua/.config/superpowers/worktrees/gxpublish-brain/manuscript-review-tdd-20260321/doc/agent/manuscript-review-tdd_20260321-020326/handoffs/20260322_manuscript-review_handoff_v04.md
2) /home/kugua/.config/superpowers/worktrees/gxpublish-brain/manuscript-review-tdd-20260321/doc/manuscript_review/product/20260321_manuscript-review_product-requirements_v01.md

先输出：
- 已确认
- 风险
- 待确认
- 推进方案

硬约束：
- 不要把旧 editorial 业务实现、页面、API、SQL 当真源或兼容基线；只允许复用通用平台能力与官方文档。
- 任何代码或持久化文件改动前，先给 Goal / Affected Files / Risk / Verification / Rollback。
- 若启用子 agent：主控只编排/监控；默认只等完成通知；10 分钟无完成通知再轻量 ping。

从以下“定向复核”开始（不要扩大范围）：
1) worktree 基线与 dirty：
cd /home/kugua/.config/superpowers/worktrees/gxpublish-brain/manuscript-review-tdd-20260321
git branch --show-current
git rev-parse HEAD
git status -sb

2) WP8 外链校验（PRD 条款与当前实现）：
rg -n "外链校验|同一流程内 URL 不允许重复|http://|https://" doc/manuscript_review/product/20260321_manuscript-review_product-requirements_v01.md -S
rg -n "validateExternalLinkUrls|EXTERNAL_LINK_" Brain/brain-modules/brain-manuscript-review/src/main/java/com/gxpublish/brain/manuscript/review/service/ManuscriptReviewService.java -S

3) WP8 定向单测复核（按 Plan 的显式 JDK/Maven 路径）：
cd /home/kugua/.config/superpowers/worktrees/gxpublish-brain/manuscript-review-tdd-20260321/Brain
JAVA_HOME=/home/kugua/.local/share/mise/installs/java/temurin-17.0.18+8 /home/kugua/.local/share/mise/installs/maven/3.9.13/apache-maven-3.9.13/bin/mvn -pl brain-modules/brain-manuscript-review -am -DskipTests=false -Dtest=ManuscriptReviewServiceTest test

4) WP9 前置：数据库表面是否已 apply：
SELECT table_name, table_rows FROM information_schema.tables WHERE table_schema = 'gxpublish_brain' AND table_name LIKE 'brain_manuscript_review%' ORDER BY table_name;
SELECT COUNT(*) AS flow_config_table_exists FROM information_schema.tables WHERE table_schema = 'gxpublish_brain' AND table_name = 'brain_manuscript_review_flow_config';

补充关注：resubmit 的 returnedToInitiator 已改为 gateway 推导，但 command 字段仍存在；applicantUserId 仍为入参，后续需切换为可信身份来源。
```

