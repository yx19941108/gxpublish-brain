Owner: Codex
Task: editorial-manuscript-rebuild
Status: Draft For Milestone Review
Updated: 2026-03-17

# 审校模块替换式重构执行报告 v1

## 1. 目标

本报告用于在进入“多 agent + 双 worktree 并行开发”前，先把当前全量重构方案的执行边界、里程碑评审门禁、并行开发边界和“可提测”判定口径固定下来。

本报告只整理执行方案，不代表已经批准启动 child agents 或进入编码。

## 2. 已确认事实

### 2.1 当前方案基线

1. 当前设计基线来自以下任务包文件：
   - `reports/20260317_editorial-manuscript-rebuild-mapping_report_v01.md`
   - `plans/20260317_editorial-manuscript-rebuild-implementation_plan_v01.md`
   - `handoffs/20260317_editorial-manuscript-rebuild-design_handoff_v01.md`
2. 当前重构方向已锁定为：
   - `gxpublish-brain` 为真实落地仓库
   - `pub-nexus` 只作为参考规范源
   - 不允许直接复制 `pub-nexus` 代码
   - 允许不兼容旧流程实例的替换式重构

### 2.2 当前仓库真实冲突

1. flow 与状态链冲突已被代码直接证实：
   - `editorial_review_flow.json` 当前只有 `dept-audit-node`、`final-audit-node` 两级审批节点
   - `formPath` 仍指向 `/editorial/review/reviewEdit`
   - `ReviewStatusEnum` 当前仍按 `WAITING_FIRST(10) -> WAITING_SECOND(20) -> WAITING_FINAL(30) -> APPROVED(40)` 设计
2. SQL 基线与运行代码冲突已被代码直接证实：
   - 模块内置 `editorial_review.sql` 的 `brain_editorial_review` 建表语句当前没有 `review_status`
   - 同一建表语句当前也没有 `process_type`
   - 但 `EditorialReviewMapper.xml`、`EditorialReviewBo.java`、`EditorialReviewVo.java`、前端 `review.ts` 已在使用这两个字段
3. 后端契约仍是粗粒度单组接口：
   - `/list`
   - `/{id}`
   - `POST /editorial/review`
   - `PUT /editorial/review`
   - `/submit`
   - `/history/{id}`
4. 前端仍以单体页承载主业务：
   - 列表页 `index.vue` 的新增、编辑、查看都仍跳转到 `/editorial/review/reviewEdit`
   - `reviewEdit.vue` 同时承载业务表单、提交、审批、历史记录和流程组件接线

### 2.3 历史链路对本轮的约束

1. `20260302_editorial-fulltest_report_v01.md` 已证实旧链路曾在真实联调下暴露两类阻断问题：
   - 中间审批阶段 `review_status` 推进异常
   - TUS `finish` 链路异常
2. `ADR-0001_editorial-fulltest-hotfix.md` 只解决既有链路的阻断项，不构成本轮“替换式重构已完成”的依据。
3. 当前 `EditorialReviewServiceImpl` 已存在 `ProcessTaskEvent` 监听和 `resolveWaitingReviewStatus(...)`，但其节点识别仍建立在旧 flow / 旧 nodeCode 语义之上，因此不能把它直接当成本轮新基线。

### 2.4 当前工作区状态

1. 仓库治理前置已满足：
   - `.agent-temp/` 已在 `.gitignore`
   - `doc/agent/README.md` 已存在
2. 当前主工作区不是干净基线，存在未跟踪噪声：
   - `.agents/`
   - `.claude/`
   - `Brain/data/`
   - 当前任务包目录本身
   - 若后续进入 worktree / baseline 对比，必须区分“既有噪声”和“本轮新增变化”

## 3. 全量重构执行方案 v1

### 3.1 Gate A: 里程碑评审先于并行开发

在进入 child-agent、多 worktree 和并行编码前，必须先完成一次“全量重构方案里程碑评审”。

本阶段的目标不是写代码，而是拍板以下事项：

1. 新 flow 节点数与节点语义
2. `ReviewStatusEnum` 与 nodeCode 的最终映射
3. SQL 收口策略：
   - 以 `Brain/script/sql/update` 为正式事实源
   - 模块内置 `editorial_review.sql` 只在增量脚本稳定后回刷样例
4. 前端新 IA：
   - `list + create/edit + detail/approval shell`
5. 并行开发边界和交接点

