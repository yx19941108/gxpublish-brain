---
Owner: Codex
Task: editorial-fulltest-prep
Status: reviewed
Updated: 2026-03-19 10:40:37 +08:00
---

# 审校主线全量测试方案评审 v01

## 已确认

1. `business_qa_lead` 已产出测试方案与测试用例，并已整理为：
   - `plans/20260319_editorial-fulltest-prep_plan_v01.md`
   - `plans/20260319_editorial-fulltest-prep_plan_v02.md`
   - `plans/20260319_editorial-fulltest-prep_plan_v02.xlsx`
2. `architect` 与 `project_reviewer` 评审口径一致：
   - 当前方案没有把 `SQL_APPLIED / REAL_HTTP_READY / BROWSER_EVIDENCED / 可测试` 与 build/lint 混写
   - 浏览器级最低覆盖项定义完整，已覆盖登录、submit、TUS finish、`10 -> 20 -> 30 -> 40`、back、cancel、termination、resubmit、fallback、detail/history
3. 已按评审意见把以下缺口收进 `plan_v02`：
   - 新增 `G-00` 基线指纹门禁
   - 将 `G-03` 收紧为 5 类账号全部登录通过
   - 将 `G-05` 升级为 `upload -> patch -> finish` 最小冒烟
   - 补充状态断言映射
   - 明确旧 evidence 只可作为 baseline 参考，不可替代本会话 fresh 证据
   - 补充账号/样本/证据绑定清单

## 风险

1. `plan_v02` 虽已完成评审收口，但仍是 `reviewed_pending_input`，还不能直接当作“下会话立即开跑”的无条件执行基线。
2. 当前真正阻断下会话执行的不是用例覆盖，而是输入未锁定：
   - 5 类账号
   - 登录方式
   - 所属租户
   - 关键权限
   - 主链样本绑定
3. 若下会话在未补齐上述输入前直接执行，最容易出现假失败、串样本、旧证据冒充 fresh 证据三类误判。

## 待确认

1. 发起人、持证发起人、一级审批、二级审批、三级审批的真实账号与登录方式。
2. 是否强制不同角色使用独立浏览器上下文或独立隐身窗口。
3. 是否要求每条关键链路绑定：
   - `reviewId`
   - `applyCode`
   - `attachmentId`
   - evidence 路径
4. 权限争议场景是否允许补看 `flow_*` 运行数据。

## Decision

1. 评审结论不是“方案不合格”，而是“方案主体通过，但执行输入未锁定”。
2. 当前推荐作为下会话入口的正式测试工件是：
   - `plans/20260319_editorial-fulltest-prep_plan_v02.md`
   - `plans/20260319_editorial-fulltest-prep_plan_v02.xlsx`
3. 在你补齐 5 类账号与绑定信息之前，最终口径应为：
   - `测试方案已通过评审`
   - `执行基线待输入锁定后放行`
