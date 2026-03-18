Owner: Codex
Task: editorial-manuscript-next-session-multicli
Status: Ready For New Session Orchestration
Updated: 2026-03-18

# 审校模块下一会话多 CLI 编排 handoff

## 1. 当前阶段状态

当前只到：

1. `SQL_PREP_READY`
2. `JOINT_DEBUG_SURFACE_READY`

当前明确不到：

1. `SQL_APPLY_READY`
2. `BACKEND_SMOKE_READY`
3. `REAL_HTTP_READY`
4. `BROWSER_EVIDENCED`
5. `可提测`

## 2. 当前唯一基线

新会话必须先读，且以其为唯一当前基线：

1. `doc/agent/editorial-manuscript-rebuild_20260317-132339/decisions/ADR-0002_editorial-manuscript-rebuild-6gate-baseline.md`
2. `doc/agent/editorial-manuscript-rebuild_20260317-132339/plans/20260317_editorial-manuscript-rebuild-implementation_plan_v01.md`
3. `doc/agent/editorial-manuscript-rebuild_20260317-132339/handoffs/20260317_editorial-manuscript-dual-codex-shared_handoff_v01.md`
4. `doc/agent/editorial-manuscript-rebuild_20260317-132339/reports/20260318_editorial-manuscript-controller-sql-browser-audit_report_v02.md`
5. `doc/agent/editorial-manuscript-rebuild_20260317-132339/handoffs/20260318_editorial-manuscript-controller-sql-browser-prep_handoff_v02.md`

## 3. 当前已确认

1. backend 最小静态验证已 fresh 通过：
   - `Tests run: 9, Failures: 0, Errors: 0, Skipped: 0`
2. live `gxpublish_brain` 当前关键事实：
   - `brain_editorial_review = 66`
   - `editorial_review_flow = 2 active + 1 deleted`
   - `flow_instance_biz_ext(关联 editorial flow) = 43`
   - 旧菜单 `reviewEdit` 仍存在
3. SQL/Backend 线已经提交 `SQL_PREP_READY` 报告。
4. Browser/QA 线已经提交 `JOINT_DEBUG_SURFACE_READY` 审核稿与明细包。
5. Browser/QA 现有 evidence 目录、case id 规则、approval-shell deeplink 假设都已被主控批准用于下一阶段运行时取证。
6. `BR08_CANCEL_PENDING` 已被主控裁定为继续保留在当前目标范围内的 pending 项，不得自行关单。

## 4. 当前阻断点

### SQL/Backend

1. 还缺 tenant-aware `editorial_review_flow` full-rebuild SQL
2. 还缺独立 editorial `menu/permission` SQL
3. 还缺 runtime cleanup 最终范围确认
4. 还缺 apply 后可供 Browser/QA 使用的记录集与 `taskId/instanceId` 来源

### Browser/QA

1. 还缺真实登录账号包：`tenantId / username / password`
2. 集成树 `plus-ui-ts/node_modules` 还不存在
3. 还没有真实 HTTP / 浏览器证据
4. 还未收到 SQL apply 后的运行时入口数据

## 5. 新会话目标

新会话主控目标不是直接宣布完成，而是把多 CLI 协作变成文件协议驱动：

1. 主控 CLI 负责：
   - 读取基线
   - 派发工作
   - 轮询文件生成
   - 审核阶段性产物
   - 决定下一步
2. SQL/Backend CLI 负责：
   - 补齐 apply-ready 所需 SQL 缺口
   - 把阶段结果写入约定文件
3. Browser/QA CLI 负责：
   - 补齐 runtime-ready 所需账号/依赖/矩阵
   - 在获批后执行浏览器取证
   - 把阶段结果写入约定文件

## 6. 多 CLI 编排原则

1. 主控优先文件协议，不优先聊天转述。
2. 外部多个 Codex CLI 之间只通过文件交接，不通过你手工复制聊天内容。
3. 每个外部 CLI 可以作为自己的“子主控”继续编排内部 child agents，但必须满足：
   - 有清晰净收益
   - 子 agent 拥有明确边界
   - 不跨目录互改
   - 阶段结果必须回写到该 CLI 自己负责的文件
4. 主控下一会话应只轮询文件是否生成、内容是否达标，再安排下一步，不要求你在几个 CLI 手工搬运结果。
5. 仍需遵守全局 AGENTS 的 child-agent 机制：
   - 在实际 spawning 前，给一次简洁的理由和边界说明
   - 经你确认后再开

