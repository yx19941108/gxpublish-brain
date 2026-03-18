Owner: Codex
Task: editorial-manuscript-rebuild
Status: Ready For Next Session
Updated: 2026-03-17

# 审校模块替换式重构设计阶段 handoff

## 1. 本轮已完成

1. 已按指定顺序读取：
   - `AGENTS.md`
   - 本仓 prep report / fulltest report / ADR
   - flow / service / mapper / `reviewEdit.vue` / `review.ts`
   - `pub-nexus` ready report / contract sql report / menu sql report / `integration.ts` / `index.ts` / `ProcessInstanceOperationButton.vue`
2. 已补充核对本仓：
   - `EditorialReviewController.java`
   - `ReviewStatusEnum.java`
   - `EditorialReviewBo.java`
   - `EditorialReviewVo.java`
   - `index.vue`
   - `approvalButton.vue`
   - `submitVerify.vue`
   - `editorial_review.sql`
3. 已落盘：
   - 映射设计报告
   - implementation plan

## 2. 当前最重要结论

1. 本仓现状不是空白重建，而是已有 editorial/workflow/TUS/history/data-scope 骨架。
2. `pub-nexus` 可迁移的是结构原则，不是代码：
   - page/detail 分契约
   - create/resubmit 分动作
   - 审批前业务冻结
   - detail 壳承载审批动作
   - 菜单 SQL 与业务表 DDL 分离
3. 当前本仓有两个必须先收口的硬冲突：
   - `editorial_review_flow.json` 只有两级审批节点，但 `ReviewStatusEnum` 与历史测试按 `10 -> 20 -> 30 -> 40` 工作
   - `editorial_review.sql` 已落后于 Java / Mapper / 前端契约，缺少 `review_status`、`process_type`
4. 当前前端 `reviewEdit.vue` 仍是单体页，不能继续作为替换式重构的目标形态。

## 3. 推荐执行方向

1. 保留：
   - `brain-editorial`
   - 四张主业务表主干
   - TUS 上传
   - workflow 事件接入
   - 数据权限与 `editorial:review:*`
2. 替换：
   - 单体 `reviewEdit.vue`
   - 粗粒度 API 契约
   - flow 节点语义
   - SQL 迁移方式
3. 删除：
   - 旧流程实例兼容约束
   - 列表和详情共用同一 VO/TS 类型
   - 继续把模块内置 SQL 文件当成真实数据库基线

## 4. 下一会话建议起点

先从以下文件恢复，不要先开写代码：

1. `doc/agent/editorial-manuscript-rebuild_20260317-132339/reports/20260317_editorial-manuscript-rebuild-mapping_report_v01.md`
2. `doc/agent/editorial-manuscript-rebuild_20260317-132339/plans/20260317_editorial-manuscript-rebuild-implementation_plan_v01.md`
3. 若直接进入执行，先做 plan 的 `Phase 0`，不要跳过。

## 5. 上下文风险提示

本轮上下文已经明显增长，且涉及：

1. 本仓历史缺陷链
2. `pub-nexus` 参考规范
3. flow / SQL / page / API 四条边界

建议：

1. 下一会话直接从本 handoff + plan 恢复；
2. 不要再重新扫一遍无关仓库文件；
3. 阶段边界结束后继续补充 handoff，而不是只留在聊天窗口。 
