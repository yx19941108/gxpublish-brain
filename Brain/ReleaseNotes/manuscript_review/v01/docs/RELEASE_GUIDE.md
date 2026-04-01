Owner: Codex
Task: manuscript_review release guide
Status: Draft
Updated: 2026-04-01

# 三审三校发布操作手册

## 1. 目标

本手册用于把 `manuscript_review` 模块及其所需运行服务，从 0 到 1 部署到目标服务器。

本手册采用的前提比较严格：

- 目标服务器初始没有 Java、Node.js、Docker、Docker Compose
- 构建产物先在构建机或 CI 上准备好，再上传到服务器
- 服务器运行阶段统一走 Docker Compose，这样服务器只需要安装 Docker 和 compose 插件

## 2. 使用边界

本发布包覆盖的是三审三校模块增量，以及支撑它运行的最小服务集合。

它不替代整个平台基座初始化。若数据库是空白平台库，先执行平台基础 SQL：

1. `Brain/script/sql/ry_vue_5.X.sql`
2. `Brain/script/sql/ry_workflow.sql`
3. `Brain/script/sql/ry_job.sql`
4. `sql/01_manuscript_review_release_ddl.sql`
5. `sql/02_manuscript_review_release_dml.sql`

## 3. 发布包结构

- `artifacts/`
  放后端 jar 与前端 dist。
- `config/`
  放 nginx 与 redis 配置。
- `docker/`
  放后端、监控中心、SnailJob、前端镜像构建文件。
- `docs/`
  放本手册和发布检查清单。
- `scripts/`
  放 Linux / Windows 服务控制脚本。
- `sql/`
  放模块 DDL / DML。
- `docker-compose.yml`
  主编排文件。
- `.env.example`
  运行参数模板。

## 4. 构建机要求

建议在构建机或 CI 上准备产物，要求：

- JDK 17
- Maven
- Node.js >= 20.15
- npm >= 8.19

### 4.1 构建后端

在 `Brain/` 目录执行：

```powershell
mvn -pl brain-admin -am -DskipTests package -P prod
mvn -pl brain-extend/brain-monitor-admin -am -DskipTests package -P prod
mvn -pl brain-extend/brain-snailjob-server -am -DskipTests package -P prod
```

将产物复制到：

- `artifacts/backend/brain-admin.jar`
- `artifacts/backend/brain-monitor-admin.jar`
- `artifacts/backend/brain-snailjob-server.jar`

### 4.2 构建前端

在 `plus-ui-ts/` 目录执行：

```powershell
npm install
npm run build:prod
```

将完整 `dist/` 目录内容复制到：

- `artifacts/frontend/dist/`

## 5. 服务器环境准备

### 5.1 Linux 服务器

先安装 Docker Engine 与 Docker Compose 插件。

高层步骤：

1. 安装 Docker Engine
2. 安装 Docker Compose 插件
3. 用 `docker --version` 和 `docker compose version` 验证

### 5.2 Windows 主机

Windows 脚本默认前提是该主机已经能运行 Linux 容器。

建议口径：

- 正式生产优先选 Linux 主机
- 若使用 Windows，建议 Docker Desktop 或等价的 Linux container 运行环境
- 同样先验证 `docker --version` 与 `docker compose version`

## 6. 上传发布包

把完整的 `Brain/ReleaseNotes/manuscript_review/v01` 上传到服务器，例如：

- Linux: `/opt/brain/manuscript_review_release_v01`
- Windows: `D:\brain\manuscript_review_release_v01`

上传后执行：

1. 复制 `.env.example` 为 `.env`
2. 编辑 `.env` 中的真实参数
3. 确认 `artifacts/` 下已经放好 jar 与前端 dist

## 7. `.env` 重点参数

至少要核对这些字段：

- `MYSQL_ROOT_PASSWORD`
- `REDIS_PASSWORD`
- `MINIO_ROOT_USER`
- `MINIO_ROOT_PASSWORD`
- `PUBLIC_BASE_URL`
- `ENABLE_MONITOR_ADMIN`
- `ENABLE_SNAIL_JOB`
- `MONITOR_USERNAME`
- `MONITOR_PASSWORD`
- `SNAIL_JOB_TOKEN`

说明：

- `ry_vue_5.X.sql` 默认会给 `sys_oss_config` 落一组 MinIO 配置，默认账号口径是 `brain / brain123`。如果 compose 也沿用这组配置，平台 OSS 默认口径就是一致的。
- 如果你修改了 MinIO 账号密码，记得在平台基础 SQL 执行后，把数据库里对应的 OSS 配置一并改掉。

