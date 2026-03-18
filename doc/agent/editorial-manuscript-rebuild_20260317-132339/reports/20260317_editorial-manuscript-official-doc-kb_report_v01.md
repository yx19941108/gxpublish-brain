Owner: Codex
Task: editorial-manuscript-rebuild
Status: Completed
Updated: 2026-03-17

# 审校模块重构官方文档知识库落盘报告

## 1. 目标

把下一会话设计评审需要优先阅读的官方文档，先落到本仓 `doc/official_doc/` 下，作为本地知识库快照。

本轮不启用 `architect` / `project_reviewer`，只完成知识库准备与恢复入口固化。

## 2. 已确认

### 2.1 RuoYi-Vue-Plus 文档源

1. 官方站点入口：
   - `https://plus-doc.dromara.org/#/ruoyi-vue-plus/home`
2. 站点结构已验证可抓：
   - 首页可访问
   - `_sidebar.md` 可直接获取
3. 本地落盘来源：
   - `https://github.com/JavaLionLi/plus-doc.git`
4. 已落盘目录：
   - `doc/official_doc/ruoyi-vue-plus-plus-doc/`

### 2.2 Warm-Flow 文档源

1. 官方站点入口：
   - `https://warm-flow.dromara.org/master/introduction/introduction.html`
2. 当前机器上的 shell 直连抓取事实：
   - `Invoke-WebRequest` / `curl` / `requests` 都遇到 TLS EOF 或连接重置
3. 但站点内容并非不存在：
   - 浏览器式 `web` 打开已验证 introduction 页面可访问
4. 上游官方 fallback 已确认：
   - `warm-flow` 官方 `README.md` 明确指向 `https://gitee.com/warm_4/warm-flow-doc.git` 作为本地部署文档源
5. 因此本轮采用的本地知识库来源为：
   - `https://gitee.com/warm_4/warm-flow-doc.git`
6. 已落盘目录：
   - `doc/official_doc/warm-flow-doc/`

### 2.3 本地知识库结果

1. 根说明文件：
   - `doc/official_doc/README.md`
2. 已验证入口文件存在：
   - `doc/official_doc/ruoyi-vue-plus-plus-doc/ruoyi-vue-plus/home.md`
   - `doc/official_doc/warm-flow-doc/src/master/introduction/introduction.md`
3. Markdown 文件数量：
   - `ruoyi-vue-plus-plus-doc`: `152`
   - `warm-flow-doc`: `264`

## 3. 风险

1. 本地知识库是快照，不是永久真理源。
2. `Warm-Flow` 站点在当前 shell 上存在连接层问题，因此本轮落盘依赖官方 docs repo，而不是直接镜像站点 HTML。
3. 本地知识库只能作为“下轮思考前的阅读基线”，不能替代对 `gxpublish-brain` 真实代码的核对。

## 4. 建议的下轮使用方式

1. 先读：
   - `doc/official_doc/ruoyi-vue-plus-plus-doc/ruoyi-vue-plus/home.md`
   - `doc/official_doc/warm-flow-doc/src/master/introduction/introduction.md`
2. 再回到任务包：
   - mapping report
   - implementation plan
   - execution report
3. 最后结合真实代码进入 `architect` / `project_reviewer` 里程碑评审。

## 5. 一句话结论

下轮评审所需的官方框架知识库已经在本仓落盘完成；后续可以直接从 `doc/official_doc/` 和任务包恢复，而不必再从聊天里重新拼接官方文档上下文。
