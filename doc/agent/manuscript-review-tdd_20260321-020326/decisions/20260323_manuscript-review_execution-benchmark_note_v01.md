Owner: Codex
Task: manuscript-review execution benchmark
Status: Current
Updated: 2026-03-23 17:05:00 +08:00

# Manuscript Review 执行基准说明 v01

## 1. 决议

自本文件生成起，`manuscript_review` 后续执行阶段统一以以下文档作为硬指标：

1. `doc/manuscript_review/product/20260321_manuscript-review_product-requirements_v01.md`
2. `doc/agent/manuscript-review-tdd_20260321-020326/plans/20260323_manuscript-review_execution-test-plan_v01.md`
3. `doc/agent/manuscript-review-tdd_20260321-020326/handoffs/20260323_manuscript-review_execution-controller_handoff_v01.md`

不允许脱离上述文档凭聊天记忆、旧 editorial 经验或随意推断推进执行。

## 2. 数据库基线

### 2.1 当前真实数据库口径

- 当前业务目标库：`gxpublish_brain`
- 虽然 MCP 默认连接返回的 `current_db` 仍显示 `approval_workflow`，但本轮已显式核对 `gxpublish_brain` 中 `brain_manuscript_review*` 表均存在。

### 2.2 已执行基础 SQL

以下 4 个 SQL 已由外部执行完成，后续不得再误报“未建表/未落基础 seed”：

1. `Brain/script/sql/update/20260321_manuscript_review_ddl_v01.sql`
2. `Brain/script/sql/update/20260321_manuscript_review_dml_v01.sql`
3. `Brain/script/sql/update/20260323_manuscript_review_dev_login_seed_v01.sql`
4. `Brain/script/sql/update/20260323_manuscript_review_readable_http_fixture_v01.sql`

### 2.3 本轮新增 SQL 口径

本轮新增 SQL 仅作为执行阶段测试矩阵补充 seed，不回写或推翻以上 4 个基础 SQL。

## 3. Controller 工作边界

下一阶段 controller 工作边界固定为：

1. 只负责编排、分派、监控、汇总。
2. 不直接执行真实 apply、真实 HTTP、浏览器动作，除非游工再次明确授权。
3. 任务拆分必须小粒度。
4. 一次只让一个 sub agent 持有一个明确小任务。
5. 一个 sub agent 完成任务后立即关闭，再开启下一个。

## 4. 长期记忆处理说明

系统长期记忆在当前环境下不可直接写入。  
本条已改为以下可审计替代措施：

1. 将执行指标固化在本决议文件。
2. 将详细测试要求固化在执行测试方案。
3. 将下一阶段 controller 提示词固化在 handoff。

后续新会话应优先读取这些仓库内持久化工件，而不是依赖隐式聊天记忆。
