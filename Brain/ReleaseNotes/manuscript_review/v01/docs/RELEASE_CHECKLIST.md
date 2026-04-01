Owner: Codex
Task: manuscript_review release checklist
Status: Draft
Updated: 2026-04-01

# 发布检查清单

## 发布前

- 构建机已具备 JDK 17、Maven、Node.js、npm
- 后端 jar 已生成
- 前端 dist 已生成
- 发布目录已上传到目标服务器
- `.env` 已由 `.env.example` 复制并填写
- 数据库备份已完成
- OSS / MinIO 配置口径已确认

## SQL

- 空白平台库已执行 `ry_vue_5.X.sql`
- 已执行 `ry_workflow.sql`
- 已执行 `ry_job.sql`
- 已执行 `sql/01_manuscript_review_release_ddl.sql`
- 已执行 `sql/02_manuscript_review_release_dml.sql`

## 运行态

- `docker compose build` 已完成
- `docker compose up -d` 已完成
- `docker compose ps` 显示预期服务处于运行状态
- `brain-server` 日志无致命启动错误
- 前端首页可访问
- 登录接口可访问

## 业务验收

- 角色绑定后能看到三审三校菜单
- 发起人能创建并提交三审三校流程
- 一级审批人能审批或驳回
- 二级审批人能审批或驳回
- 三级审批人能审批、驳回、终止
- 驳回链路能回到发起人
- 校对流程也能正常发起
- 附件上传与预览链路正常
- 历史时间线顺序符合预期

## 交接留痕

- 已归档本次发布目录与准确构建物版本
- 最终 `.env` 已进入服务器侧运维保管，不入 Git
- 实际执行的 SQL 顺序已留痕
- 回滚包与回滚位置已记录
