Owner: Codex
Task: editorial-fulltest-exec
Status: handoff
Updated: 2026-03-20 09:13:30 +08:00

# 20260320 editorial-fulltest-exec HP-03 blocker handoff v02

## Resume Entry

新会话先读以下工件：

1. `doc/agent/decisions/ADR-0001_editorial-fulltest-hotfix.md`
2. `doc/agent/editorial-fulltest-exec_20260319-113303/plans/20260319_editorial-fulltest-exec_plan_v01.md`
3. `doc/agent/editorial-fulltest-exec_20260319-113303/reports/20260319_editorial-fulltest-exec_gate-progress_report_v01.md`
4. `doc/agent/editorial-fulltest-exec_20260319-113303/reports/20260320_editorial-fulltest-exec_static-gate-runtime-blocker_report_v01.md`
5. `doc/agent/editorial-fulltest-exec_20260319-113303/reports/20260320_editorial-fulltest-exec_runtime-gate-hp03-blocker_report_v01.md`
6. `doc/agent/editorial-fulltest-exec_20260319-113303/handoffs/20260320_editorial-fulltest-exec_hp03-blocker_handoff_v02.md`
7. `.agent-temp/editorial-fulltest-exec_20260319-113303/LATEST_REGRESSION_DIR.txt`

## Current State

1. `main` 仍是当前接管基线。
2. 本轮 runtime prerequisite 已恢复，不再被 Redis 阻断。
3. fresh gate 结果：
   - `G-01`: pass
   - `G-02`: pass
   - `G-03(primary)`: pass
   - `G-04`: pass after explicit `gxpublish_brain.*` query
   - `G-06`: pass
4. 本轮按固定顺序进入业务项后，第一项 `HP-03` fresh 失败并已停止：
   - 账号：`wangwu`
   - 页面：`/workflow/editorial/review`
   - 现象：存在 `待一审 + 可编辑=是` 的记录，但操作列只有“详情”，没有“审批”

## Locked Facts

1. Redis 已在本机 `127.0.0.1:6379` 响应 `+PONG`。
2. backend jar 可正常启动到 `9888` 监听。
3. frontend dev server 可正常启动到 `9999` 监听。
4. 登录态建立的关键合同：
   - `POST /auth/login` body 需要 `tenantId=099142`、`clientId=e5cd7e4891bf95d1d19206ce24a7b32e`
   - `PasswordAuthStrategy` 当前已注释掉验证码校验，因此图形码不是登录硬阻断
   - 后续受保护接口必须同时带：
     - `Authorization: Bearer <token>`
     - `clientid: e5cd7e4891bf95d1d19206ce24a7b32e`
5. `G-06` 的主入口口径已由运行时菜单确认：
   - 父级 `/workflow`
   - 子级 `editorial/review`
   - 浏览器最终 URL：`/workflow/editorial/review`

## Fresh Evidence Root

本轮 fresh runtime 目录：

`C:\kuguaHome\project\gxpublish-brain\.agent-temp\editorial-fulltest-exec_20260319-113303\regression_20260319-122101\runtime_20260320-090121\`

关键证据：

1. `g03_primary_login.json`
2. `g06_routers_zhangsan.json`
3. `g06_zhangsan/zhangsan_summary.json`
4. `g06_zhangsan/zhangsan_page.png`
5. `hp03_wangwu/wangwu_summary.json`
6. `hp03_wangwu/wangwu_page.png`
7. `fresh_browser_probe.py`

## Working Tree Notes

1. 不要碰未跟踪文件：`test_qa_flow.py`
2. 当前还有非本轮脏内容，不可误提交：
   - `plus-ui-ts/.eslintrc-auto-import.json`
   - `doc/agent/editorial-fulltest-prep_20260319-111500/plans/~$20260319_editorial-fulltest-prep_plan_v02.xlsx`
3. `.agent-temp/**` 仅作运行证据，不进入提交。

## Next Session Goal

下会话首要目标不是继续 `HP-04`，而是先修 `HP-03`：

1. 让当前节点责任人在三审三校列表页对可审批记录看到“审批”入口
2. 修补后按固定顺序重新 fresh：
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
3. 任一 stop rule 命中立即停

## Collaboration Boundary

1. 若需要数据库改动：
   - 只修改仓库 SQL 文件
   - 把准确路径发给用户手工执行
2. 若需要代码修补：
   - Backend 仅在 `Brain/brain-modules/brain-editorial/**`
   - Frontend 仅在 `plus-ui-ts/src/views/editorial/review/**`、`plus-ui-ts/src/api/editorial/review.ts`、`plus-ui-ts/src/components/Process/approvalButton.vue`
3. QA/Browser 继续用 fresh evidence discipline：
   - 新建新子目录
   - 不复用旧 probe
   - 不把旧 evidence 写成本轮 fresh 结论
