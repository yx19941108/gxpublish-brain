Owner: Codex
Task: editorial-fulltest
Status: Draft-待游工确认
Updated: 2026-03-02

# 三审三校全量测试计划（本地联调）

## 1. 目标与范围

### 1.1 测试目标
- 对三审三校功能做端到端全量验证，覆盖功能正确性、权限与数据可见性、异常恢复、文件上传专项。
- 覆盖两类流程：`AUDIT` 与 `PROOFREAD`。
- 按缺陷默认规则执行：`P0/P1` 阻断验收，`P2/P3` 记录风险。

### 1.2 测试范围（In Scope）
- 后端接口：`/editorial/review/*`（list/detail/add/edit/submit/delete/history）
- 工作流联动：提交流程、审批推进、驳回/撤销/终止、流程删除事件
- 数据可见性：发起人/审批人/管理员跨角色与同级多账号可见范围
- 前端页面：审校列表、编辑页、审批按钮动作、历史记录展示
- 文件上传专项：分片上传、断点续传、预览

### 1.3 非范围（Out of Scope）
- 性能压测、容量压测、长稳压测

## 2. 基线与实现锚点

以下文件作为计划与结果判定依据：
- `Brain/brain-modules/brain-editorial/src/main/java/com/gxpublish/brain/editorial/controller/EditorialReviewController.java`
- `Brain/brain-modules/brain-editorial/src/main/java/com/gxpublish/brain/editorial/service/impl/EditorialReviewServiceImpl.java`
- `Brain/brain-modules/brain-editorial/src/main/resources/mapper/editorial/EditorialReviewMapper.xml`
- `Brain/brain-modules/brain-editorial/src/main/resources/flow/editorial_review_flow.json`
- `plus-ui-ts/src/views/editorial/review/index.vue`
- `plus-ui-ts/src/views/editorial/review/reviewEdit.vue`
- `plus-ui-ts/src/api/editorial/review.ts`

## 3. 环境与账号矩阵

### 3.1 联调环境
- 模式：本地联调（固定环境）
- 后端：`http://localhost:9888`
- 前端：`http://localhost:9999`
- 租户与客户端：按你本地固定配置执行

### 3.2 账号矩阵（你已确认）
- 发起人：`zhangsan / 123456`
- 持证发起人：`lisi / 123456`
- 一审：`wangwu / 123456`、`sunba / 123456`
- 二审：`zhaoliu / 123456`、`zhoujiu / 123456`
- 三审：`qianqi / 123456`、`wushi / 123456`

### 3.3 执行前门禁（必须全部通过）
- 门禁-G1：测试前先通知游工确认“服务已启动”
- 门禁-G2：`/auth/login` 可用，主要测试账号均可登录成功
- 门禁-G3：工作流定义 `editorial_review_flow` 在当前环境可用
- 门禁-G4：数据库中存在审校相关表（`brain_editorial_review`、`brain_editorial_attachment`、`brain_editorial_link`、`brain_editorial_history`）
- 门禁-G5：前端可正常访问审校页面并加载菜单/按钮权限

> 说明：当前 MySQL MCP 指向库中未发现 `brain_editorial_*` 表，执行阶段将优先做门禁-G4；若失败则中止执行并反馈环境问题。

## 4. 风险点与重点验证

- R1：`processHandler` 中 `nodeCode` 映射使用硬编码 UUID，可能与流程定义节点码不一致，导致 `review_status` 分级错误。
- R2：数据权限 SQL 存在“发起人 + 审批人 + 历史参与”复合条件，需防止越权可见。
- R3：持证发起人自动跳过一级审批，需同时核对状态流转与历史记录一致性。
- R4：文件上传链路涉及前端 Tus 组件与后端存储，需重点验证分片、断点、预览。

## 5. 执行流程（批准后实施）

