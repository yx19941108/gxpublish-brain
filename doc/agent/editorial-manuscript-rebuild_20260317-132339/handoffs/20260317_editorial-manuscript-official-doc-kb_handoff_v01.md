Owner: Codex
Task: editorial-manuscript-rebuild
Status: Ready For Next Session
Updated: 2026-03-17

# 审校模块重构官方文档知识库阶段 handoff

## 1. 本轮已完成

1. 未启用 `architect` / `project_reviewer`
2. 已在本仓落盘官方文档知识库：
   - `doc/official_doc/ruoyi-vue-plus-plus-doc/`
   - `doc/official_doc/warm-flow-doc/`
3. 已补充说明文件：
   - `doc/official_doc/README.md`
4. 已补充本轮报告：
   - `reports/20260317_editorial-manuscript-official-doc-kb_report_v01.md`

## 2. 关键事实

1. `plus-doc` 采用官方 docs repo 快照落盘
2. `warm-flow` 官方站点在当前 shell 上存在 TLS/连接重置问题
3. 但 `warm-flow` 官方 `README.md` 已明确指向 `warm-flow-doc.git` 作为本地部署文档源
4. 因此本轮 `Warm-Flow` 知识库采用 `warm-flow-doc.git`，不是非官方替代源

## 3. 下一会话建议读取顺序

1. `doc/official_doc/README.md`
2. `doc/official_doc/ruoyi-vue-plus-plus-doc/ruoyi-vue-plus/home.md`
3. `doc/official_doc/warm-flow-doc/src/master/introduction/introduction.md`
4. `doc/agent/editorial-manuscript-rebuild_20260317-132339/reports/20260317_editorial-manuscript-rebuild-mapping_report_v01.md`
5. `doc/agent/editorial-manuscript-rebuild_20260317-132339/plans/20260317_editorial-manuscript-rebuild-implementation_plan_v01.md`
6. `doc/agent/editorial-manuscript-rebuild_20260317-132339/reports/20260317_editorial-manuscript-rebuild-execution_report_v01.md`

## 4. 下一会话目标

1. 基于本地知识库和真实代码重新审视当前全量重构方案
2. 再启用 `architect` / `project_reviewer` 做里程碑评审
3. 若评审结论一致，落 review / decision
4. 若评审结论冲突，先汇总冲突点、推荐方案、风险和取舍，等待用户拍板

## 5. 注意事项

1. 不要把 `doc/official_doc/` 本地快照误当成最新在线真理
2. 任何关键结论仍需回到 `gxpublish-brain` 真实代码核对
3. 未完成方案拍板前，不要进入 worktree + 多 agent 编码阶段