## 7. 建议的下一会话编排结构

### 主控 CLI

职责：

1. 只读当前基线与最新主控 `v02`
2. 派发两个外部 CLI
3. 轮询以下约定文件
4. 审核是否满足推进条件
5. 在阶段边界落新的主控 report/handoff

### SQL/Backend CLI

建议内部可拆 2 个子 agent，仍由 SQL/Backend CLI 自己主控：

1. `sql-flow-rebuild`
   - 只负责 tenant-aware full-rebuild SQL 草案与风险校核
2. `sql-menu-permission`
   - 只负责独立 menu/permission SQL 缺口与草案

SQL/Backend CLI 自己负责最终汇总，不把未审子结果直接当结论。

### Browser/QA CLI

建议内部可拆 2 个子 agent，仍由 Browser/QA CLI 自己主控：

1. `runtime-pack`
   - 只负责账号包、依赖前置、运行路径确认
2. `browser-evidence-plan`
   - 只负责 `BR01 -> BR10` 的执行矩阵、证据模板与目录校验

Browser/QA CLI 自己负责最终汇总，不把未审子结果直接当结论。

## 8. 约定文件协议

下一会话主控应要求各线只往这些路径写：

### SQL/Backend 线

1. 当前已存在输入：
   - `C:\kuguaHome\project\gxpublish-brain\.agent-temp\editorial-manuscript-rebuild\sql-backend\20260318_sql-backend_prep-ready_report_v01.md`
2. 下一阶段约定输出：
   - `C:\kuguaHome\project\gxpublish-brain\.agent-temp\editorial-manuscript-rebuild\sql-backend\20260318_sql-backend_apply-ready_report_v01.md`
   - `C:\kuguaHome\project\gxpublish-brain\.agent-temp\editorial-manuscript-rebuild\sql-backend\20260318_sql-backend_tenant-aware-flow-sql_v01.md`
   - `C:\kuguaHome\project\gxpublish-brain\.agent-temp\editorial-manuscript-rebuild\sql-backend\20260318_sql-backend_menu-permission-sql_v01.md`

### Browser/QA 线

1. 当前已存在输入：
   - `C:\kuguaHome\project\gxpublish-brain\.agent-temp\editorial-manuscript-rebuild\browser-qa\20260318_editorial-manuscript-browser-qa_controller-review-note_v01.md`
   - `C:\kuguaHome\project\gxpublish-brain\.agent-temp\editorial-manuscript-rebuild\browser-qa\20260318_editorial-manuscript-browser-qa_joint-debug-surface-ready_v01.md`
2. 下一阶段约定输出：
   - `C:\kuguaHome\project\gxpublish-brain\.agent-temp\editorial-manuscript-rebuild\browser-qa\20260318_editorial-manuscript-browser-qa_runtime-pack_v01.md`
   - `C:\kuguaHome\project\gxpublish-brain\.agent-temp\editorial-manuscript-rebuild\browser-qa\20260318_editorial-manuscript-browser-qa_runtime-evidence-index_v01.md`

### 主控线

1. 约定派发与轮询记录：
   - `C:\kuguaHome\project\gxpublish-brain\.agent-temp\editorial-manuscript-rebuild\controller\20260318_editorial-manuscript-next-session-prompt-pack_v01.md`
2. 下一阶段主控落盘：
   - `doc/agent/editorial-manuscript-rebuild_20260317-132339/reports/20260318_editorial-manuscript-controller-runtime-dispatch_report_v01.md`
   - `doc/agent/editorial-manuscript-rebuild_20260317-132339/handoffs/20260318_editorial-manuscript-controller-runtime-dispatch_handoff_v01.md`

## 9. 主控轮询规则

1. 主控每轮先看约定输出文件是否已生成。
2. 若未生成：
   - 不要催用户来回复制聊天内容
   - 直接继续等待或轮询
3. 若已生成：
   - 读取文件
   - 审核是否满足本轮出口条件
   - 只用文件内容做推进决策
4. 若某个文件生成但内容未达标：
   - 主控直接再派发下一轮修正要求
   - 继续等待下一版文件

## 10. 下一会话启动提示

下一会话直接从以下文件恢复：

1. 本 handoff
2. `.agent-temp/editorial-manuscript-rebuild/controller/20260318_editorial-manuscript-next-session-prompt-pack_v01.md`

