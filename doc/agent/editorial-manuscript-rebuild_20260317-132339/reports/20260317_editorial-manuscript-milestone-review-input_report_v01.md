Owner: Codex
Task: editorial-manuscript-rebuild
Status: Draft For Milestone Review
Updated: 2026-03-17

# 审校模块替换式重构里程碑评审输入包

## 1. 目标与边界

本文件用于在启用 `architect` / `project_reviewer` 前，先把本轮设计评审所需的事实基线、风险边界、评审议题和 child-agent 申请清单固定下来。

本文件不是实现计划替代物，也不是最终 review / decision。

本文件只服务以下目标：

1. 为里程碑设计评审提供统一输入；
2. 固定“只评审、不写代码、不建 worktree”的 child-agent 边界；
3. 为后续冲突汇总、用户拍板和 review / decision 落盘提供事实底稿。

## 2. 本轮已读取并核对的输入

### 2.1 官方知识库与既有任务包

已按指定顺序读取：

1. `doc/official_doc/README.md`
2. `doc/official_doc/ruoyi-vue-plus-plus-doc/ruoyi-vue-plus/home.md`
3. `doc/official_doc/warm-flow-doc/src/master/introduction/introduction.md`
4. `reports/20260317_editorial-manuscript-rebuild-mapping_report_v01.md`
5. `plans/20260317_editorial-manuscript-rebuild-implementation_plan_v01.md`
6. `reports/20260317_editorial-manuscript-rebuild-execution_report_v01.md`
7. `handoffs/20260317_editorial-manuscript-official-doc-kb_handoff_v01.md`

### 2.2 本轮补充核对的真实代码与历史材料

已继续核对：

1. `reports/20260317_editorial-manuscript-rebuild-prep_report_v01.md`
2. `doc/agent/reports/20260302_editorial-fulltest_report_v01.md`
3. `doc/agent/decisions/ADR-0001_editorial-fulltest-hotfix.md`
4. `Brain/brain-modules/brain-editorial/src/main/resources/flow/editorial_review_flow.json`
5. `Brain/brain-modules/brain-editorial/src/main/java/com/gxpublish/brain/editorial/enums/ReviewStatusEnum.java`
6. `Brain/brain-modules/brain-editorial/src/main/java/com/gxpublish/brain/editorial/controller/EditorialReviewController.java`
7. `Brain/brain-modules/brain-editorial/src/main/java/com/gxpublish/brain/editorial/service/impl/EditorialReviewServiceImpl.java`
8. `Brain/brain-modules/brain-editorial/src/main/java/com/gxpublish/brain/editorial/domain/bo/EditorialReviewBo.java`
9. `Brain/brain-modules/brain-editorial/src/main/java/com/gxpublish/brain/editorial/domain/vo/EditorialReviewVo.java`
10. `Brain/brain-modules/brain-editorial/src/main/java/com/gxpublish/brain/editorial/mapper/EditorialReviewMapper.java`
11. `Brain/brain-modules/brain-editorial/src/main/resources/mapper/editorial/EditorialReviewMapper.xml`
12. `Brain/brain-modules/brain-editorial/src/main/resources/sql/editorial_review.sql`
13. `plus-ui-ts/src/views/editorial/review/index.vue`
14. `plus-ui-ts/src/views/editorial/review/reviewEdit.vue`
15. `plus-ui-ts/src/api/editorial/review.ts`
16. `plus-ui-ts/src/components/Process/approvalButton.vue`
17. `plus-ui-ts/src/components/Process/submitVerify.vue`

## 3. 本轮锁定约束

以下约束在本轮评审阶段已锁定，不再重新讨论：

1. `gxpublish-brain` 是真实落地仓库，`pub-nexus` 仅作参考规范源；
2. 不允许直接复制 `pub-nexus` 代码到 `gxpublish-brain`；
3. 允许不兼容旧流程实例的替换式重构；
4. 当前阶段先做设计评审，不写代码；
5. “可提测”不等于“代码已合并”，后续必须分别拿到定向验证和集成验证证据。

## 4. 官方知识库可用于本轮评审的结论

### 4.1 使用边界

`doc/official_doc/README.md` 已明确：

1. 本地知识库是设计评审前的阅读基线，不是长期真理；
2. 若结论依赖版本敏感行为，最终仍需回看官方在线源；
3. 不能把文档快照当成“本仓已经实现这些能力”的证据。

### 4.2 对本轮有价值的上游约束

从 `ruoyi-vue-plus/home.md` 可提炼出与本仓评审相关的上游约束：

1. 前端技术基线是 `Vue3 + TS + Element Plus`；
2. 后端强调插件化/扩展式结构；
3. 代码风格要求显式遵守 Alibaba 规范；
4. 数据访问、分页、数据权限等均偏向 MyBatis-Plus 风格扩展。

