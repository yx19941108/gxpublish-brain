Owner: Codex
Task: editorial-manuscript-controller-audit
Status: SQL_PREP_READY + JOINT_DEBUG_SURFACE_READY
Updated: 2026-03-18

# 2026-03-18 主控 SQL / Browser 联合审核报告

## 1. 范围与口径

本报告只基于以下当前基线继续推进，不重开已锁定问题：

1. `decisions/ADR-0002_editorial-manuscript-rebuild-6gate-baseline.md`
2. `plans/20260317_editorial-manuscript-rebuild-implementation_plan_v01.md`
3. `handoffs/20260317_editorial-manuscript-dual-codex-shared_handoff_v01.md`

本报告目标仅为：

1. 审核 SQL/Backend 线与 Browser/QA 线的当前准备度；
2. 把状态推进到 `SQL_PREP_READY + JOINT_DEBUG_SURFACE_READY`；
3. 明确仍未拿到的执行证据与禁止越界项。

本报告不代表：

1. `SQL_APPLIED`
2. `REAL_HTTP_READY`
3. `BROWSER_EVIDENCED`
4. `可提测`
5. `运行时闭环通过`

## 2. 已确认事实

### 2.1 集成树与静态代码事实

1. 集成树同时存在 backend `POST /editorial/review/resubmit` 与 frontend `resubmitReviewAction -> /editorial/review/resubmit`。
2. backend `editorial_review_flow.json` 已把 `formPath` 静态切到 `/editorial/review/detail`。
3. frontend 已存在 `plus-ui-ts/src/views/editorial/review/verification.ts`，并把 `page/detail`、`IA`、`legacy fallback`、`resubmit`、`WAITING_*` 壳、`lint/build` 纳入前端定向矩阵。
4. frontend 登录仍是仓库标准链路：`/login` 页面 -> `POST /auth/login` -> token 写入 store -> `GET /system/user/getInfo`。
5. frontend 常量路由已包含：
   - `/editorial/review/form`
   - `/editorial/review/detail`
   - `/editorial/review/reviewEdit`
6. `approvalButton.vue` 已对 `WAITING` 与 `WAITING_*` 做审批入口兼容判断。

### 2.2 本轮 fresh 静态验证

1. 已 fresh 执行 backend 最小验证：
   - 命令：`mvn -pl brain-modules/brain-editorial -am -DskipTests=false "-Dsurefire.failIfNoSpecifiedTests=false" "-Dtest=EditorialReviewControllerContractTest,EditorialReviewServiceResubmitTest,EditorialReviewContractAssemblerTest,EditorialReviewWorkflowDefinitionTest" test`
   - 结果：`Tests run: 9, Failures: 0, Errors: 0, Skipped: 0`
2. 本轮未执行 fresh frontend `vitest / eslint / build:prod`。
3. 原因不是前端代码已被判失败，而是当前集成树 `plus-ui-ts/node_modules` 不存在，尚未具备本树直接运行前端静态命令的依赖前置条件。

### 2.3 目标库当前事实

已通过 MySQL 只读查询确认 `gxpublish_brain` 存在，且以下目标表存在：

| 表名 | 当前行数/现状 |
| --- | --- |
| `brain_editorial_review` | 63 行 |
| `flow_definition` | 8 行 |
| `flow_node` | 51 行 |
| `flow_skip` | 48 行 |
| `sys_role` | 9 行 |
| `sys_user` | 15 行 |
| `sys_user_role` | 12 行 |

补充事实：

1. `brain_editorial_review.review_status` 当前是 `tinyint not null default 0`。
2. `brain_editorial_review.process_type` 当前是 `varchar(20) null`。
3. 共享 handoff v01 中“`brain_editorial_review` 已有 66 行数据”的细节已漂移；当前 live 证据为 63 行，本报告以后者为准。

### 2.4 当前 editorial flow / runtime 事实

`flow_definition` 中当前存在 3 条 `editorial_review_flow` 记录：

| id | version | del_flag | form_path |
| --- | --- | --- | --- |
| `2026563160604966914` | 2 | 0 | `/editorial/review/reviewEdit.vue` |
| `2022477562920669186` | 2 | 1 | `/editorial/review/reviewEdit.vue` |
| `2022475323749535746` | 1 | 0 | `/editorial/review/reviewEdit.vue` |

关键风险事实：

1. 当前不是“单一 active definition”，而是同一 `flow_code` 下仍有两条 `del_flag=0` 的定义。
2. 最新 active definition 下仍保留旧 `submit` 与 UUID 节点编码。
3. 最新 active definition 下还有额外节点 `a205066c-d3cd-49d0-8d93-666e900295e4`。
4. 当前 runtime 数据真实存在，且分布在两个 definition 上：

| 表名 | 关联方式 | 当前数量 |
| --- | --- | --- |
| `flow_instance` | `definition_id IN editorial_review_flow` | 43 |
| `flow_task` | `definition_id IN editorial_review_flow` | 122 |
| `flow_his_task` | `definition_id IN editorial_review_flow` | 143 |
| `flow_instance_biz_ext` | `business_id IN brain_editorial_review.id` | 43 |

