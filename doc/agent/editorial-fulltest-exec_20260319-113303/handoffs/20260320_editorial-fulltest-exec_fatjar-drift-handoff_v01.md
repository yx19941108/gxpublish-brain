Owner: Codex
Task: editorial-fulltest-exec
Status: Handoff
Updated: 2026-03-20 10:26:00 +08:00

# editorial fulltest fat-jar drift handoff v01

## 1. 本会话已确认

1. 前端 `HP-03/HP-02` 点击链的当前代码修补已落地：
   - `plus-ui-ts/src/views/editorial/review/index.vue`
   - `plus-ui-ts/src/views/editorial/review/integration.ts`
   - `plus-ui-ts/src/views/editorial/review/integration.spec.ts`
2. 前端定向静态验证已通过：
   - `pnpm exec vitest run src/views/editorial/review/integration.spec.ts` -> `16 passed`
   - `pnpm exec eslint src/views/editorial/review/index.vue src/views/editorial/review/integration.ts src/views/editorial/review/integration.spec.ts --ext .ts,.vue` -> clean
3. fresh runtime 证据已证明“旧 blocker 不是前端列表按钮缺失”，而是运行时产物漂移：
   - `HP-03` fresh click 证据显示，列表点击审批时真实命中 `GET /workflow/task/pageByTaskWait?...flowCode=editorial_review_flow`，最终跳转到 `/editorial/review/detail?...&taskId=...&instanceId=...`
   - `HP-02` fresh click 证据显示，列表点击修改后最终跳到 `/editorial/review/form?...&taskId=...&instanceId=...`，主按钮已变成 `保存并审批`，点击后命中 `PUT /editorial/review`，不再是旧的 `POST /editorial/review/submit + 当前操作没有权限`
4. fat jar 漂移根因已闭环：
   - `Brain/brain-modules/brain-editorial/target/brain-editorial-5.5.3.jar` 与 `brain-admin.jar` 内嵌 `BOOT-INF/lib/brain-editorial-5.5.3.jar` 最初 hash 不一致
   - 内嵌旧 jar 的 `EditorialReviewServiceImpl.class` 含旧文案：`当前流程状态为审批中或已结束，不允许修改申请内容`
   - 当前源码/模块 target class 不含该旧文案，只含当前源码权限提示
   - Maven 本地仓库路径已确认在 `C:\kuguaHome\tools\devTools\apache-maven-3.9.12\repository`
   - 已执行 `mvn -pl brain-modules/brain-editorial -am -DskipTests install`，把当前 editorial jar 写入本地 Maven 仓库
5. 新 backend 已重新打包并重启：
   - 新 jar：`Brain/brain-admin/target/brain-admin.jar`
   - jar 时间：`2026-03-20 10:23:58`
   - backend PID：`39236`
   - `9888` 当前已监听在 `PID=39236`

## 2. 当前停点

1. 尚未对“新 backend + 已修补前端”重新执行 fresh `HP-03/HP-02` 浏览器级复验。
2. 因此，当前不能宣称：
   - `HP-03` 已在新 backend 上通过
   - `HP-02` 已从旧 backend 业务报错转绿
3. 当前诚实结论是：
   - 代码级前端修补完成并静态验证通过
   - 旧 fat jar 漂移已定位并修正到重打包/重启阶段
   - 运行时闭环还差新 backend 下的 fresh browser 复验

## 3. 建议下一会话第一批动作

1. 先只做轻量 runtime ready 复核：
   - backend `PID 39236` 仍存活
   - `9888` 仍监听
   - `9999` frontend dev server 仍监听
   - 读取 `.agent-temp/.../backend_restart/backend.out.log` 和 `backend.err.log` 尾部确认无启动级致命错误
2. 在当前 regression 根下继续使用本会话新建的 runtime 目录与 probe：
   - runtime 目录：`C:\kuguaHome\project\gxpublish-brain\.agent-temp\editorial-fulltest-exec_20260319-113303\regression_20260319-122101\runtime_20260320-100557`
   - 列表动作 probe：`review_list_action_probe.py`
3. 固定样本先复跑两条，不扩矩阵：
   - 标题：`browser-submit-chain-20260319-095406`
   - `HP-03`: `wangwu + action=approve`
   - `HP-02`: `wangwu + action=modify --submit-after-nav`
4. 只要其中任一仍失败，继续按 stop rule 停在 blocker 分析，不继续 `HP-01/HP-05/NR-*`

## 4. 关键证据路径

1. 新 runtime click 证据：
   - `C:\kuguaHome\project\gxpublish-brain\.agent-temp\editorial-fulltest-exec_20260319-113303\regression_20260319-122101\runtime_20260320-100557\hp03_wangwu_click\wangwu_approve_summary.json`
   - `C:\kuguaHome\project\gxpublish-brain\.agent-temp\editorial-fulltest-exec_20260319-113303\regression_20260319-122101\runtime_20260320-100557\hp02_wangwu_click\wangwu_modify_summary.json`
2. fat jar 漂移取证：
   - `C:\kuguaHome\project\gxpublish-brain\.agent-temp\brain-editorial-from-fat.jar`
   - `C:\kuguaHome\project\gxpublish-brain\.agent-temp\brain-editorial-from-fat-clean.jar`
   - `C:\kuguaHome\project\gxpublish-brain\.agent-temp\editorial-class-from-fat.class`
   - `C:\kuguaHome\project\gxpublish-brain\.agent-temp\editorial-class-from-fat-clean.class`
3. backend restart 日志：
   - `C:\kuguaHome\project\gxpublish-brain\.agent-temp\editorial-fulltest-exec_20260319-113303\regression_20260319-122101\runtime_20260320-100557\backend_restart\backend.out.log`
   - `C:\kuguaHome\project\gxpublish-brain\.agent-temp\editorial-fulltest-exec_20260319-113303\regression_20260319-122101\runtime_20260320-100557\backend_restart\backend.err.log`

## 5. 工作树边界

1. 本会话明确不要碰未跟踪文件：`test_qa_flow.py`
2. 当前相关代码改动边界：
   - `plus-ui-ts/src/views/editorial/review/index.vue`
   - `plus-ui-ts/src/views/editorial/review/integration.ts`
   - `plus-ui-ts/src/views/editorial/review/integration.spec.ts`
3. 工作树里还有非本轮边界项，续会话不要误回退：
   - `Brain/brain-modules/brain-editorial/src/test/java/com/gxpublish/brain/editorial/service/impl/EditorialReviewServiceResubmitTest.java`
   - `plus-ui-ts/.eslintrc-auto-import.json`
   - `doc/agent/editorial-fulltest-prep_20260319-111500/plans/~$20260319_editorial-fulltest-prep_plan_v02.xlsx`

## 6. 新会话成功标准

1. 先在新 backend 上 fresh 复跑 `HP-03/HP-02`
2. 明确给出：
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
3. 若 `HP-02` 仍失败，必须基于新 backend fresh evidence 重新判定是否进入后端源码修补，而不是再引用旧 fat jar blocker