从 `warm-flow` introduction 可提炼出对工作流方案讨论有价值的约束：

1. 上游强调轻量工作流、节点扩展、监听器、变量表达式和多 ORM 适配；
2. 文档明确“审批动作多、监听器多、变量参与深”是这类引擎的常见能力；
3. 但这些能力只能作为“如何看待流程节点/监听器/变量”的参考，不等于本仓当前就是 Warm-Flow 实现。

## 5. 当前仓库已确认事实

### 5.1 flow、状态链与页面入口当前不一致

真实代码已证实：

1. `editorial_review_flow.json` 当前节点只有：
   - `start-node`
   - `applicant-node`
   - `dept-audit-node`
   - `final-audit-node`
   - `end-node`
2. 当前 `flowCode` 是 `editorial_review_flow`；
3. 当前 `formPath` 仍指向 `/editorial/review/reviewEdit`；
4. `ReviewStatusEnum` 却仍按 `WAITING_FIRST(10) -> WAITING_SECOND(20) -> WAITING_FINAL(30) -> APPROVED(40)` 设计。

这说明：

1. 当前等待态语义比 flow 节点语义更细；
2. 当前页面入口仍绑定旧单体页；
3. “先并行开发后补统一”会把状态、节点和页面入口继续分叉。

### 5.2 中间状态同步仍依赖启发式节点识别

`EditorialReviewServiceImpl` 当前实现已证实：

1. 提交初始化与终态同步由 `ProcessEvent` 负责；
2. 中间等待态同步由 `ProcessTaskEvent` 负责；
3. `resolveWaitingReviewStatus(...)` 通过 `nodeCode/nodeName` 包含关系做推断：
   - 命中 `dept-audit` 或“一审/部门经理审批”则回写 `10`
   - 命中 `second/二审` 则回写 `20`
   - 命中 `final/总编室审批/终审` 则回写 `30`

这说明：

1. 当前等待态不是由一套锁定的节点契约驱动，而是由启发式匹配驱动；
2. 如果新 flow 不先把 `nodeCode / nodeName / reviewStatus` 关系锁死，重构后仍会重复旧问题。

### 5.3 SQL 样例基线已落后于运行代码

真实文件已证实：

1. `editorial_review.sql` 的 `brain_editorial_review` 建表语句目前只有 `status`；
2. 当前样例 SQL 中没有 `review_status`；
3. 当前样例 SQL 中没有 `process_type`；
4. 但 `EditorialReviewMapper.xml` 查询已在读取 `r.review_status`、`r.process_type`；
5. `EditorialReviewBo.java`、`EditorialReviewVo.java` 也都定义了 `reviewStatus`、`processType` 字段。

这说明：

1. 模块内置 SQL 不能继续被视为真实数据库事实源；
2. 评审时必须把“正式事实源切到 `Brain/script/sql/update`”作为门禁，而不是实施细节。

### 5.4 后端契约仍是粗粒度单组接口

当前 `EditorialReviewController.java` 仍提供：

1. `GET /editorial/review/list`
2. `GET /editorial/review/{id}`
3. `POST /editorial/review`
4. `PUT /editorial/review`
5. `POST /editorial/review/submit`
6. `DELETE /editorial/review/{ids}`
7. `GET /editorial/review/history/{id}`

同时 `EditorialReviewServiceImpl` 仍在同一实现中承担：

1. 详情聚合；
2. 草稿新增/修改；
3. 提交流程；
4. 附件/链接写入；
5. 流程事件回写；
6. 历史查询。

这说明：

1. 当前“单服务实现承载所有动作”的耦合是真实存在的；
2. 设计评审必须先拍板 service 拆分边界，再谈并行开发 owned boundary。

### 5.5 前端仍以单体页承载主业务

当前 `index.vue` 与 `reviewEdit.vue` 已证实：

1. 列表页的新增/修改/查看全部跳到 `/editorial/review/reviewEdit`；
2. 页面模式靠 `query.type=add|update|view|approval` 区分，而不是独立路由；
3. `reviewEdit.vue` 同时包含：
   - `approvalButton`
   - 表单字段
   - `TusUpload`
   - 历史记录时间轴
   - `submitVerify`
   - `approvalRecord`
4. `reviewEdit.vue` 内部同时调用：
   - `getReview`
   - `addReview`
   - `updateReview`
   - `submitReview`
   - `getHistory`

这说明：

1. 当前前端 IA 仍是“单页 + 查询参数分模式”；
2. 若不先拍板新页面分层，frontend worktree 很容易回到“在旧页上继续堆逻辑”。

### 5.6 当前前端契约只暴露粗粒度状态

