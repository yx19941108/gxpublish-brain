Owner: Codex
Task: editorial-manuscript-controller-audit
Status: SQL_PREP_READY + JOINT_DEBUG_SURFACE_READY
Updated: 2026-03-18

# 2026-03-18 主控 SQL / Browser 联合审核报告 v02

> Supersedes: `20260318_editorial-manuscript-controller-sql-browser-audit_report_v01.md`

## 1. 范围与口径

本报告只基于以下当前基线继续推进，不重开已锁定问题：

1. `decisions/ADR-0002_editorial-manuscript-rebuild-6gate-baseline.md`
2. `plans/20260317_editorial-manuscript-rebuild-implementation_plan_v01.md`
3. `handoffs/20260317_editorial-manuscript-dual-codex-shared_handoff_v01.md`

本次 v02 的新增输入仅来自：

1. Browser/QA 审核稿  
   `C:\kuguaHome\project\gxpublish-brain\.agent-temp\editorial-manuscript-rebuild\browser-qa\20260318_editorial-manuscript-browser-qa_controller-review-note_v01.md`
2. Browser/QA 明细包  
   `C:\kuguaHome\project\gxpublish-brain\.agent-temp\editorial-manuscript-rebuild\browser-qa\20260318_editorial-manuscript-browser-qa_joint-debug-surface-ready_v01.md`
3. SQL/Backend 准备态报告  
   `C:\kuguaHome\project\gxpublish-brain\.agent-temp\editorial-manuscript-rebuild\sql-backend\20260318_sql-backend_prep-ready_report_v01.md`
4. 主控本轮 live 复核结果

本报告目标仍仅为：

1. 审核 SQL/Backend 线与 Browser/QA 线的准备度；
2. 把状态维持在 `SQL_PREP_READY + JOINT_DEBUG_SURFACE_READY`；
3. 明确主控已批准项、仍待裁决项和下一 gate 前置条件。

本报告不代表：

1. `SQL_APPLIED`
2. `BACKEND_SMOKE_READY`
3. `REAL_HTTP_READY`
4. `BROWSER_EVIDENCED`
5. `可提测`
6. `运行时闭环通过`

## 2. 已确认事实

### 2.1 当前 live 事实

1. 主控本轮通过 MySQL live 查询确认：
   - `brain_editorial_review = 66`
   - `editorial_review_flow` 当前 `active definition = 2`、`deleted definition = 1`
   - `flow_instance_biz_ext` 中与 `editorial_review_flow` runtime 关联的记录数为 `43`
2. 主控本轮通过 MySQL live 查询确认：
   - 当前菜单仍保留旧 editorial 路径痕迹
   - 主菜单：`path=editorial/review`, `component=editorial/review/index`
   - 子菜单：`path=reviewEdit`, `component=editorial/review/reviewEdit`
3. 主控本轮通过磁盘 live 查询确认：
   - `C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe` 存在
   - `C:\Program Files\MySQL\MySQL Server 8.0\bin\mysqldump.exe` 存在
4. 主控本轮通过源码 live 查询确认：
   - `EditorialReviewApprovalContextVo` 当前只包含 `flowCode/formPath/applyCode/processType/status/reviewStatus/canEdit`
   - 不包含 `taskId/instanceId/canApprove`
5. 主控本轮通过源码 live 查询确认：
   - `submitVerify.vue` 已具备 `completeTask/backProcess/terminationTask`
   - 当前 `form.vue/detail.vue/submitVerify.vue` 未静态确认独立业务 `cancel` 入口

### 2.2 对两条执行线产物的审核结论

1. SQL/Backend 线提交的准备态报告覆盖了主控要求的关键包：
   - 受影响表
   - 备份落点与 `mysqldump` 模板
   - apply 顺序
   - SQL 执行器路径
   - backend 启动与 HTTP smoke 方案
   - 当前阻断点
2. Browser/QA 线提交的准备态包覆盖了主控要求的关键包：
   - login prep
   - browser matrix
   - evidence 目录与命名规则
   - `JOINT_DEBUG_SURFACE_READY` 口径
   - 待主控裁决项
3. 两条执行线都没有把本轮表述成“联调已完成”或“运行时闭环通过”。

### 2.3 对 v01 主控稿的纠偏

1. v01 中 `brain_editorial_review = 63` 已被本轮 live 查询纠正为 `66`。
2. Browser/QA 审核稿中“当前 MySQL MCP 不是目标 truth source”的风险点，只能代表其子线本轮能力边界，不能继续作为主控当前结论：
   - 主控当前会话已直接查询 `gxpublish_brain`
   - 因此该项已从“主控 blocker”降为“Browser/QA 子线未自行验证 live DB”

## 3. SQL / Backend 线审核结论

### 3.1 已批准项

1. `SQL_PREP_READY` 口径成立。
2. 受影响表覆盖范围基本完整，已达到 destructive 前审核粒度：
   - `brain_editorial_review`
   - `brain_editorial_attachment`
   - `brain_editorial_link`
   - `brain_editorial_history`
   - `flow_definition`
   - `flow_node`
   - `flow_skip`
   - `flow_instance`
   - `flow_instance_biz_ext`
   - `flow_task`
   - `flow_his_task`
   - `sys_role`
   - `sys_user_role`
   - `sys_menu`
   - `sys_role_menu`
