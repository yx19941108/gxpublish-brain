# manuscript_review UI 设计稿说明

- Owner: UI Designer
- Task: manuscript_review 4 张管理页风格 UI 设计稿
- Status: Draft for controller screenshot export
- Updated: 2026-03-26 15:05 +08:00

## 本目录产物

- `ledger.html`：业务台账列表页设计稿
- `create.html`：新增页设计稿
- `edit.html`：修改页设计稿
- `detail.html`：详情页设计稿
- `style.css`：4 张页面共用样式

> 本轮按游工最新指令，优先生成可直接在浏览器打开、可截图的 HTML 设计稿；PNG 由主控侧用 headless Chrome 补齐。

## 设计基线

仅按以下文档口径收敛，不以现存失败实现或旧页面业务语义反推：

1. `R6.28_前后端执行基线-新增提交一体化.md`
2. `R6.26_冻结契约包-新增提交一体化.md`
3. `R6.27_测试方案及结论-新增提交一体化.md`
4. `product/20260321_manuscript-review_product-requirements_v01.md`
5. 低版本文档仅作补充；若冲突，以版本号更大者为准

## 已锁定的硬约束

1. 不参考任何旧失败实现
2. 不以 `plus-ui-ts/src/views/manuscript-review/**` 或 `editorial/**` 的业务语义反推设计
3. `brain-manuscript-review` 仅作为未来代码落点，不作为实现基线
4. 本轮只做 UI 设计图，不做前端实现、不改冻结契约
5. 视觉语言仅提取现有系统管理页样式结构：搜索区、卡片、表格、表单分区、Element Plus 后台密度、按钮层级

## 四张图对应页面职责

### 1. `ledger.html`

- 只体现：搜索 / 筛选 / 状态查看 / 进详情
- 行内默认且唯一动作：`详情`
- 禁止出现：`修改 / 去审批 / 撤销 / 重新提交` 等多动作入口

### 2. `create.html`

- 只体现新增发起
- 正式业务动作只有：`提交`
- 上传只做暂存展示
- 未提交前可删除暂存附件
- 不画：草稿、保存并提交、提交后审批

### 3. `edit.html`

- 只体现资料修改
- 正式业务动作只有：`保存`
- 保存后回详情
- 不推 BPM
- 不画：保存并提交、保存后审批、去审批、重新提交

### 4. `detail.html`

- 详情页是动作中枢
- 固定承载：状态摘要、基本信息、当前有效资源区、统一时间线、基于 `permissionMatrix` 的按钮分流
- 审批不在详情页内完成，只能 `去审批` 跳 BPM 办理页
- `重新提交` 只在详情动作层体现，不混入修改页主提交区

## 当前未扩写项 / blocker 处理

### 无阻塞出图 blocker

当前文档足以确定四页最小信息架构与视觉承载边界。

### 已主动收缩的复杂能力

为避免超出本轮最小闭环：

- 视频能力仅以“视频列表 + 单播放器”信息块表现
- 标注能力仅在资源区作为信息块预留
- 不扩写成复杂交互原型、拖拽时间轴或页内审批原型

## 主控侧补 PNG 方法

以下命令可在本机直接把 HTML 输出为 PNG；路径已按游工补充的 Chrome 路径写死：

```powershell
& 'C:\Program Files\Google\Chrome\Application\chrome.exe' `
  --headless `
  --disable-gpu `
  --window-size=1440,1400 `
  --screenshot='C:\kuguaHome\project\gxpublish-brain\doc\manuscript_review\uipic\ledger.png' `
  'file:///C:/kuguaHome/project/gxpublish-brain/doc/manuscript_review/uipic/ledger.html'
```

同理替换为：

- `create.html -> create.png`
- `edit.html -> edit.png`
- `detail.html -> detail.png`

建议统一使用：

- 视口：`1440 x 1400`
- 浏览器缩放：默认
- 截图前等待页面静态样式加载完成（本稿仅本地 CSS，无外网依赖）

## 产物自检要点

1. 列表页是否只有 `详情` 行内动作
2. 新增页是否只有 `提交` 为正式业务动作
3. 修改页是否只有 `保存` 为正式业务动作
4. 详情页是否仅做按钮分流、未页内审批
5. 详情页动作是否显式绑定 `permissionMatrix` 语义
6. 视觉是否呈现“管理页语言”，而不是旧 manuscript/editorial 页面布局语义
