Owner: Codex Controller
Task: editorial-fulltest-exec
Status: Handoff
Updated: 2026-03-20 17:57 +08:00

# 目的

本 handoff 用于把当前 `main` 工作树的 editorial fulltest checkpoint 迁移到其他机器继续开发/验收。

# 当前 checkpoint 结论

## 已确认

- runtime ready 在当前机器已复核通过：
  - backend PID `55324`
  - `9888` listen + `GET /auth/code = 200`
  - frontend `9999` listen + `GET / = 200`
- backend 创建历史文案已按要求收敛：
  - 目标：`发起人创建申请{申请标题}`
- frontend 详情页历史渲染已按要求收敛：
  - 直接显示最终业务文案
  - 不再 prepend 操作人姓名
  - 不再暴露无意义调试字段
- 最小验证已完成：
  - backend targeted test pass
  - frontend vitest pass
  - frontend targeted eslint pass
  - frontend build:prod pass

## 未完成

- fresh QA 还没有在本轮新代码上继续跑完：
  - 问题 1 复测
  - 问题 `2/3/8/10/12`
- 因此本次 push 是 continuity checkpoint，不是最终验收点

# 其他机器续跑提示词

```text
接管 `C:\kuguaHome\project\gxpublish-brain` 的 `main` 分支当前工作树，继续 editorial fulltest 主线。你在本会话中只作为 controller，负责调度子 agent、收集结果、控制上下文，不直接做代码实现；代码修改和测试执行都交给子 agent。

先读且优先只读以下工件，建立基线，不要重做 packaging/fatjar 漂移分析：
1. `doc/agent/editorial-fulltest-exec_20260319-113303/handoffs/20260320_editorial-fulltest-exec_controller-resume-handoff_v01.md`
2. `doc/agent/editorial-fulltest-exec_20260319-113303/handoffs/20260320_editorial-fulltest-exec_fatjar-drift-handoff_v01.md`
3. `doc/agent/decisions/ADR-0001_editorial-fulltest-hotfix.md`
4. `doc/agent/editorial-fulltest-exec_20260319-113303/plans/20260319_editorial-fulltest-exec_plan_v01.md`
5. `doc/agent/editorial-fulltest-exec_20260319-113303/reports/20260320_editorial-fulltest-exec_static-gate-runtime-blocker_report_v01.md`
6. `doc/agent/editorial-fulltest-exec_20260319-113303/reports/20260320_editorial-fulltest-exec_history-copy-hotfix_report_v01.md`
7. `doc/agent/editorial-fulltest-exec_20260319-113303/handoffs/20260320_editorial-fulltest-exec_other-machine-dev-handoff_v01.md`

当前已知基线：
- 前端与后端围绕历史文案 blocker 的本轮修补已落地到主工作树
- backend 创建历史文案目标已改为精确 `发起人创建申请{申请标题}`
- frontend 详情页历史渲染已改为直接展示最终业务文案，不再 prepend 操作人姓名
- backend targeted test 已通过：
  - `mvn -pl brain-modules/brain-editorial "-Dtest=EditorialHistoryFactoryTest" test`
- frontend 最小验证已通过：
  - `pnpm exec vitest run src/views/editorial/review/integration.spec.ts` -> `21 passed`
  - targeted eslint -> pass
  - `pnpm build:prod` -> pass
- 这不是 12 个问题全部闭环通过的最终验收点；fresh QA 还需要继续

工作方式和约束继续沿用：
1. 你只做调度，不直接改代码。
2. 用 `business_qa_lead` 做端到端测试。
3. 如果 QA 发现前端问题，唤醒或新建 `frontend_developer` 修复。
4. 如果 QA 发现后端问题，唤醒或新建 `senior_developer` 修复。
5. 修复完成后，再唤醒或新建 `business_qa_lead` 继续测试。
6. 如果某个子 agent 上下文明显变长、历史轮次过多、输出开始重复，直接关闭并新建 fresh agent，不要硬续。
7. 不要碰未跟踪文件 `test_qa_flow.py`。
8. 不要回退工作树中与本轮无关的现有改动。
9. 页面上不要再出现任何对用户无意义的调试内容，例如 `detail shell`、`approval shell`、`taskId` 等。

新的 QA 执行规则：
- 只有在以下情况才立即 stop 并回报 controller：
  - 阻断主流程，后续无法继续
  - 可能导致数据污染、误判、级联故障或其他严重风险
  - 环境不可用，无法继续产出有效证据
- 如果是非阻断 bug：
  - QA 不要停止
  - 继续跑后续用例
  - 把问题按严重级别、复现步骤、证据路径汇总回报给 controller
  - 由 controller 统一拆给前后端子 agent 修复

下一轮首要目标：
1. 先复核 runtime ready：
  - backend PID 是否仍存活
  - `9888` 是否仍监听并可用
  - frontend `9999` 是否仍监听并可用
2. 然后新建 fresh `business_qa_lead`：
  - 先只复测问题 1
  - 通过后继续覆盖问题 `2/3/8/10/12`
  - 非阻断问题继续执行，不必立即停
3. 若 QA 继续发现问题，继续按 controller 模式拆给 frontend/backend agent 修补

输出结构继续使用：
- 已确认
- 风险
- 待确认
- 推进方案
- Decision
- Need From Backend
- Need From Frontend
- Need From QA/Browser
- Artifacts
- Next Gate
```

# 建议的首轮动作

1. 拉取本次 checkpoint 后先做 runtime ready 复核。
2. 不要重复做 fatjar 漂移分析。
3. 直接进入 fresh QA 问题 1 复测，再串到问题 `2/3/8/10/12`。
