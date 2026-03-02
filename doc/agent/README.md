Owner: Codex
Task: Agent Artifact Policy Onboarding
Status: Active
Updated: 2026-03-02

# doc/agent 使用说明

## 1. 存储位置
- 持久化文档（纳入版本控制）：`doc/agent/`
- 临时过程文件（不纳入版本控制）：`.agent-temp/`

## 2. 推荐子目录
- `doc/agent/plans/`：计划文档
- `doc/agent/reports/`：执行报告
- `doc/agent/reviews/`：评审记录
- `doc/agent/decisions/`：决策记录（含 ADR）
- `doc/agent/handoffs/`：交接文档

## 3. 命名规范
- 常规文档：`YYYYMMDD_<task-id>_<type>_vNN.<ext>`
- ADR 文档：`ADR-XXXX_<short-title>.md`
- `<type>` 允许值：`plan`、`report`、`review`、`handoff`、`note`

## 4. 元数据头（必填）
每个持久化文档开头必须包含以下字段：
- `Owner`
- `Task`
- `Status`
- `Updated`

示例：

```text
Owner: Codex
Task: editorial-fulltest
Status: Draft
Updated: 2026-03-02
```

## 5. 使用约束
- 不要把临时调试文件放在 `doc/agent/`
- 临时文件统一放在 `.agent-temp/`
- 文档应保持可追踪（版本号递增）
