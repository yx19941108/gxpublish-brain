---
Owner: Codex
Task: editorial-fulltest-exec
Status: handoff
Updated: 2026-03-19 11:49:30 +08:00
---

# 20260319 editorial-fulltest-exec handoff v01

## Resume Entry

新会话只先读以下工件：

1. `doc/agent/editorial-fulltest-exec_20260319-113303/plans/20260319_editorial-fulltest-exec_plan_v01.md`
2. `doc/agent/editorial-fulltest-exec_20260319-113303/reports/20260319_editorial-fulltest-exec_gate-progress_report_v01.md`
3. `doc/agent/editorial-fulltest-prep_20260319-111500/plans/20260319_editorial-fulltest-prep_plan_v02.md`
4. `doc/agent/editorial-mainline-closure_20260318-184705/reports/20260319_editorial-mainline-closure_runtime-progress_report_v03.md`
5. `doc/agent/editorial-mainline-closure_20260318-184705/handoffs/20260319_editorial-mainline-closure_runtime-progress_handoff_v03.md`
6. `.agent-temp/editorial-fulltest-exec_20260319-113303/browser/gate_browser_probe_summary.json`

## Current Gate State

1. `G-01`: yes
2. `G-02`: yes
3. `G-03`: yes for primary accounts
4. `G-04`: yes
5. `G-06`: yes
6. `G-00`: pending fresh runtime spot-check
7. `G-05`: not executed

说明：当前会话已经因为 fresh 高优先级 blocker 停止，没有进入 `plan_v02` 全量矩阵。

## Fresh Blockers Already Confirmed

### HP-03

三审三校列表页没有“审批”按钮。

### HP-04

当前节点责任人在列表页看到的是“修改、详情”，不是“修改、审批”。

### HP-01 高风险预警

当前实现下：

1. `brain_editorial_history` 不是完整审批留痕真值源
2. 中间审批通过不一定写 `brain_editorial_history`
3. 修改写历史的路径当前存在守卫错位，按现状大概率打不到

## Locked Live Accounts

下会话主测账号固定用以下 5 类：

| 角色 | 主测账号 | 备用账号 | 备注 |
|---|---|---|---|
| 发起人（无证） | `zhangsan` | 无 | 已 fresh 登录成功 |
| 发起人（有证） | `lisi` | 无 | 已 fresh 登录成功 |
| 一级审批 | `wangwu` | `sunba` | `wangwu` 已 fresh 登录成功 |
| 二级审批 | `zhoujiu` | `zhaoliu` | `zhoujiu` 昵称与角色一致；`zhaoliu` 可作补充样本 |
| 三级审批 | `qianqi` | `wushi` | `qianqi` 已 fresh 登录成功 |

以下账号不再作为下会话主测账号：

1. `editorial_applicant`
2. `edi_applicant2`

原因：本会话 fresh 登录返回“账号不存在”。

## Next Session Goal

下会话不再继续 full-test，而是进入“修复 1~5 -> fresh 回归”的实现阶段。

### 本次必须修

1. 列表页审批入口与按钮矩阵
2. 审批人修改后仍可提交
3. 审批/修改历史留痕
4. 新建提交校验与多附件/多链接
5. 修改口径附件追加与历史展示

### 本次一起纳入的新增需求

1. 隐藏“暂存”按钮，但保留代码逻辑，不删暂存实现
2. 发起人新增审批时，附件和关联链接二者必填其一；附件支持多文件，链接支持多条；并补充测试用例
3. 审批人修改时，上传附件为追加，不允许覆盖已提交流程附件
4. 修改口径必须在历史记录中留痕：
   - 修改人
   - 修改时间
   - 修改前值
   - 修改后值
   - 若有新增附件，显示文件名并支持下载
5. 文件预览：
   - 本次先支持 `pdf`、图片预览
   - 视频支持页面内播放插件在线观看
6. 以下需求本次不做，但必须记入待办，与既有次优先级一起保留：
   - Office 文件预览
   - 视频时间节点与附件联动跳转
   - 上一轮次优先级：页面重构、按 `nexus-platform` 方向重构模块

## Child-Agent Orchestration

下会话允许直接开执行型 child agent，不需要重新确认，但保持最多 3 个在线。

建议固定为 3 线：

### Line A / Backend

职责：

1. 历史留痕重构
2. 修改/审批合同修复
3. 多附件追加与下载/预览 contract
4. 新建提交校验补齐

边界：

1. `Brain/brain-modules/brain-editorial/**`
2. 必要时 `Brain/brain-modules/brain-workflow/**`
3. 必要 SQL 脚本放 `Brain/script/sql/update/**`

### Line B / Frontend

职责：

1. 列表页按钮矩阵重做
2. 审批入口进入 detail approval shell
3. form/detail/history 交互修复
4. 暂存按钮隐藏但逻辑保留
5. 文件预览与视频播放基础支持

边界：

1. `plus-ui-ts/src/views/editorial/review/**`
2. `plus-ui-ts/src/api/editorial/**`
3. 必要时 `plus-ui-ts/src/components/Process/**`

### Line C / QA/Regression

职责：

1. 维护 fresh evidence 根目录，不回写旧目录
2. 修复后先回归：
   - `HP-03`
   - `HP-04`
   - `HP-02`
   - `HP-01`
   - 新增需求 1~5 对应用例
