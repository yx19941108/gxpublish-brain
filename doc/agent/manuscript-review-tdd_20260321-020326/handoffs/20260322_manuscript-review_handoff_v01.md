Owner: Codex
Task: manuscript-review handoff
Status: Ready for WP5 continuation
Updated: 2026-03-22 17:32 CST

# Manuscript Review Handoff v01

## 1. 当前 worktree

- Worktree 根路径：`/home/kugua/.config/superpowers/worktrees/gxpublish-brain/manuscript-review-tdd-20260321`
- 当前续接基线以该 worktree 未提交内容为准，不回退、不切回旧 `editorial` 业务线。

## 2. 需求真源与硬禁边界

- 唯一需求真源：`doc/manuscript_review/product/20260321_manuscript-review_product-requirements_v01.md`
- PRD 固定前提：本轮是全新审校模块建设，只复用平台通用能力和 BPM 办理页，不以旧失败审校实现为兼容基线。
- 硬禁边界：禁止把旧 editorial 业务实现、页面、API、SQL 当作需求真源或兼容基线，包括但不限于：
  - `Brain/brain-modules/brain-editorial/**`
  - `plus-ui-ts/src/views/editorial/review/**`
  - `plus-ui-ts/src/api/editorial/review.ts`
  - 旧 editorial SQL update 脚本
- 允许复用的仅是通用平台能力：`Brain/brain-common/**`、`Brain/brain-modules/brain-workflow/**`、`Brain/brain-system/**`、`doc/official_doc/**`

## 3. WP1-WP4 结果与证据摘要

- WP1 已完成：ADR 与实施计划已落地，新后端模块 `brain-manuscript-review` 已接入 `Brain/brain-modules/pom.xml`、`Brain/brain-admin/pom.xml`、`Brain/pom.xml`。
- WP2 已完成首轮 RED 资产沉淀：后端测试已锁住 `无草稿/无删除`、稿件号生成、缺配置拦截、持证发起人跳过一级、详情动作边界；前端 spec 已锁住路由壳和详情页业务可读展示。
- WP3 已完成首个实现切片，但当前不是全绿闭环：`brain-manuscript-review` 已有 `CreateManuscriptReviewCommand`、`ManuscriptReviewService`、权限策略、gateway 接口与结果对象；历史 surefire 报告显示 `ManuscriptReviewServiceTest` 9/9 通过、`ManuscriptReviewDetailPermissionPolicyTest` 1/1 通过。
- WP3 当前真实阻塞：2026-03-22 现场复跑 `mvn -pl brain-modules/brain-manuscript-review -am -DskipTests=false test` 在 `testCompile` 失败，原因是 `ManuscriptReviewServiceTest` 仍引用缺失的 `ResubmitManuscriptReviewCommand`；先修这个编译红点，再扩后端。
- WP4 已完成脚本起草，但未完成库内闭环：`20260321_manuscript_review_ddl_v01.sql`、`..._dml_v01.sql`、`..._rollback_v01.sql` 都已写出备份、apply、rollback 顺序，且显式写明“一期无草稿/删除语义”。
- WP4 当前数据库事实不是“已全量 apply”：MySQL 里目前只看到 `brain_manuscript_review_serial`，未见 `brain_manuscript_review_flow_config`，`sys_menu 60000-60007`、`sys_role 63000-63004`、`sys_dict_type 61000-61003` 也都未落库。把 WP4 视为“脚本完成、apply 未闭环且存在部分漂移”。
- 前端侧额外已验证事实：2026-03-22 定向执行 `pnpm exec vitest run src/views/manuscript-review/route-shell.spec.ts src/views/manuscript-review/detail-readable-display.spec.ts`，结果 2 个文件 4 个测试通过；这说明壳层与可读展示 contract 仍可运行，但不等于真实 API/BPM 已闭环。

## 4. 当前已知风险 / 待确认项

- 最大阻塞：后端当前 Maven 定向复跑不通过，先修 `ResubmitManuscriptReviewCommand` 缺失或相关 stale 引用，再继续 WP5。
- 数据库状态有漂移：脚本头声明“未 apply”，但库里已存在单张 `brain_manuscript_review_serial`；下一步要先决定是清理回干净基线后重放，还是承认部分 apply 后继续补齐。
- DML 种子尚未落库，`60000-65001` 号段是否与现场其他分支/环境冲突仍需 apply 前再确认。
- 当前前端只是 route shell + readable contract 级验证，尚未接上真实后端 DTO/API，也未证明 BPM 跳转、历史审计、资源生命周期、多视频标注闭环成立。
- 仓库里仍同时存在旧 editorial 路由与模块，grep 很容易把旧实现当“现成答案”；必须持续执行硬禁边界。

## 5. 后续工作包顺序

1. WP5：修复当前后端编译红点，并在 TDD 下补齐 `再次提交/撤销/驳回/历史追加/资源生命周期` 的命令模型与服务切片。
2. WP6：收敛数据库状态，确认备份方案与号段冲突后，完成 manuscript-review DDL/DML/rollback 的可重复 apply 与查询证据。
3. WP7：落地台账/详情/历史/资源区的业务可读 API 契约，严格拦住技术 ID、账号、原始码值、`roleKey` 外泄。
4. WP8：把前端 `index/form/detail` 与 BPM “去审批”跳转接到新 API，保持 `无草稿/无删除` 和“修改/再次提交”“修改/去审批”拆步口径。
5. WP9：做集成与浏览器级验收，覆盖 API 可读性、统一时间线、资源追加历史、多视频标注和 BPM 分流闭环，并输出下一版报告/handoff。

## 6. 子 agent 工作方式

- 主控只负责编排、监控、收敛事实，不把需求真源判断外包给子 agent。
- 默认只等待完成通知，不做高频轮询。
- 10 分钟无完成通知时，再做一次轻量 ping；只要状态、阻塞、下一步，不刷长日志。
- 任一子 agent 在做代码或持久化文件变更前，必须先写：
  - Goal
  - Affected Files
  - Risk
  - Verification
  - Rollback

## 7. 下一会话可直接粘贴的提示词

```text
你负责继续 gxpublish-brain 的 manuscript_review 线。

不要读旧聊天；只从以下两类材料恢复上下文：
1. /home/kugua/.config/superpowers/worktrees/gxpublish-brain/manuscript-review-tdd-20260321/doc/agent/manuscript-review-tdd_20260321-020326/handoffs/20260322_manuscript-review_handoff_v01.md
2. /home/kugua/.config/superpowers/worktrees/gxpublish-brain/manuscript-review-tdd-20260321/doc/manuscript_review/product/20260321_manuscript-review_product-requirements_v01.md

先输出：
- 已确认
- 风险
- 待确认
- 推进方案

硬约束：
- 不要把旧 editorial 业务实现、页面、API、SQL 当真源或兼容基线；只允许复用通用平台能力和官方文档。
- 先处理当前真实阻塞：`mvn -pl brain-modules/brain-manuscript-review -am -DskipTests=false test` 于 2026-03-22 失败，原因是 `ManuscriptReviewServiceTest` 仍引用缺失的 `ResubmitManuscriptReviewCommand`。
- 把 WP4 拆成“脚本已完成”和“库内未全量 apply/存在部分漂移”两层，不要混写成已闭环。
- 后续按 WP5 -> WP9 顺序推进。
- 任何代码或持久化文件改动前，先给 Goal / Affected Files / Risk / Verification / Rollback。
- 若启用子 agent，主控只编排/监控；默认只等完成通知；10 分钟无完成通知再轻量 ping。
```
