Owner: Codex
Task: manuscript-review handoff
Status: Ready for WP6 continuation
Updated: 2026-03-22 18:03 CST

# Manuscript Review Handoff v02 (Latest)

## 0. 重要声明

- `v01` 已过时，不再作为续接入口：
`/home/kugua/.config/superpowers/worktrees/gxpublish-brain/manuscript-review-tdd-20260321/doc/agent/manuscript-review-tdd_20260321-020326/handoffs/20260322_manuscript-review_handoff_v01.md`
- 本文件 `v02` 是当前唯一续接入口。下一会话从 `v02 + 需求真源` 恢复上下文。

## 1. 当前 worktree / 分支上下文

Worktree 根路径：
`/home/kugua/.config/superpowers/worktrees/gxpublish-brain/manuscript-review-tdd-20260321`

Git 分支与基线：
- 分支：`manuscript-review-tdd-20260321`
- HEAD：`23428b81a12a6aa7becea6c4657b98703a56cd26`
- 工作树：Dirty（存在未提交修改与未跟踪文件）

关键证据命令（建议下一会话复核）：
`cd /home/kugua/.config/superpowers/worktrees/gxpublish-brain/manuscript-review-tdd-20260321 && git status --porcelain=v1`

Dirty 摘要（只列关键面，避免误判“全量已提交”）：
- `Brain/**/pom.xml` 有改动（新模块 wiring）
- `Brain/brain-modules/brain-manuscript-review/` 未跟踪（新模块主体）
- `Brain/script/sql/update/20260321_manuscript_review_*_v01.sql` 未跟踪（审校 SQL 草案）
- `plus-ui-ts/src/views/manuscript-review/**`、`plus-ui-ts/src/types/manuscript-review/**` 未跟踪（前端模块草案）
- `plus-ui-ts/package.json`、`plus-ui-ts/src/router/index.ts` 有改动（前端路由/依赖面已进入变更）

## 2. 需求真源与硬禁边界

唯一需求真源（PRD）：
`/home/kugua/.config/superpowers/worktrees/gxpublish-brain/manuscript-review-tdd-20260321/doc/manuscript_review/product/20260321_manuscript-review_product-requirements_v01.md`

硬禁边界（禁止旧 editorial 作为真源或兼容基线）：
- 禁止把旧 editorial 业务实现、页面、API、SQL 当作需求真源或兼容基线，包括但不限于：
`Brain/brain-modules/brain-editorial/**`
`plus-ui-ts/src/views/editorial/review/**`
`plus-ui-ts/src/api/editorial/review.ts`
旧 editorial SQL update 脚本

允许复用的仅通用平台能力与官方文档：
`Brain/brain-common/**`
`Brain/brain-modules/brain-workflow/**`
`Brain/brain-system/**`
`doc/official_doc/**`

## 3. WP1-WP5 结果与证据摘要

WP1（Plan/ADR/模块接线）：已完成。
- 结果：ADR 与实施计划已落地；新后端模块 `brain-manuscript-review` 已接入 Maven wiring。
- 证据：文件存在：
`doc/agent/manuscript-review-tdd_20260321-020326/decisions/ADR-0001_manuscript-review-module-implementation-strategy.md`
`doc/agent/manuscript-review-tdd_20260321-020326/plans/20260321_manuscript-review_tdd-implementation_plan_v01.md`

WP2（首批 RED tests + 前端壳层 contract）：已沉淀并可运行。
- 后端关键验证命令（2026-03-22 18:01 通过）：
`cd /home/kugua/.config/superpowers/worktrees/gxpublish-brain/manuscript-review-tdd-20260321/Brain`
`JAVA_HOME=/home/kugua/.local/share/mise/installs/java/temurin-17.0.18+8 /home/kugua/.local/share/mise/installs/maven/3.9.13/apache-maven-3.9.13/bin/mvn -pl brain-modules/brain-manuscript-review -am -DskipTests=false test`
- 证据摘要：`BUILD SUCCESS`；`Tests run: 11, Failures: 0, Errors: 0, Skipped: 0`
- 前端关键验证命令（2026-03-22 18:01 通过）：
`cd /home/kugua/.config/superpowers/worktrees/gxpublish-brain/manuscript-review-tdd-20260321/plus-ui-ts`
`pnpm exec vitest run src/views/manuscript-review/route-shell.spec.ts src/views/manuscript-review/detail-readable-display.spec.ts`
- 证据摘要：2 个文件通过，4 个测试通过