5. 这意味着 SQL 路径如果只改 `flow_definition/node/skip` 而不先声明 runtime 清理策略，会保留混合 definition / nodeCode / task 数据。

### 2.5 当前角色与可登录用户矩阵

以下 editorial 角色已存在且已分配到真实启用用户：

| role_key | 用户数 |
| --- | --- |
| `editorial_review_applicant` | 1 |
| `editorial_review_applicant_has_certificate` | 1 |
| `editorial_first_level_approver` | 2 |
| `editorial_second_level_approver` | 2 |
| `editorial_third_level_approver` | 2 |

说明：

1. Browser/QA 线不缺“角色样本”，缺的是实际可登录凭据与使用顺序。
2. SQL 线若删除并重建 role id，会直接影响现有 `sys_user_role` 绑定，因此当前更安全的默认方向是保留 role id、按 `role_key` 原位规范化。

## 3. SQL / Backend 线审核结论

### 3.1 受影响表

本轮必须显式纳入的直接表：

1. `brain_editorial_review`
2. `flow_definition`
3. `flow_node`
4. `flow_skip`
5. `sys_role`

本轮若按“全量重构旧 editorial 设计”执行，则不能回避的联动表：

1. `flow_instance`
2. `flow_task`
3. `flow_his_task`
4. `flow_instance_biz_ext`

条件性联动表：

1. `sys_user_role`
   - 仅当 SQL 线打算删除并重建 role id 时才进入 destructive 范围
   - 若继续按现有 `role_key` 原位规范化，则只需只读备份，不应删除

### 3.2 当前脚本与 live 库之间的主要缺口

1. `20260317_editorial_review_flow_role_seed.sql` 当前是“改最新 definition + 补缺节点/skip”的脚本，不是“先清旧再重建唯一 active definition”的脚本。
2. 当前脚本不会清掉旧 active definition `2022475323749535746`。
3. 当前脚本不会清掉最新 definition 下的额外历史节点 `a205066c-d3cd-49d0-8d93-666e900295e4`。
4. 当前脚本未覆盖 runtime 表清理。
5. 当前脚本插入新 `flow_node/flow_skip` 时使用 `tenant_id='000000'`，而 live 目标 definition / node / skip / role 现状是 `tenant_id='099142'`；SQL 线必须明确是否统一 tenant 口径。
6. `20260317_editorial_review_rebuild_rollback.sql` 采用 `DROP COLUMN` 回退，属于数据语义不可逆回退；如果执行线要保留回滚可用性，必须把这一点显式标注为 destructive rollback，而不是“无损回滚”。

### 3.3 主控批准的 SQL PREP 包内容

在真正执行 destructive SQL 前，SQL/Backend 线必须至少交付以下清单并以此执行：

1. 备份范围
   - `flow_definition` 中全部 `flow_code='editorial_review_flow'` 行
   - 上述 definition 对应的 `flow_node`、`flow_skip`
   - 上述 definition 对应的 `flow_instance`、`flow_task`、`flow_his_task`
   - 与上述 instance 对应的 `flow_instance_biz_ext`
   - `sys_role` 中 editorial applicant / approver 相关角色
   - `sys_user_role` 中这些 role_id 的绑定关系
   - `brain_editorial_review` 全表，或至少所有将被 reset / 清理的行
2. 清理范围
   - 是否删除旧 active definition 与其全部 node/skip/runtime
   - 是否只保留一条新的 active `editorial_review_flow`
   - 当前 `WAITING_*` 与 `BACK` 业务行在 runtime 清理后如何处理
   - 当前 `apply_code` 与 runtime 解绑后的复位策略
3. apply 顺序
   - 先备份，再清 runtime / definition 旧数据，再执行业务表 normalization / DDL，再执行 flow-role seed，最后做 backend smoke
4. 执行器
   - MySQL MCP 本轮只用于只读审核，不作为 destructive SQL 执行器
   - SQL/Backend 线必须单独给出实际执行器，例如 `mysql.exe` 绝对路径或等价 DB 执行入口
5. backend smoke
   - 明确应用使用 `gxpublish_brain`
   - 明确 smoke 接口与样本记录
   - 至少覆盖 `GET list`、`GET detail`、`POST resubmit`

### 3.4 主控建议的 apply 顺序

以下顺序已被主控审核通过，可作为 SQL/Backend 线执行模板：

1. 只读确认当前 definition_id、role_id、instance_id、business_id 映射。
2. 导出或备份上述范围内的业务表、flow 表、角色表、用户角色表。
3. 冻结“当前 `WAITING_*` / `BACK` 业务行如何复位”的方案。
4. 清理 editorial flow 关联 runtime 表。
5. 清理或逻辑退役旧 `flow_definition / flow_node / flow_skip`，确保最终只剩一条目标 active definition。
6. 处理 `brain_editorial_review` 的字段与状态归一。
7. 应用 `20260317_editorial_review_rebuild.sql`。
8. 在 tenant 口径调整完成后应用 `20260317_editorial_review_flow_role_seed.sql`。
9. 启动 backend 并做真实 HTTP smoke。

## 4. Browser / QA 线审核结论

