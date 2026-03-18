Owner: Codex
Task: editorial-manuscript-rebuild
Status: Ready For Parallel Coding After Worktree Setup
Updated: 2026-03-17

# 审校模块双 Codex 并行共享 handoff

## 1. 并行是否成立

结论：**成立，且优于主控本地单线程继续推进。**

原因：

1. `ADR-0002_editorial-manuscript-rebuild-6gate-baseline.md` 已锁定编码准入基线；
2. 下一阶段的主要工作可以按明确文件边界拆成后端/前端两条并行线；
3. 主要跨边界风险不再是架构未定，而是 contract 漂移与旧入口退出顺序，可通过共享 handoff 约束。

## 2. 并行模式

采用：

1. 主控
2. Worker A
3. Worker B

其中：

1. 主控拥有本文件，负责每轮汇总、写回、裁决冲突；
2. Worker A / B 只读本文件，不直接共同编辑本文件；
3. 每轮开始前，A/B 必须先读：
   - `decisions/ADR-0002_editorial-manuscript-rebuild-6gate-baseline.md`
   - 本文件最新版本
4. 每轮结束后，A/B 只向主控回报结构化更新，由主控统一写回本文件。

## 3. 文件边界

### 3.1 Worker A 边界

Owned files:

1. `Brain/brain-modules/brain-editorial/**`
2. `Brain/script/sql/update/**`

职责：

1. Gate 1:
   - flow / status / role / processType 对齐
2. Gate 2:
   - backend contract 拆分
3. Gate 4:
   - MySQL 增量脚本
4. 为前端提供稳定 contract delta

禁止：

1. 不改 `plus-ui-ts/**`
2. 不改主控 handoff
3. 不自行拍板变更 `ADR-0002`

### 3.2 Worker B 边界

Owned files:

1. `plus-ui-ts/src/views/editorial/review/**`
2. `plus-ui-ts/src/api/editorial/review.ts`
3. `plus-ui-ts/src/router/**`
4. `plus-ui-ts/src/components/Process/**` 中与审批前业务 hook 直接相关的最小改动

职责：

1. Gate 2:
   - frontend contract 对齐
2. Gate 3:
   - `index + form + detail/approval shell` IA 切换
3. Gate 5:
   - 前端定向验证口径落地
4. Gate 6:
   - 旧页 fallback 退场顺序前端侧实现

禁止：

1. 不改 `Brain/**`
2. 不改主控 handoff
3. 不自行拍板变更 `ADR-0002`

## 4. 共享约束

### 4.1 两个 worker 都必须遵守

1. 不自由聊天，不输出泛泛讨论；
2. 只按 `已确认 / 风险 / 待确认 / 推进方案` 回报；
3. 每轮必须先读 `ADR-0002` 和本文件最新版本；
4. 发现跨边界冲突时，不自行扩写对方目录，只回报主控；
5. 不得把“代码 ready”表述成“可提测”。

### 4.2 合同边界处理

1. `ADR-0002` 是 contract 真正基线；
2. Worker A 若调整 backend contract，必须在回报中明确：
   - 新字段
   - 删除字段
   - 语义变化
3. Worker B 若因前端 IA 需要新增契约字段，也必须先回报主控，不得默认扩 backend。

## 5. 每轮回报模板

每个 worker 每轮只回报这 4 段：

1. `已确认`
2. `风险`
3. `待确认`
4. `推进方案`

并附：

1. `Changed Files`
2. `Verification`
3. `Need From Other Worker`

## 6. 当前起跑线

当前统一基线：

1. `ADR-0002` 已生效，项目已进入 **可开始编码** 阶段；
2. 采用 **单 `flowCode=editorial_review_flow` + 显式 `processType(AUDIT|PROOFREAD)`**；
3. `freeze` 当前只锁审批前业务 hook 边界；
4. SQL 以 **MySQL 增量脚本** 为本轮编码入口；
5. `reviewEdit.vue` 只允许作为迁移期内部 fallback，最终 `formPath` 必须切走。

## 7. 下一会话启动顺序

1. 主控会话先读：
   - `ADR-0002`
   - 本文件
   - `implementation_plan_v01.md`