1. 阶段A：环境与基线检查
2. 阶段B：接口功能与字段校验（CRUD + submit + history）
3. 阶段C：流程主链验证（AUDIT/PROOFREAD）
4. 阶段D：权限与数据可见性矩阵验证（含同级多账号）
5. 阶段E：文件上传专项验证（分片/断点续传/预览）
6. 阶段F：异常链路与恢复验证（驳回/撤销/终止/越权）
7. 阶段G：回归核验与报告输出

## 6. 测试用例清单

### 6.1 环境与登录

| ID | 场景 | 前置条件 | 步骤 | 预期结果 | 证据 |
|---|---|---|---|---|---|
| ENV-001 | 服务启动确认 | 无 | 通知游工确认服务已启动 | 获得“已启动”确认 | 聊天记录 |
| ENV-002 | 账号登录可用性 | 服务已启动 | 逐一登录 8 个账号 | 全部登录成功，拿到 token | API 响应 |
| ENV-003 | 审校表门禁 | DB 可访问 | 查询审校4张业务表 | 表均存在 | SQL 结果 |

### 6.2 基础功能（接口 + 页面）

| ID | 场景 | 前置条件 | 步骤 | 预期结果 | 证据 |
|---|---|---|---|---|---|
| API-001 | 草稿新增 | zhangsan 登录 | 新增审校单（AUDIT） | 返回成功，`status=draft/review_status=0` | 响应+SQL |
| API-002 | 草稿修改 | API-001 成功 | 修改标题/内容/备注 | 修改成功，历史按规则记录 | 响应+SQL |
| API-003 | 草稿删除 | 新建草稿 | 删除草稿 | 删除成功，不可见 | 响应+SQL |
| API-004 | 详情查询 | 存在记录 | 查询详情 | 字段完整，附件/链接正确回显 | 响应 |
| API-005 | 历史查询 | 有流转记录 | 查询 history | 返回时间倒序，操作类型正确 | 响应 |
| UI-001 | 列表筛选 | 有多条记录 | 用标题/状态过滤 | 筛选结果正确 | 页面截图 |
| UI-002 | 编辑限制 | 非草稿记录 | 在 UI 尝试编辑 | 按规则不可编辑 | 页面截图 |

### 6.3 流程主链（AUDIT）

| ID | 场景 | 前置条件 | 步骤 | 预期结果 | 证据 |
|---|---|---|---|---|---|
| WF-A-001 | 发起提交 | zhangsan 草稿 | 调用 submit | 进入待审，生成 applyCode | 响应+SQL |
| WF-A-002 | 一审处理 | WF-A-001 | wangwu 完成一审 | 进入二审待办 | 待办+SQL |
| WF-A-003 | 二审处理 | WF-A-002 | zhaoliu 完成二审 | 进入三审待办 | 待办+SQL |
| WF-A-004 | 三审处理 | WF-A-003 | qianqi 完成三审 | 完成，`review_status=40` | 待办+SQL |
| WF-A-005 | 节点状态映射 | WF-A-001~004 | 每节点后查询状态 | `10->20->30->40` 符合预期 | SQL/接口 |

### 6.4 流程主链（PROOFREAD）

| ID | 场景 | 前置条件 | 步骤 | 预期结果 | 证据 |
|---|---|---|---|---|---|
| WF-P-001 | PROOFREAD 发起 | zhangsan 新草稿 | `processType=PROOFREAD` 提交 | 流程成功发起 | 响应+SQL |
| WF-P-002 | PROOFREAD 审批链 | WF-P-001 | 一二三审依次处理 | 全链路完成 | 待办+SQL |
| WF-P-003 | PROOFREAD 历史 | WF-P-002 | 查询 history | 记录完整且顺序正确 | 响应 |

### 6.5 持证发起人与异常分支

| ID | 场景 | 前置条件 | 步骤 | 预期结果 | 证据 |
|---|---|---|---|---|---|
| BR-001 | 持证自动跳一审 | lisi 草稿 | submit 发起 | 直接进入二审或体现跳过一审规则 | 待办+history+SQL |
| BR-002 | 驳回链路 | 发起后待审 | 审批人驳回 | `status/review_status` 变更为 back，发起人可改 | 响应+SQL |
| BR-003 | 撤销链路 | 发起后待审 | 发起人撤销 | 变更为 cancel | 响应+SQL |
| BR-004 | 终止链路 | 发起后待审 | 审批侧终止 | 变更为 termination | 响应+SQL |
| BR-005 | 已流转记录编辑限制 | 非draft/back | 发起人尝试修改 | 后端拒绝并返回错误 | 响应 |

