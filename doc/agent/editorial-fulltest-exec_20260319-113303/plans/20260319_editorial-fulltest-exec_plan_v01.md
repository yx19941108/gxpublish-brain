---
Owner: Codex
Task: editorial-fulltest-exec
Status: draft
Updated: 2026-03-19 11:33:03 +08:00
---

# 审校主线全量测试执行计划 v01

## 1. 目标

本计划用于在 `main` 分支进入 editorial 全量测试前，先锁定执行输入、fresh evidence 规则、停止条件，以及本会话新增的最高优先级验收项。

本计划只覆盖以下两段：

1. 执行前输入锁定与门禁复核
2. 高优先级项 `HP-01 ~ HP-05` 的 fresh 现状判定

若 `HP-01 ~ HP-05` 任一失败，本会话立即停在阻断结论，不继续扩大到 `plan_v02` 全量矩阵。

## 2. 继承基线

### 2.1 已继承工件

1. `doc/agent/editorial-fulltest-prep_20260319-111500/plans/20260319_editorial-fulltest-prep_plan_v02.md`
2. `doc/agent/editorial-fulltest-prep_20260319-111500/reviews/20260319_editorial-fulltest-prep_review_v01.md`
3. `doc/agent/editorial-mainline-closure_20260318-184705/reports/20260319_editorial-mainline-closure_runtime-progress_report_v03.md`
4. `doc/agent/editorial-mainline-closure_20260318-184705/handoffs/20260319_editorial-mainline-closure_runtime-progress_handoff_v03.md`

### 2.2 已确认 gate

1. `SQL_APPLIED=yes`
2. `REAL_HTTP_READY=yes`
3. `BROWSER_EVIDENCED=yes`
4. `可测试=yes`

说明：以上只代表运行时基线已到“可进入 full-test 前置阶段”，不代表本会话高优先级验收项已通过。

## 3. 账号与权限锁定

### 3.1 用户提供账号

密码统一为 `123456`：

1. `editorial_applicant`
2. `edi_applicant2`
3. `zhangsan`
4. `lisi`
5. `wangwu`
6. `zhaoliu`
7. `qianqi`
8. `sunba`
9. `zhoujiu`
10. `wushi`

### 3.2 当前数据库已验证事实

`sys_user` 样本显示：

1. 上述 10 个账号均存在，`tenant_id=099142`
2. `zhangsan -> editorial_review_applicant`
3. `lisi -> editorial_review_applicant_has_certificate`
4. `wangwu / sunba -> editorial_first_level_approver`
5. `zhaoliu / zhoujiu -> editorial_second_level_approver`
6. `qianqi / wushi -> editorial_third_level_approver`
7. `editorial_applicant / edi_applicant2` 当前无 `sys_user_role` 绑定
8. `zhaoliu` 的昵称是“二级发起人”，但真实 `role_key` 是二级审批人

### 3.3 本会话主测账号候选

优先主测链路先采用“角色绑定已实锤”的 5 类账号：

| 角色 | 主测账号候选 | 备用账号候选 | 当前结论 |
|---|---|---|---|
| 发起人（无证） | `zhangsan` | `editorial_applicant` | `zhangsan` 已有真实角色；`editorial_applicant` 待验异常 |
| 发起人（有证） | `lisi` | `edi_applicant2` | `lisi` 已有真实角色；`edi_applicant2` 待验异常 |
| 一级审批 | `wangwu` | `sunba` | 两者角色一致 |
| 二级审批 | `zhoujiu` | `zhaoliu` | `zhoujiu` 昵称与角色一致；`zhaoliu` 角色正确但昵称异常 |
| 三级审批 | `qianqi` | `wushi` | 两者角色一致 |

### 3.4 本会话账号异常处理

以下异常不阻止计划编写，但会进入 `G-03` / `HP-05` 的第一优先核验项：

1. `editorial_applicant` 无角色绑定，不能直接当作发起人通过事实
2. `edi_applicant2` 无角色绑定，不能直接当作持证发起人通过事实
3. `zhaoliu` 昵称与真实角色不一致，需要 live 登录后以功能面复核

## 4. 门禁顺序

本会话开始正式 fresh 测试前，严格按以下顺序执行：

1. `G-00` 基线指纹核验
2. `G-01` 后端服务可用
3. `G-02` 前端服务可用
4. `G-03` 5 类账号登录态建立
5. `G-04` 数据库核验
6. `G-05` 上传链路最小冒烟
7. `G-06` 主入口无误

