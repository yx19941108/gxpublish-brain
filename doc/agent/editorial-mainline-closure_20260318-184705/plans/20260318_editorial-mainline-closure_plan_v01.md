Owner: Codex
Task: editorial-mainline-closure
Status: In Progress
Updated: 2026-03-18

# Editorial Mainline Closure Plan v01

## Scope

本计划用于把 `main@30189ad` 从“已合并 + 最小验证完成”推进到本会话目标 gate：

1. `SQL_APPLY_READY`
2. `SQL_APPLIED`
3. `REAL_HTTP_READY`
4. `BROWSER_EVIDENCED`
5. `可测试`

本计划不把 compile/build/minimal HTTP 混写成 `可测试`。

## Gate 序列

### G1 `SQL_APPLY_READY`

Goal:
- 确认 tenant-aware flow/menu/runtime destructive apply 包、backup 方案、rollback 方案都已具备。

Affected Files:
- `Brain/script/sql/update/**`
- `.agent-temp/editorial-mainline-closure_20260318-184705/**`
- `doc/agent/editorial-mainline-closure_20260318-184705/reports/**`

Risk:
- destructive SQL 范围判断错误
- runtime 清理误伤非目标数据
- rollback 方案不完整

Verification:
- live DB 只读 inventory
- SQL 草案与仓库现有脚本对比
- apply/rollback 顺序核对

Rollback:
- 先生成备份清单与 restore 路径，再允许进入 G2

### G2 `SQL_APPLIED`

Goal:
- 执行备份、apply、必要 rollback 验证，并拿到 apply 后 live 证据。

Affected Files:
- `Brain/script/sql/update/**`
- `.agent-temp/editorial-mainline-closure_20260318-184705/mysql/**`
- `doc/agent/editorial-mainline-closure_20260318-184705/reports/**`

Risk:
- 数据不可逆丢失
- 菜单/角色权限断裂
- flow definition/runtime 状态异常

Verification:
- mysqldump/mysql 执行日志
- apply 后库表、菜单、flow definition、runtime 记录核对

Rollback:
- 使用 apply 前备份恢复
- 如必要，执行回滚脚本或 restore 流程

### G3 `REAL_HTTP_READY`

Goal:
- 在 JDK17 下完成 backend real HTTP 关键链路验证。

Affected Files:
- `Brain/**`
- `.agent-temp/editorial-mainline-closure_20260318-184705/**`
- `doc/agent/editorial-mainline-closure_20260318-184705/reports/**`

Risk:
- backend 与 apply 后数据面不一致
- workflow runtime 事件链路未闭环

Verification:
- editorial 定向测试
- JDK17 启动证据
- 登录、create、detail、history、submit、approval、back、cancel、termination、resubmit、TUS finish 的 HTTP 证据

Rollback:
- 停服务
- 恢复 SQL 备份
- 回退本轮 backend 改动

### G4 `BROWSER_EVIDENCED`

Goal:
- 拿到前端登录态与浏览器级关键链路证据。

Affected Files:
- `plus-ui-ts/**`
- `.agent-temp/editorial-mainline-closure_20260318-184705/**`
- `doc/agent/editorial-mainline-closure_20260318-184705/reports/**`

Risk:
- UI 路由、approval shell、upload/fallback 与 backend contract 漂移
- 浏览器证据与 HTTP 事实不一致

Verification:
- `pnpm exec vitest run src/views/editorial/review/integration.spec.ts`
- `pnpm build:prod`
- 截图、HAR、console、request-response

Rollback:
- 回退前端路由/页面变更
- 保留 `reviewEdit` fallback

### G5 `可测试`

Goal:
- 形成完整测试矩阵与 blocker 清单，满足进入人工测试的前置证据。

Affected Files:
- `doc/agent/editorial-mainline-closure_20260318-184705/reports/**`
- `doc/agent/editorial-mainline-closure_20260318-184705/handoffs/**`

Risk:
- 将局部通过误报为全链路 ready

Verification:
- QA 矩阵覆盖 submit、`10 -> 20 -> 30 -> 40`、back、cancel、termination、resubmit、TUS finish
- 明确未覆盖项与 owner

Rollback:
- 若任一前置 gate 不成立，则维持为对应 gate 状态，不宣称 `可测试`

## Owner

1. Controller: G1 裁决、集成结果、最终 gate 判定
2. Agent A: `Brain/**` + `Brain/script/sql/update/**`
3. Agent B: `plus-ui-ts/**`
4. Agent C: 测试矩阵、运行证据、通过/失败矩阵
