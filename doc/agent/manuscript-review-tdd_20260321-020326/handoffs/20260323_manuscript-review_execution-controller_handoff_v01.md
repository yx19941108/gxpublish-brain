Owner: Codex
Task: manuscript-review execution controller handoff
Status: Current
Updated: 2026-03-23 17:05:00 +08:00

# Manuscript Review 执行阶段 Controller Handoff v01

## 1. 本轮产物

本轮已经形成以下执行基线：

1. 测试方案：
   - `doc/agent/manuscript-review-tdd_20260321-020326/plans/20260323_manuscript-review_execution-test-plan_v01.md`
2. 执行基准说明：
   - `doc/agent/manuscript-review-tdd_20260321-020326/decisions/20260323_manuscript-review_execution-benchmark_note_v01.md`
3. 本 handoff：
   - `doc/agent/manuscript-review-tdd_20260321-020326/handoffs/20260323_manuscript-review_execution-controller_handoff_v01.md`
4. 待外部执行的补充 SQL：
   - `Brain/script/sql/update/20260323_manuscript_review_execution_role_matrix_seed_v01.sql`
   - `Brain/script/sql/update/20260323_manuscript_review_execution_role_matrix_seed_rollback_v01.sql`

## 2. 当前基线

### 2.1 已确认

1. 目标数据库以 `gxpublish_brain` 为准。
2. 4 个基础 SQL 已执行完成，不得再误报不存在业务表：
   - `20260321_manuscript_review_ddl_v01.sql`
   - `20260321_manuscript_review_dml_v01.sql`
   - `20260323_manuscript_review_dev_login_seed_v01.sql`
   - `20260323_manuscript_review_readable_http_fixture_v01.sql`
3. 当前仍未闭环的重点不是“能否读到页面”，而是：
   - 角色矩阵是否补齐
   - 表单是否从壳层进化为真实可测
   - BPM 办理页真实可达性
   - 动作流转、历史审计、资源生命周期运行时闭环

### 2.2 blocker

1. 持证发起人、同级第二审批人、纯无关用户账号仍需补 SQL。
2. 资源 URL 当前仍偏测试占位值，影响真实下载/播放。
3. `form.vue` 仍是壳层，表单浏览器测试未到可执行状态。
4. `去审批 -> BPM 办理页` 仍需真实 route/页面可达性验证。

## 3. Controller 工作模式

下一轮会话必须采用以下工作模式：

1. 你是 controller，只做编排、调度、监控、汇总，不亲自执行真实工作。
2. 使用 sub agent 做真实工作。
3. sub agent 任务必须拆小：
   - 一个任务只处理一个明确目标
   - 完成后立刻关闭
   - 再开启下一个
4. 不允许把多个真实执行任务揉成一个超大 sub agent 包。
5. controller 汇报必须区分：
   - 已确认
   - blocker
   - 正在执行的单个任务
   - 下一个待派发任务

## 4. 建议的执行顺序

1. Worker-1：
   - 只负责核对补充 SQL 是否满足角色矩阵，并输出 apply 前检查清单
2. Worker-2：
   - 只负责补充 SQL 的静态审查与 rollback 边界复核
3. Worker-3：
   - 只负责执行前 HTTP/浏览器前置条件核对，不跑真实链路
4. Worker-4 起：
   - 按测试方案逐阶段执行单个工作包

## 5. 下一阶段会话提示词

```text
你继续接管 `gxpublish-brain` 仓库里的 clean-slate `manuscript_review` 研发线。

本会话正式进入执行阶段，但你是 controller，只做编排、调度、监控、汇总，不真正执行 apply、真实 HTTP、浏览器动作。
真实工作必须通过 sub agent 完成，而且 sub agent 必须拆小：一个任务完成后立即关闭，再开启下一个。

先不要读旧聊天；只从仓库内以下文件恢复上下文：
1. `doc/manuscript_review/product/20260321_manuscript-review_product-requirements_v01.md`
2. `doc/agent/manuscript-review-tdd_20260321-020326/plans/20260323_manuscript-review_execution-test-plan_v01.md`
3. `doc/agent/manuscript-review-tdd_20260321-020326/decisions/20260323_manuscript-review_execution-benchmark_note_v01.md`
4. `doc/agent/manuscript-review-tdd_20260321-020326/handoffs/20260323_manuscript-review_execution-controller_handoff_v01.md`
5. `doc/agent/manuscript-review-tdd_20260321-020326/handoffs/20260323_manuscript-review_handoff_v08.md`

当前基线：
- 数据库以 `gxpublish_brain` 为准。
- 以下 4 个 SQL 已由外部执行完成，不得误报未执行：
  - `Brain/script/sql/update/20260321_manuscript_review_ddl_v01.sql`
  - `Brain/script/sql/update/20260321_manuscript_review_dml_v01.sql`
  - `Brain/script/sql/update/20260323_manuscript_review_dev_login_seed_v01.sql`
  - `Brain/script/sql/update/20260323_manuscript_review_readable_http_fixture_v01.sql`
- 本轮新增待执行阶段参考 SQL：
  - `Brain/script/sql/update/20260323_manuscript_review_execution_role_matrix_seed_v01.sql`
  - `Brain/script/sql/update/20260323_manuscript_review_execution_role_matrix_seed_rollback_v01.sql`

你的输出与行动要求：
1. 先按 `已确认 / blocker / 当前单任务编排 / 下一个任务` 汇报。
2. 先给 controller 的最小执行编排方案。
3. 然后只创建第一个最小 sub agent 任务，不要一次开很多。
4. 第一个 sub agent 完成后，你先审其结果，再决定是否关闭并开启下一个。
5. 全程不允许把旧 `brain-editorial` 当需求或兼容基线。
6. 任何代码或持久化文件变更前，先给 `Goal / Affected Files / Risk / Verification / Rollback`。
7. 展示与报告必须业务可读，不得直出数据库 ID、账号、role key、permission flag；`reviewId` 仅允许用于隐式定位。

当前优先任务建议从以下最小工作包开始：
1. 补充 SQL 静态审查与 apply 前清单
2. 角色矩阵数据准备核对
3. 表单当前实现缺口核对
4. BPM 办理页路由可达性核对

注意：controller 本会话只调度，不直接执行真实操作。
```
