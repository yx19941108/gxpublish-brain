---
Owner: Codex
Task: editorial-fulltest-exec
Status: blocked
Updated: 2026-03-19 11:44:30 +08:00
---

# 20260319 editorial-fulltest-exec gate progress report v01

## 已确认

1. 当前分支仍为 `main`。
2. 运行面当前可达：
   - `GET http://127.0.0.1:9888/auth/code -> 200`
   - `GET http://127.0.0.1:9999/ -> 200`
   - `9888`、`9999` 都在监听
3. 数据库当前仍是 `gxpublish_brain`，且 `brain_editorial_review / brain_editorial_attachment / brain_editorial_link / brain_editorial_history` 存在。
4. 本会话 fresh browser probe 已在独立目录落盘：
   - `.agent-temp/editorial-fulltest-exec_20260319-113303/browser/gate_browser_probe_summary.json`
   - `.agent-temp/editorial-fulltest-exec_20260319-113303/browser/evidence/**`
5. 主测 5 类账号已在 fresh browser probe 中建立登录态并进入真实入口 `/workflow/editorial/review`：
   - `zhangsan`
   - `lisi`
   - `wangwu`
   - `zhoujiu`
   - `qianqi`
6. 补充账号探针结果：
   - `editorial_applicant`：live 登录返回“账号不存在”
   - `edi_applicant2`：live 登录返回“账号不存在”
   - `zhaoliu`：live 登录成功，且可进入 `/workflow/editorial/review`
7. backend/static 合同已确认以下高风险事实：
   - `brain_editorial_history` 当前不是完整审批留痕真值源
   - 中间审批通过不一定写 `brain_editorial_history`
   - 修改历史写入路径当前存在明显守卫错位，按现状大概率打不到
8. frontend/static + fresh browser 共同确认以下现状：
   - 三审三校列表页没有“审批”按钮
   - 当前节点责任人在列表页看到的是“修改、详情”，不是“修改、审批”
   - 非当前节点责任人看到的是“详情”

## 风险

1. 最高优先级阻断已出现：`HP-03`、`HP-04` 与用户要求不一致。
2. `editorial_applicant / edi_applicant2` 在数据库里存在，但 live 登录返回“不存在”，说明账号源与登录有效集不一致；若后续继续拿它们做主测，会制造假失败。
3. `brain_editorial_history` 当前无法单独承诺“每个审批节点审批/修改都留痕”；若继续跑 `HP-01`，高概率继续阻断。
4. 旧 browser harness 可复用逻辑，但旧脚本写死旧目录和旧路径，不能直接当本会话 fresh harness 原样复跑。
5. 当前工作树存在用户明确要求不要碰的未跟踪文件：
   - `doc/agent/reports/20260304_AI_Policy_Review_report_v02.md`
   - `test_qa_flow.py`
   - `doc/agent/editorial-fulltest-prep_20260319-111500/plans/~$20260319_editorial-fulltest-prep_plan_v02.xlsx`

## 待确认

1. `editorial_applicant / edi_applicant2` 是否本就废弃，只保留作历史命名，不再作为 live 测试账号。
2. `HP-01` 的最终验收真值源是否接受“业务历史 + workflow 历史双源联合判定”；若只认 `brain_editorial_history`，当前实现静态上已不满足。
3. 后续修复阶段里，二级审批主测是否固定用 `zhoujiu`，`zhaoliu` 仅作为补充样本。

## 推进方案

1. 当前轮立即停在阻断，不继续扩大到 `G-05`、`plan_v02` 全量矩阵或更低优先级用例。
2. 先把本轮 blocker 固化为实现需求：
   - 列表页补“审批”入口
   - 列表按钮矩阵按“当前节点责任人 / 非当前节点责任人”重做
   - 审批人修改后必须仍有提交能力
   - 历史留痕重构为满足节点审批/修改可追溯
3. 修复完成后，再开新一轮 fresh gate：
   - `G-00`
   - `G-05`
   - `HP-01 ~ HP-05`