3. 仅在高优先级全绿后，再考虑恢复 `G-05` 和 `plan_v02` 其余矩阵

边界：

1. `.agent-temp/editorial-fulltest-exec_20260319-113303/**`
2. `doc/agent/editorial-fulltest-exec_20260319-113303/**`

## Stop Rules

下会话继续沿用以下纪律：

1. 不把 build/lint/minimal HTTP 混写成测试通过
2. 不拿旧截图或旧 JSON 充当 fresh evidence
3. 不在账号、样本、证据路径未绑定前扩大执行
4. 任一最高优先级用例失败，立即停在阻断结论

## Paste-Ready Prompt

下方内容可直接作为新会话提示词：

```text
要求你接管 `C:\kuguaHome\project\gxpublish-brain` 的 `main` 分支，继续 editorial 主线工作，但本会话不再继续 full-test 扩矩阵，而是先修复高优先级 blocker，再做 fresh 回归。

先读且只读以下工件：
1. `C:\kuguaHome\project\gxpublish-brain\doc\agent\editorial-fulltest-exec_20260319-113303\plans\20260319_editorial-fulltest-exec_plan_v01.md`
2. `C:\kuguaHome\project\gxpublish-brain\doc\agent\editorial-fulltest-exec_20260319-113303\reports\20260319_editorial-fulltest-exec_gate-progress_report_v01.md`
3. `C:\kuguaHome\project\gxpublish-brain\doc\agent\editorial-fulltest-exec_20260319-113303\handoffs\20260319_editorial-fulltest-exec_handoff_v01.md`
4. `C:\kuguaHome\project\gxpublish-brain\doc\agent\editorial-fulltest-prep_20260319-111500\plans\20260319_editorial-fulltest-prep_plan_v02.md`
5. `C:\kuguaHome\project\gxpublish-brain\doc\agent\editorial-mainline-closure_20260318-184705\reports\20260319_editorial-mainline-closure_runtime-progress_report_v03.md`
6. `C:\kuguaHome\project\gxpublish-brain\doc\agent\editorial-mainline-closure_20260318-184705\handoffs\20260319_editorial-mainline-closure_runtime-progress_handoff_v03.md`
7. `C:\kuguaHome\project\gxpublish-brain\.agent-temp\editorial-fulltest-exec_20260319-113303\browser\gate_browser_probe_summary.json`

当前已确认：
- `G-01=yes`
- `G-02=yes`
- `G-03=yes`（主测 5 类账号）
- `G-04=yes`
- `G-06=yes`
- `HP-03=fail`
- `HP-04=fail`
- `editorial_applicant`、`edi_applicant2` live 登录返回“账号不存在”，不要再拿它们当主测账号

主测账号固定使用：
- 发起人（无证）：`zhangsan`
- 发起人（有证）：`lisi`
- 一级审批：`wangwu`（备用 `sunba`）
- 二级审批：`zhoujiu`（备用 `zhaoliu`）
- 三级审批：`qianqi`（备用 `wushi`）
- 密码统一：`123456`
- 租户：`099142`

本会话目标：
1. 编排执行型 child agent 并行修复高优先级 1~5
2. 修复完成后，先做 fresh 回归，不要直接扩大到全量矩阵
3. 只有高优先级和新增本次范围全绿后，才决定是否恢复 `G-05` 和 `plan_v02` 其余用例

高优先级修复范围：
1. 每个审批节点审批人的操作（审批、修改）都必须有历史记录留痕
2. 审批人在其负责节点应当能修改并提交，不能出现“修改页没有提交按钮”
3. 审批人不应只在待办页才能审批，三审三校列表页也必须提供审批按钮，并跳到详情审批壳显示审批提交按钮
4. 列表按钮矩阵必须改成：
   - 当前节点责任人：`修改、审批`
   - 非当前节点责任人：`详情`
5. 三审三校列表页可见性必须与“我发起的、我的待办、我的已办”口径一致

本次新增需求，也纳入实现与回归：
1. 表单当前不再显示“暂存”按钮，但代码逻辑保留，不删暂存实现
2. 发起人新增审批时，附件和关联链接二者必填其一；附件支持多个，链接支持多个；把这条加入测试用例
3. 审批人修改时，附件上传为追加，不允许覆盖既有附件
4. 修改口径必须在历史记录中展示：修改人、修改时间、修改前值、修改后值；若新增附件，展示文件名且支持下载
5. 文件预览本次先支持：pdf、图片；视频支持页面内播放
6. 以下需求本次不做，但必须记录到待办并和既有次优先级一起保留：
   - Office 文件预览
   - 视频时间节点与附件联动跳转
   - 页面重构参考 `pub-nexus` 截图
   - 按 `nexus-platform` 方向重构审校模块

执行约束：
- 允许直接开 child agent，不要再反复确认，但同时在线最多 3 个
- 建议拆成 Backend / Frontend / QA-Regression 三线，边界清晰，避免互相改同一文件
- 在任何代码修改前，先给 `Goal / Affected Files / Risk / Verification / Rollback`
- 不要把 build/lint/minimal HTTP 混写成测试通过
- 不要使用旧 evidence 充当 fresh evidence
- fresh evidence 继续写到 `C:\kuguaHome\project\gxpublish-brain\.agent-temp\editorial-fulltest-exec_20260319-113303\` 下的新子目录
- 任一最高优先级回归失败，立即停在阻断结论，不继续扩散

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
