Owner: Codex
Task: manuscript_review release package
Status: Draft
Updated: 2026-04-01

# 三审三校 v01 发布包说明

本目录是 `manuscript_review` 模块的发布交付包。

目录内容：

- `sql/01_manuscript_review_release_ddl.sql`
  三审三校模块表结构 SQL。
- `sql/02_manuscript_review_release_dml.sql`
  三审三校模块角色、菜单、字典、BPM 流程定义与权限收敛 SQL。
- `docker-compose.yml`
  面向空白服务器的发布版容器编排文件。
- `.env.example`
  运行参数模板，复制为 `.env` 后按环境填写。
- `docker/`
  后端、监控中心、SnailJob、前端镜像构建文件。
- `config/nginx/nginx.conf`
  前端静态资源与后端反向代理配置。
- `config/redis/redis.conf`
  Redis 运行配置。
- `scripts/release-control-linux.sh`
  Linux 服务启停脚本。
- `scripts/release-control-windows.ps1`
  Windows 服务启停脚本。
- `docs/RELEASE_GUIDE.md`
  从 0 到 1 的发布操作手册。
- `docs/RELEASE_CHECKLIST.md`
  发布执行与验收检查清单。
- `artifacts/README.md`
  构建产物放置说明。

推荐执行顺序：

1. 先看 `docs/RELEASE_GUIDE.md`。
2. 先在构建机准备好 `artifacts/` 下的后端 jar 与前端 dist。
3. 复制 `.env.example` 为 `.env`，填入真实环境参数。
4. 若目标库为空白平台库，先执行平台基础 SQL：
   `Brain/script/sql/ry_vue_5.X.sql` -> `Brain/script/sql/ry_workflow.sql` -> `Brain/script/sql/ry_job.sql`
5. 再执行本模块 SQL：
   `sql/01_manuscript_review_release_ddl.sql` -> `sql/02_manuscript_review_release_dml.sql`
6. 最后用 `scripts/` 下的系统对应脚本启动服务。
