Owner: Codex
Task: editorial-manuscript-rebuild
Status: Pending User Decision
Updated: 2026-03-17

# 审校模块替换式重构里程碑评审反馈汇总

## 1. 目的

本文件用于固化本轮 `architect` 与 `project_reviewer` 的设计评审输出。

本文件只记录：

1. 两位评审的一致结论；
2. 两位评审的差异点与补充点；
3. 当前待游工拍板事项。

本文件不是最终 `review` 或 `decision`。

## 2. 评审范围

两位评审均基于以下材料进行设计评审：

1. `reports/20260317_editorial-manuscript-milestone-review-input_report_v01.md`
2. `reports/20260317_editorial-manuscript-rebuild-mapping_report_v01.md`
3. `plans/20260317_editorial-manuscript-rebuild-implementation_plan_v01.md`
4. `reports/20260317_editorial-manuscript-rebuild-execution_report_v01.md`
5. `doc/official_doc/README.md`
6. 真实代码锚点：
   - `editorial_review_flow.json`
   - `ReviewStatusEnum.java`
   - `EditorialReviewController.java`
   - `EditorialReviewServiceImpl.java`
   - `EditorialReviewMapper.xml`
   - `editorial_review.sql`
   - `index.vue`
   - `reviewEdit.vue`
   - `review.ts`
   - `approvalButton.vue`
   - `submitVerify.vue`

## 3. 一致结论

### 3.1 当前不批准进入开发准备

`architect` 与 `project_reviewer` 一致认为：

1. 当前阶段**不批准进入开发准备**；
2. 本轮阻断并不是实现细节未定，而是当前真实代码里已有多条关键基线冲突还未被单一基线锁死；
3. 在未先锁定设计门禁前进入开发准备，只会把当前漂移整体带入后续实施。

### 3.2 两位评审共同确认的关键事实

两位评审均确认以下事实已被真实代码直接证实：

1. `flow` 当前只有两级审批节点，但 `ReviewStatusEnum` 仍按 `10 -> 20 -> 30 -> 40` 设计；
2. `EditorialReviewServiceImpl` 的中间等待态仍依赖 `nodeCode/nodeName` 启发式匹配；
3. 后端仍是粗粒度接口组，且单一 service 实现承载过多职责；
4. `EditorialReviewMapper.xml` 与 `EditorialReviewVo` 仍在复用单一契约承接 page/detail；
5. 前端仍以 `/editorial/review/reviewEdit` 作为单体主入口；
6. 模块内置 `editorial_review.sql` 缺少 `review_status`、`process_type`，但 Java / Mapper / 前端已在使用这些字段；
7. “开发准备通过”与“可提测”必须明确分离。

## 4. 评审差异与补充

### 4.1 是否存在方向冲突

当前**不存在方向性冲突**。

两位评审在以下核心方向上保持一致：

1. 不进入开发准备；
2. 先锁 gate，再谈实施；
3. 不允许继续把 `reviewEdit.vue` 当作主实现；
4. 不允许继续把模块内置 SQL 当正式数据库事实源；
5. 不允许在没有真实 HTTP / 浏览器证据前宣称“可提测”。

### 4.2 主要差异点

差异不在“走不走”，而在“门禁是否还需要加严”。

#### 差异 A：Gate 数量

`architect` 当前按 5 个 gate 收口：

1. flow-status
2. contract
3. frontend IA
4. SQL
5. verification

`project_reviewer` 认为还需显式补强为 6 个 gate：

1. `flow-status-role-processType` 一体锁定
2. contract
3. frontend IA
4. SQL
5. verification
6. rollback/cutover

#### 差异 B：角色/权限/processType 是否单独提升为显式门禁

`project_reviewer` 额外指出：

