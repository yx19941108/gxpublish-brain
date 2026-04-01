Owner: Codex
Task: manuscript_review release artifacts
Status: Draft
Updated: 2026-04-01

# 发布产物放置说明

在构建镜像或启动服务前，先把发布产物放到本目录。

必须准备的文件：

- `backend/brain-admin.jar`
- `backend/brain-monitor-admin.jar`
- `backend/brain-snailjob-server.jar`
- `frontend/dist/*`

推荐构建入口：

- 后端目录：`cd Brain`
- 前端目录：`cd plus-ui-ts`

具体构建命令见 `../docs/RELEASE_GUIDE.md`。