2. 主控先确认隔离 worktree 位置并完成创建，不得在 `main` 或脏共享工作树直接启动 worker
3. Worker A 会话启动
4. Worker B 会话启动
5. A/B 首轮完成后，由主控汇总并写回本文件，再进入下一轮

## 7.1 主控首轮收口结论

1. `implementation_plan_v01.md` 已按 `ADR-0002` 收口；若计划条目与 ADR 冲突，以 ADR 为准。
2. Phase 0 已由主控完成，Worker A / B 不再执行新的“基线定稿”动作。
3. 首批 `freeze` 仍只锁审批前业务 hook 边界；独立 snapshot 持久化不属于当前默认首批目标。
4. 当前唯一迁移事实入口仍是 `Brain/script/sql/update/**`；模块内置 `editorial_review.sql` 不作为本轮 worker 首批修改目标。
5. Worker 不得在当前 `main` 共享工作树上直接编码。

## 7.2 Worktree 启动前置条件

1. 本仓当前不存在 `.worktrees/` 或 `worktrees/` 目录；启动前需先确定 worktree 位置。
2. 若无额外仓库约束，主控推荐创建项目内隐藏目录 `.worktrees/`。
3. 推荐分支名：
   - Worker A: `codex/editorial-worker-a`
   - Worker B: `codex/editorial-worker-b`
4. 推荐目录名：
   - Worker A: `.worktrees/editorial-worker-a`
   - Worker B: `.worktrees/editorial-worker-b`
5. 创建完成后，A/B 各自在自己的 worktree 中读取 `ADR-0002`、本 handoff、`implementation_plan_v01.md` 再开始执行。

## 7.3 主控首轮汇总（基于 Worker A / B 回报，未做主控二次运行验证）

1. 已对齐：
   - backend page contract 已明确返回 `id/title/status/reviewStatus/canEdit/processType/createTime/user/dept`
   - backend detail contract 已明确承载 `attachment/linkList/historyList/approvalContext`
   - frontend 主路径应固定到 `/editorial/review/detail`
   - `reviewEdit.vue` 只保留内部 fallback，不再作为正式主入口或最终 `formPath`
2. 已落地但仍属 worker 自报：
   - Worker A 已完成 Gate 1 后端状态机/flow-role/processType 收口、Gate 2 contract 拆分、Gate 4 MySQL 增量 SQL 分离
   - Worker B 已完成 Gate 2 前端兼容层、Gate 3 新 IA、Gate 6 旧页 fallback 退场、Gate 5 前端验证骨架
3. 仍未闭环：
   - `freezeHook` 仍只有“审批前业务 hook 边界”口径，本轮不形成模块自管正式 API
   - `20260317_editorial_review_flow_role_seed.sql` 尚未在目标 `gxpublish_brain` schema 做 dry-run / apply
   - 前端 `vitest / eslint / build:prod` 尚未 fresh 执行；当前 blocker 是 `plus-ui-ts/node_modules` 不存在
4. 主控当前裁决：
   - Worker B 提出的 `page.reviewStatus/page.canEdit/page.user/page.dept`、`detail.historyList/approvalContext` 已由 Worker A 回报覆盖，可进入下一轮前端对齐
   - Worker B 提出的 `formPath` 切换目标已裁定为 `/editorial/review/detail`
   - `resubmit` 裁定为 editorial 模块独立 action，不再继续复用旧 `submit` 语义
   - `freezeHook` 裁定为后端内部 hook 边界，本轮不新增前端直调模块 API

## 7.4 下一轮优先级（主控裁决）

1. 主控优先：
   - 在目标库执行前先备份 `flow_definition/flow_node/flow_skip` 现存 editorial 相关行
   - 安排 `20260317_editorial_review_rebuild.sql` 与 `20260317_editorial_review_flow_role_seed.sql` 的 dry-run / apply
2. Worker A 下一轮：
   - 补齐 editorial 模块 `resubmit` 独立 action contract 与服务接线
   - 保持 `freezeHook` 为后端内部 hook 边界，不新增模块侧对外 controller URL
3. Worker B 下一轮：
   - 去掉对旧列表/详情负载的推断式 fallback，按显式 page/detail contract 收口
   - 把“退回后再次提交”切到独立 `resubmit` action；不请求独立 `freezeHook` API
   - 保持 `/editorial/review/detail` 为详情审批壳唯一主路径
