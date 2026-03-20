Owner: Codex Controller
Task: editorial-fulltest-exec
Status: Handoff
Updated: 2026-03-20 17:45 +08:00

# 背景

本 handoff 用于把 2026-03-20 当前会话的 controller 编排状态压缩给下一新会话继续执行。当前方向不是重新做 packaging/fatjar 漂移分析，而是在已修补前后端的基础上，继续完成 editorial 页面与历史记录行为的最终闭环验收。

# 已确认

## 已完成实现

### Frontend

- 列表页任何时候都有 `详情` 按钮。
- 待办态列表项仍可按条件显示 `修改 / 审批 / 详情`。
- 修改页只保留 `保存`，不再显示 `保存并审批`，也不再显示审批按钮。
- 页面上的调试/壳信息已移除，包括 `detail shell`、`approval shell`、`taskId`、当前 entry 等。
- 详情页已补 `审核状态 / 创建时间 / 修改时间`。
- 修改页现有附件/链接只读展示，仅允许新增，不覆盖。
- `submitVerify.vue` 中 editorial 流程已移除转办入口。
- 静态验证已通过：
  - `vitest`: `20 passed`
  - boundary `eslint`: clean
  - `build:prod`: passed

### Backend

- 已新增 `EditorialDiffField`，用于字段中文名/差异描述支撑。
- 已新增 `EditorialHistoryFactory`，并重构创建/修改/审批历史记录生成逻辑。
- 修改时 `linkList` 已调整为 append-only 语义。
- `brain_editorial_history` 已扩展字段：
  - `event_type`
  - `operator_role_name`
- editorial 流程配置及相关 SQL 已移除 `transfer/转办`。
- 已新增并落地 SQL：
  - `Brain/script/sql/update/20260320_editorial_review_history_contract_upgrade.sql`
  - `Brain/script/sql/update/20260320_editorial_review_history_contract_upgrade_rollback.sql`
- 目标模块定向测试已通过：
  - editorial module targeted tests: `19 tests, 0 fail, 0 error`

## 运行时状态

以下是本会话最后一次已知验证结果，进入新会话后应先复核，不可直接当作当前事实：

- backend 已由 JDK17 启动：
  - `C:\kuguaHome\env\java\jdk17\bin\java.exe`
  - jar: `C:\kuguaHome\project\gxpublish-brain\Brain\brain-admin\target\brain-admin.jar`
  - 最后记录 PID: `55324`
- `9888` 在最后一次验证时可用，`GET /auth/code` 返回 `200`
- frontend `9999` 在最后一次验证时监听正常，主页 `GET /` 返回 `200`

# 当前 blocker

fresh QA 已启动并在首个阻断点停止，当前整包仍然 **未验收通过**。

## Blocker-01: 创建历史文案不符合精确要求

用户要求：

- 发起人创建申请时，审批记录必须显示：`发起人创建申请{申请标题}`

实际 fresh evidence：

- 后端 API 返回：
  - `eventType=CREATE`
  - `operatorRoleName=三审三校发起人`
  - `operateType=三审三校发起人创建申请qa-fresh-history-1773998938538`
- 前端页面展示：
  - `zhangsan · 三审三校发起人创建申请qa-fresh-history-1773998938538`

结论：

- backend 仍把系统角色显示名写入创建文案，未按业务要求收敛为 `发起人`
- frontend 详情页历史渲染仍在二次拼接操作人姓名，导致最终文案不符合要求

# 关键证据

- blocker artifact root:
  - `.agent-temp/editorial-fresh-acceptance_20260320-171631/history_ui_item01/`
- key files:
  - `.agent-temp/editorial-fresh-acceptance_20260320-171631/history_ui_item01/result.json`
  - `.agent-temp/editorial-fresh-acceptance_20260320-171631/history_ui_item01/detail_history.png`
- failed sample:
  - `reviewId=2034925155080560642`
  - title: `qa-fresh-history-1773998938538`

# 关键文件

## Backend

