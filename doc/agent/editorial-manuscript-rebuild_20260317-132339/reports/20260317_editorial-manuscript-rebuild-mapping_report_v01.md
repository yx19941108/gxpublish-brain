Owner: Codex
Task: editorial-manuscript-rebuild
Status: Draft For Review
Updated: 2026-03-17

# `pub-nexus -> gxpublish-brain` 审校模块映射设计报告

## 1. 结论先行

本轮推荐的不是把 `pub-nexus` 的稿件审核实现平移到 `gxpublish-brain`，而是：

1. 保留 `gxpublish-brain` 现有 `brain-editorial` 模块、四张主业务表、TUS 上传链路、workflow 事件接入与权限框架；
2. 把 `pub-nexus` 已验证的规范意图，转换为 `gxpublish-brain` 的新契约、新页面分层、新流程节点语义与新 SQL 增量脚本；
3. 允许不兼容旧流程实例，因此本次可按替换式重构处理旧 flow / 旧页面 / 旧粗粒度接口，不需要为历史实例保留兼容壳。

## 2. 本仓当前真实实现

### 2.1 后端

1. 当前入口仍是单一模块：
   - `Brain/brain-modules/brain-editorial`
2. 当前主接口仍是粗粒度一组：
   - `GET /editorial/review/list`
   - `GET /editorial/review/{id}`
   - `POST /editorial/review`
   - `PUT /editorial/review`
   - `POST /editorial/review/submit`
   - `DELETE /editorial/review/{ids}`
   - `GET /editorial/review/history/{id}`
3. 当前 `EditorialReviewServiceImpl` 同时承担：
   - page/list 查询
   - 详情聚合
   - 草稿新增/修改
   - 提交流程
   - 附件/链接写入
   - 历史记录
   - `ProcessEvent` / `ProcessTaskEvent` / `ProcessDeleteEvent` 同步
4. 当前主查询 SQL 仍是单一 `EditorialReviewVo` 投影，分页项与详情项未拆契约。

### 2.2 前端

1. 当前主入口是 `plus-ui-ts/src/views/editorial/review/index.vue`
2. 当前新增 / 修改 / 详情 / 审批全部落在同一页：
   - `plus-ui-ts/src/views/editorial/review/reviewEdit.vue`
3. 当前单页同时承载：
   - 基础表单
   - 附件上传
   - 关联链接
   - 审批按钮
   - 审批弹窗
   - 历史差异展示
4. 当前前端 API 仍是粗粒度一组：
   - `listReview`
   - `getReview`
   - `addReview`
   - `updateReview`
   - `submitReview`
   - `delReview`
   - `getHistory`

### 2.3 flow / 状态 / SQL

1. 当前 flow 文件：
   - `editorial_review_flow.json`
   - `formPath=/editorial/review/reviewEdit`
2. 当前 flow 只有两级审批节点：
   - `dept-audit-node`
   - `final-audit-node`
3. 当前状态枚举与历史测试链路却按三段等待态工作：
   - `WAITING_FIRST(10)`
   - `WAITING_SECOND(20)`
   - `WAITING_FINAL(30)`
   - `APPROVED(40)`
4. 当前模块内置 SQL 基线文件已与代码漂移：
   - `editorial_review.sql` 中没有 `review_status`
   - `editorial_review.sql` 中没有 `process_type`
   - Mapper / BO / VO / Service 已在读写这些字段

## 3. 关键冲突与设计含义

### 3.1 flow 定义与状态语义冲突

冲突事实：

1. `editorial_review_flow.json` 只有两级审批节点；
2. `ReviewStatusEnum`、`ProcessTaskEvent` 映射逻辑、`20260302_editorial-fulltest_report_v01.md` 都按三段等待态工作。

设计含义：

1. 不能把本次工作定义成“现有 flow 的小修小补”；
2. 必须把 flow 节点设计与细粒度业务状态一起重建；
3. 旧 nodeCode 兼容不再是本轮目标。

### 3.2 SQL 基线与运行代码冲突

冲突事实：

1. 模块内置 SQL 文件落后于 Java / Mapper / 前端契约；
2. 当前代码已默认存在 `review_status`、`process_type`、附件扩展字段。

设计含义：

