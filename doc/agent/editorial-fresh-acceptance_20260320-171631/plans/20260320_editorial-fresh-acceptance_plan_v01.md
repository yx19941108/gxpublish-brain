Owner: Codex
Task: editorial-fresh-acceptance
Status: approved_running
Updated: 2026-03-20 17:16:31 +08:00

# 测试方案文档

## 1. 测试概述

### 1.1 测试目标
对 gxpublish-brain editorial 审校主线执行 fresh 联调验收，优先验证历史展示/详情口径，再扩展到 12 项验收与补充回归；命中新 blocker 立即停。

### 1.2 测试范围
| 类别 | 内容 |
|------|------|
| **覆盖范围** | editorial review 列表/详情/修改/审批、附件/链接持久化、审批与修改历史、HP-01~05 回归、upload preview、草稿隔离、持证跳级、驳回、终止、撤销、非本人不可编辑、一个非 editorial 流程 transfer 抽样 |
| **不在范围** | 代码修改、controller 编排、非 blocker 后的继续扩矩阵 |
| **前置假设** | backend 127.0.0.1:9888 ready；frontend 127.0.0.1:9999 ready；PID 55324 对应 rain-admin.jar --spring.profiles.active=dev；账号矩阵按 zhangsan/lisi/wangwu/zhaoliu/qianqi 等现网数据可用 |

### 1.3 参考文档
- AGENTS.md
- doc/agent/editorial-fulltest-prep_20260319-111500/plans/20260319_editorial-fulltest-prep_plan_v02.md
- doc/agent/editorial-mainline-closure_20260318-184705/reports/20260319_editorial-mainline-closure_runtime-progress_report_v03.md
- doc/agent/editorial-fulltest-exec_20260319-113303/handoffs/20260320_editorial-fulltest-exec_hp03-blocker_handoff_v02.md

## 2. 测试环境

### 2.1 服务配置
| 服务 | 地址 | 备注 |
|------|------|------|
| Backend | http://127.0.0.1:9888 | JDK17 fat jar, PID 55324 |
| Frontend | http://127.0.0.1:9999 | dev server |
| MySQL | localhost:3306 / gxpublish_brain | MCP 默认库非本业务库，查询需显式 gxpublish_brain.* |
| Redis | localhost:6379 | 仅做轻量会话/路由辅助检查 |

### 2.2 测试数据准备
- fresh 申请人：zhangsan
- fresh 审批人：wangwu / zhaoliu / qianqi
- 可选持证回归：lisi
- 非 editorial transfer 抽样：leave1 流程现存任务

## 3. 测试流程
1. 门禁 spot-check
2. fresh 登录 + 新建 1 条 editorial 样本（含附件、链接）
3. 优先验证第 1/2/3/8/10 项
4. 若通过，继续第 4/5/6/7/9/11/12 项
5. 若仍通过，继续补充回归包与非 editorial transfer 抽样
6. 任一 blocker 命中立即停并出正式报告

## 4. 测试策略 — MCP & Skill 依赖
| 测试类型 | 工具 / MCP | 用途 |
|----------|-----------|------|
| 前端 UI 测试 | Playwright 本地脚本 + webapp-testing skill | 页面交互、截图、网络/控制台取证 |
| 数据库验证 | mcp__mysql__execute_sql | 核对审校/流程/附件/历史落库 |
| 缓存验证 | mcp__redis__list | 轻量确认会话/任务相关缓存现状 |
| 深度分析 | mcp__sequential-thinking__sequentialthinking | 执行顺序与 blocker 面推导 |

## 5. 测试用例
| 用例 ID | 场景描述 | 前置条件 | 测试步骤 | 预期结果 | 优先级 |
|---------|---------|---------|---------|---------|-------|
| QA-01 | 历史首批关键项 | 服务 ready、账号可登录 | fresh 创建/提交/审批并核对历史、详情 | 第 1/2/3/8/10 项全部通过 | P0 |
| QA-02 | 编辑页限制与持久化 | QA-01 通过 | 打开 update 页并保存 | 第 4/5/6/7/9/11/12 项通过 | P0 |
| QA-03 | 主线回归包 | QA-02 通过 | 跑 HP/NR 样本与非 editorial transfer 抽样 | 既有通过项不回退，shared transfer 未误伤 | P0 |

## 6. 风险评估
| 风险项 | 影响 | 缓解措施 |
|--------|------|---------|
| 历史文案/roleName 仍有旧数据或旧 jar 痕迹 | 可能在 QA-01 即阻断 | 以 fresh 样本+截图+SQL 三重证据判定 |
| MySQL MCP 默认库不是 gxpublish_brain | 可能误读数据 | 全部 SQL 显式写 gxpublish_brain.* |
| 角色并发登录态污染 | 可能误判按钮/权限 | 独立浏览器上下文/存储状态 |

## 7. 时间估算
| 阶段 | 预计耗时 |
|------|---------|
| 环境 & 数据准备 | 10-15 分钟 |
| 历史关键项验证 | 15-25 分钟 |
| 编辑/持久化验证 | 15-25 分钟 |
| 回归与抽样 | 20-30 分钟 |
| 报告输出 | 5-10 分钟 |