4. `6 ~ 7` 的 UI/模块重构继续保留为下一阶段，但应在 `1 ~ 5` 收口后再扩大。

## Decision

1. 本会话不继续全量测试。
2. 停止原因不是 build/lint/服务不可用，而是已经拿到 fresh 业务阻断证据：
   - `HP-03`：三审三校列表页未提供审批按钮
   - `HP-04`：当前节点责任人的列表按钮矩阵仍为“修改、详情”，不是“修改、审批”
3. 按“最高优先级失败立即停”的纪律，当前结论应为：
   - `full-test not started to matrix`
   - `blocked by fresh HP evidence`

## Need From Backend

1. 明确 `HP-01` 的最终合同：是否接受 `brain_editorial_history + flow_his_task` 联合构成完整审批留痕。
2. 修正审批节点留痕，至少覆盖：
   - 节点审批通过
   - 节点退回
   - 节点终止
   - 节点修改
3. 排查 `editorial_applicant / edi_applicant2` 为何 `sys_user` 可见但 live 登录报“不存在”。

## Need From Frontend

1. 三审三校列表页补审批按钮，并让当前节点责任人看到“修改、审批”。
2. 非当前节点责任人只保留“详情”。
3. 审批人从修改页进入后，必须保留提交能力，不能出现等待中记录无提交按钮。
4. 后续 UI 重构要去掉内部语义泄露字段，如：
   - `审批壳上下文`
   - `taskId`
   - `detail/approval shell`

## Need From QA/Browser

1. 保留本轮 fresh 目录，后续继续沿同一 root 新增新版本证据，不回写旧 closure 目录。
2. 后续修复验证时，优先复跑：
   - `HP-03`
   - `HP-04`
   - `HP-02`
   - `HP-01`
3. 旧 `.agent-temp/editorial-mainline-closure_20260318-184705/**` 继续只做 baseline 参考。

## Artifacts

1. 计划：
   - `doc/agent/editorial-fulltest-exec_20260319-113303/plans/20260319_editorial-fulltest-exec_plan_v01.md`
2. Fresh browser gate probe：
   - `.agent-temp/editorial-fulltest-exec_20260319-113303/browser/gate_browser_probe.py`
   - `.agent-temp/editorial-fulltest-exec_20260319-113303/browser/gate_browser_probe_summary.json`
   - `.agent-temp/editorial-fulltest-exec_20260319-113303/browser/evidence/G03_APPLICANT/**`
   - `.agent-temp/editorial-fulltest-exec_20260319-113303/browser/evidence/G03_APPLICANT_CERT/**`
   - `.agent-temp/editorial-fulltest-exec_20260319-113303/browser/evidence/G03_APPROVER_L1/**`
   - `.agent-temp/editorial-fulltest-exec_20260319-113303/browser/evidence/G03_APPROVER_L2/**`
   - `.agent-temp/editorial-fulltest-exec_20260319-113303/browser/evidence/G03_APPROVER_L3/**`
   - `.agent-temp/editorial-fulltest-exec_20260319-113303/browser/evidence/SUP_APPLICANT_NO_ROLE/**`
   - `.agent-temp/editorial-fulltest-exec_20260319-113303/browser/evidence/SUP_APPLICANT_CERT_NO_ROLE/**`
   - `.agent-temp/editorial-fulltest-exec_20260319-113303/browser/evidence/SUP_APPROVER_L2_NICK_MISMATCH/**`

## Next Gate

1. 当前 gate 状态：
   - `G-01`: yes
   - `G-02`: yes
   - `G-03`: yes for primary accounts; supplemental anomaly captured
   - `G-04`: yes
   - `G-06`: yes
   - `G-00`: pending fresh runtime spot-check
   - `G-05`: not executed
2. 由于 `HP-03` / `HP-04` 已出现 fresh blocker，本轮不再继续推进 gate。
3. 下一 gate 不再是测试 gate，而是修复 gate：先修 `1 ~ 5`，再回到 fresh gate 复跑。