当前 `plus-ui-ts/src/api/editorial/review.ts` 已证实：

1. TS `EditorialReviewVo` 暴露了 `status`、`processType`、附件、链接等字段；
2. 当前 TS 契约没有声明 `reviewStatus`；
3. 当前 TS 契约也没有声明 `canEdit`；
4. 列表页渲染逻辑只看粗粒度 `status`；
5. 列表页的“修改/删除”按钮仍主要依赖 `status + userId` 判定。

同时后端 `EditorialReviewServiceImpl` 在分页结果上又会根据 `reviewStatus` 计算 `canEdit`。

这说明：

1. 前后端对“细粒度状态”和“可编辑性”的契约已经发生分层漂移；
2. 这不是实现细节问题，而是设计评审必须显式拍板的契约问题。

### 5.7 共享流程组件当前没有业务前置 hook

当前共享流程组件已证实：

1. `approvalButton.vue` 只是发出 `submitForm`、`approvalVerifyOpen`、`handleApprovalRecord` 事件；
2. `submitVerify.vue` 在弹窗内直接执行业务无关的流程动作，如：
   - `completeTask`
   - `backProcess`
   - `terminationTask`
   - `taskOperation`
3. 当前共享流程组件没有“审批前先执行 editorial 业务冻结/校验”的显式扩展点。

这说明：

1. execution plan 里提到的“审批前业务冻结 hook”确实不是现成功能；
2. 这项能力需要先被设计成通用扩展点，再决定由 frontend 还是共享流程层持有。

### 5.8 历史缺陷链仍对本轮评审有效

历史材料已证实：

1. `20260302_editorial-fulltest_report_v01.md` 曾在真实联调中暴露：
   - 中间审批阶段 `review_status` 推进异常；
   - `tus/upload/finish` 回调失败；
2. `ADR-0001_editorial-fulltest-hotfix.md` 的修复决策是：
   - 用 `ProcessTaskEvent` 补中间态；
   - 强化 TUS `finish` 链路；
   - 前端显式设置 `chunkSize`。

这说明：

1. 旧链路问题不是纯理论风险；
2. 本轮设计评审不能只看“目标形态”，还要看是否会重新踩回旧缺陷链。

### 5.9 当前主工作区不是干净基线

本轮 `git status --short --untracked-files=all` 已证实：

1. 存在未跟踪噪声目录与文件，如：
   - `.agents/`
   - `.claude/`
   - `Brain/data/`
   - `doc/official_doc/`
   - 当前任务包下多个 `report/plan/handoff`
2. 当前主工作区不适合作为“未建 baseline 就直接并行开发”的默认基线。

这说明：

1. 后续若进入 worktree，必须先记一份噪声基线；
2. 但这一步属于评审后的开发准备，不属于本轮 child-agent 评审范围。

## 6. 当前里程碑评审必须回答的问题

### 6.1 Flow Gate

必须拍板：

1. 新流程到底是两级节点还是三级等待节点；
2. `nodeCode / nodeName / ReviewStatusEnum` 的最终一一映射；
3. `formPath` 是否切到新 detail/approval 壳；
4. `AUDIT / PROOFREAD` 与 flow 定义的关系是“一套 flow 多变量”还是“两套 flowCode”。

### 6.2 Contract Gate

必须拍板：

1. 是否拆成 `page/detail/action` 多契约；
2. 是否显式补 `resubmit`；
3. 是否补 `freeze` 或等价的审批前业务冻结动作；
4. page 与 detail 是否允许继续复用同一 VO / TS 类型。

### 6.3 Frontend IA Gate

必须拍板：

1. 最终是三页：
   - `index`
   - `create/edit`
   - `detail/approval`
2. 还是两页：
   - `form`
   - `detail`
3. 共享流程组件 hook 应该由哪一层持有：
   - editorial 页面适配层
   - 共享流程组件可选扩展点

### 6.4 SQL Gate

必须拍板：

1. 正式事实源是否切到 `Brain/script/sql/update`；
2. 菜单 / 权限 SQL 是否与业务表 DDL 分离；
3. 何时回刷模块内置 `editorial_review.sql` 样例。

### 6.5 Verification Gate

必须拍板：

1. 里程碑评审通过的标准是什么；
2. 后续“可提测”至少需要哪些定向验证和集成验证证据；
3. 评审结论是否允许直接进入 worktree 准备，还是先落 review / decision 再进入。

## 7. 建议的评审输出格式

为避免 child-agent 输出继续散乱，建议要求两个评审角色都按以下结构出具结果：

1. `已确认`
2. `风险`
3. `待确认`
4. `推荐方案`
5. `是否批准进入开发准备`

其中：

1. `architect` 需要给出：
   - 方案是否可进入开发准备
   - 必须先锁定的设计门禁
   - 禁止越界项