3. `mysql.exe` / `mysqldump.exe` 作为 destructive SQL 执行器的建议路径已被主控 live 确认可用。
4. “先备份，再 apply”的顺序已被主控接受。

### 3.2 当前仍不批准直接 apply 的原因

1. 当前 `20260317_editorial_review_flow_role_seed.sql` 仍不是 tenant-aware full-rebuild SQL。
2. 当前 repo 仍不存在独立 editorial `menu/permission` SQL 文件。
3. live `sys_menu` 仍保留 `reviewEdit` 旧子菜单，证明菜单切换尚未闭环。
4. live `editorial_review_flow` 仍有两条 active definition，且 runtime 残留真实存在。
5. 因此当前状态仍是 `SQL_PREP_READY`，不是 `SQL_APPLY_READY`。

### 3.3 需要主控继续向 SQL/Backend 要的只剩这些

1. tenant-aware flow full-rebuild SQL
2. 独立 menu/permission SQL
3. 对 runtime 清理是否一并纳入 destructive cleanup 的最终确认
4. apply 后可供 Browser/QA 使用的记录集与 taskId 来源

## 4. Browser / QA 线审核结论

### 4.1 已批准项

1. `JOINT_DEBUG_SURFACE_READY` 口径成立。
2. Browser/QA 的 login prep、browser matrix、evidence plan 已达到主控要求，不需要重写一版文档才能继续。
3. 证据目录拆分可接受：
   - `login`
   - `list`
   - `form`
   - `detail`
   - `workflow`
   - `tus`
   - `network`
   - `console`
   - `http`
4. 现有 approval-shell deeplink 假设被主控“有条件批准”：
   - 在 backend 未扩 `approvalContext` 之前
   - 运行时验证允许依赖 `type=approval&taskId=<taskId>`，`instanceId` 可选
   - 该批准只用于浏览器取证，不等于 contract 已闭环
5. Browser/QA 当前提出的五类账号包结构被主控接受：
   - normal applicant
   - certified applicant
   - first approver
   - second approver
   - final approver

### 4.2 当前仍不批准 runtime 取证完成的原因

1. 集成树 `plus-ui-ts/node_modules` 仍不存在。
2. 真实账号/密码包仍未提供。
3. runtime backend 是否已 apply SQL 仍未被主控收到执行后证据。
4. 真实 HTTP、截图、HAR、console、request-response 证据仍不存在。

### 4.3 对 Browser/QA 待裁决项的主控裁决

1. `approval-shell deeplink`：
   - 结论：`批准为当前运行时验证入口`
2. `BR08_CANCEL_PENDING`：
   - 结论：`分类正确，继续保留为 pending`
   - 原因：ADR verification matrix 仍要求覆盖 `cancel`，不能因为当前 UI 未静态确认就擅自关闭或移出当前目标范围
3. `evidence 目录拆分`：
   - 结论：`批准`
4. `是否可在 GO_BROWSER_RUNTIME 后直接执行现有 case id 与目录规则`：
   - 结论：`批准`
   - 前提：先补齐账号包、依赖和 SQL apply 状态

## 5. 决策

主控本轮决策如下：

1. `SQL_PREP_READY` 维持成立。
2. `JOINT_DEBUG_SURFACE_READY` 维持成立。
3. 当前不把状态推进到：
   - `SQL_APPLY_READY`
   - `BACKEND_SMOKE_READY`
   - `REAL_HTTP_READY`
   - `BROWSER_EVIDENCED`
   - `可提测`

## 6. Need From SQL / Backend

1. 提交 tenant-aware `editorial_review_flow` full-rebuild SQL。
2. 提交独立 editorial `menu/permission` SQL。
3. 明确是否把 `flow_instance / flow_instance_biz_ext / flow_task / flow_his_task` 一并纳入 destructive cleanup。
4. SQL apply 后提供可用于 Browser/QA 的记录集：
   - `DRAFT`
   - `BACK`
   - `WAITING_FIRST`
   - `WAITING_SECOND`
   - `WAITING_FINAL`
5. SQL apply 后提供 Browser/QA 可用的 `taskId` / `instanceId` 来源。

## 7. Need From Browser / QA

1. 提供可登录账号包：
   - `tenantId`
   - `username`
   - `password`
2. 在集成树补齐 `node_modules` 后执行 fresh frontend 静态命令。
3. 在收到 `GO_BROWSER_RUNTIME` 且 SQL apply 状态明确后，直接按 `BR01 -> BR10` 开始取证。
4. 保持 `BR08_CANCEL_PENDING` 为当前范围内 pending 项，不得自行关单。

## 8. 本轮主控产物

1. 本报告：`reports/20260318_editorial-manuscript-controller-sql-browser-audit_report_v02.md`
2. 配套 handoff：`handoffs/20260318_editorial-manuscript-controller-sql-browser-prep_handoff_v02.md`

