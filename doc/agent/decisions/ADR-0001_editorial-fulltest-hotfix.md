Owner: Codex
Task: editorial-fulltest-hotfix
Status: Accepted
Updated: 2026-03-03

# ADR-0001: 三审三校全量测试阻断项修复方案

## 背景
- 全量测试结果：52 条，48 通过，4 失败。
- 阻断问题（P1）：
  - 中间审批阶段 `review_status` 未按 `10 -> 20 -> 30` 推进。
  - 文件上传 `POST /tus/upload/finish` 返回业务失败，导致上传与预览链路不可用。
- 约束：
  - 保留测试数据，不做清理。
  - 验收规则：P0/P1 阻断。

## 决策
1. `review_status` 中间态同步采用 `ProcessTaskEvent` 驱动
- 在 `EditorialReviewServiceImpl` 新增 `ProcessTaskEvent` 监听。
- 基于 `nodeCode/nodeName` 对应更新 `review_status`：
  - 一级审批节点 -> 10
  - 二级审批节点 -> 20
  - 三级审批节点 -> 30
- 现有 `ProcessEvent` 监听只负责终态同步（`finish/back/cancel/termination`），避免中间态被回写为 10。

2. 上传完成回调增强可诊断性并改为流式转存
- 在 `TusController.finishUpload` 中细化异常分类，返回可定位错误信息，避免统一泛化为系统错误。
- 在 `SysOssTusUploadStrategy.finishUpload` 中避免全量 `readBytes`，改为流式落临时文件再调用 OSS 上传。
- 前端 `TusUpload.vue` 显式设置 `chunkSize`（建议 5MB），确保可观测分片上传。

3. `uploadUrl` 规范化
- 前端传给 `finish` 的 `uploadUrl` 固化为 `/tus/upload/{id}` 形式。
- 后端保留兼容逻辑（可接受全路径并截断）。

## 验收标准
1. 流程状态
- `AUDIT` 与 `PROOFREAD` 全链路中间态必须命中：
  - 提交后 10
  - 一审后 20
  - 二审后 30
  - 终审后 40

2. 上传能力
- 分片：观察到多次 PATCH（由 `chunkSize` 触发）。
- 续传：中断后 `HEAD` 返回非 0 `upload-offset`，续传成功。
- 预览：`finish` 返回有效 `ossId/url`，预览可打开。

3. 回归
- 保持已通过项不回退：
  - 草稿隔离
  - 持证跳级
  - 驳回/撤销/终止
  - 非本人不可编辑

## 实施顺序
1. 先修 `review_status` 中间态同步（后端）。
2. 再修 TUS `finish` 链路（后端 + 前端）。
3. 全量回归复测（含文件专项取证）。

## 回滚策略
- 后端：
  - `ProcessTaskEvent` 新增监听可独立回滚，不影响既有接口契约。
  - TUS 改造保持接口路径不变，若异常可回退到旧实现。
- 前端：
  - `chunkSize` 与 `uploadUrl` 规范化为小改动，可独立回滚。

## 影响面
- 后端：
  - `brain-editorial` 事件处理
  - `brain-common-tus` 完成回调
  - `brain-system` OSS 转存策略
- 前端：
  - `plus-ui-ts` 审校上传组件

## 关联测试产物
- 计划文档：`doc/agent/plans/20260302_editorial-fulltest_plan_v01.md`
- 测试报告：`doc/agent/reports/20260302_editorial-fulltest_report_v01.md`
- 自动化结果：`.agent-temp/editorial_api_results.json`