### 6.6 权限与可见性（多账号）

| ID | 场景 | 前置条件 | 步骤 | 预期结果 | 证据 |
|---|---|---|---|---|---|
| PERM-001 | 一审双账号可见性 | 有待一审单 | wangwu/sunba 分别查列表与待办 | 仅可见其权限范围内数据 | 列表+待办 |
| PERM-002 | 二审双账号可见性 | 有待二审单 | zhaoliu/zhoujiu 验证 | 可见性符合角色 | 列表+待办 |
| PERM-003 | 三审双账号可见性 | 有待三审单 | qianqi/wushi 验证 | 可见性符合角色 | 列表+待办 |
| PERM-004 | 发起人隔离 | 多发起人数据 | zhangsan/lisi 互查 | 不可越权查看他人草稿 | 列表+详情 |
| PERM-005 | 接口越权 | 低权限账号 | 调用删改等受限接口 | 返回鉴权失败/无权限 | 响应 |

### 6.7 文件上传专项（重点）

| ID | 场景 | 前置条件 | 步骤 | 预期结果 | 证据 |
|---|---|---|---|---|---|
| FILE-001 | 分片上传生效 | 前端可访问 | 上传大文件，抓取请求 | 存在分片请求，偏移连续，合并成功 | Network 记录+页面 |
| FILE-002 | 断点续传生效 | FILE-001 | 上传中断后恢复继续 | 从断点继续，最终文件完整一致 | Network+文件校验 |
| FILE-003 | 预览可用 | 上传成功 | 在详情页预览/打开附件 | 预览正常，URL 可访问，权限正确 | 页面截图+响应 |
| FILE-004 | 链接+附件约束 | 提交流程 | 附件与链接都为空时提交 | 被阻止并提示 | 页面截图 |

### 6.8 回归与一致性

| ID | 场景 | 前置条件 | 步骤 | 预期结果 | 证据 |
|---|---|---|---|---|---|
| REG-001 | 列表状态与详情一致 | 多状态记录 | 对比列表与详情 | 状态字段一致 | 页面+接口 |
| REG-002 | 历史记录完整性 | 全链路完成 | 检查 history 操作序列 | 无缺失、时间有序 | SQL+接口 |
| REG-003 | 数据保留策略 | 全部测试结束 | 不做清理，仅核验可追溯 | 数据保留可复核 | SQL |

## 7. 证据与缺陷管理

### 7.1 证据要求
- 每条用例至少一种证据：接口响应、SQL结果、浏览器截图、网络请求、控制台日志。
- 文件专项必须同时保留：Network 请求轨迹 + 页面结果截图。

### 7.2 缺陷分级与出入口
- `P0/P1`：阻断，停止验收
- `P2/P3`：记录风险，可继续执行并在报告给出修复建议

## 8. 执行依赖（MCP / Skills）

### 8.1 MCP
- `mcp__mysql__execute_sql`
- `mcp__redis__list`
- `mcp__redis__get`
- `mcp__chrome-devtools__*`（页面操作、网络与截图取证）
- `mcp__apifox__read_project_oas_19ng28`（如需对照接口规范）
- `shell_command`（本地脚本执行与日志收集）

### 8.2 Skills
- `senior-qa-engineer`

## 9. 输出物

- 测试计划：`doc/agent/plans/20260302_editorial-fulltest_plan_v01.md`
- 测试报告（执行后）：`doc/agent/reports/20260302_editorial-fulltest_report_v01.md`

---

确认项（请游工确认后我再执行测试）：
1. 按本计划执行全量测试
2. 执行时遵循“测试前先通知服务启动确认”
3. 发现 `P0/P1` 时立即中断并回报
