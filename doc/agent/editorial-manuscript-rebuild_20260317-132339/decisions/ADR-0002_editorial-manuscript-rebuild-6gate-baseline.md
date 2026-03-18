Owner: Codex
Task: editorial-manuscript-rebuild
Status: Approved For Coding
Updated: 2026-03-17

# ADR-0002: 审校模块替换式重构 6-Gate 基线与编码准入决策

## 1. 摘要

本 ADR 用于把 `gxpublish-brain` 审校模块替换式重构的编码前基线一次性锁定。

本 ADR 生效后，项目进入 **可开始编码** 阶段；但这不等于 **可提测**，也不等于 **已合并**。

本 ADR 只解决编码准入问题，不解决所有实现细节。

## 2. 适用范围

本 ADR 适用于：

1. `Brain/brain-modules/brain-editorial/**`
2. `Brain/script/sql/update/**`
3. `plus-ui-ts/src/views/editorial/review/**`
4. `plus-ui-ts/src/api/editorial/review.ts`
5. `plus-ui-ts/src/components/Process/**` 中与审批前业务 hook 直接相关的最小改动

本 ADR 不适用于：

1. 旧流程实例兼容迁移
2. 引入新的外部 BPM 引擎或运行时表
3. 直接复制 `pub-nexus` 代码
4. 直接宣布“可提测”

## 3. 决策状态

### 3.1 当前状态

本 ADR 状态为：**Approved For Coding**

生效含义：

1. 6-gate 基线已被正式锁定；
2. 后续实现可以进入编码；
3. 编码期间不得再回到“边写边定 gate”。

### 3.2 生效条件

本 ADR 以以下默认值正式生效：

1. 采用 `6-gate` 统一口径，不再新增第 7 个设计 gate；
2. 采用 **单 `flowCode=editorial_review_flow` + 显式 `processType(AUDIT|PROOFREAD)`**；
3. `freeze` 在编码准入阶段只锁 **审批前业务 hook 边界**，不把独立快照表作为编码 blocker；
4. SQL 以 **MySQL 增量脚本** 作为本轮编码入口，多数据库脚本同步降为后续集成阶段事项；
5. `reviewEdit.vue` 只允许作为迁移期内部 fallback，不再作为主入口或最终 `formPath`。

### 3.3 失效条件

若后续出现以下任一情况，本 ADR 需重新评审：

1. 用户明确要求 `AUDIT` 与 `PROOFREAD` 采用双 `flowCode`；
2. 用户明确要求首批必须落独立 freeze 快照持久化；
3. 用户明确要求首批必须同时覆盖 `oracle/postgres/sqlserver`；
4. 用户要求保留旧流程实例兼容。

## 4. 编码就绪定义

本 ADR 将“编码就绪”定义为：

> 6 个 gate 全部从讨论态转为锁定态，且跨 gate 的动作-状态-角色矩阵完整、无 blocker 级 TBD、无默认继承项。

本 ADR 生效后：

1. **可开始编码**：是
2. **可提测**：否
3. **可合并**：否

## 5. 6-Gate 总览

| Gate | 决策 | 编码准入含义 |
| --- | --- | --- |
| `flow-status-role-processType` | 锁定 | 允许重建 flow 与状态机，不再依赖启发式节点识别 |
| `contract` | 锁定 | 允许拆 page/detail/action 契约 |
| `frontend IA` | 锁定 | 允许从单体 `reviewEdit.vue` 迁移到新 IA |
| `SQL` | 锁定 | 允许以 MySQL 增量脚本作为事实入口开始落库改造 |
| `verification` | 锁定 | 允许编码，但不得用“编码通过”替代“可提测” |
| `rollback/cutover` | 锁定 | 允许带 fallback 的替换式重构，而非一刀切 |

## 6. Gate 1: flow-status-role-processType

### 6.1 锁定项

1. 最终等待态语义锁定为：
   - `DRAFT`
   - `WAITING_FIRST`
   - `WAITING_SECOND`
   - `WAITING_FINAL`
   - `APPROVED`
   - `BACK`
   - `CANCELED`
   - `TERMINATED`
2. 最终采用 **单 `flowCode=editorial_review_flow` + 显式 `processType`**。
3. 审批语义槽位锁定为 3 段：
   - `first-review-node` -> `WAITING_FIRST(10)` -> 一级审批角色
   - `second-review-node` -> `WAITING_SECOND(20)` -> 二级审批角色
   - `final-review-node` -> `WAITING_FINAL(30)` -> 终审角色