### 4.1 当前已具备的 joint debug surface

1. 登录链路已明确：
   - `/login`
   - `POST /auth/login`
   - token 写入 store
   - `GET /system/user/getInfo`
2. 前端开发代理已明确：
   - `VITE_APP_BASE_API -> http://localhost:9888`
   - `/tus/upload -> http://localhost:9888`
3. 审校路由面已明确：
   - `/editorial/review`
   - `/editorial/review/form`
   - `/editorial/review/detail`
   - `/editorial/review/reviewEdit`
4. 前端矩阵文件已明确要验证：
   - page/detail contract
   - IA 切换
   - legacy fallback
   - BACK 独立 `resubmit`
   - `WAITING_*` 审批壳
   - lint/build
5. 数据库中已有可用于真实登录分工的 applicant / approver 用户样本。

### 4.2 当前仍缺的 joint debug 执行前提

1. 集成树 `plus-ui-ts/node_modules` 不存在。
2. Browser/QA 线尚未提交“使用哪个真实账号、如何拿到密码或重置密码”的方案。
3. 尚未提交“哪条记录用于 applicant / certified applicant / approver 验证”的浏览器矩阵。
4. 尚未提交浏览器证据沉淀路径。
5. 尚未提交真实 HTTP 与浏览器链路的截屏 / HAR / console / request-response 证据。

### 4.3 主控批准的 Browser / QA 矩阵骨架

Browser/QA 线后续必须至少覆盖以下 6 条链路：

1. 无证发起人登录 -> 新建草稿 -> 提交 -> 进入 `WAITING_FIRST`
2. 有证发起人登录 -> 新建草稿 -> 提交 -> 跳过一级 -> 进入 `WAITING_SECOND`
3. `BACK` 记录登录 -> `/editorial/review/form?id=<id>&type=update` -> 主按钮文案为“再次提交” -> 真实请求命中 `/editorial/review/resubmit`
4. 一级审批人登录 -> `/editorial/review/detail?id=<id>&type=approval&taskId=<taskId>` -> 显示审批入口
5. 二级/三级审批人登录 -> 同类 `detail/approval shell` 验证 `WAITING_SECOND / WAITING_FINAL`
6. 旧入口 `/editorial/review/reviewEdit?...` -> 自动落到新 `form/detail`，不再承载主逻辑

### 4.4 主控批准的证据沉淀路径

后续 Browser/QA 证据请按以下口径落盘：

1. 临时截图、HAR、console、网络抓包：`C:\kuguaHome\project\gxpublish-brain\.agent-temp\editorial-manuscript-rebuild\controller\browser-qa\`
2. 持久化结论摘要：写回本任务包 `reports/` 新版本报告
3. 共享状态收口：写回本任务包 `handoffs/` 新版本 handoff

## 5. 决策

主控本轮决策如下：

1. 允许把当前状态推进到 `SQL_PREP_READY`。
2. 允许把当前状态推进到 `JOINT_DEBUG_SURFACE_READY`。
3. 不允许把当前状态表述成：
   - `SQL_APPLIED`
   - `BACKEND_READY`
   - `REAL_HTTP_READY`
   - `BROWSER_EVIDENCED`
   - `可提测`
   - `运行时闭环通过`

原因：

1. SQL 执行前置清单、清理范围、执行器与 runtime 清理策略已被锁定到可审核粒度，但 destructive SQL 尚未执行。
2. Browser/QA 登录方案、关键链路矩阵与证据沉淀口径已被锁定到可执行粒度，但尚无真实浏览器与 HTTP 证据。

## 6. Need From SQL / Backend

1. 把 `WAITING_*` 与 `BACK` 业务行在 runtime 清理后的复位策略写成明确 SQL 或明确不处理说明。
2. 把 `flow_definition / flow_node / flow_skip / flow_instance / flow_task / flow_his_task / flow_instance_biz_ext / sys_role / sys_user_role / brain_editorial_review` 的备份语句或备份命令落成清单。
3. 把“只保留一条 active definition”的 pre-clean SQL 落成可执行版本。
4. 明确 tenant 口径是否继续沿用 `099142`，并修正 seed 脚本中的 `000000` 假设。
5. 指定实际 destructive SQL 执行器。
6. 给出 apply 后 backend smoke 的真实 HTTP 清单、样本记录选择规则与预期结果。

## 7. Need From Browser / QA

1. 指定 applicant / certified applicant / first / second / third approver 的实际登录账号。
2. 指定凭据来源或密码重置方案。
3. 在集成树补齐 `plus-ui-ts/node_modules` 后，再执行 fresh frontend `vitest / eslint / build / dev`。
4. 把 6 条浏览器链路的真实操作顺序、样本记录 id、预期状态迁移、截图点位写成矩阵。
5. 产出真实 HTTP 与浏览器证据并按本报告约定位置沉淀。

## 8. 本轮产物

1. 本报告：`reports/20260318_editorial-manuscript-controller-sql-browser-audit_report_v01.md`
2. 配套 handoff：`handoffs/20260318_editorial-manuscript-controller-sql-browser-prep_handoff_v01.md`