1. `Brain/brain-modules/brain-editorial/src/main/resources/sql/editorial_review.sql` 不能继续被当成真实数据库基线；
2. 本轮必须用 `Brain/script/sql/update` 下的增量 SQL 收口；
3. 等增量脚本稳定后，再回刷模块内置 SQL 样例，避免继续漂移。

### 3.3 前端 IA 与参考规范冲突

冲突事实：

1. 当前 `reviewEdit.vue` 是“表单 + 审批 + 历史”的单体页；
2. `pub-nexus` 参考规范已验证“列表台账 + 独立 create/detail + detail 壳承接审批动作”更清晰。

设计含义：

1. 这轮前端不应继续在 `reviewEdit.vue` 上堆逻辑；
2. 应把业务表单与审批承载壳拆开；
3. 可迁移的是“分层思路”和“集成适配层”，不是 `pub-nexus` 的页面代码本身。

## 4. `pub-nexus -> gxpublish-brain` 映射矩阵

| 参考规范点 | `gxpublish-brain` 当前真实实现 | 推荐映射目标 | 决策 |
| --- | --- | --- | --- |
| page / detail 双契约 | 单一 `EditorialReviewVo` 同时给列表与详情 | 拆成 `PageItem` 与 `Detail` 两套 VO / TS 类型 | 替换 |
| create / resubmit 分动作 | 草稿新增、修改、提交混在 `add/update/submit` | 保留草稿概念，但新增显式 `resubmit` 契约 | 替换 |
| 业务表单与审批壳分离 | `reviewEdit.vue` 单页承载全部逻辑 | 列表页保留，新增独立 create/edit 页与 detail/approval 壳 | 替换 |
| integration adapter | View 直接绑 API 类型 | 在 `src/views/editorial/review/` 增加 `integration.ts` / `model.ts` | 新增 |
| 审批前业务冻结 | 当前 `submitVerify.vue`/`approvalButton.vue` 无业务前置 hook | 在本仓共享流程组件上增加 pre-action hook，不引入 `ProcessInstanceOperationButton.vue` | 替换式吸收 |
| 菜单 / 权限 SQL 与业务表 DDL 分离 | 当前尚未形成本轮专属增量脚本 | 业务表变更与菜单权限脚本分文件落盘 | 保留原则，新增实现 |
| Yudao/Flowable 运行时依赖 | 当前为自定义 workflow JSON + 事件监听 | 继续使用本仓 workflow，不引入外部 BPM runtime 表假设 | 保留 |
| `manuscript` 领域命名 | 当前仓命名为 `editorial/review` | 领域意图迁入，但模块名与主路径保持本仓命名 | 保留本仓命名 |

## 5. 保留项 / 替换项 / 删除项

### 5.1 保留项

1. 后端模块归属：
   - `brain-editorial`
2. 主业务表主干：
   - `brain_editorial_review`
   - `brain_editorial_attachment`
   - `brain_editorial_link`
   - `brain_editorial_history`
3. 当前上传基础设施：
   - `TusUpload.vue`
   - 既有 TUS 后端链路
4. 当前 workflow 事件接入方式：
   - `ProcessEvent`
   - `ProcessTaskEvent`
   - `ProcessDeleteEvent`
5. 当前数据权限与角色框架：
   - `EditorialDataScopeFactory`
   - `ReviewStatusEnum`
   - `editorial:review:*` 权限前缀可继续作为主前缀

### 5.2 替换项

1. 页面信息架构：
   - 从单体 `reviewEdit.vue` 替换为 `list + create/edit + detail/approval shell`
2. API 契约：
   - 从粗粒度 `list/get/add/update/submit/history` 替换为 `page/detail/create/update/submit/resubmit/history/freeze`
3. 查询模型：
   - 从单一 `EditorialReviewVo` 替换为分页项/详情项分离
4. flow 设计：
   - 从当前两级审批节点重建为与 `10 -> 20 -> 30 -> 40` 对齐的节点语义
5. SQL 事实来源：
   - 从模块内置 `editorial_review.sql` 的旧快照，替换为 `Brain/script/sql/update` 的正式增量脚本

### 5.3 删除项

1. 删除“继续维护旧流程实例兼容”的实现约束
2. 删除“继续把所有业务形态塞进 `reviewEdit.vue`”的前端架构
3. 删除“列表和详情复用同一 VO/TS 类型”的契约模式
4. 删除“把模块内置 SQL 文件当成当前数据库真实基线”的使用方式

