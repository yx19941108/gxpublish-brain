Owner: Codex
Task: manuscript-review interrupted-session summary
Status: completed
Updated: 2026-03-23 04:28:00 CST

# 019d1619 会话总结报告 v01

## 1. 总结范围

本报告总结被中断会话 `019d1619-01dc-7c80-96cb-8cb161e19d3f` 的实际工作内容、已达成进度、当前可验证状态，以及本次续接会话在同一工作流下补完的关键收口项。

恢复与取证基线仅使用以下真源：
- `doc/agent/manuscript-review-tdd_20260321-020326/handoffs/20260322_manuscript-review_handoff_v05.md`
- `doc/manuscript_review/product/20260321_manuscript-review_product-requirements_v01.md`
- `.codex/history.jsonl` 与对应 rollout/subagent 日志中的可验证执行证据

## 2. 019d1619 会话实际完成内容

### 2.1 已完成的需求与边界收敛

- 延续并保持了 clean-slate `manuscript_review` 口径，没有把旧 `brain-editorial` 业务实现作为需求/兼容真源。
- 继续沿用 controller-only orchestration + TDD 规则：主控负责编排、约束、验证，实际切片由 backend/frontend worker 完成。
- 继续锁定以下业务口径：
  - `reviewId` 仅允许用于路由/API 定位，UI 文案与业务展示不可直出。
  - `manuscript_code` 允许展示。
  - 正文为纯文本，保留“正文不能为空”提示。
  - 一期无 `draft/delete/recycle`。

### 2.2 已完成的代码落地

019d1619 会话及其子 worker 实际把以下切片落到了代码里：

- 后端 readable contract 主链：
  - `ManuscriptReviewReadableApiController`
  - `ManuscriptReviewReadableService`
  - readable request/response DTO
  - 审校主记录、历史、附件、外链、视频标注相关 entity/mapper
- 后端定向测试扩充：
  - `ManuscriptReviewApiControllerTest`
  - `ManuscriptReviewServiceTest`
  - `ManuscriptReviewDetailPermissionPolicyTest`
- 前端 readable 页面接线：
  - `plus-ui-ts/src/views/manuscript-review/index.vue`
  - `plus-ui-ts/src/views/manuscript-review/detail.vue`
  - `plus-ui-ts/src/views/manuscript-review/detail.contract.ts`
  - `plus-ui-ts/src/views/manuscript-review/detail-readable-display.spec.ts`
- 运行时 blocker 修复：
  - `ManuscriptReviewApiController` 改为 `@ConditionalOnBean(ManuscriptReviewService.class)`，避免 submit/resubmit 运行态未闭环时阻塞整应用启动。

### 2.3 已完成的验证

019d1619 线中，已经拿到以下真实验证结果：

- 前端定向 Vitest 通过：
  - `route-shell.spec.ts`
  - `detail-readable-display.spec.ts`
- 后端 `brain-admin` 打包并成功启动：
  - 关键证据包含 `http-nio-9888` 启动与 `Started BrainApplication`
- 本次续接会话进一步确认并复跑通过：
  - 后端定向 Maven：`Tests run: 26, Failures: 0, Errors: 0, Skipped: 0`
  - 前端定向 Vitest：`Tests 7 passed`
  - 前端生产构建：`pnpm run build:prod` 成功
  - 后端全量 `brain-admin` 打包：`mvn -pl brain-admin -am -DskipTests package` 成功

## 3. 本次续接会话补完的关键收口

### 3.1 后端补齐“当前审批人”识别

本次续接在 readable detail 权限链上补了 019d1619 遗留的一个真实缺口：

- 问题：
  - `ManuscriptReviewReadableService` 原先固定 `currentApproverUserIds = emptySet`。
  - 这会导致详情页即使面对真实当前审批人，也永远拿不到 `修改 / 去审批 / 返回` 动作。
- 本次补完：
  - 按 `review.tenant_id + process_type + current_node_label`
  - 查询 `brain_manuscript_review_flow_config`
  - 再查 `sys_role -> sys_user_role -> sys_user`
  - 推导当前节点审批角色下的有效用户集合
- 定向测试已覆盖 `待二级审批 + 当前用户属于二级审批角色` 场景并转绿。

### 3.2 前端补齐 nested readable payload 兼容

本次续接在 readable detail 视图模型归一化上补了 019d1619 遗留的另一个真实缺口：

- 问题：
  - 前端原先主要兼容平铺 payload。
  - 面对后端当前 readable detail contract 的 nested 结构：
    - `summaryCard`
    - `manuscriptCard`
    - `actionBar`
    - `timeline`
    - `resources`
  - 会造成历史、资源、动作栏角色推导在真接口下丢失或降级。
- 本次补完：
  - `normalizeDetailViewModel()` 兼容 nested contract
  - 从 `actionBar.actions` 中文动作标签反推 `CURRENT_APPROVER / RETURNED_INITIATOR / HISTORY_PARTICIPANT`
  - 正确消费 `timeline[{ createTime, text }]`
  - 正确消费 `resources{ currentAttachments/currentExternalLinks/currentVideos/historyAttachments/historyExternalLinks/historyVideoMarkers }`

## 4. 当前已验证状态

截至本报告写入时，当前分支 `manuscript-review-tdd-20260321` 的已验证状态为：

- 已确认
  - WP10 readable ledger/detail 基本 contract 已落地。
  - readable detail 的当前审批人动作判定已补齐最小闭环。
  - 前端对当前后端 nested payload 的归一化已补齐。
  - 后端定向测试、前端定向测试、前端生产构建、后端 `brain-admin` 打包全部通过。
- 未完成
  - 真实登录 + `readable/ledger` + `readable/detail` 的 HTTP 闭环证据尚未在本次会话中落盘。
  - `去审批` 仍未打通到 BPM 办理页的真实跳转链。
  - 浏览器级证据尚未补齐。

## 5. 当前风险

- `current_node_label -> 审批级别` 的映射仍依赖中文节点文案；若节点展示文案未来变化，需要同步调整映射。
- flow config 读取目前按 `tenant_id + process_type + status=0` 取有效配置；若未来同租户同流程类型允许多条并行有效配置，需要再加更严格约束。
- 当前 `去审批` 仅完成“可显示”前置条件，未完成 BPM 办理页路由闭环。
- 当前分支可以作为稳定检查点提交，但不能把它误报为“真实 HTTP / 浏览器级联调已完成”。
