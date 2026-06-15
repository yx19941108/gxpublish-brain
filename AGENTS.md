# AGENTS.md

## 适用范围

本文件是 `gxpublish-brain` 项目级总规则。若子目录存在更具体的 `AGENTS.md`，以更具体规则优先。

## 项目结构

- 后端工程：`Brain/`
- 前端工程：`plus-ui-ts/`
- 后端规则：`Brain/AGENTS.md`
- 前端规则：`plus-ui-ts/AGENTS.md`
- 需求、设计、任务、验收和会话接续：`openspec/`

## 上下文优先级

1. 优先读取本仓库代码、配置、脚本、OpenSpec 和项目文档。
2. 功能需求、设计、任务、验收和会话接续，以 `openspec/` 为主记录。
3. `RuoYi-Vue-Plus` 官方文档仅作为框架补充和交叉验证。
4. 若本地事实与外部文档冲突，先列出冲突点、推荐方案和风险，等待用户确认。

## OpenSpec 规则

1. 新功能、行为变更、规格调整，必须建立或选择对应 OpenSpec change。
2. 若已有相关 change，优先复用；若没有，再新建 change。不得为同一业务目标重复创建多个 change。
3. OpenSpec change 维护需求、设计、任务、验收和会话接续；不得在 `doc/agent/` 重复维护同类进度文档。
4. 需求未收敛时，先确认业务规则、页面展示、输入输出、点击跳转、筛选、权限、状态、约束和验收口径。
5. 设计未收敛时，先确认模块边界、数据模型、表关系、接口定义、关键流程、异常处理、测试方案和回滚方式。
6. OpenSpec 变更完成或阶段性收敛后，运行 `openspec validate <change-id> --strict`。
7. 轻量缺陷修复、文案调整、局部样式调整、测试补充，若不改变功能规格，可不新建 change，但交付时必须说明判断依据。

## Skills 规则

1. 需求、业务规则、领域语言或验收口径不清时，优先使用 `grill-with-docs` 或 `grill-me` 收敛结论。
2. 真实 bug、失败测试、性能回退或原因不明的问题，使用 `diagnose`。
3. 用户要求 TDD，或涉及高风险共享逻辑、权限、状态流转、并发、数据一致性时，使用 `tdd`。
4. 已确认的 OpenSpec 设计或任务需要拆分执行项时，使用 `to-issues`。
5. 轻量、局部、可从代码和 OpenSpec 直接判断的任务，可读取上下文后直接执行。
6. 写入 `docs/agents/`、`CONTEXT.md`、`docs/adr/` 或 OpenSpec 前，必须说明目标文件和写入范围；涉及归档、删除、大范围重写等不可逆操作时必须确认。

## CodeGraph 规则

1. 涉及结构化代码理解、调用链、影响范围、模块边界或架构关系时，优先使用 CodeGraph。
2. 基于 `codegraph_context`、`codegraph_trace`、`codegraph_callers`、`codegraph_callees`、`codegraph_impact` 等结果判断。
3. 字面文本、配置项、文案、SQL 片段等精确文本查询，使用 `rg`。
4. 单文件、小范围实现细节或精确文本确认，可在 CodeGraph 定位后直接读取相关文件。
5. 不得用人工全文搜索重复还原 CodeGraph 已覆盖的调用关系和结构关系。

## 验证命令路由

- 后端验证命令见 `Brain/AGENTS.md`。
- 前端验证命令见 `plus-ui-ts/AGENTS.md`。
- 涉及前后端联调的变更，必须同时执行对应后端和前端验证；无法执行时说明原因和残余风险。

## 交付要求

每次交付必须说明：

1. 变更范围。
2. 影响面。
3. 验证结果。
4. 回滚方式。
5. 未验证项和残余风险。

交付前检查：

1. 无密钥泄漏。
2. 无无关改动。
3. 无构建产物提交。
4. 涉及 OpenSpec 的任务已更新对应 change 状态。