4. 验证顺序：
   - 先目标库 flow-role/SQL 生效验证
   - 再前端 `pnpm install` + fresh `vitest / eslint / build:prod`
   - 最后进入真实 HTTP / 浏览器级联调
5. 最新主控批准：
   - 已批准 Worker B 在 `plus-ui-ts` 执行 project-local `pnpm install`
   - `resubmit` 权限点本轮先不拆，继续沿用 `editorial:review:edit`

## 7.5 第二轮前端验证更新（基于 Worker B 回报，未做主控二次运行验证）

1. 目标模块定向验证：
   - `pnpm install` 已完成，未命中依赖安装失败或 Node/pnpm/registry 环境阻断
   - `pnpm exec vitest run src/views/editorial/review/integration.spec.ts` 通过
   - Worker B 定向 `eslint` 命令仅覆盖其边界文件，结果通过
   - `pnpm build:prod` 通过
2. 仓库全量 baseline 噪声：
   - `pnpm lint:eslint` 仍失败
   - 剩余 63 个错误均位于 Worker B 边界外，不应计为本轮前端实现回归
3. 运行时闭环状态：
   - 仍无真实 HTTP / 浏览器级证据
   - Worker B 明确指出其 worktree 看到的 backend 仍不是已集成的新 contract 形态，因此当前不能做前后端运行时闭环结论
4. 主控当前裁决：
   - 前端目标模块定向验证已达“阶段完成”所需的前端侧最低证据
   - 仓库全量 lint 噪声需单独建包处理，不并入当前 Worker B 范围
   - 下一步优先级仍应先于浏览器联调完成集成树与目标库 SQL 生效验证

## 7.6 集成树与 SQL dry-run 预备结论（主控已执行）

1. 集成树状态：
   - 主控已创建 `C:\kuguaHome\project\gxpublish-brain\.worktrees\editorial-integration`
   - A/B 改动已以补丁方式导入集成树，无冲突
   - 当前集成树同时包含 backend `POST /editorial/review/resubmit` 与 frontend `resubmitReviewAction -> /editorial/review/resubmit`
2. 集成静态核对：
   - backend `EditorialReviewController` 已暴露 `@PostMapping("/resubmit")`
   - frontend `review.ts` 已暴露独立 `resubmitReviewAction`
   - frontend `form.vue` 已在 `BACK` 状态走独立 `resubmit` action
   - `page/detail` 的 `reviewStatus/canEdit/historyList/approvalContext` 契约在集成树中同时可见
3. SQL dry-run 预备事实：
   - `application-dev.yml` 的主库仍指向 `jdbc:mysql://localhost:3306/gxpublish_brain`
   - 主控通过 MySQL MCP 只读确认：`gxpublish_brain` schema 存在，`brain_editorial_review/flow_definition/flow_node/flow_skip/sys_role` 五张目标表存在
   - `brain_editorial_review` 已有 66 行数据，且 `review_status/process_type` 列已存在并非空白初始态
   - 当前线上 `editorial_review_flow` 仍指向旧 `form_path=/editorial/review/reviewEdit.vue`
   - 当前线上 `flow_node` 仍保留旧 `submit`/UUID 节点编码与旧 `permission_flag role:<id>` 口径，尚未切到 `first-review-node/second-review-node/final-review-node` 与变量化占位符
   - 当前线上三类审批角色已存在，但名称仍是“三审三校一级/二级/三级审批人”
4. dry-run 执行前约束：
   - 这不是首次建表脚本执行，而是对已有运行数据的替换式收口
   - 正式执行前必须先备份 `flow_definition/flow_node/flow_skip` 中 `editorial_review_flow` 相关行，以及三类 editorial 审批角色现状
   - 当前 `mysql` 客户端未在 PATH 命中；若继续执行，优先使用已验证可跨 schema 只读访问的 MySQL MCP，或再明确本机 SQL 执行器路径
5. 2026-03-18 用户新增裁决：
   - 当前 `editorial_*` 历史数据不要求保留，可按“全量重构”处理
   - 旧 `editorial_*` 相关流程/节点/角色设计被视为历史不合理设计，不作为迁移保留目标
   - 但真正执行 destructive SQL 前，主控仍需单独列出受影响表、清理范围、备份动作和 apply 顺序，不能把“允许清空”误写成“已执行清空”

