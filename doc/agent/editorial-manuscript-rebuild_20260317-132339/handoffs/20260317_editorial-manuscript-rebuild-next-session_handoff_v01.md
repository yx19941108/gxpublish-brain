Owner: Codex
Task: editorial-manuscript-rebuild
Status: Ready For Next Session
Updated: 2026-03-17

# `gxpublish-brain` 审校模块替换式重构下一会话 Handoff

## 当前状态

1. 已明确放弃在 `pub-nexus` 上继续猜 BPM SQL。
2. 已明确采用：
   - `pub-nexus` 作为参考规范源
   - `gxpublish-brain` 作为真实落地仓库
   - 允许不兼容旧流程实例的替换式重构
3. 已完成前置收口：
   - `gxpublish-brain` 当前 editorial/workflow 锚点已确认
   - `pub-nexus` 可迁移的高价值经验已确认
   - 推荐路径已锁定

## 下个会话不要做的事

1. 不要继续在 `pub-nexus` 上补猜 BPM 表结构。
2. 不要把 `pub-nexus` 代码直接整块复制到 `gxpublish-brain`。
3. 不要一上来就全量重写所有文件。
4. 不要绕过 `gxpublish-brain` 本地文档与现有 workflow/editorial 锚点。

## 下个会话主目标

下个会话的主目标是：

**先完成 `pub-nexus -> gxpublish-brain` 的映射设计与重构边界，再输出 implementation plan。**

不是直接开写全部代码。

## 下个会话先读

### 先读本仓

1. `C:\kuguaHome\project\gxpublish-brain\AGENTS.md`
2. `C:\kuguaHome\project\gxpublish-brain\doc\agent\editorial-manuscript-rebuild_20260317-132339\reports\20260317_editorial-manuscript-rebuild-prep_report_v01.md`
3. `C:\kuguaHome\project\gxpublish-brain\doc\agent\reports\20260302_editorial-fulltest_report_v01.md`
4. `C:\kuguaHome\project\gxpublish-brain\doc\agent\decisions\ADR-0001_editorial-fulltest-hotfix.md`
5. `C:\kuguaHome\project\gxpublish-brain\Brain\brain-modules\brain-editorial\src\main\resources\flow\editorial_review_flow.json`
6. `C:\kuguaHome\project\gxpublish-brain\Brain\brain-modules\brain-editorial\src\main\java\com\gxpublish\brain\editorial\service\impl\EditorialReviewServiceImpl.java`
7. `C:\kuguaHome\project\gxpublish-brain\Brain\brain-modules\brain-editorial\src\main\resources\mapper\editorial\EditorialReviewMapper.xml`
8. `C:\kuguaHome\project\gxpublish-brain\plus-ui-ts\src\views\editorial\review\reviewEdit.vue`
9. `C:\kuguaHome\project\gxpublish-brain\plus-ui-ts\src\api\editorial\review.ts`

### 再读参考仓

1. `C:\kuguaHome\project\pub-nexus\.worktrees\mg\doc\agent\manuscript-module-architecture_20260316-104303\reports\20260317_manuscript-fullstack-ready_report_v01.md`
2. `C:\kuguaHome\project\pub-nexus\.worktrees\mg\nexus-platform\doc\agent\reports\20260317_manuscript-approval-contract-sql_report_v01.md`
3. `C:\kuguaHome\project\pub-nexus\.worktrees\mg\nexus-platform\doc\agent\reports\20260317_manuscript-approval-menu-sql_report_v01.md`
4. `C:\kuguaHome\project\pub-nexus\.worktrees\mg\yudao-ui-admin-vue3\src\views\bpm\manuscript\integration.ts`
5. `C:\kuguaHome\project\pub-nexus\.worktrees\mg\yudao-ui-admin-vue3\src\api\bpm\manuscript\index.ts`
6. `C:\kuguaHome\project\pub-nexus\.worktrees\mg\yudao-ui-admin-vue3\src\views\bpm\processInstance\detail\ProcessInstanceOperationButton.vue`

## 下个会话先输出什么

先输出：

1. `已确认`
2. `风险`
3. `待确认`
4. `推进方案`

然后给出 2 到 3 条重构路线，并明确推荐哪一条。

## 下个会话建议产物

至少输出并落盘：

1. 一份 `report`
   - `pub-nexus -> gxpublish-brain` 映射设计
2. 一份 `plan`
   - 替换式重构 implementation plan
3. 如上下文变长，再补一份 `handoff`

## 下个会话建议重构边界

### 保留项

1. `gxpublish-brain` 的仓库结构
2. `brain-editorial` 模块承载方式
3. `editorial_review_flow.json` 作为现有流程定义锚点
4. 现有 workflow/service/mapper 的真实实现入口

