---
Owner: Codex QA Worker R1
Task: manuscript_review tdd test strategy
Status: v01
Updated: 2026-03-21 08:47:49 CST
---

# 审校模块 TDD 测试策略报告 v01

## 1. 当前固定基线

- 需求真源仅为 `doc/manuscript_review/product/20260321_manuscript-review_product-requirements_v01.md`。
- 当前数据库只有平台通用表，无审校业务表，因此 SQL 合同测试必须前置。
- 后端定向命令已经到达真实 `testCompile` RED，说明可以直接从生产类/接口缺失开始建立 backend RED。
- 前端定向 route-shell 测试仍卡在 harness 级 `window is not defined`，所以前端第一优先不是业务断言，而是先建立可运行的壳层 RED。

## 2. 策略原则

- 先小后大：先锁规则与接口契约，再扩到页面与浏览器流。
- 先 RED 再实现：首批测试允许因类缺失、表缺失、壳层缺失而失败，但失败信号必须准确指向 PRD 约束。
- 严禁语义漂移：一期禁止草稿、删除、回收站、恢复；测试必须把这些语义作为负向断言写死。
- 严禁技术暴露：测试必须显式拦住数据库主键、账号、原始枚举码、`roleKey` 等技术值直接进入 API/UI/历史文案。
- 历史优先于覆盖：附件、外链、视频标注都按“追加历史 + 当前可停用”设计验收，不接受覆盖更新或硬删除。

## 3. 首批 10 个 failing tests 优先级

| 顺序 | 测试 ID | 层级 | 首次失败目标 | 通过标准 |
| --- | --- | --- | --- | --- |
| 1 | T01_SubmitGate_NoDraftDelete_ValidationSpec | Backend Unit | 当前应因提交应用服务/校验器缺失而 RED。测试锁定：提交/再次提交只能走正式流转；正文必填；附件或外链至少一种；审批链完整；各级角色有有效成员；错误信息必须指出缺失配置层级。 | 无任何 `draft/delete/recycle` 入口；提交校验完整且错误提示可定位到具体缺口。 |
| 2 | T02_SqlContract_CoreSchemaWithoutDraftDeleteSpec | SQL DDL/DML | 当前应因业务 DDL 缺失而 RED。测试锁定：必须存在能承载流程主记录、附件、外链、视频标注、历史的核心结构；租户隔离可表达；审批角色配置依据为 `roleKey`；不存在草稿/删除语义列。 | DDL/DML 能支撑 PRD 必要能力，且无 `draft`、`delete`、`recycle` 语义字段或初始化数据。 |
| 3 | T03_SerialNumber_PerTypePerDay_ResetSpec | Backend Unit | 当前应因编号生成器或领域规则缺失而 RED。测试锁定：审核流程前缀 `SH`、校对流程前缀 `JD`，按流程类型和日期分别三位流水、每日重置。 | 稿件号格式与重置逻辑完全符合 PRD。 |
| 4 | T04_PermissionVisibility_ActionBoundarySpec | Permissions / Backend Unit | 当前应因权限判定服务、动作边界建模缺失而 RED。测试锁定：发起人、当前审批人、历史参与人可见范围；可审即可改；纯查看人不可改；“修改 + 再次提交”与“修改 + 去审批”必须拆两步。 | 可见/可改/可动作边界与 PRD 完整一致，无跨角色越权。 |
| 5 | T05_CertifiedInitiator_SkipLevelOne_AndAuditSpec | Backend Unit | 当前应因持证资格判定、跳级路由、历史写入缺失而 RED。测试锁定：提交与再次提交都实时按“持证发起人”角色判定；命中时跳过一级进入二级；不把持证资格持久化为基本信息；时间线记入自然语言。 | 持证跳级只影响当次流转，且历史可追溯。 |
| 6 | T06_Frontend_RouteShell_JsdomWindowIsolationSpec | Frontend Unit | 当前已知会因 `window is not defined` 在 harness 级 RED。测试锁定：列表/详情壳层在测试环境导入与渲染时不得在模块初始化阶段直接依赖浏览器全局对象。 | 前端壳层测试可稳定运行，随后才允许进入业务 UI RED。 |
| 7 | T07_Frontend_DetailActions_AndReadableDisplaySpec | Frontend Unit | 在 T06 通过前不推进；通过后首个业务 RED。测试锁定：当前审批人仅见“修改/去审批/返回”；退回发起人仅见“修改/再次提交/返回”；纯查看人仅“返回”；页面不出现草稿/删除按钮；不展示 ID、账号、原始码值。 | 详情动作分流与展示净化均符合 PRD。 |
| 8 | T08_ApiContract_ListDetail_ReadableLabelsSpec | Integration / API | 当前应因 DTO/接口缺失而 RED。测试锁定：列表按最近更新时间倒序；仅保留详情入口；状态/节点为中文业务文案；姓名替代账号；时间格式统一 `yyyy-MM-dd HH:mm:ss`；接口不暴露主键/原始枚举/账号/`roleKey`。 | API 契约直接满足业务可读性，不依赖前端二次清洗兜底。 |
| 9 | T09_AuditTrail_AppendOnly_ResourceLifecycleSpec | History Audit / Integration | 当前应因历史与资源生命周期实现缺失而 RED。测试锁定：新增流程、修改、再次提交、附件/外链/标注增停用、通过/退回/撤销/驳回、系统跳级均写入统一时间线；当前有效资源列表不含停用项；历史仍可追溯停用资源。 | “当前有效”与“追加历史”同时成立，且历史文案是自然语言。 |
| 10 | T10_Browser_MultiVideo_AnnotationFlowSpec | Browser / Video Annotation | 当前在前置层未通过前不执行；一旦执行，测试锁定：多视频列表 + 单播放器切换；时间输入支持 `ss/mm:ss/HH:mm:ss` 并统一显示 `HH:mm:ss`；非法格式、越界、结束早于开始、时长未识别必须拦截；点击标注跳转到开始时间；停用后仅从当前列表消失、历史仍在。 | 浏览器层完整证明视频与标注业务闭环，不自动猜测非法时间，不强制结束时间自动停播。 |

## 4. 执行门槛与顺序说明

- Gate A：先跑 T01-T05，建立后端和权限域真实 RED。
- Gate B：并行推动 T02，避免后续后端规则没有可落库的结构承载。
- Gate C：先修通 T06 的前端 harness，再允许写 T07 的业务断言；否则前端 RED 不具业务意义。
- Gate D：T08 与 T09 在基础领域对象可编译后立刻补上，防止接口先天泄露技术值、历史先天做成覆盖更新。
- Gate E：只有当前 4 个门槛成立后才进入 T10 浏览器取证，否则只会得到噪声失败。

## 5. 明确防滑条款

- 任何测试设计都必须包含 `no draft` 与 `no delete` 负向断言，不能只测正向流。
- 任何接口/页面/历史断言都必须包含 `no technical id / no account / no raw enum` 断言，不能假设“展示层后续会转义”。
- 任何资源变更测试都必须同时验证“当前有效列表”与“历史追溯能力”，不能只验证其中一侧。

## 6. 当前最高风险 acceptance gap

- 最高风险 acceptance gap 是 `T08 + T09 + T10` 的组合尚未成链：如果没有把 API 可读契约、追加历史审计、浏览器资源展示一起锁定，开发很可能做出“流程能提交审批，但页面/API 仍暴露技术值，且资源历史不可追溯”的假完成态。