## 8. Worker A 提示词

```text
你是 Worker A，负责 `gxpublish-brain` 审校模块替换式重构的后端实现线。不要自由聊天，直接执行。

你必须先读以下文件，且以其为唯一当前基线：
1. C:\kuguaHome\project\gxpublish-brain\doc\agent\editorial-manuscript-rebuild_20260317-132339\decisions\ADR-0002_editorial-manuscript-rebuild-6gate-baseline.md
2. C:\kuguaHome\project\gxpublish-brain\doc\agent\editorial-manuscript-rebuild_20260317-132339\handoffs\20260317_editorial-manuscript-dual-codex-shared_handoff_v01.md
3. C:\kuguaHome\project\gxpublish-brain\doc\agent\editorial-manuscript-rebuild_20260317-132339\plans\20260317_editorial-manuscript-rebuild-implementation_plan_v01.md

你的文件边界只允许：
1. Brain/brain-modules/brain-editorial/**
2. Brain/script/sql/update/**

你的本轮目标：
1. 落 Gate 1 的后端侧：flow / status / role / processType 对齐
2. 落 Gate 2 的后端 contract 拆分
3. 落 Gate 4 的 MySQL 增量脚本
4. 给前端提供明确 contract delta
5. 不把独立 freeze snapshot 持久化作为首批默认目标
6. 把 `resubmit` 落成 editorial 模块独立 action，不再继续复用旧 `submit` 语义
7. `freezeHook` 仅保留后端内部 hook 边界，本轮不新增前端直调模块 API

你不得：
1. 修改 plus-ui-ts/**
2. 修改共享 handoff
3. 重开 ADR-0002 已锁定问题

每次开始前先输出：
- 已确认
- 风险
- 待确认
- 推进方案

每轮结束时只回报：
- 已确认
- 风险
- 待确认
- 推进方案
- Changed Files
- Verification
- Need From Other Worker

如果发现需要前端配合的 contract 变化，不要擅自越界，只把字段 delta 明确列出来交给主控。
```

## 9. Worker B 提示词

```text
你是 Worker B，负责 `gxpublish-brain` 审校模块替换式重构的前端实现线。不要自由聊天，直接执行。

你必须先读以下文件，且以其为唯一当前基线：
1. C:\kuguaHome\project\gxpublish-brain\doc\agent\editorial-manuscript-rebuild_20260317-132339\decisions\ADR-0002_editorial-manuscript-rebuild-6gate-baseline.md
2. C:\kuguaHome\project\gxpublish-brain\doc\agent\editorial-manuscript-rebuild_20260317-132339\handoffs\20260317_editorial-manuscript-dual-codex-shared_handoff_v01.md
3. C:\kuguaHome\project\gxpublish-brain\doc\agent\editorial-manuscript-rebuild_20260317-132339\plans\20260317_editorial-manuscript-rebuild-implementation_plan_v01.md

你的文件边界只允许：
1. plus-ui-ts/src/views/editorial/review/**
2. plus-ui-ts/src/api/editorial/review.ts
3. plus-ui-ts/src/router/**
4. plus-ui-ts/src/components/Process/** 中与审批前业务 hook 直接相关的最小改动

你的本轮目标：
1. 落 Gate 2 的前端 contract 对齐
2. 落 Gate 3 的 IA 切换：index + form + detail/approval shell
3. 落 Gate 6 的旧页 fallback 退场顺序前端侧实现
4. 准备 Gate 5 的前端定向验证骨架
5. 不提前写菜单路径 SQL，也不把 `reviewEdit.vue` 继续当主入口
6. 把“退回后再次提交”切到独立 `resubmit` action，不再复用旧 `submit`
7. 不请求独立 `freezeHook` API；继续把它视为后端内部 hook 结果

你不得：
1. 修改 Brain/**
2. 修改共享 handoff
3. 重开 ADR-0002 已锁定问题

每次开始前先输出：
- 已确认
- 风险
- 待确认
- 推进方案

每轮结束时只回报：
- 已确认
- 风险
- 待确认
- 推进方案
- Changed Files
- Verification
- Need From Other Worker

如果发现 backend contract 仍缺字段，不要默认扩 backend，只把字段需求和理由明确回报给主控。
```