- `Brain/brain-modules/brain-editorial/src/main/java/com/gxpublish/brain/editorial/support/EditorialHistoryFactory.java`
- `Brain/brain-modules/brain-editorial/src/main/java/com/gxpublish/brain/editorial/service/impl/EditorialReviewServiceImpl.java`
- `Brain/brain-modules/brain-editorial/src/main/java/com/gxpublish/brain/editorial/domain/bo/EditorialReviewBo.java`
- `Brain/brain-modules/brain-editorial/src/main/java/com/gxpublish/brain/editorial/domain/vo/EditorialHistoryVo.java`
- `Brain/brain-modules/brain-editorial/src/main/resources/flow/editorial_review_flow.json`
- `Brain/script/sql/update/20260320_editorial_review_history_contract_upgrade.sql`
- `Brain/script/sql/update/20260320_editorial_review_history_contract_upgrade_rollback.sql`

## Frontend

- `plus-ui-ts/src/views/editorial/review/detail.vue`
- `plus-ui-ts/src/views/editorial/review/integration.ts`
- `plus-ui-ts/src/views/editorial/review/index.vue`
- `plus-ui-ts/src/views/editorial/review/form.vue`
- `plus-ui-ts/src/views/editorial/review/components/ReviewFormFields.vue`
- `plus-ui-ts/src/views/editorial/review/components/ReviewAttachmentList.vue`
- `plus-ui-ts/src/components/Process/submitVerify.vue`

# 下一会话的 controller 编排规则

## 角色边界

- 主控只做调度、收集、集成结论，不直接写代码。
- `business_qa_lead` 负责端到端测试与结果汇报。
- 若 QA 命中前端问题，由 `frontend_developer` 负责修补。
- 若 QA 命中后端问题，由 `senior_developer` 负责修补。
- 修补完成后，由主控唤醒或新建 `business_qa_lead` 继续回归。

## QA stop rule 更新

新会话中，QA 子 agent 不必对所有缺陷都立即停止。

只在以下情况停止并回报主控：

- 阻断主流程，后续用例无法继续
- 可能导致数据污染、误判、级联故障或其他严重风险
- 环境不可用，无法继续产出有效证据

若是非阻断、可继续执行的缺陷：

- QA 继续跑剩余用例
- 将缺陷按严重级别、复现步骤、证据路径汇总回报主控
- 由主控统一拆单给前后端开发子 agent

## 上下文治理

- controller 要控制自身上下文长度，只保留：
  - 最新 blocker 列表
  - 当前验证结论
  - 子 agent 任务边界
  - 关键证据路径
- 当某个子 agent 上下文开始变长、历史轮次过多、输出开始重复时，直接关闭该 agent，新建 fresh agent 继续，不要硬续。

# 下一会话建议执行顺序

1. 先读取本 handoff 与当前 task bundle 内既有 ADR/plan/report，不重做 fatjar/漂移分析。
2. 复核运行时：
   - backend PID 是否仍存活
   - `9888` 是否仍监听并可用
   - frontend `9999` 是否仍监听
3. 新建 fresh `senior_developer`：
   - 只修 `CREATE` 历史文案
   - 目标文案必须精确为 `发起人创建申请{申请标题}`
   - 同时检查角色名在审批/修改记录里是否与业务要求一致，不要输出系统调试式角色文本
4. 新建 fresh `frontend_developer`：
   - 只修详情页历史渲染
   - 页面必须直接展示最终业务文案，不再二次 prepend 操作人姓名
5. 各自完成后跑最小静态验证：
   - backend targeted tests
   - frontend `vitest / eslint / build`
6. 新建 fresh `business_qa_lead`：
   - 先只复测问题 1
   - 若通过，再继续覆盖问题 `2/3/8/10/12`
   - 非阻断问题继续执行，不必立即停；阻断问题才停
7. 若 QA 新增问题：
   - controller 依据归因唤醒或新建前后端 agent 修补
   - 修补完成后继续 QA，直到 12 项全部闭环

# 明确不要做的事

- 不要重做 packaging/fatjar 漂移分析
- 不要碰未跟踪文件 `test_qa_flow.py`
- 不要回退工作树里与本轮无关的现有改动
- 不要把页面调试信息再带回用户可见区域

# 新会话启动判定

当且仅当以下条件同时满足，才可对用户宣称“本轮问题已全部解决”：

- 12 项问题全部复测通过
- 关键 runtime 路径 fresh 复跑通过
- 历史记录文案与页面展示完全符合用户精确要求
- 详情/修改/审批页附件与链接回显、保存、下载/跳转闭环通过
- 无新的 blocker_type