说明：

上述“删除”首先是架构废弃，不要求本阶段立刻物理删文件；可按“新入口切换后再移除旧页/旧接口”的顺序推进。

## 6. 重构边界

### 6.1 Backend 边界

本轮 backend 应覆盖：

1. Controller 契约拆分
2. BO / VO / Mapper 查询投影拆分
3. Service 责任拆分
4. 状态机与 workflow 事件映射重建
5. resubmit / freeze 类业务动作补齐
6. SQL 增量脚本与必要索引补齐

本轮 backend 不应覆盖：

1. 引入新的外部 BPM 引擎或运行时表模型
2. 直接照搬 `pub-nexus` 模块命名、包结构、SQL 文件

### 6.2 Frontend 边界

本轮 frontend 应覆盖：

1. 列表台账字段与筛选项收口
2. create / edit / detail 页面拆分
3. shared form 组件抽取
4. integration adapter 引入
5. 审批前业务冻结 hook 接入现有共享流程组件

本轮 frontend 不应覆盖：

1. 直接复制 `pub-nexus` 的 `index.vue/create.vue/detail.vue`
2. 直接迁入 `ProcessInstanceOperationButton.vue` 的大体量实现

### 6.3 Flow 边界

本轮 flow 应覆盖：

1. 明确节点数与节点语义
2. 统一 nodeCode / nodeName / `ReviewStatusEnum` 映射
3. 统一 `AUDIT` / `PROOFREAD` 与 flowCode 的关系
4. 统一 `formPath` 到新 detail/approval 壳

本轮 flow 不应覆盖：

1. 为旧实例保留旧 nodeCode 兼容分支
2. 在未改状态机前先单独改 flow 文件

### 6.4 SQL 边界

本轮 SQL 应覆盖：

1. 现有缺失字段补齐：
   - `review_status`
   - `process_type`
   - 需要的索引 / 注释 / 默认值
2. 如引入冻结快照能力，再新增独立表或字段
3. 菜单 / 权限 SQL 与业务表 DDL 分离

本轮 SQL 不应覆盖：

1. 重刷初始化总库 SQL
2. 猜测 `pub-nexus` BPM 付费表结构并照搬

## 7. 推荐目标形态

### 7.1 后端目标形态

推荐拆成以下层次：

1. 台账契约：
   - `EditorialReviewPageItemVo`
2. 详情契约：
   - `EditorialReviewDetailVo`
3. 动作请求：
   - `EditorialReviewCreateReq`
   - `EditorialReviewUpdateReq`
   - `EditorialReviewSubmitReq`
   - `EditorialReviewResubmitReq`
   - `EditorialReviewFreezeReq`
4. Service 责任：
   - query / assembler
   - form command
   - workflow sync
   - freeze / history

### 7.2 前端目标形态

推荐拆成以下层次：

1. `index.vue`
   - 只承载台账、筛选、进入动作
2. `create.vue` / `edit.vue`
   - 只承载业务表单录入
3. `detail.vue`
   - 只承载只读详情、审批操作、历史/冻结记录
4. `components/ReviewFormFields.vue`
   - 共享表单域
5. `integration.ts`
   - 视图模型与 API 契约适配
6. `model.ts`
   - 页面内聚模型与枚举

说明：

如果实施时希望减少路由数量，也可以合并为 `form/index.vue` 与 `detail/index.vue` 两页；但不建议继续保留单体 `reviewEdit.vue` 作为主实现。

## 8. 本轮建议的落地顺序

1. 先收口契约、flow、SQL 差异，固定新基线
2. 再做 backend contract / query / schema 改造
3. 再做 workflow action / freeze hook
4. 最后做 frontend IA 重构与路由切换

不推荐顺序：

1. 先改前端页面再补 backend 契约
2. 先改 flow 再补状态机
3. 先写菜单 SQL 再确认新页面路径

## 9. 一句话结论

`pub-nexus` 对 `gxpublish-brain` 的价值，不是“给现成代码”，而是“给已经验证过的结构原则”。本轮最合理的实施方向，是在保留 `gxpublish-brain` 当前 editorial/workflow 基础设施的前提下，替换掉单体页面、粗粒度契约、漂移的 flow/SQL 基线，重建成可审计、可回滚、可验证的新审校模块。 
