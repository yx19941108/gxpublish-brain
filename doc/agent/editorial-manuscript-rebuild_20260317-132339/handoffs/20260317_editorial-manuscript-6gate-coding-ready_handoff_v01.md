Owner: Codex
Task: editorial-manuscript-rebuild
Status: Ready For Next Session
Updated: 2026-03-17

# 审校模块 6-Gate 编码准入 handoff

## 1. 本轮已完成

1. 已完成官方知识库、本仓历史报告、真实代码和 3 个 child-agent 的设计评审。
2. 已落盘正式编码准入决策：
   - `decisions/ADR-0002_editorial-manuscript-rebuild-6gate-baseline.md`
3. 项目状态已从“待评审”推进到“可开始编码”。

## 2. 当前有效基线

后续实现必须以以下文档为第一入口：

1. `decisions/ADR-0002_editorial-manuscript-rebuild-6gate-baseline.md`
2. `plans/20260317_editorial-manuscript-rebuild-implementation_plan_v01.md`
3. `reports/20260317_editorial-manuscript-milestone-review-input_report_v01.md`

## 3. 已锁定关键决策

1. 采用 `6-gate` 统一基线，不再新增第 7 个设计 gate。
2. 采用 **单 `flowCode=editorial_review_flow` + 显式 `processType(AUDIT|PROOFREAD)`**。
3. `freeze` 在编码准入阶段只锁审批前业务 hook 边界。
4. SQL 以 **MySQL 增量脚本** 为本轮编码入口。
5. `reviewEdit.vue` 只允许作为迁移期内部 fallback，最终 `formPath` 必须切走。
6. “可开始编码”与“可提测”已明确分离。

## 4. 下个会话建议起点

1. 先读 `ADR-0002`，不要重开 gate 讨论。
2. 再按 implementation plan 从最前面的 phase 开始执行。
3. 每次代码改动前继续给：
   - `Goal`
   - `Affected Files`
   - `Risk`
   - `Verification`
   - `Rollback`

## 5. 仍需注意

1. 本仓 dev 配置当前指向 `gxpublish_brain` schema；
2. 当前尚未在正确 schema 上 live verify editorial approver 角色 seed 是否已存在；
3. workflow `permissionFlag` 仍需与角色 seed/对齐脚本配套推进；
4. 本轮只是编码准入，不代表已经可提测。 
