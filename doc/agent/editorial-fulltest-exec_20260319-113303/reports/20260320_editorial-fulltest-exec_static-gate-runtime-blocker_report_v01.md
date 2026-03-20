Owner: Codex
Task: editorial-fulltest-exec
Status: blocked
Updated: 2026-03-20 08:40:00 +08:00

# 20260320 editorial-fulltest-exec static gate and runtime blocker report v01

## 已确认

1. 当前工作分支仍为 `main`，且本轮接管的是“用户已手工提交后的已提交基线”，不是空白开始。
2. 本轮静态 gate 已 fresh 重跑并拆开记录：
   - backend 定向单测：`mvn -pl brain-modules/brain-editorial -am "-Dtest=EditorialReviewServiceResubmitTest,EditorialReviewContractAssemblerTest" "-Dsurefire.failIfNoSpecifiedTests=false" test`
     - 结果：`Tests run: 11, Failures: 0, Errors: 0, Skipped: 0`
     - 时间：`2026-03-19 18:21:52 +08:00`
   - frontend vitest：`pnpm exec vitest run src/views/editorial/review/integration.spec.ts`
     - 结果：`13 passed`
     - 时间：`2026-03-19 18:22:58 +08:00`
   - targeted eslint：
     - 首轮结果：仅命中 `Prettier/CRLF` 噪声，没有新增逻辑类报错
     - 处理：仅在 `plus-ui-ts/src/views/editorial/review/**`、`plus-ui-ts/src/api/editorial/review.ts`、`plus-ui-ts/src/components/Process/approvalButton.vue` 边界内执行格式收口
     - 重跑结果：通过
   - frontend build：`pnpm build:prod`
     - 实际脚本：`vite build --mode production`
     - 结果：`exit 0`
3. 本轮 fresh runtime 预检已重做：
   - `2026-03-20` 对 `http://127.0.0.1:9888/auth/code` 的 `curl` 连接失败
   - `2026-03-20` 对 `http://127.0.0.1:9999/` 的 `curl` 连接失败
   - 说明：运行时服务在本轮开始时并未处于 ready 状态
4. 本轮为恢复 runtime，已做最小服务启动尝试：
   - frontend dev：`pnpm dev -- --host 127.0.0.1 --port 9999`
     - 结果：Vite `ready`
   - backend：`C:\kuguaHome\env\java\jdk17\bin\java.exe -jar Brain\brain-admin\target\brain-admin.jar`
     - 结果：启动失败
     - 关键阻断：`RedisConnectionException: Unable to connect to Redis server: localhost/127.0.0.1:6379`
5. 因 `G-01` 未通过，本轮没有进入 `HP-03 -> HP-04 -> HP-05 -> HP-02 -> HP-01 -> NR-*` 的 fresh regression。

## 风险

1. 当前 runtime blocker 是外部依赖未就绪，不是 editorial 业务用例已 fresh 通过或 fresh 失败。
2. Redis 缺失会阻断整个后端启动，因此：
   - `G-01` 失败
   - `G-02` 即便前端 dev server ready，也不能视为 runtime gate 已过
3. 旧 browser harness 和旧 evidence 仍然禁止复用：
   - `.agent-temp/editorial-fulltest-exec_20260319-113303/browser/evidence/**`
   - `.agent-temp/editorial-mainline-closure_20260318-184705/**`
4. 当前会话 MySQL MCP 不可用；后续若 runtime 或修复需要数据库变更，应只落仓库 SQL 文件并把路径交给用户执行。
5. 当前工作树存在非本轮产物，不能误提交：
   - 已跟踪删除：`doc/agent/editorial-fulltest-prep_20260319-111500/plans/~$20260319_editorial-fulltest-prep_plan_v02.xlsx`
   - 未跟踪：`test_qa_flow.py`

## 待确认

1. 这台机器或目标机器上，Redis 是否应由用户手工启动，还是仓库已有约定好的本地启动方式。
2. 若后续需要数据库改动，是否沿用“我落 SQL 文件，你手工执行”的协作边界。
3. Redis 就绪后，是否继续沿用当前 regression 根目录，还是新开 `regression_YYYYMMDD-HHmmss` 子目录承接本轮真正 fresh runtime 证据。

## 推进方案

1. 当前轮先停在 `runtime env blocker`，不把静态 gate 全绿误写成业务回归已通过。
2. 下一步先补足 Redis 运行条件，再重新执行：
   - `G-01` backend ready
   - `G-02` frontend ready
   - `G-03(primary)` 主测账号登录态
   - `G-04` 数据核验
   - `G-06` 主入口无误
3. 只有上述 gate fresh 通过后，才进入固定顺序：
   - `HP-03`
   - `HP-04`
   - `HP-05`
   - `HP-02`
   - `HP-01`
   - `NR-01`
   - `NR-02`
   - `NR-03`
   - `NR-04`
   - `NR-05`
4. 若 runtime 暴露 backend/frontend 缺陷，再按边界重开 Backend / Frontend / QA 三线修补与回归。

## Decision

1. 本轮静态 gate 结论：
   - backend tests：green
   - frontend vitest：green
   - targeted eslint：green after boundary-only format cleanup
   - build:prod：green
2. 本轮 runtime 结论：
   - `full runtime regression not started`
   - `blocked at G-01 by Redis dependency`
3. 按 stop rule，本轮不进入 `HP-*` / `NR-*`。

## Need From Backend

1. 无新增代码 blocker 结论；当前首先需要的是 runtime 依赖 `Redis 6379` 可用。
2. 若后续 runtime 暴露后端问题，再仅在 `Brain/brain-modules/brain-editorial/**` 范围内修补。

## Need From Frontend

1. 当前静态面没有新增 blocker。
2. 后续若 runtime 暴露列表按钮矩阵、approval-edit、历史 diff 或预览问题，再仅在已锁定前端边界内修补。

## Need From QA/Browser

1. 下一轮 runtime 必须继续使用 fresh evidence discipline：
   - 新建本轮 evidence 子目录
   - 不复用旧 probe
   - 不把旧 closure/exec evidence 写成本轮 fresh 结论
2. 若要跨机继续，优先从本报告和 handoff 恢复，不要从聊天窗口恢复。

## Artifacts

1. 本报告：
   - `doc/agent/editorial-fulltest-exec_20260319-113303/reports/20260320_editorial-fulltest-exec_static-gate-runtime-blocker_report_v01.md`
2. 本轮 handoff：
   - `doc/agent/editorial-fulltest-exec_20260319-113303/handoffs/20260320_editorial-fulltest-exec_runtime-blocker_handoff_v01.md`
3. 本轮代码改动：
   - 前端 editorial 审阅边界内的格式收口文件

## Next Gate

1. 先解决 Redis `localhost:6379` 可用性。
2. 然后 fresh 重跑：
   - `G-01`
   - `G-02`
   - `G-03(primary)`
   - `G-04`
   - `G-06`
3. gate 通过后，按固定顺序进入 `HP-03` 起步，不恢复旧矩阵。
