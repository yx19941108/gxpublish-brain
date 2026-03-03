Owner: Codex
Task: editorial-fulltest
Status: Completed
Updated: 2026-03-02

# 三审三校全量测试报告（本地联调）

## 1. 执行概览

- 执行时间：2026-03-02
- 执行环境：本地联调（后端 `9888`、前端 `9999`）
- 账号矩阵：`zhangsan/lisi/wangwu/sunba/zhaoliu/zhoujiu/qianqi/wushi`
- 覆盖范围：`AUDIT`、`PROOFREAD`、权限可见性、驳回/撤销/终止、文件上传专项（分片/续传/预览）
- 不覆盖：性能压测

## 2. 结果汇总

- API/流程自动化：`52` 条，`48` 通过，`4` 失败
- 文件上传专项（UI + Network）：
  - 分片链路：触发 `POST /dev-api/tus/upload`、`PATCH /tus/upload/{id}`，但未观察到多 PATCH 分片（单次大 PATCH）
  - 断点续传：已验证可恢复到非 0 偏移继续上传（通过）
  - 预览：受上传完成回调失败阻塞，无法形成有效预览（失败）

阻断结论：存在 `P1` 缺陷，按默认规则本轮 **阻断验收**。

## 3. 关键通过项

- 登录与多账号会话正常（8 账号全部登录成功）
- 发起人草稿隔离正确（`zhangsan` 与 `lisi` 互不可见对方草稿）
- `AUDIT` 主链可走通到完成（一二三审通过后 `review_status=40`）
- `PROOFREAD` 主链可走通到完成（`processType` 保持 `PROOFREAD`）
- 持证发起人跳过一级审批生效（首审不可见、二审可见）
- 驳回/终止/撤销能力可用（含状态落库）
- 编辑权限约束生效（待审不可改、非本人不可改）

## 4. 失败项与缺陷

### D-001（P1）`review_status` 在中间审批阶段未正确推进

- 现象：
  - `AUDIT`：一级通过后应到 `20`，实际仍 `10`；二级通过后应到 `30`，实际仍 `10`
  - `PROOFREAD`：同样在二级/三级前保持 `10`
- 自动化失败用例：
  - `WF-A-STATUS-20`
  - `WF-A-STATUS-30`
  - `WF-P-STATUS-20`
  - `WF-P-STATUS-30`
- 复现路径：
  1. 发起审校单并提交
  2. 一审通过后查询 `/editorial/review/{id}`
  3. 继续二审通过后再次查询
- 实际结果：`status=waiting`，但 `review_status` 未随节点推进（保持 `10`）
- 证据：
  - 自动化结果文件：`.agent-temp/editorial_api_results.json`
  - 数据库记录（示例）：
    - `audit-*`、`proof-*` 最终可到 `40`，但中间阶段断言失败
  - 相关代码：
    - [EditorialReviewServiceImpl.java:395](C:/kuguaHome/project/gxpublish-brain/Brain/brain-modules/brain-editorial/src/main/java/com/gxpublish/brain/editorial/service/impl/EditorialReviewServiceImpl.java:395)
    - [EditorialReviewServiceImpl.java:397](C:/kuguaHome/project/gxpublish-brain/Brain/brain-modules/brain-editorial/src/main/java/com/gxpublish/brain/editorial/service/impl/EditorialReviewServiceImpl.java:397)
    - [EditorialReviewServiceImpl.java:399](C:/kuguaHome/project/gxpublish-brain/Brain/brain-modules/brain-editorial/src/main/java/com/gxpublish/brain/editorial/service/impl/EditorialReviewServiceImpl.java:399)
- 初步判断：
  - 审批推进事件触发时机/事件类型与 `processHandler` 当前监听逻辑存在偏差，导致中间节点状态同步不稳定。

### D-002（P1）文件上传完成回调失败，上传不可用并阻断预览

- 现象：
  - `tus` 会话创建与 PATCH 上传成功，但 `finish` 返回业务失败
- 复现路径：
  1. 打开 `reviewEdit` 新增页
  2. 选择文件上传
  3. 观察网络请求与控制台
- 实际结果：
  - `POST /dev-api/tus/upload` -> `201`
  - `PATCH /tus/upload/{id}` -> `204`
  - `POST /dev-api/tus/upload/finish` -> HTTP `200`，响应体 `{"code":500,"msg":"System error during upload finalization"}`
  - 控制台报错：`上传完成回调出错: Error: System error during upload finalization`
- 证据：
  - 网络请求 `reqid=1067/1068/1069`
  - 控制台消息 `msgid=36/37`
  - 上传组件实现：
    - [TusUpload.vue:198](C:/kuguaHome/project/gxpublish-brain/plus-ui-ts/src/views/editorial/review/components/TusUpload.vue:198)
    - [TusUpload.vue:201](C:/kuguaHome/project/gxpublish-brain/plus-ui-ts/src/views/editorial/review/components/TusUpload.vue:201)
- 影响：
  - 无法获得有效 `ossId/url`，附件回显与预览流程被阻断。

### D-003（P2）“分片上传”未观察到多分片请求（仅单 PATCH 流）

- 现象：
  - 8MB 文件仅 1 次 PATCH（`content-length` 为整文件）
  - 64MB 文件在慢网下同样是单大 PATCH，暂停后通过偏移续传继续同一对象
- 说明：
  - 当前实现具备 tus 可续传语义，但未配置客户端固定 `chunkSize`，因此无多分片请求证据。
- 证据：
  - `reqid=1068`（8MB，单 PATCH）
  - `reqid=1071`（初始 PATCH aborted）
  - `reqid=1072`（HEAD 返回 `upload-offset=311296`）
  - `reqid=1073`（续传 PATCH，`upload-offset=311296`）
- 相关代码：
  - [TusUpload.vue:248](C:/kuguaHome/project/gxpublish-brain/plus-ui-ts/src/views/editorial/review/components/TusUpload.vue:248)
  - [TusUpload.vue:250](C:/kuguaHome/project/gxpublish-brain/plus-ui-ts/src/views/editorial/review/components/TusUpload.vue:250)

## 5. 数据层核验摘录

- 审校主表存在并可写入：`brain_editorial_review`
- 本轮样本状态符合已通过链路：
  - `audit-*`、`proof-*`：`status=finish, review_status=40`
  - `cert-*`：`finish, 40`（持证跳级链路）
  - `term-*`：`termination, 60`
  - `cancel-*`：`cancel, 70`
  - `back-*`：`back, 50`
- 说明：中间阶段状态推进断言失败仅发生在“等待态细粒度状态更新”。

## 6. 风险与建议

- 阻断项优先级：
  1. 修复上传 `finish` 失败（D-002）
  2. 修复中间审批阶段 `review_status` 同步（D-001）
- 优化项：
  1. 若业务要求“真实分片”，在 Tus 客户端显式配置 `chunkSize`
  2. 增加自动化断言：每节点推进后必须命中 `10->20->30->40`

## 7. 产物清单

- 自动化结果：`.agent-temp/editorial_api_results.json`
- 自动化脚本：`.agent-temp/editorial_fulltest_api.py`
- 上传页面截图：`.agent-temp/upload_page_after_cancel.png`
- 本报告：`doc/agent/reports/20260302_editorial-fulltest_report_v01.md`
