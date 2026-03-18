Owner: Codex
Task: editorial-manuscript-rebuild
Status: Ready For Next Session
Updated: 2026-03-17

# `gxpublish-brain` 审校模块替换式重构前置收口报告

## 1. 目标

本报告用于把 `pub-nexus` 稿件审核主线中已经验证过的高价值经验，整理为 `gxpublish-brain` 下个会话可直接使用的重构输入。

本轮只做前置工作，不直接进入实现。

本次已锁定的方向：

1. **不再继续** 在 `pub-nexus` 上猜 BPM 收费 SQL 或推断 Flowable/Yudao 运行时表结构。
2. **采用替换式重构**：
   - 允许不兼容旧流程实例
   - 以 `gxpublish-brain` 为真实落地仓库
   - 以 `pub-nexus` 为参考规范源，而不是直接复制代码

## 2. 已确认

### 2.1 `gxpublish-brain` 当前有效锚点

1. 仓库治理前置通过：
   - `.agent-temp/` 已在 `.gitignore`
   - `doc/agent/README.md` 存在
2. 当前审校模块不是空白实现，关键锚点仍在：
   - 流程定义：
     - `Brain/brain-modules/brain-editorial/src/main/resources/flow/editorial_review_flow.json`
   - 后端核心服务：
     - `Brain/brain-modules/brain-editorial/src/main/java/com/gxpublish/brain/editorial/service/impl/EditorialReviewServiceImpl.java`
   - 主查询 SQL：
     - `Brain/brain-modules/brain-editorial/src/main/resources/mapper/editorial/EditorialReviewMapper.xml`
   - 当前模块表结构：
     - `Brain/brain-modules/brain-editorial/src/main/resources/sql/editorial_review.sql`
   - 前端主页面：
     - `plus-ui-ts/src/views/editorial/review/reviewEdit.vue`
   - 前端 API：
     - `plus-ui-ts/src/api/editorial/review.ts`
3. 旧问题与历史修复已沉淀：
   - `doc/agent/plans/20260302_editorial-fulltest_plan_v01.md`
   - `doc/agent/reports/20260302_editorial-fulltest_report_v01.md`
   - `doc/agent/decisions/ADR-0001_editorial-fulltest-hotfix.md`

### 2.2 `gxpublish-brain` 当前实现形态

1. 当前流程引擎承载不是 Yudao BPM/Flowable 业务表单模式，而是：
   - 自定义 `editorial_review_flow.json`
   - `flowCode = editorial_review_flow`
   - `formPath = /editorial/review/reviewEdit`
2. 当前状态模型是双轨：
   - `status`
   - `review_status`
3. 当前业务数据模型已经存在四张主业务表：
   - `brain_editorial_review`
   - `brain_editorial_attachment`
   - `brain_editorial_link`
   - `brain_editorial_history`
4. 当前 UI 形态仍是传统表单页 + 审批按钮 + 历史列表，尚未形成像 `pub-nexus` 那样清晰的：
   - 台账入口
   - 独立 create/detail
   - 统一 detail shell 挂业务表单

### 2.3 `pub-nexus` 中可安全迁移的高价值结论

以下内容可作为**设计输入**迁入 `gxpublish-brain`，但不是直接复制实现：

1. 业务表单与审批壳分离：
   - 业务 create/detail 独立
   - BPM/流程 detail 壳负责承载业务表单
2. 路由与业务入口思路：
   - 列表是主入口
   - create/detail 是显式独立页面
3. API 契约分层：
   - page / detail / create / resubmit / freeze-annotation 分开
4. 批注冻结顺序：
   - 审批动作前先收口批注/工作台状态
5. 验收口径：
   - `目标模块定向验证`
   - `仓库全量 baseline 噪声`
   - `运行时闭环证据`
6. 不应迁入的内容：
   - 付费 BPM SQL 假设
   - Yudao/Flowable 运行时表依赖
   - `pub-nexus` 的特定模块命名和引擎实现细节

## 3. 风险

1. **最大风险不是代码迁移，而是错误迁移架构假设。**
   - `pub-nexus` 是 Yudao/BPM 业务表单思路
   - `gxpublish-brain` 当前是自定义 workflow JSON + editorial 业务层
   - 若直接复制 `pub-nexus` 代码，会把错误的引擎假设一起带进来
2. 当前 `gxpublish-brain` 模块已经有历史缺陷链：
   - 中间审批阶段 `review_status` 推进不稳
   - TUS `finish` 链路有过阻断缺陷
3. 替换式重构虽允许不兼容旧流程实例，但仍需小心两类耦合：
   - 后端状态机与 workflow event 的耦合
   - 前端上传/预览/审批按钮与后端接口的耦合