4. 审批语义角色锁定为现有 editorial 角色体系：
   - `editorial_first_level_approver`
   - `editorial_second_level_approver`
   - `editorial_third_level_approver`
5. `formPath` 必须最终切离 `/editorial/review/reviewEdit`。
6. 保留现有“持证发起人可跳过一级审批”的业务语义，不在本轮重开。

### 6.2 编码准入解释

1. 允许开始编码 flow 与状态机重建；
2. 不得继续把 `EditorialReviewServiceImpl.resolveWaitingReviewStatus(...)` 的启发式匹配当最终真相；
3. gate 1 锁定的是**语义映射**，不是最终 Java 类名或 UI 文案。

### 6.3 实施限制

1. 本仓 dev 配置当前指向 `gxpublish_brain` schema；
2. 当前 workflow 权限解析使用 `role:<id>` 形式；
3. 由于当前 MySQL MCP 连接的不是 `gxpublish_brain`，本轮未在正确 schema 上 live verify editorial 审批角色 seed 是否已存在；
4. 因此首批编码必须把“审批角色 seed/对齐”与“flow permissionFlag 重写”作为一组配套改动推进；
5. 这不是编码 blocker，但不得被遗漏。

## 7. Gate 2: contract

### 7.1 锁定项

1. 后端/前端契约统一拆为：
   - `page`
   - `detail`
   - `action`
2. page 最低必备字段锁定为：
   - `id`
   - `title`
   - `status`
   - `reviewStatus`
   - `canEdit`
   - `processType`
   - `createTime`
   - `user`
   - `dept`
3. detail 最低承载内容锁定为：
   - 表单主数据
   - 附件
   - 链接
   - 历史
   - 审批壳上下文
4. action 最低动作集锁定为：
   - `create/saveDraft`
   - `updateDraft`
   - `submit`
   - `resubmit`
   - `history`
   - `freezeHook`
5. page/detail 不允许继续复用同一 VO / TS 类型。

### 7.2 编码准入解释

1. `canEdit` 在 page 中必须显式暴露；
2. `canEdit` 只作为 UI 提示，不代替服务端最终授权判断；
3. 允许在编码期再细化 DTO/VO/BO/TS 命名，不允许回退到单体契约。

## 8. Gate 3: frontend IA

### 8.1 锁定项

1. 最终 IA 锁定为：
   - `index`
   - `form(create/edit)`
   - `detail/approval shell`
2. `reviewEdit.vue` 只允许作为迁移期内部 fallback。
3. 新 `formPath` 必须指向新的 `detail/approval shell`，不得继续指向旧页。
4. 审批操作统一挂在 `detail/approval shell`，不再与可编辑主表单共居主路径。
5. `integration.ts` / `model.ts` 允许作为实现手段，但不作为编码前必须单独冻结的文件名。

### 8.2 编码准入解释

1. 允许开始拆路由与页面壳；
2. 不允许继续在 `reviewEdit.vue` 上堆新增主逻辑；
3. 不允许在 IA 未锁定前先写菜单路径 SQL。

## 9. Gate 4: SQL

### 9.1 锁定项

1. `Brain/script/sql/update/` 为本轮**唯一迁移审批入口**。
2. 业务 DDL、菜单/权限 SQL、rollback SQL 必须分文件。
3. 主表最小必补字段锁定为：
   - `review_status`
   - `process_type`
4. 必要索引、默认值、注释与 rollback 顺序必须配套给出。
5. 模块内置 `editorial_review.sql` 暂不再作为运行事实源，只在增量脚本稳定后回刷样例。
6. 本轮编码阶段以 **MySQL** 为唯一必须实现的数据库入口。

### 9.2 编码准入解释

1. 允许开始写 MySQL 增量脚本；
2. 不得再把 `editorial_review.sql` 当作当前数据库真相；
3. `oracle/postgres/sqlserver` 同步不作为本轮编码 blocker，但若后续进入集成阶段需重新评估。

## 10. Gate 5: verification

### 10.1 锁定项

1. 状态层级锁定为：
   - `可开始编码`
   - `阶段完成`
   - `可提测`
2. `可开始编码` 的证据：
   - 6-gate 决策文档已生效
3. `阶段完成` 的最低证据：
   - backend compile / targeted tests
   - frontend eslint / build