### 3.2 Gate B: architect / project_reviewer 仅做评审

若用户确认启用 child agents，则本轮建议只启用：

1. `architect`
2. `project_reviewer`

角色边界必须固定为：

1. `architect`
   - 只审当前 mapping report、implementation plan、execution report
   - 判断方案是否足以进入 worktree 并行开发
   - 明确必须先收口的冲突与禁止越界项
2. `project_reviewer`
   - 独立挑战方案的可执行性、风险隔离和验证口径
   - 审核 owned boundary、交接点和“可提测”判定是否足够严格

限制：

1. 两个 agent 都不直接写代码
2. 两个 agent 都不直接创建 worktree
3. 若两者结论冲突，必须先回到主控汇总冲突点、推荐方案、风险与取舍，再等待用户拍板

### 3.3 Gate C: 只有拍板完成后才能进入 worktree 准备

只有在以下条件同时满足后，才允许进入“双 worktree + 并行开发”阶段：

1. 用户确认启用评审 agents
2. `architect` 与 `project_reviewer` 已完成里程碑评审
3. 若存在冲突，冲突点已由用户拍板
4. 至少有一份 review 或 decision 文档已落盘

进入 worktree 前还必须做治理检查：

1. `.worktrees/` 是否存在
2. `.worktrees/` 是否被 git ignore
3. 当前主工作区噪声清单是否已记录
4. backend 与 frontend worktree 的 baseline 是否分别验证

### 3.4 Gate D: 并行开发 owned boundary

若进入开发，推荐的 owned boundary 如下：

1. backend worktree
   - 范围：`Brain/**`
   - 优先角色：`senior_developer`
   - 责任：
     - flow / enum / SQL 基线收口
     - controller / service / mapper / VO-BO 契约拆分
     - workflow 事件与状态机映射
     - 定向 compile / test / SQL consistency checks
2. frontend worktree
   - 范围：`plus-ui-ts/**`
   - 优先角色：`frontend_developer`
   - 必要时可改用 `senior_developer`
   - 责任：
     - `index + create/edit + detail/approval shell`
     - `integration.ts` / `model.ts`
     - 共享流程组件 hook 接入
     - 定向 `eslint` / `build` / 页面交互验证
3. 主控代理
   - 范围：`doc/agent/editorial-manuscript-rebuild_20260317-132339/**`
   - 责任：
     - 任务包持续更新
     - 交接点定义
     - 集成决策
     - 最终分支合并
     - 最终验证与结论

共享边界原则：

1. 尽量避免两个 worker 同改同一文件
2. 若存在共享接口文件，由主控先定义改动顺序和交接点
3. worker 不得越界改动对方拥有的目录或文件

### 3.5 Gate E: 合并前后验证口径

各 worktree 完成后，必须先各自完成定向验证：

1. backend worktree
   - compile
   - targeted tests
   - SQL consistency checks
2. frontend worktree
   - eslint
   - build
   - targeted interaction checks

两侧都过关后，再由主控完成合并并执行集成验证。

集成验证至少拆为四层：

1. 静态验证
2. backend ready / 真实 HTTP
3. frontend 登录态与关键页面
4. 浏览器级关键链路

只有定向验证与集成验证都拿到证据，才可以表述为“可提测”。

## 4. 当前不建议直接进入并行开发的原因

1. flow 节点语义、状态链和 `formPath` 还没有统一新基线
2. SQL 事实源仍然漂移，若直接并行编码会放大前后端契约错位
3. 当前前端目标页形态尚未从 `reviewEdit.vue` 彻底抽离
4. 主工作区已有未跟踪噪声，未先建立 baseline 会影响后续 worktree 差异判断

## 5. 当前推荐下一步

1. 先由主控给出 child-agent 申请清单，并等待用户确认
2. 用户确认后，再启用 `architect` 与 `project_reviewer` 做里程碑评审
3. 评审结论落盘为 review 或 decision
4. 用户确认继续开发后，再进入 `.worktrees/` 治理检查与双 worktree baseline 验证

## 6. 一句话结论

当前最合理的推进方式，不是立即进入“多 agent + 双 worktree 编码”，而是：先用本报告把执行边界固定，再由 `architect` 与 `project_reviewer` 对当前全量重构方案做里程碑拍板；只有拍板完成且用户确认继续开发后，才进入并行开发与最终“可提测”验证闭环。