### 替换项

1. 页面 IA 与 UI
2. 业务对象与状态语义的表达方式
3. API 契约组织
4. 审批壳与业务表单的挂载方式
5. 批注/附件/链接/历史的交互组织

### 删除项

1. 对旧流程实例兼容的约束
2. 明显错误或丑陋的 UI/交互负担
3. 不能支撑新结构的旧状态流转假设

## 建议提示词

```text
项目：gxpublish-brain 审校模块替换式重构

你当前继续的是 `C:\kuguaHome\project\gxpublish-brain` 的审校模块重构前置设计阶段。
当前已确认：
1. 不再继续在 `pub-nexus` 上猜 BPM 付费 SQL。
2. 本轮采用“`pub-nexus` 作为参考规范源、`gxpublish-brain` 作为真实落地仓库”的路径。
3. 允许不兼容旧流程实例做替换式重构。

先不要凭聊天记忆推进，按以下顺序读取：
1. `C:\kuguaHome\project\gxpublish-brain\AGENTS.md`
2. `C:\kuguaHome\project\gxpublish-brain\doc\agent\editorial-manuscript-rebuild_20260317-132339\reports\20260317_editorial-manuscript-rebuild-prep_report_v01.md`
3. `C:\kuguaHome\project\gxpublish-brain\doc\agent\reports\20260302_editorial-fulltest_report_v01.md`
4. `C:\kuguaHome\project\gxpublish-brain\doc\agent\decisions\ADR-0001_editorial-fulltest-hotfix.md`
5. `C:\kuguaHome\project\gxpublish-brain\Brain\brain-modules\brain-editorial\src\main\resources\flow\editorial_review_flow.json`
6. `C:\kuguaHome\project\gxpublish-brain\Brain\brain-modules\brain-editorial\src\main\java\com\gxpublish\brain\editorial\service\impl\EditorialReviewServiceImpl.java`
7. `C:\kuguaHome\project\gxpublish-brain\Brain\brain-modules\brain-editorial\src\main\resources\mapper\editorial\EditorialReviewMapper.xml`
8. `C:\kuguaHome\project\gxpublish-brain\plus-ui-ts\src\views\editorial\review\reviewEdit.vue`
9. `C:\kuguaHome\project\gxpublish-brain\plus-ui-ts\src\api\editorial\review.ts`
10. `C:\kuguaHome\project\pub-nexus\.worktrees\mg\doc\agent\manuscript-module-architecture_20260316-104303\reports\20260317_manuscript-fullstack-ready_report_v01.md`
11. `C:\kuguaHome\project\pub-nexus\.worktrees\mg\nexus-platform\doc\agent\reports\20260317_manuscript-approval-contract-sql_report_v01.md`
12. `C:\kuguaHome\project\pub-nexus\.worktrees\mg\nexus-platform\doc\agent\reports\20260317_manuscript-approval-menu-sql_report_v01.md`
13. `C:\kuguaHome\project\pub-nexus\.worktrees\mg\yudao-ui-admin-vue3\src\views\bpm\manuscript\integration.ts`
14. `C:\kuguaHome\project\pub-nexus\.worktrees\mg\yudao-ui-admin-vue3\src\api\bpm\manuscript\index.ts`
15. `C:\kuguaHome\project\pub-nexus\.worktrees\mg\yudao-ui-admin-vue3\src\views\bpm\processInstance\detail\ProcessInstanceOperationButton.vue`

本轮目标不是直接开写所有代码，而是先完成：
1. `pub-nexus -> gxpublish-brain` 的映射设计
2. 保留项 / 替换项 / 删除项
3. backend / frontend / flow / SQL 的重构边界
4. 一份可执行 implementation plan

第一条回复必须先给：
- `已确认`
- `风险`
- `待确认`
- `推进方案`

要求：
1. 先基于本仓真实实现，再把 `pub-nexus` 当参考规范输入，不要反过来。
2. 不要直接复制 `pub-nexus` 代码到 `gxpublish-brain`。
3. 不要重开“是否兼容旧流程实例”的讨论，这一点已锁定为允许不兼容的替换式重构。
4. 所有重要结论落盘到 `doc/agent/editorial-manuscript-rebuild_20260317-132339/` 任务包。
5. 至少补一份 report 和一份 plan；如果上下文变长，再补 handoff。
```

## 一句话状态

下个会话最合理的起点不是“马上实现”，而是：**先把 `pub-nexus` 的正确经验和 `gxpublish-brain` 的真实锚点映射清楚，再进入替换式重构计划。**
