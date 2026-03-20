Owner: Codex
Task: editorial-fulltest-exec
Status: blocked
Updated: 2026-03-20 09:13:30 +08:00

# 20260320 editorial-fulltest-exec runtime gate and HP-03 blocker report v01

## 已确认

1. 本轮先按 runtime prerequisite 恢复，再重新做 fresh gate，未复用旧 `browser/gate_browser_probe.py`。
2. Redis fresh 可达：
   - `127.0.0.1:6379 -> +PONG`
   - 监听进程来自 `wslrelay.exe` 与 `com.docker.backend.exe`
3. `G-01` fresh 通过：
   - `Brain\\brain-admin\\target\\brain-admin.jar` 在 JDK 17 下启动成功
   - `GET http://127.0.0.1:9888/auth/code -> 200`
   - fresh 日志目录：`.agent-temp/editorial-fulltest-exec_20260319-113303/regression_20260319-122101/runtime_20260320-090121/`
4. `G-02` fresh 通过：
   - `pnpm dev -- --host 127.0.0.1 --port 9999` ready
   - `GET http://127.0.0.1:9999/ -> 200`
5. `G-03(primary)` fresh 通过：
   - `zhangsan / lisi / wangwu / zhoujiu / qianqi` 均 `POST /auth/login -> 200`
   - 加上 `Authorization + clientid` 后，`GET /system/user/getInfo -> 200`
   - 角色映射与主测预期一致：
     - `zhangsan -> editorial_review_applicant`
     - `lisi -> editorial_review_applicant_has_certificate`
     - `wangwu -> editorial_first_level_approver`
     - `zhoujiu -> editorial_second_level_approver`
     - `qianqi -> editorial_third_level_approver`
6. `G-04` fresh 通过，但结论必须带限定：
   - MySQL MCP 默认连接库是 `approval_workflow`，不能直接作为业务库事实
   - 显式查询 `gxpublish_brain.*` 后，`brain_editorial_review / brain_editorial_attachment / brain_editorial_link / brain_editorial_history` 均存在
   - 5 个主测账号在 `gxpublish_brain.sys_user/sys_user_role/sys_role` 中的 tenant/role 映射与 API 返回一致
7. `G-06` fresh 通过：
   - 真实菜单路由 `GET /system/menu/getRouters` 返回 `Workflow -> path=/workflow -> child path=editorial/review`
   - 新 fresh browser probe 在 `zhangsan` 身上进入了 `http://127.0.0.1:9999/workflow/editorial/review`
   - 页面 title 正常、截图已落盘、无 page error
   - `networkidle` 10 秒未收敛，但页面已 load 且 DOM/screenshot 正常，判定为前端长连接特征，不单独视为 gate 失败
8. 进入固定顺序后的首个业务项 `HP-03` fresh 失败：
   - 使用一级审批人 `wangwu` 打开 `/workflow/editorial/review`
   - 列表中存在多条 `待一审` 且 `可编辑=是` 的记录
   - 但 fresh browser evidence 中操作列仍只出现 `详情`，没有 `审批`
   - 因此“当前节点责任人在三审三校列表页拿到审批入口”未满足

## 风险

1. 本轮 runtime blocker 已解除，但这不代表业务阻断自动消失；`HP-03` fresh 失败已经证实业务阻断仍在。
2. `G-06` 的 `networkidle` 超时如果被误写成页面失败，会污染结论；当前应以：
   - 最终 URL 命中
   - 页面主体渲染完成
   - screenshot/DOM 已落盘
   - 无 page error
   作为主入口 gate 通过证据。
3. 当前工作树仍有非本轮内容，不可误提交：
   - `plus-ui-ts/.eslintrc-auto-import.json`
   - `doc/agent/editorial-fulltest-prep_20260319-111500/plans/~$20260319_editorial-fulltest-prep_plan_v02.xlsx`
   - `test_qa_flow.py`

## 待确认

1. `HP-03` 的验收口径是否继续维持为：
   - 当前节点责任人必须在三审三校列表页直接看到并使用“审批”入口
   - 不能仅依赖“详情页内有 approval shell”
2. 后续修补若涉及列表按钮矩阵，是否仍按既定边界只改：
   - `plus-ui-ts/src/views/editorial/review/**`
   - `plus-ui-ts/src/api/editorial/review.ts`
   - `plus-ui-ts/src/components/Process/approvalButton.vue`

## 推进方案

1. 本轮到 `HP-03` 即止，不继续 `HP-04 -> HP-05 -> HP-02 -> HP-01 -> NR-*`。
2. 以本轮 fresh 证据为输入，回到实现修补阶段：
   - Frontend：补列表直接审批入口与按钮矩阵
   - Backend：如列表合同/任务映射不足，再按边界补合同
   - QA/Browser：修补后重新 fresh 从 `HP-03` 起步
3. 修补完成后，新一轮顺序仍保持不变：
   - `HP-03`
   - `HP-04`
   - `HP-05`
   - `HP-02`
   - `HP-01`
   - `NR-01 ~ NR-05`

## Decision

1. 本轮结论不是 `runtime blocked`，而是：
   - `runtime gates recovered`
   - `blocked by fresh HP-03 evidence`
2. 按 stop rule，本轮执行到 `HP-03` 即停止，不恢复旧矩阵，不继续后续 `HP/NR`。

## Need From Backend

1. 复核列表页数据合同是否给足“当前节点责任人可直接审批”的判定信息。
2. 若前端需要基于任务上下文展示“审批”按钮，确认列表接口是否稳定返回所需 `taskId / approvalContext / editable-state`。

## Need From Frontend

1. 三审三校列表页需要让当前节点责任人对可审批记录看到“审批”入口，而不是仅有“详情”。
2. 列表按钮矩阵应与角色/节点责任一致；当前 `wangwu` 在 `待一审 + 可编辑=是` 记录上只有“详情”，与目标不符。

## Need From QA/Browser

1. 保留本轮 fresh root 与新探针：
   - `.agent-temp/editorial-fulltest-exec_20260319-113303/regression_20260319-122101/runtime_20260320-090121/fresh_browser_probe.py`
2. 后续修补复测时，仍从同一 regression root 下新建 fresh 子目录，不回写旧 evidence。

## Artifacts

1. runtime fresh 目录：
   - `.agent-temp/editorial-fulltest-exec_20260319-113303/regression_20260319-122101/runtime_20260320-090121/`
2. `G-01`：
   - `backend.stdout.log`
   - `backend.stderr.log`
   - `g01_auth_code.txt`
3. `G-02`：
   - `frontend.stdout.log`
   - `frontend.stderr.log`
   - `g02_front_root.txt`
4. `G-03(primary)`：
   - `g03_primary_login.json`
5. `G-06`：
   - `g06_routers_zhangsan.json`
   - `g06_zhangsan/zhangsan_summary.json`
   - `g06_zhangsan/zhangsan_page.png`
6. `HP-03`：
   - `hp03_wangwu/wangwu_summary.json`
   - `hp03_wangwu/wangwu_page.png`

## Next Gate

1. 本轮不再有下一个测试 gate。
2. 下一 gate 是修补 gate：
   - 先修 `HP-03`
   - 修完后 fresh 重跑 `HP-03`
   - 仅当 `HP-03` 通过，再推进 `HP-04`