2. `project_reviewer` 需要给出：
   - 对执行性、验证口径、边界划分的独立挑战
   - 若反对，明确指出冲突点、风险和替代建议

## 8. Child-Agent 申请清单

### 8.1 是否值得启用 child agent

本轮建议：**值得，但仅限设计评审，不进入编码。**

理由：

1. 当前任务是典型的“多文档 + 多代码锚点 + 高风险边界”的里程碑评审；
2. 需要独立挑战，单主控自审容易把既有 mapping/plan/execution 的惯性带入结论；
3. 当前上下文已经较长，角色化评审有助于降低上下文污染与遗漏风险；
4. 任务边界清晰，适合 child-agent：
   - 输入明确
   - 输出明确
   - 不涉及写代码
   - 不涉及 worktree 实施

### 8.2 为什么本地执行不是更优默认

不是因为主控做不了，而是因为：

1. 本轮关键价值在“独立评审”和“挑战现有方案”，不是信息搜集本身；
2. `architect` 与 `project_reviewer` 的角色提示能更稳定地产出两个不同视角：
   - 方案闭环视角
   - 风险挑战视角
3. 这比主控继续单线程压缩结论，更接近真正的里程碑评审。

### 8.3 申请角色与 owned boundary

#### Role 1: `architect`

Owned boundary:

1. 只评审本任务包材料与已核对代码事实；
2. 判断是否可以进入开发准备；
3. 锁定必须先拍板的设计门禁。

禁止事项：

1. 不写代码；
2. 不创建 worktree；
3. 不扩展成实现方案比较会；
4. 不把 `pub-nexus` 当成可直接迁入代码源。

预期产物：

1. 一份架构评审意见；
2. 明确 `approve / conditional approve / block` 之一；
3. 列出必须先落盘的 review / decision 主题。

#### Role 2: `project_reviewer`

Owned boundary:

1. 只挑战当前方案的可执行性、验证口径和边界隔离；
2. 审核 child-agent 之后是否真的适合进入 worktree 准备。

禁止事项：

1. 不写代码；
2. 不创建 worktree；
3. 不代替 architect 做最终批准。

预期产物：

1. 一份独立挑战意见；
2. 明确同意点、反对点与冲突点；
3. 对“可提测口径”和“并行开发边界”给出严格性评估。

### 8.4 推荐执行顺序

本轮**不建议并行启用两个评审 agent**，推荐顺序是：

1. `architect`
2. `project_reviewer`
3. 主控汇总冲突点
4. 等用户拍板

原因：

1. 当前全局治理默认链路就是 `architect -> project_reviewer -> architect` 的里程碑评审逻辑；
2. 先让 `architect` 固定设计门禁，再让 `project_reviewer` 挑战，更利于收口；
3. 这一阶段目标是“拍板前评审”，不是“追求并行速度”。

### 8.5 预期收益评估

按 child-agent 固定评估维度，本轮判断如下：

1. `context cleaner`：是
   - 两个角色只看评审输入，不继续卷入实现细节。
2. `corruption/context-loss lower`：是
   - 长上下文下，角色化评审比主控继续单线程推进更稳。
3. `task large enough`：是
   - 评审输入覆盖官方知识库、历史缺陷链、flow、SQL、前后端契约与共享组件。
4. `net execution efficiency improves`：中高
   - 额外有整合成本，但能显著降低“边评审边实现后返工”的风险。
5. `independent review or parallel value`：高
   - 核心价值是独立挑战，不是并行编码。

### 8.6 启用前的确认口径

只有在用户明确确认后，才允许：

1. 启用 `architect`；
2. 启用 `project_reviewer`；
3. 进入正式设计评审。

启用时的固定约束应原样带给 child agents：

1. `gxpublish-brain` 是真实落地仓库；
2. `pub-nexus` 仅作参考规范源；
3. 不允许直接复制 `pub-nexus` 代码；
4. 允许不兼容旧流程实例；
5. 本轮不写代码；
6. 若评审结论冲突，先回主控汇总，不直接自行拍板。

## 9. 当前推荐下一步

1. 由用户确认是否按本输入包启用 `architect`；
2. 若确认，再启用 `project_reviewer` 做独立挑战；
3. 主控汇总两者结论；
4. 若存在冲突，整理“冲突点 / 推荐方案 / 风险 / 取舍”等待用户拍板；
5. 拍板完成后，至少落一份 `review` 或 `decision`。

## 10. 一句话结论

当前最合理的推进方式不是立即并行开发，而是先基于本输入包完成一次严格的里程碑设计评审，把 `flow / 状态 / 契约 / IA / SQL / 验证口径` 六类门禁锁死，再决定是否进入 worktree 与并行实现。 