## 8. 数据库初始化

### 8.1 空白数据库

按下面顺序执行：

1. `Brain/script/sql/ry_vue_5.X.sql`
2. `Brain/script/sql/ry_workflow.sql`
3. `Brain/script/sql/ry_job.sql`
4. `sql/01_manuscript_review_release_ddl.sql`
5. `sql/02_manuscript_review_release_dml.sql`

### 8.2 已有平台库，只缺三审三校模块

如果目标库已经是可运行的平台库，仅缺 `manuscript_review` 增量，则只执行：

1. `sql/01_manuscript_review_release_ddl.sql`
2. `sql/02_manuscript_review_release_dml.sql`

执行前先确认：

- workflow 引擎表已经存在
- 本模块使用的 `sys_menu`、`sys_role`、`sys_dict_type`、`sys_dict_data`、`sys_role_menu` 编号范围不与现有自定义数据冲突

## 9. 服务启动

### 9.1 Linux

```bash
chmod +x scripts/release-control-linux.sh
./scripts/release-control-linux.sh build minimal
./scripts/release-control-linux.sh start minimal
```

如果还要一并带起监控中心和 SnailJob：

```bash
./scripts/release-control-linux.sh build extended
./scripts/release-control-linux.sh start extended
```

### 9.2 Windows

```powershell
Set-ExecutionPolicy -Scope Process Bypass
.\scripts\release-control-windows.ps1 -Action build -Target minimal
.\scripts\release-control-windows.ps1 -Action start -Target minimal
```

如果还要一并带起监控中心和 SnailJob：

```powershell
.\scripts\release-control-windows.ps1 -Action build -Target extended
.\scripts\release-control-windows.ps1 -Action start -Target extended
```

## 10. 启动后验证

服务起来后按这个顺序核验：

1. `docker compose ps`
2. `docker compose logs --tail=200 brain-server`
3. 打开 `PUBLIC_BASE_URL` 对应的前端首页
4. 确认 `/prod-api/` 反向代理正常
5. 用有效平台账号登录
6. 确认三审三校菜单可见
7. 用真实角色验证创建、详情、审批链路

建议最小检查项：

- 前端首页返回 `200`
- 登录接口返回 `200`
- 角色绑定后能看到 `审校管理`
- `审核流程` 与 `校对流程` 都能发起
- 审批链能走到 `一级审批 -> 二级审批 -> 三级审批`
- 驳回链路能回到发起人
- 附件上传链路能写到配置好的 OSS

## 11. SQL 执行后的角色绑定

本发布 DML 会创建模块角色，但不会创建生产账号。

SQL 执行后需要补这一步：

1. 在平台中把真实用户绑定到这些角色
2. 确认发起人账号拥有 `manuscript_review_initiator`
3. 确认持证发起人账号额外拥有 `manuscript_review_certified_initiator`
4. 确认审批账号绑定到正确级别的审批角色

## 12. 停止、重启与日志

Linux:

```bash
./scripts/release-control-linux.sh status
./scripts/release-control-linux.sh logs minimal brain-server
./scripts/release-control-linux.sh restart minimal
./scripts/release-control-linux.sh down
```

Windows：

```powershell
.\scripts\release-control-windows.ps1 -Action status
.\scripts\release-control-windows.ps1 -Action logs -Service brain-server
.\scripts\release-control-windows.ps1 -Action restart -Target minimal
.\scripts\release-control-windows.ps1 -Action down
```

## 13. 回滚建议

建议按三层来准备回滚：

1. 应用回滚
   把发布目录中的 jar 和前端 dist 替换为上一个稳定版本，再重建并重启 compose 服务
2. 数据库回滚
   恢复模块 SQL 执行前的数据库备份
3. 对象存储回滚
   若本次改动了 MinIO/OSS 配置或导入了业务数据，则恢复对应对象存储备份

如果问题仅在代码或构建产物，且还没有写入不可逆业务数据，可以走最小回滚：

1. 停掉当前服务
2. 换回上一版 jar 与前端 dist
3. 重建并重启 compose 服务

## 14. 推荐生产形态

推荐：

- 生产运行主机用 Linux
- 运行层统一走 Docker Compose
- 构建产物在受控构建机或 CI 上生成

可选：

- 若监控中心和 SnailJob 本来就是生产基线，则启用 `extended` 组服务
