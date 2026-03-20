Owner: Codex Controller
Task: editorial-fulltest-exec
Status: Checkpoint
Updated: 2026-03-20 17:57 +08:00

# 本轮目标

在不重做 fatjar/packaging 漂移分析的前提下，先修复 fresh QA 已确认的首个 blocker：

- backend 创建历史文案必须精确为 `发起人创建申请{申请标题}`
- frontend 详情页历史区域必须直接展示最终业务文案，不再 prepend 操作人姓名

# 已确认

## 基线

- 当前工作树为 `main`
- runtime ready 已复核：
  - backend PID `55324` 存活
  - `9888` 监听且 `GET /auth/code` 返回 `200`
  - frontend `9999` 监听且 `GET /` 返回 `200`

## backend 修补

- 已新增/落地后端历史工厂边界：
  - `Brain/brain-modules/brain-editorial/src/main/java/com/gxpublish/brain/editorial/support/EditorialHistoryFactory.java`
  - `Brain/brain-modules/brain-editorial/src/test/java/com/gxpublish/brain/editorial/support/EditorialHistoryFactoryTest.java`
- 当前源码已将创建历史的角色显示名收敛为 `发起人`
- 当前源码与测试断言均锁定创建文案：
  - `发起人创建申请{申请标题}`

## frontend 修补

- 详情页历史展示已改为直接渲染最终业务文案：
  - `plus-ui-ts/src/views/editorial/review/detail.vue`
  - `plus-ui-ts/src/views/editorial/review/integration.ts`
  - `plus-ui-ts/src/views/editorial/review/integration.spec.ts`
- 页面不再使用 `operatorName + " · " + operateType` 的展示方式
- 历史卡片标题改为 `审批 / 修改历史`
- 详情侧栏继续保持无 `detail shell`、`approval shell`、`taskId` 等用户无意义调试字段

# 最小验证

## backend

- 子 agent 执行：
  - `mvn -pl brain-modules/brain-editorial "-Dtest=EditorialHistoryFactoryTest" test`
- 结果：
  - `Tests run: 4, Failures: 0, Errors: 0, Skipped: 0`
  - `BUILD SUCCESS`

## frontend

- 子 agent 执行：
  - `pnpm exec vitest run src/views/editorial/review/integration.spec.ts`
  - `pnpm exec eslint src/views/editorial/review/detail.vue src/views/editorial/review/integration.ts src/views/editorial/review/integration.spec.ts`
  - `pnpm build:prod`
- 结果：
  - `vitest`: `21 passed`
  - targeted `eslint`: pass
  - `build:prod`: pass

# 风险

1. 本轮只完成了首个 blocker 的代码修补和最小静态/定向验证，不等于 12 个问题全部闭环。
2. fresh `business_qa_lead` 尚未在本轮新代码上继续执行：
   - 问题 1 复测
   - 问题 `2/3/8/10/12` 继续覆盖
3. 当前工作树包含 editorial fulltest 主线的既有未提交改动，本次提交应视为 continuity checkpoint，不应对外表述为最终验收通过。

# 证据

- fresh blocker evidence：
  - `.agent-temp/editorial-fresh-acceptance_20260320-171631/history_ui_item01/result.json`
  - `.agent-temp/editorial-fresh-acceptance_20260320-171631/history_ui_item01/detail_history.png`
- 本轮 controller baseline：
  - `doc/agent/editorial-fulltest-exec_20260319-113303/handoffs/20260320_editorial-fulltest-exec_controller-resume-handoff_v01.md`
  - `doc/agent/editorial-fulltest-exec_20260319-113303/handoffs/20260320_editorial-fulltest-exec_fatjar-drift-handoff_v01.md`

# 建议下一步

1. 在其他机器拉取本次 checkpoint 后，先复核 runtime ready。
2. 新建 fresh `business_qa_lead`，先只复测问题 1。
3. 若问题 1 通过，再继续覆盖问题 `2/3/8/10/12`。
4. 非阻断问题继续执行并汇总；阻断问题再拆回 frontend/backend agent 修补。