4. `可提测` 的最低证据：
   - backend ready
   - 真实 HTTP
   - frontend 登录态
   - 浏览器级关键链路
5. 最低验证矩阵必须覆盖：
   - `10 -> 20 -> 30 -> 40`
   - `submit`
   - `back`
   - `cancel`
   - `termination`
   - `resubmit`
   - TUS `finish`

### 10.2 编码准入解释

1. 本 ADR 生效后可以编码；
2. 但任何人不得把“开始编码”表述成“可提测”；
3. 也不得用 compile/build 替代真实联调证据。

## 11. Gate 6: rollback/cutover

### 11.1 锁定项

1. 退出顺序锁定为：
   - 先新契约/新路由就位
   - 再切 `formPath`
   - 再降级旧页
   - 最后移除旧粗接口
2. 首批编码阶段不得物理删除 `reviewEdit.vue`，但必须降为内部 fallback。
3. 旧粗接口在 cutover 前可保留适配层，不再作为新前端主依赖。
4. 菜单 rollback 与业务表 rollback 必须分离。

### 11.2 编码准入解释

1. 允许开始替换式重构；
2. 不允许“一边宣称切换完成，一边让旧入口继续当主路径”；
3. 不允许没有 rollback SQL 就推进切换。

## 12. 跨 Gate 动作-状态-角色矩阵

| 动作 | 发起角色 | 允许状态 | `processType` | 所属页面 | 所属接口 | 成功后状态 | 影响项 |
| --- | --- | --- | --- | --- | --- | --- | --- |
| `create/saveDraft` | `APPLICANT` / `APPLICANT_CER` | 新建 | `AUDIT` / `PROOFREAD` | `form` | `create` | `DRAFT` | 业务表 |
| `updateDraft` | `APPLICANT` / `APPLICANT_CER` | `DRAFT` / `BACK` | `AUDIT` / `PROOFREAD` | `form` | `update` | 原状态保持 | 业务表/历史 |
| `submit` | `APPLICANT` / `APPLICANT_CER` | `DRAFT` / `BACK` | `AUDIT` / `PROOFREAD` | `form` | `submit` | 默认进入等待态；持证发起人可跳一级 | workflow/history |
| `resubmit` | `APPLICANT` / `APPLICANT_CER` | `BACK` | `AUDIT` / `PROOFREAD` | `form` 或 `detail` | `resubmit` | 重新进入等待态 | workflow/history |
| `approve/back/cancel/termination` | 对应审批角色 | `WAITING_*` | `AUDIT` / `PROOFREAD` | `detail/approval shell` | workflow action | `20/30/40/50/60/70` | workflow/history/freezeHook |

说明：

1. 本矩阵锁定的是业务边界，不是最终接口命名字符串；
2. `freeze` 在本轮以 `freezeHook` 形式进入动作边界，不把独立快照表作为编码前 blocker。

## 13. 禁止越界项

1. 禁止直接复制 `pub-nexus` 代码或 SQL。
2. 禁止先改 `editorial_review_flow.json` 再补状态机与角色语义。
3. 禁止继续以 `reviewEdit.vue` 作为正式主入口或最终 `formPath`。
4. 禁止继续复用单一 VO/TS 承接 page/detail。
5. 禁止继续把模块内置 `editorial_review.sql` 当数据库事实源。
6. 禁止在未区分“可开始编码”与“可提测”前混用验证结论。

## 14. 编码准入结论

结论：

1. 本 ADR 生效后，项目进入 **可开始编码** 阶段。
2. 首批编码应优先覆盖：
   - Gate 1 的 flow / status / role / processType 对齐
   - Gate 2 的 contract 拆分
   - Gate 3 的 IA 切换骨架
   - Gate 4 的 MySQL 增量脚本
3. 首批编码不要求：
   - 旧流程实例兼容
   - 多数据库脚本同步
   - freeze 独立快照表
   - 可提测证据

## 15. 下一步

1. 按本 ADR 执行 implementation plan，但不再重开 gate 讨论；
2. 第一批代码变更前，必须继续按 phase/step 方式给出 `Goal / Affected Files / Risk / Verification / Rollback`；
3. 进入实现阶段后，新的争议只允许围绕“实现如何满足本 ADR”，不再回到“要不要 6-gate / 要不要单 flowCode / 要不要旧页主入口”等已锁定问题。