WP3（Green slice 1）：已完成首切片并全绿（unit scope）。
- 结果：`Create/Resubmit` 的 domain-level contract + `ManuscriptReviewService` 基础切片已存在，能满足当前单测基线。
- 证据：同 WP2 的 Maven 定向 `test` 已全绿；同时存在 surefire 报告：
`Brain/brain-modules/brain-manuscript-review/target/surefire-reports/com.gxpublish.brain.manuscript.review.service.ManuscriptReviewServiceTest.txt`
`Brain/brain-modules/brain-manuscript-review/target/surefire-reports/com.gxpublish.brain.manuscript.review.service.ManuscriptReviewDetailPermissionPolicyTest.txt`

WP4（DDL/DML/Rollback 脚本起草）：脚本已完成，但数据库 apply 未闭环。
- 结果：`20260321_manuscript_review_ddl_v01.sql`、`..._dml_v01.sql`、`..._rollback_v01.sql` 已写出备份/apply/rollback 顺序，并显式写明“一期无草稿/无删除语义”。
- 关键证据（MySQL，2026-03-22 18:02）：
执行 `SELECT table_name, table_rows FROM information_schema.tables WHERE table_schema = 'gxpublish_brain' AND table_name LIKE 'brain_manuscript_review%' ORDER BY table_name;`
结果摘要：仅 `brain_manuscript_review_serial` 存在（0 行），其余 `brain_manuscript_review*` 表均不存在。
执行 `SELECT COUNT(*) AS flow_config_table_exists FROM information_schema.tables WHERE table_schema = 'gxpublish_brain' AND table_name = 'brain_manuscript_review_flow_config';`
结果摘要：`0`（表不存在）。
执行号段占用检查（60000-65001）：
`sys_menu 60000-60007`、`sys_role 63000-63004`、`sys_dict_type 61000-61003`、`sys_dict_data 62000-62039`、`sys_user 64000-64004`、`sys_user_role 64000-64004` 计数均为 `0`（当前环境未占用，但仍需 apply 前备份与复核）。

WP5（后端扩展下一步）：当前仅完成 unit 级 contract，尚未落库/未启动真实 BPM 流转/未闭环历史与资源生命周期。
- 已确认：`ResubmitManuscriptReviewCommand` 已存在且被 `ManuscriptReviewService` 使用；本轮后端不再被 “缺 class / testCompile” 阻塞。
- 待补齐：持久化与工作流、撤销/驳回/退回等状态机、统一时间线（自然语言）、资源追加历史与停用、以及对 `permissionFlag (role:<id>)` 的运行时转换策略。
- 关键验证命令：沿用 WP2 的 Maven 定向 `test` 作为后端编译与单测基线。

## 4. 当前风险 / 待确认项

- Worktree 脏：当前变更面跨后端 pom、后端新模块、SQL 草案、前端路由与页面/types，若直接扩范围容易失控；需要先决定“先收敛提交还是继续堆叠”。
- 数据库状态漂移风险：当前库里只有 `brain_manuscript_review_serial`，其余表与 seeds 均未落库；在未统一“清理回干净基线后重放”或“承认部分 apply 后补齐”的策略前，不要推进依赖落库的实现。
- `resubmit` 身份与权限上下文仍不完整：当前 contract 以 `applicantUserId` 为主，后续扩展到撤销/驳回/退回/审批办理时，需要明确 `operatorUserId`、是否允许代办、以及 `permissionFlag` 与角色映射落点，避免把技术值泄露到 API/UI。
- 号段冲突待确认：DML 预留 `60000-65001`，虽然当前环境计数为 0，但仍需要在真实 apply 前做备份与“全库唯一”复核。
- 硬禁边界易滑坡：仓库内旧 editorial 仍在，grep/复制粘贴很容易把旧实现当现成答案；任何引用旧 editorial 的方案都必须先停下、写出理由并请游工确认。
- 当前验证仍是“unit/contract 级”：尚未证明真实 API、真实 BPM 跳转、历史审计、资源生命周期、多视频标注闭环成立。

