Owner: Codex
Task: manuscript-review handoff
Status: WP10 code landed and reverified; runtime HTTP/BPM proof pending
Updated: 2026-03-23 04:29:00 CST

# Manuscript Review Handoff v06 (Current, Only Entry)

## 0. 重要声明（唯一续接入口）

从本文件开始，`v05` 不再是当前唯一入口。后续会话必须优先从以下两份材料恢复：

1. `doc/agent/manuscript-review-tdd_20260321-020326/handoffs/20260323_manuscript-review_handoff_v06.md`
2. `doc/manuscript_review/product/20260321_manuscript-review_product-requirements_v01.md`

如需了解被中断会话 `019d1619-01dc-7c80-96cb-8cb161e19d3f` 的详细总结，读取：

- `doc/agent/manuscript-review-tdd_20260321-020326/reports/20260323_manuscript-review_019d1619-session-summary_report_v01.md`

## 1. 当前分支状态

- 分支：`manuscript-review-tdd-20260321`
- 基线提交：`0e8283c4fa20684ef0b647c3fed91108ec458e10`
- 当前阶段：可提交检查点

本轮代码仍严格限定在：
- `Brain/brain-modules/brain-manuscript-review/**`
- `plus-ui-ts/src/views/manuscript-review/**`

## 2. 本轮新增已确认事实

### 2.1 readable 后端已补齐两条关键缺口

- readable ledger/detail controller + service + DTO + entity/mapper 已在模块内落地。
- `ManuscriptReviewApiController` 已改为 `@ConditionalOnBean(ManuscriptReviewService.class)`，避免 submit/resubmit 未闭环时阻塞应用启动。
- readable detail 的动作栏判定不再固定成 history-only。
- 当前审批人识别已补齐最小运行态链路：
  - `brain_manuscript_review`
  - `brain_manuscript_review_flow_config`
  - `sys_role`
  - `sys_user_role`
  - `sys_user`

### 2.2 readable 前端已补齐真接口兼容

- 台账、详情页已从裸 `fetch` 切换到项目统一 `request` 调用链。
- `detail.contract.ts` 已兼容当前后端 nested readable payload：
  - `summaryCard`
  - `manuscriptCard`
  - `actionBar`
  - `timeline`
  - `resources`
- 历史、资源、动作栏角色推导不再只依赖旧平铺 payload。

## 3. 本轮验证结果

### 3.1 定向验证

- 后端定向 Maven：
  - 命令：
    - `cd Brain`
    - `JAVA_HOME=/home/kugua/.local/share/mise/installs/java/temurin-17.0.18+8 /home/kugua/.local/share/mise/installs/maven/3.9.13/apache-maven-3.9.13/bin/mvn -pl brain-modules/brain-manuscript-review -am -DskipTests=false -Dtest=ManuscriptReviewServiceTest,ManuscriptReviewDetailPermissionPolicyTest,ManuscriptReviewApiControllerTest test`
  - 结果：
    - `Tests run: 26, Failures: 0, Errors: 0, Skipped: 0`
    - `BUILD SUCCESS`

- 前端定向 Vitest：
  - 命令：
    - `cd plus-ui-ts`
    - `pnpm exec vitest run src/views/manuscript-review/route-shell.spec.ts src/views/manuscript-review/detail-readable-display.spec.ts`
  - 结果：
    - `Test Files 2 passed`
    - `Tests 7 passed`

### 3.2 构建验证

- 前端生产构建：
  - 命令：`cd plus-ui-ts && pnpm run build:prod`
  - 结果：成功

- 后端 `brain-admin` 打包：
  - 命令：`cd Brain && JAVA_HOME=/home/kugua/.local/share/mise/installs/java/temurin-17.0.18+8 /home/kugua/.local/share/mise/installs/maven/3.9.13/apache-maven-3.9.13/bin/mvn -pl brain-admin -am -DskipTests package`
  - 结果：成功

## 4. 当前仍未闭环项

### 4.1 真实 HTTP 证据未落盘