说明：

1. `G-03` 先验证主测账号候选；若异常，再切备用账号
2. `G-06` 的主入口固定为 `/workflow/editorial/review`
3. 所有 gate 通过前，不进入 `plan_v02` 全量矩阵

## 5. 新增最高优先级项

在 `G-00 ~ G-06` 全通过后，先只做以下 `HP-01 ~ HP-05` 的 fresh 判定：

| 编号 | 场景 | 通过标准 | 证据 |
|---|---|---|---|
| `HP-01` | 审批节点历史留痕 | 发起、审批、审批节点修改都能在历史中看到操作人、时间、操作类型，且顺序可追溯 | 页面截图 + 请求响应 + `brain_editorial_history` |
| `HP-02` | 审批人修改后可提交 | 当前节点审批人从可编辑入口进入修改后，页面存在提交能力，且能形成有效提交动作 | 页面截图 + 控制台/网络 + SQL |
| `HP-03` | 三审三校列表直接审批 | 三审三校列表页对当前节点责任人提供“审批”入口，并跳转到详情审批壳，操作区出现审批提交按钮 | 列表截图 + 路由 + 详情截图 |
| `HP-04` | 列表按钮矩阵 | 当前节点责任人看到“修改、审批”；非当前节点责任人只看到“详情” | 多角色截图 |
| `HP-05` | 列表可见性与我的任务一致 | 三审三校列表页的可见口径与“我发起的、我的待办、我的已办”一致；待办映射到可操作按钮，已办映射到只读按钮 | 多角色截图 + 网络 + 必要 SQL |

## 6. 停止条件

出现以下任一情况，立即停止，不继续扩大执行：

1. 任一 `G-00 ~ G-06` 失败
2. 任一 `HP-01 ~ HP-05` 失败
3. 旧 evidence 被误当作 fresh evidence
4. 样本 `reviewId / applyCode / attachmentId / caseId` 未绑定完成
5. 角色登录态串扰，无法保证不同用户上下文隔离

## 7. Fresh Evidence 规则

### 7.1 目录

本会话 fresh evidence 根目录固定为：

`C:\kuguaHome\project\gxpublish-brain\.agent-temp\editorial-fulltest-exec_20260319-113303\`

推荐子目录：

1. `gate/`
2. `hp/`
3. `matrix/`
4. `sql/`
5. `network/`
6. `screenshots/`

### 7.2 命名

统一采用：

`<phase>_<caseId>_<user>_<reviewId>_<timestamp>.<ext>`

示例：

`hp_HP-03_wangwu_2034463255494508545_20260319-114500.png`

### 7.3 旧证据边界

1. `.agent-temp/editorial-mainline-closure_20260318-184705/browser/**` 只可标记为 `baseline`
2. 本会话所有 `G-*` 和 `HP-*` 证据必须为 fresh
3. 报告中不得把旧截图、旧 JSON、旧 SQL 结果写成“本会话通过证据”

## 8. 执行编排

### 8.1 主控职责

主控只做以下工作：

1. 串行放行 `G-00 ~ G-06`
2. 决定是否继续 `HP-*`
3. 一旦阻断，立即停止扩散
4. 汇总 child agent 只读探索结果

### 8.2 并行子线职责

本会话允许最多 3 条执行型子线在线，但当前阶段只允许其做只读探索/准备：

1. `Backend/Code`：锁后端合同、历史留痕、角色/权限映射
2. `Frontend/Code`：锁列表/详情/表单/审批入口与按钮矩阵差距
3. `QA Runtime Prep`：锁 evidence 协议、门禁执行顺序、旧 evidence 风险

说明：在 `HP-*` 明确阻断前，不让子线直接越过主控去跑正式 full-test。

## 9. 与本轮需求的关系

本计划对用户新增要求的处理方式如下：

1. 用户意见 `1 ~ 5`：作为最高优先级，先做 fresh 判定
2. 用户意见 `6 ~ 7`：作为阻断后的下一阶段设计/重构输入，不属于当前 full-test 先行范围

## 10. 下一步

本计划写入后，下一步顺序固定为：

1. 回读计划关键段落
2. 收齐剩余子线只读结果
3. 若计划确认无误，从 `G-00` 开始
4. `G-*` 通过后先做 `HP-01 ~ HP-05`
5. 仅当 `HP-*` 全绿时，才切入 `plan_v02` 的全量矩阵