## 5. 后续 WP6-WP11 顺序与一句话目标

1. WP6：收敛数据库状态，确定备份与清理策略后，让 DDL/DML/rollback 可重复 apply，并用查询证据闭环。
2. WP7：在 TDD 下扩后端到“持久化 + 工作流启动/再次提交/撤销/驳回/退回”，并把权限与状态机口径固化为可测规则。
3. WP8：落地台账/详情/历史/资源区的业务可读 API 契约，硬拦技术 ID、账号、原始码值、`roleKey/permissionFlag` 外泄。
4. WP9：把前端 `index/form/detail` 与 BPM “去审批”跳转接到新 API，保持 `无草稿/无删除` 与“修改/再次提交”“修改/去审批”拆步口径。
5. WP10：做集成与浏览器级验收，覆盖 API 可读性、统一时间线、资源追加历史、多视频标注与 BPM 分流闭环，并沉淀证据包。
6. WP11：仓库治理与交付收尾（build/lint/敏感信息/临时文件），输出 report + handoff，并准备合入主线的最小安全变更集。

## 6. 子 agent 工作协议

- 主控职责：只编排/监控/收敛事实，不把“需求真源判断”和“硬禁边界执行”外包给子 agent。
- 默认节奏：只等完成通知；10 分钟无完成通知再轻量 ping。
- 轻量 ping 内容约束：只要状态、阻塞、下一步，不刷长日志。
- 变更前硬门槛：任何代码或持久化文件变更前必须先写清：
Goal / Affected Files / Risk / Verification / Rollback
- 禁止读旧 editorial：子 agent 不得以旧 editorial 代码/SQL/API 作为方案输入或兼容基线。

## 7. 下一会话可直接粘贴的提示词

```text
你负责继续 gxpublish-brain 的 manuscript_review 线。

不要读旧聊天；只从以下两类材料恢复上下文：
1) /home/kugua/.config/superpowers/worktrees/gxpublish-brain/manuscript-review-tdd-20260321/doc/agent/manuscript-review-tdd_20260321-020326/handoffs/20260322_manuscript-review_handoff_v02.md
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

建议从以下“验证事实”开始复核环境（不要扩大范围）：
1) 后端单测基线：
cd /home/kugua/.config/superpowers/worktrees/gxpublish-brain/manuscript-review-tdd-20260321/Brain
JAVA_HOME=/home/kugua/.local/share/mise/installs/java/temurin-17.0.18+8 /home/kugua/.local/share/mise/installs/maven/3.9.13/apache-maven-3.9.13/bin/mvn -pl brain-modules/brain-manuscript-review -am -DskipTests=false test
2) 前端 contract 基线：
cd /home/kugua/.config/superpowers/worktrees/gxpublish-brain/manuscript-review-tdd-20260321/plus-ui-ts
pnpm exec vitest run src/views/manuscript-review/route-shell.spec.ts src/views/manuscript-review/detail-readable-display.spec.ts
3) 数据库现状复核（MySQL）：
SELECT table_name, table_rows FROM information_schema.tables WHERE table_schema = 'gxpublish_brain' AND table_name LIKE 'brain_manuscript_review%' ORDER BY table_name;
SELECT COUNT(*) AS flow_config_table_exists FROM information_schema.tables WHERE table_schema = 'gxpublish_brain' AND table_name = 'brain_manuscript_review_flow_config';

推进顺序：WP6 -> WP11。
WP6 要先把“脚本完成但库内未 apply 闭环”的事实收敛清楚，再扩到依赖落库的实现。
```