当前尚未在本轮 handoff 中固化以下证据：
- 登录取 token
- 真实调用 `/manuscript-review/readable/ledger`
- 真实调用 `/manuscript-review/readable/detail`
- 基于本地验证账号的 readable 动作栏差异取证

### 4.2 BPM 去审批未闭环

- 当前详情页可基于 readable payload 推导出 “去审批” 按钮是否展示。
- 但 `去审批` 仍未打通到 BPM 办理页真实跳转。
- 因此不能把当前状态误报成 “WP11 BPM 去审批已完成”。

### 4.3 浏览器级证据未闭环

- 当前只有源码级、单测级、构建级与后端启动级证据。
- 尚未补浏览器级页面证据。

## 5. 风险

- 当前审批级别映射依赖中文节点文案：
  - `待一级审批`
  - `待二级审批`
  - `待三级审批`
- 若后续节点文案改名，需要同步 readable service 映射。
- 当前 flow config 读取策略按 `tenant_id + process_type + status=0` 取有效配置；若未来允许多条并行有效配置，需要补更严约束。
- `mr_*` 本地验证账号虽然存在，但当前数据库密码为空；若要做真实审批人 HTTP 取证，需要先补 dev-only 密码。

## 6. 下一会话建议起点

先输出：
- 已确认
- 风险
- 待确认
- 推进方案

然后只围绕以下三件事推进：
1. 为 `mr_*` 本地验证账号补 dev-only 密码，并插入一条高位样例审校记录。
2. 跑真实登录 + readable ledger/detail HTTP，固化本地联调证据。
3. 评估并补齐 `去审批 -> BPM 办理页` 的真实路由跳转链；若闭环不了，明确 blocker 和最小缺口。

## 7. 另一台机器的新会话提示词（仓库内版）

在另一台机器的新会话中，可直接使用以下提示词：

```text
你继续接管 `gxpublish-brain` 仓库中 `manuscript_review` 这条 clean-slate 研发线。

不要读旧聊天；只从以下两类材料恢复上下文：
1. `doc/agent/manuscript-review-tdd_20260321-020326/handoffs/20260323_manuscript-review_handoff_v06.md`
2. `doc/manuscript_review/product/20260321_manuscript-review_product-requirements_v01.md`

如需了解被中断会话 `019d1619-01dc-7c80-96cb-8cb161e19d3f` 的工作总结，再读：
3. `doc/agent/manuscript-review-tdd_20260321-020326/reports/20260323_manuscript-review_019d1619-session-summary_report_v01.md`

先输出：
- 已确认
- 风险
- 待确认
- 推进方案

硬约束继续有效：
- 主控只编排/调度/监控 sub agent；自己不直接实现，除非我另行批准破例。
- 继续 TDD：无 failing test 不写生产代码；每个切片先 RED 再 GREEN；先定向验证再扩矩阵。
- 绝对不要读取或引用任何旧 editorial 业务实现/页面/API/SQL 作为需求或兼容基线。
- 任何代码或持久化文件改动前，先给 Goal / Affected Files / Risk / Verification / Rollback。
- 默认只等 sub agent 完成通知；10 分钟无完成通知再轻量 ping。
- 不要每个工作包都停下来问我，直接顺推。

当前已确认状态：
- readable ledger/detail 后端 contract 已落地。
- readable detail 当前审批人识别已补最小闭环。
- 前端已兼容 nested readable payload。
- 后端定向测试 26 绿，前端定向测试 7 绿，前端 build 绿，backend brain-admin package 绿。

当前未闭环项：
1. 真实登录 + readable ledger/detail HTTP 证据尚未落盘。
2. `去审批` 尚未打通 BPM 办理页真实跳转。
3. 浏览器级证据尚未完成。

下一轮目标：
1. 给本地验证账号补 dev-only 密码并插入高位样例数据。
2. 拿到真实 HTTP 联调证据。
3. 继续评估并补 `去审批` 的 BPM 跳转链；如果做不完，明确 blocker 并落 handoff。
```
