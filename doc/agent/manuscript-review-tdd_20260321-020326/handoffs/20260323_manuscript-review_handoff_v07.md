Owner: Codex
Task: manuscript-review github push checkpoint handoff
Status: current checkpoint prepared for commit/push; runtime HTTP/BPM/browser proof pending
Updated: 2026-03-23 06:41:39 CST

# Manuscript Review Handoff v07 (Current, Push Checkpoint Entry)

## 0. 唯一续接入口

从本文件开始，`v06` 降级为历史入口。后续会话默认只从以下材料恢复：

1. `doc/agent/manuscript-review-tdd_20260321-020326/handoffs/20260323_manuscript-review_handoff_v07.md`
2. `doc/manuscript_review/product/20260321_manuscript-review_product-requirements_v01.md`

如需补读本轮中断恢复与 WP10 收口细节，再读取：

3. `doc/agent/manuscript-review-tdd_20260321-020326/handoffs/20260323_manuscript-review_handoff_v06.md`
4. `doc/agent/manuscript-review-tdd_20260321-020326/reports/20260323_manuscript-review_019d1619-session-summary_report_v01.md`

## 1. 当前工作情况

### 1.1 已确认

- 当前研发线仍是 clean-slate `manuscript_review`，旧 `brain-editorial` 业务实现/页面/API/SQL 仍禁止作为需求或兼容真源。
- 当前提交面限定在：
  - `Brain/brain-modules/brain-manuscript-review/**`
  - `plus-ui-ts/src/views/manuscript-review/**`
  - `doc/agent/manuscript-review-tdd_20260321-020326/**`
- readable 后端主链已落地：
  - `ManuscriptReviewReadableApiController`
  - `ManuscriptReviewReadableService`
  - readable request/response DTO
  - review/history/attachment/link/video/config/user-role 映射 entity + mapper
- `ManuscriptReviewApiController` 已改成 `@ConditionalOnBean(ManuscriptReviewService.class)`，submit/resubmit 未闭环时不再阻塞整应用启动。
- readable detail 的动作栏权限不再固定成 history-only；当前审批人可经 `brain_manuscript_review_flow_config -> sys_role -> sys_user_role -> sys_user` 推导。
- readable 前端主链已落地：
  - 台账与详情页切换到项目统一 `request`
  - `detail.contract.ts` 已兼容 nested payload：`summaryCard` / `manuscriptCard` / `actionBar` / `timeline` / `resources`
  - `detail.vue` / `index.vue` 已接入当前 readable contract

### 1.2 已验证

- 后端定向测试通过：
  - `ManuscriptReviewServiceTest`
  - `ManuscriptReviewDetailPermissionPolicyTest`
  - `ManuscriptReviewApiControllerTest`
  - 结果：`Tests run: 26, Failures: 0, Errors: 0, Skipped: 0`
- 前端定向测试通过：
  - `route-shell.spec.ts`
  - `detail-readable-display.spec.ts`
  - 结果：`Tests 7 passed`
- 前端生产构建通过：`pnpm run build:prod`
- 后端 `brain-admin` 打包通过：`mvn -pl brain-admin -am -DskipTests package`

### 1.3 未闭环

- 真实登录 + `/manuscript-review/readable/ledger` + `/manuscript-review/readable/detail` 的 HTTP 证据还没落盘。
- 浏览器级联调证据还没落盘。
- `去审批` 目前只完成“是否显示”的前置判定，尚未完成跳到 BPM 办理页的真实路由闭环。

## 2. 当前风险

- 当前审批级别识别依赖中文节点文案：
  - `待一级审批`
  - `待二级审批`
  - `待三级审批`
  若节点文案改名，需要同步 readable service 的映射。
- flow config 当前按 `tenant_id + process_type + status=0` 取有效配置；若未来允许并行有效配置，需要补更严格约束。
- 本地 `mr_*` 验证账号存在，但数据库密码为空；要拿真实 HTTP 证据，通常需要先补 dev-only 密码。
- 本地数据库里 `brain_manuscript_review` 目前无样例数据；真实接口与浏览器证据需要先准备高位样例记录、历史和资源数据。

## 3. 下一阶段建议

后续会话先按以下顺序推进，不要把静态验证误报成运行时闭环：

1. 为本地 `mr_*` 验证账号补 dev-only 密码。
2. 插入一条高位样例审校记录，并补齐 history / attachment / external link / video marker。
3. 用真实登录态取证 `/manuscript-review/readable/ledger` 与 `/manuscript-review/readable/detail`。
4. 再评估并补 `去审批 -> BPM 办理页` 路由；若无法闭环，明确 blocker 和最小缺口。
5. 最后补浏览器级页面证据。

## 4. 另一台机器新会话提示词（仓库通用版）

以下提示词不依赖本机 worktree 或 `.codex` 私有路径，只依赖 Git 仓库内文件。到另一台机器后，先获取分支 `manuscript-review-tdd-20260321`，再直接使用：

```text
你继续接管 `gxpublish-brain` 仓库里的 `manuscript_review` clean-slate 研发线。

先不要读旧聊天；只从仓库内以下文件恢复上下文：
1. `doc/agent/manuscript-review-tdd_20260321-020326/handoffs/20260323_manuscript-review_handoff_v07.md`
2. `doc/manuscript_review/product/20260321_manuscript-review_product-requirements_v01.md`

如需补读上一轮中断会话总结，再读：
3. `doc/agent/manuscript-review-tdd_20260321-020326/reports/20260323_manuscript-review_019d1619-session-summary_report_v01.md`

先输出：
- 已确认
- 风险
- 待确认
- 推进方案

硬约束继续有效：
- 这是 clean-slate `manuscript_review` 线；绝对不要把旧 `brain-editorial` 业务实现/页面/API/SQL 当需求或兼容基线。
- 主控默认只负责编排/调度/监控；除非用户明确批准，否则不要本地越权直接实现。
- 继续 TDD：没有 failing test 不写生产代码；每个切片先 RED 再 GREEN；先定向验证，再扩矩阵。
- 任何代码或持久化文件改动前，先给 `Goal / Affected Files / Risk / Verification / Rollback`。
- 展示必须业务可读：不要在 UI 里直出数据库 ID、枚举码、账号、role key、permission flag；`reviewId` 仅允许用于隐式定位。
- 不要把定向测试/build/package 说成真实 HTTP 或浏览器联调通过。

当前已确认状态：
- readable ledger/detail 后端 contract 已落地。
- readable detail 当前审批人识别已补最小闭环。
- 前端已兼容 nested readable payload。
- 后端定向测试 26 绿，前端定向测试 7 绿，前端 build 绿，backend brain-admin package 绿。

当前未闭环项：
1. 真实登录 + `/manuscript-review/readable/ledger` + `/manuscript-review/readable/detail` 的 HTTP 证据未落盘。
2. `去审批` 尚未打通 BPM 办理页真实跳转。
3. 浏览器级证据未完成。

下一轮优先目标：
1. 为本地 `mr_*` 验证账号补 dev-only 密码。
2. 插入高位样例数据并获取真实 readable HTTP 证据。
3. 继续评估并补 `去审批 -> BPM 办理页`；若闭环不了，写清 blocker 和最小缺口并更新 handoff。
```