1. `EditorialRoleEnum` 已有 `FIRST/SECOND/FINAL_APPROVER` 三段角色；
2. 但当前 `editorial_review_flow.json` 两个审批节点仍是同一类 `permissionFlag`；
3. 当前 `processType` 也尚未与 flow 定义形成显式绑定；
4. 因此即使 flow/status/contract/IA/SQL 被改了，只要“谁审批哪一步”没锁死，开发仍会建立在错误基线上。

#### 差异 C：`reviewEdit.vue` 的降级条件

`architect`：

1. 允许 `reviewEdit.vue` 作为迁移期兜底。

`project_reviewer`：

1. 接受兜底，但要求更硬；
2. 如果它继续作为 `formPath`，或继续承接新增/编辑主路径，它就不是兜底，而是旧主入口；
3. 因此新壳一旦确定，`formPath` 必须切走，旧页只能保留为内部 fallback。

#### 差异 D：SQL 单一事实源的表述力度

`architect`：

1. 建议将 `Brain/script/sql/update` 设为唯一正式事实源。

`project_reviewer`：

1. 同意其作为**本轮迁移审批入口**；
2. 但提醒若不同时锁定：
   - 模块 SQL 样例何时回刷；
   - 是否覆盖多数据库脚本；
3. 那么“双事实源”只是延期，不是解决。

#### 差异 E：`freeze` 的阻断级别

`architect` 倾向把 `freeze` 纳入首批关键动作边界。

`project_reviewer` 更保守：

1. 真正 blocker 是“审批前业务前置 hook 契约”；
2. 是否立即落独立快照表，取决于追溯/合规要求；
3. 因此现阶段可先锁 hook 边界，再决定 freeze 持久化形态。

## 5. 当前推荐方案

主控当前建议采纳**更严格但仍可执行**的收口口径：

1. 维持“不批准进入开发准备”的阶段结论；
2. 采纳 `project_reviewer` 的补强意见，把 gate 从 5 个提升为 6 个；
3. 但对外表述时，仍保持与当前实现边界相兼容，不提前冻结 Java 文件级拆分类名。

推荐的 6 个 gate 为：

1. `flow-status-role-processType` gate
   - 锁定 `nodeCode / nodeName / reviewStatus / permissionFlag / processType / formPath`
2. `contract` gate
   - 锁定 `page/detail/action`
   - 明确 page 至少暴露 `status/reviewStatus/canEdit/processType`
3. `frontend IA` gate
   - 锁定 `index + create/edit + detail/approval shell`
   - `reviewEdit.vue` 降为过渡期内部 fallback
4. `SQL` gate
   - 锁定本轮迁移审批入口、DDL/菜单/回滚拆分与模块 SQL 样例回刷策略
5. `verification` gate
   - 明确“开发准备通过”与“可提测”是两个阶段
6. `rollback/cutover` gate
   - 锁定旧 `reviewEdit.vue`、旧 `formPath`、旧粗接口的退出顺序

## 6. 待游工拍板事项

当前需要游工拍板的，不是“要不要开发”，而是“采用哪一版门禁口径作为正式基线”。

### 6.1 推荐拍板项

推荐游工确认以下口径：

1. 接受当前阶段**不批准进入开发准备**；
2. 接受从 5 gate 升级到 6 gate；
3. 接受 `role/permission/processType` 不再隐含在 flow 讨论里，而是显式锁定；
4. 接受 `reviewEdit.vue` 只能作为迁移期内部 fallback，且新壳确定后 `formPath` 必须切走；
5. 接受 `freeze` 当前先锁 hook 边界，是否落独立快照表再单独决定。

### 6.2 拍板后的文档动作

在游工拍板后，主控至少应落一份正式文档：

1. `reviews/` 下的 milestone review；或
2. `decisions/` 下的 milestone gate decision。

## 7. 一句话结论

两位评审没有方向性分歧，当前共识是：**先锁更严格的设计门禁，再谈开发准备；其中需要游工拍板的主要差异，不是要不要重构，而是门禁是否提升为 6 gate，以及旧单页和角色/权限/processType 是否被显式纳入基线。**