4. 若下个会话直接“全量重写”而不先做映射，会很容易把：
   - 旧 bug
   - 新交互
   - 新契约
   一起混成不可回退的大改。

## 4. 推荐路径

### 4.1 推荐方案

推荐采用：

**`pub-nexus` 规范迁移 + `gxpublish-brain` 本仓替换式重构**

具体做法：

1. 先提炼 `pub-nexus` 的“正确经验”：
   - 页面信息架构
   - 业务表单/detail 壳分离
   - 契约边界
   - 审批前冻结批注
   - 验收口径
2. 再映射到 `gxpublish-brain` 的真实锚点：
   - `editorial_review_flow.json`
   - `EditorialReviewServiceImpl`
   - `EditorialReviewMapper.xml`
   - `reviewEdit.vue`
   - `review.ts`
3. 最后分阶段替换：
   - 第一阶段：后端状态语义与契约收口
   - 第二阶段：前端 IA/UI 重构
   - 第三阶段：workflow JSON 与事件映射收口
   - 第四阶段：上传/历史/审批动作闭环

### 4.2 明确不推荐

1. 不推荐直接把 `pub-nexus` 模块代码复制进来再“边改边适配”。
2. 不推荐继续在 `pub-nexus` 上猜 BPM SQL 并试图把它补成可运行。

## 5. 下个会话建议目标

下个会话的目标不应是“马上开写所有代码”，而应是：

1. 先完成一份正式的 **映射设计 + 重构边界说明**
2. 明确保留项 / 替换项 / 删除项
3. 然后输出一份可执行 implementation plan
4. 最后再进入实现

## 6. 下个会话必须先读

### 6.1 `gxpublish-brain` 本仓文件

1. `C:\kuguaHome\project\gxpublish-brain\AGENTS.md`
2. `C:\kuguaHome\project\gxpublish-brain\doc\agent\reports\20260302_editorial-fulltest_report_v01.md`
3. `C:\kuguaHome\project\gxpublish-brain\doc\agent\decisions\ADR-0001_editorial-fulltest-hotfix.md`
4. `C:\kuguaHome\project\gxpublish-brain\Brain\brain-modules\brain-editorial\src\main\resources\flow\editorial_review_flow.json`
5. `C:\kuguaHome\project\gxpublish-brain\Brain\brain-modules\brain-editorial\src\main\java\com\gxpublish\brain\editorial\service\impl\EditorialReviewServiceImpl.java`
6. `C:\kuguaHome\project\gxpublish-brain\Brain\brain-modules\brain-editorial\src\main\resources\mapper\editorial\EditorialReviewMapper.xml`
7. `C:\kuguaHome\project\gxpublish-brain\plus-ui-ts\src\views\editorial\review\reviewEdit.vue`
8. `C:\kuguaHome\project\gxpublish-brain\plus-ui-ts\src\api\editorial\review.ts`

### 6.2 `pub-nexus` 参考文件

1. `C:\kuguaHome\project\pub-nexus\.worktrees\mg\doc\agent\manuscript-module-architecture_20260316-104303\reports\20260317_manuscript-fullstack-ready_report_v01.md`
2. `C:\kuguaHome\project\pub-nexus\.worktrees\mg\doc\agent\manuscript-module-architecture_20260316-104303\reports\20260317_manuscript-runtime-readiness_report_v01.md`
3. `C:\kuguaHome\project\pub-nexus\.worktrees\mg\nexus-platform\doc\agent\reports\20260317_manuscript-approval-contract-sql_report_v01.md`
4. `C:\kuguaHome\project\pub-nexus\.worktrees\mg\nexus-platform\doc\agent\reports\20260317_manuscript-approval-menu-sql_report_v01.md`
5. `C:\kuguaHome\project\pub-nexus\.worktrees\mg\yudao-ui-admin-vue3\src\views\bpm\manuscript\integration.ts`
6. `C:\kuguaHome\project\pub-nexus\.worktrees\mg\yudao-ui-admin-vue3\src\api\bpm\manuscript\index.ts`
7. `C:\kuguaHome\project\pub-nexus\.worktrees\mg\yudao-ui-admin-vue3\src\views\bpm\processInstance\detail\ProcessInstanceOperationButton.vue`

## 7. 本报告一句话结论

`gxpublish-brain` 下个会话最合理的起点，不是“照抄 `pub-nexus`”，而是：**先把 `pub-nexus` 已验证的稿件审核经验转成 `gxpublish-brain` 审校模块的映射设计，再基于现有 editorial/workflow 骨架做替换式重构。**
