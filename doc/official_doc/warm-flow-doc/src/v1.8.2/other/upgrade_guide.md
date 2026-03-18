# 升级指南

### 注意事项
::: warning
- 更新脚本在项目里面的sql文件下，对应数据库类型，对应版本号
- 只提供mysql升级脚本和全量脚本，其他升级脚本的自行由mysql升级脚本转换
- 如果二开设计器，请自行手动同步
- 未提到的版本号升级，就只需要改动jar包版本号，如v1.7.6 --> v1.7.7

:::

### v1.8.2
- 执行升级脚本1.8.2版本[warm-flow_1.8.2.sql](https://gitee.com/dromara/warm-flow/blob/master/sql/mysql/v1-upgrade/warm-flow_1.8.2.sql)
- 如果二开设计器，请自行手动同步，因为本次改动比较多，就不一一列举，参考工作流引擎源码中`warm-flow/warm-flow-ui`文件夹的提交记录，提交时间范围：`2025/8/24 17:51 ~ 2025/10/9 22:50`

### v1.8.1
- 如果二开设计器，请自行手动同步，因为本次改动比较多，就不一一列举，参考工作流引擎源码中`warm-flow/warm-flow-ui`文件夹的提交记录，提交时间范围：`2025/8/6 11:19 ~ 2025/8/24 17:51`

### v1.8.0
- 执行升级脚本1.8.0版本[warm-flow_1.8.0.sql](https://gitee.com/dromara/warm-flow/blob/master/sql/mysql/v1-upgrade/warm-flow_1.8.0.sql)
- 访问设计器时传入的disable变量可以不用传，内部会通过发布状态自行判断
- 如果要单独访问流程设计，不显示基础信息，可以在访问设计器的时候传入参数`&onlyDesignShow=true`
- 如果二开设计器，请自行手动同步，因为本次改动比较多，就不一一列举，参考工作流引擎源码中`warm-flow/warm-flow-ui`文件夹的提交记录，提交时间范围：`2025/6/18 14:53 ~ 2025/8/6 11:19`

### v1.7.6
- 执行升级脚本1.7.6版本[warm-flow_1.7.6.sql](https://gitee.com/dromara/warm-flow/blob/master/sql/mysql/v1-upgrade/warm-flow_1.7.6.sql)
- 如果二开设计器，请自行手动同步，因为本次改动比较多，就不一一列举，参考工作流引擎源码中`warm-flow/warm-flow-ui`文件夹的提交记录，提交时间范围：`2025/5/28 11:50 ~ 2025/6/18 14:53`

### v1.7.4
- 执行升级脚本1.7.4版本[warm-flow_1.7.4.sql](https://gitee.com/dromara/warm-flow/blob/master/sql/mysql/v1-upgrade/warm-flow_1.7.4.sql)
- 如果二开设计器，请自行手动同步，因为本次改动比较多，就不一一列举，参考工作流引擎源码中`warm-flow/warm-flow-ui`文件夹的提交记录，提交时间范围：`2025/5/28 11:50 ~ 2025/6/18 14:53`

### v1.7.3
- 业务系统涉及到/warm-flow-ui/token-name改成/warm-flow-ui/config，比如匿名访问等
- 如果二开设计器，请自行手动同步，因为本次改动比较多，就不一一列举，参考工作流引擎源码中`warm-flow/warm-flow-ui`文件夹的提交记录，提交时间范围：`2025/5/27 11:50 ~ 2025/5/27 11:50`
- 查询流程图接口，请使用新版本替换，[前端渲染流程图](../primary/chart_manage.html)


### v1.7.2
- 执行升级脚本1.7.2版本[warm-flow_1.7.2.sql](https://gitee.com/dromara/warm-flow/blob/master/sql/mysql/v1-upgrade/warm-flow_1.7.2.sql)
- mybatis-plus逻辑删除强制设置为（未删除值：0，删除值：1），如果流程表的逻辑删除字段不是这个，请刷库修改为为这个
- 如果二开设计器，请自行手动同步，参考如下：

::: tip 原between.vue：
```vue {3}
<el-table-column label="操作" width="55" v-if="!disabled">
  <template #default="scope">
  <el-button type="danger" v-if="form.permissionFlag.length !== 1 && !disabled" :icon="Delete" @click="delPermission(scope.$index)"/>
</template>
</el-table-column>
```
:::

::: tip 现between.vue：
```vue {3}
<el-table-column label="操作" width="55" v-if="!disabled">
  <template #default="scope">
  <el-button type="danger" v-if="!disabled" :icon="Delete" @click="delPermission(scope.$index)"/>
</template>
</el-table-column>
```
:::

::: tip 原skip.vue：
```js {3}
watch(() => form, n => {
  n = n.value;
  let skipCondition = '';
  skipCondition = n.conditionType + "@@";
  if (!/^spel/.test(n.conditionType) && !/^default/.test(n.conditionType)) {
    skipCondition = skipCondition
            + (n.condition ? n.condition : '') + "|";
  }
  n.skipCondition = skipCondition
          + (n.conditionValue ? n.conditionValue : '')
  if (n) {
    emit('change', n)
  }
}, {deep: true});
```
:::

::: tip 现skip.vue：
```js {3}
watch(() => form, n => {
  n = n.value;
  if (n.conditionType) {
    let skipCondition;
    skipCondition = n.conditionType + "@@";
    if (!/^spel/.test(n.conditionType) && !/^default/.test(n.conditionType)) {
      skipCondition = skipCondition
              + (n.condition ? n.condition : '') + "|";
    }
    n.skipCondition = skipCondition
            + (n.conditionValue ? n.conditionValue : '')
    if (n) {
      emit('change', n)
    }
  }

}, {deep: true});
```
:::

::: tip 原selectUser.vue：
```js
const tabsValue = ref("用户");

    ....

/** 获取tabs列表 */
function getTabsType() {
  handlerType().then(res => {
    tabsList.value = res.data;
  });
}

    ....

/** 查询用户列表 */
function getList() {
    loading.value = true;
    ....
}

```
:::

::: tip 现selectUser.vue：
```js
const tabsValue = ref("");

    ....

/** 获取tabs列表 */
function getTabsType() {
  handlerType().then(res => {
    tabsList.value = res.data;
    if(res.data && res.data.length > 0) {
      tabsValue.value = res.data[0]
    }
    getList();
  });
}

    ....

/** 查询用户列表 */
function getList() {
    loading.value = true;
    if (!tabsValue.value) {
        loading.value = false;
        return;
    }
    ....
}
```
:::

### v1.7.0
- 执行升级脚本1.7.0版本[warm-flow_1.7.0.sql](https://gitee.com/dromara/warm-flow/blob/master/sql/mysql/v1-upgrade/warm-flow_1.7.0.sql)
- 设计器原办理人输入改成列表，使用`HandlerSelectService.handlerFeedback`接口回显名称，他内部是利用`HandlerSelectService.getHandlerSelect`接口，但是性能会差，如果有特别要求 ，可以重写`HandlerSelectService.handlerFeedback`，[重写文档](/master/primary/designerIntroduced.html#_5-设计器办理人列表回显)
<div><img src="https://foruda.gitee.com/images/1745570346631861131/f5ba4bf7_2218307.png" width="500"></div>

- InsService的skipByInsId接口标识为即将删除，请使用TaskService.skipByInsId代替
- InsService的termination接口标识为即将删除，请使用TaskService.terminationByInsId代替
- 如果二开设计器，请自行手动同步，参考如下：

::: tip 原between.vue：`["serial", "parallel"]`
```js {5}
const filteredNodes = computed(() => {
  let skipList = props.skips.filter(skip => skip.properties.skipType === "PASS");

  let previousCode = getPreviousCode(skipList, form.value.nodeCode)
  return props.nodes.filter(node => !["serial", "parallel"].includes(node.type)
      && previousCode.includes(node.id)).reverse();
});
```
:::

::: tip 现between.vue：改成`["start", "serial", "parallel"]`，驳回指定节点下拉框排除开始节点
```js {5}
const filteredNodes = computed(() => {
  let skipList = props.skips.filter(skip => skip.properties.skipType === "PASS");

  let previousCode = getPreviousCode(skipList, form.value.nodeCode)
  return props.nodes.filter(node => !["start", "serial", "parallel"].includes(node.type)
      && previousCode.includes(node.id)).reverse();
});
```
:::

### v1.6.8
- 执行升级脚本1.6.8版本[warm-flow_1.6.8.sql](https://gitee.com/dromara/warm-flow/blob/master/sql/mysql/v1-upgrade/warm-flow_1.6.8.sql)
- 如果二开设计器，请自行手动同步，参考如下：

::: tip 原between.vue：`showWays.default=false`
```shell {4}
// 是否展示所有协作方式
showWays: {
  type: Boolean,
  default: false
},
```
:::

::: tip 现between.vue：改成`showWays.default=true`
```shell {4}
// 是否展示所有协作方式
showWays: {
  type: Boolean,
  default: true
},
```
:::

### v1.6.7
- 执行升级脚本1.6.7版本[warm-flow_1.6.7.sql](https://gitee.com/dromara/warm-flow/blob/master/sql/mysql/v1-upgrade/warm-flow_1.6.7.sql)
- 如果二开设计器，请自行手动同步，参考工作流引擎源码中`warm-flow-ui`文件夹的提交记录，提交时间范围：`2025/1/21 11:46 ~ 2025/2/24 14:59`

### v1.6.6
- 执行升级脚本1.6.0版本[warm-flow_1.6.0.sql](https://gitee.com/dromara/warm-flow/blob/master/sql/mysql/v1-upgrade/warm-flow_1.6.0.sql)
- 导入、导出和保存xml格式标识为即将删除，请参照[hh-vue](https://gitee.com/min290/hh-vue.git)的这个类`DefController`切换json方式
- 移除DefService获取流程图api，由ChartService中chartIns和chartDef代替
- 全局FlowFactory替换成FlowEngine
- [mybatis-flex](https://gitee.com/warm_4/warm-flow-mybatis-flex.git),[easy-query](https://gitee.com/warm_4/warm-flow-easy-query.git)和[jpa](https://gitee.com/warm_4/warm-flow-jpa.git)的扩展包迁移到新的仓库，独立维护
- 如果设计器是自己维护的，需要相应调整，可以参考如下
  - 条件表达式前端拼接需要把原本`eq|flag|5`格式 改成 `eq@@flag|5`,
  - `spel|#{@user.eval(#flag)}`改成`spel@@#{@user.eval(flag)}`
  - `default|${flag == 5 && flag > 4}``改成``default@@${flag == 5 && flag > 4}`
- 最后流程图数据同步：通过DefService.hisToDefJson接口，把历史任务表记录转成新的流程图元数据，保存到实例表中的defJson，新版本流程图才能正常查看

::: tip 原skip.vue：`|`分隔回显
```js {4}
watch(() => form, n => {
  n = n.value;
  let skipCondition = '';
  skipCondition = n.conditionType + "|";
  if (!/^spel/.test(n.conditionType) && !/^default/.test(n.conditionType)) {
    skipCondition = skipCondition
            + (n.condition ? n.condition : '') + "|";
  }
  n.skipCondition = skipCondition
          + (n.conditionValue ? n.conditionValue : '')
  if (n) {
    emit('change', n)
  }
}, {deep: true});
```
:::

::: tip 现skip.vue：`@@`分隔回显
```js {4}
watch(() => form, n => {
  n = n.value;
  let skipCondition = '';
  skipCondition = n.conditionType + "@@";
  if (!/^spel/.test(n.conditionType) && !/^default/.test(n.conditionType)) {
    skipCondition = skipCondition
            + (n.condition ? n.condition : '') + "|";
  }
  n.skipCondition = skipCondition
          + (n.conditionValue ? n.conditionValue : '')
  if (n) {
    emit('change', n)
  }
}, {deep: true});
```
:::

::: tip 原/PropertySetting/index.vue：`|`分隔回显
```js {2,8-9}
if (skipCondition) {
  let conditionSpl = skipCondition.split('|')
  if (skipCondition && (/^spel/.test(skipCondition) || /^default/.test(skipCondition))) {
    conditionType = conditionSpl && conditionSpl.length > 0 ? conditionSpl[0] : ''
    conditionValue = conditionSpl && conditionSpl.length > 1 ? conditionSpl[1] : ''
  } else if (skipCondition) {
    conditionType = conditionSpl && conditionSpl.length > 0 ? conditionSpl[0] : ''
    condition = conditionSpl && conditionSpl.length > 1 ? conditionSpl[1] : ''
    conditionValue = conditionSpl && conditionSpl.length > 2 ? conditionSpl[2] : ''
  }
}
```
:::

::: tip 现/PropertySetting/index.vue：`@@`分隔回显
```js {2,8-10}
if (skipCondition) {
  let conditionSpl = skipCondition.split('@@')
  if (skipCondition && (/^spel/.test(skipCondition) || /^default/.test(skipCondition))) {
    conditionType = conditionSpl && conditionSpl.length > 0 ? conditionSpl[0] : ''
    conditionValue = conditionSpl && conditionSpl.length > 1 ? conditionSpl[1] : ''
  } else if (skipCondition) {
    conditionType = conditionSpl && conditionSpl.length > 0 ? conditionSpl[0] : ''
    let conditionOneSpl = conditionSpl[1].split("|")
    condition = conditionOneSpl && conditionOneSpl.length > 0 ? conditionOneSpl[0] : ''
    conditionValue = conditionOneSpl && conditionOneSpl.length > 1 ? conditionOneSpl[1] : ''
  }
}
```
:::

::: tip 原DefController.java：导入
```java
public R<Void> importDefinition(MultipartFile file) throws Exception {
  defService.importXml(file.getInputStream());
  return R.ok();
}
```
:::

::: tip 现DefController.java：导入
```java
public R<Void> importDefinition(MultipartFile file) throws Exception {
  defService.importIs(file.getInputStream());
  return R.ok();
}
```
:::

::: tip 原DefController.java：导出
```java
public void exportDefinition(@PathVariable("id") Long id, HttpServletResponse response) throws Exception {
  Document document = defService.exportXml(id);
  // 设置生成xml的格式
  OutputFormat of = OutputFormat.createPrettyPrint();
  // 设置编码格式
  of.setEncoding("UTF-8");
  of.setIndent(true);
  of.setIndent("    ");
  of.setNewlines(true);

  // 创建一个xml文档编辑器
  XMLWriter writer = new XMLWriter(response.getOutputStream(), of);
  writer.setEscapeText(false);
  response.reset();
  response.setCharacterEncoding("UTF-8");
  response.setContentType("application/x-msdownload");
  response.setHeader("Content-Disposition", "attachment;");
  writer.write(document);
  writer.close();
}
```
:::

::: tip 现DefController.java：导出
```java
public ResponseEntity<byte[]> exportDefinition(@PathVariable("id") Long id) {
  // 要导出的字符串
  String content = defService.exportJson(id);

  // 设置响应头
  HttpHeaders headers = new HttpHeaders();
  headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=exported_string.txt");

  // 返回响应
  return ResponseEntity.ok()
          .headers(headers)
          .contentType(MediaType.TEXT_PLAIN)
          .body(content.getBytes(StandardCharsets.UTF_8));
}
```
:::


### v1.3.7
- 执行[1.3.7版本升级脚本](https://gitee.com/dromara/warm-flow/tree/master/sql)
- 如果设计器是自己维护的，需要相应调整，可以参考如下
    - 流程设计时，办理人有多个，回显通过`,`分隔，改为`@@`
    - 流程设计时，办理人设置了多个，入库拼接通过`,`拼接改为`@@`

::: tip 原between.vue：`,`分隔回显
```js {3}
/** 选择角色权限范围触发 */
function getPermissionFlag() {
  form.value.permissionFlag = form.value.permissionFlag ? form.value.permissionFlag.split(",") : [""];
  if (form.value.listenerType) {
    const listenerTypes = form.value.listenerType.split(",");
    const listenerPaths = form.value.listenerPath.split("@@");
    form.value.listenerRows = listenerTypes.map((type, index) => ({
      listenerType: type,
      listenerPath: listenerPaths[index]
    }));
  }
}
```
:::

::: tip 现between.vue：`@@`分隔回显
```js {3}
/** 选择角色权限范围触发 */
function getPermissionFlag() {
  form.value.permissionFlag = form.value.permissionFlag ? form.value.permissionFlag.split("@@") : [""];
  if (form.value.listenerType) {
    const listenerTypes = form.value.listenerType.split(",");
    const listenerPaths = form.value.listenerPath.split("@@");
    form.value.listenerRows = listenerTypes.map((type, index) => ({
      listenerType: type,
      listenerPath: listenerPaths[index]
    }));
  }
}
```
:::

::: tip 原/PropertySetting/index.vue：入库拼接`,`
```js {4}
watch(() => form.value.permissionFlag, (n) => {
  // 监听节点属性变化并更新
  props.lf.setProperties(objId.value, {
    permissionFlag: Array.isArray(n) ? n.filter(e => e).join(',') : n
  })
}, { deep: true });
```
:::

::: tip 现/PropertySetting/index.vue：入库拼接`@@`
```js {4}
watch(() => form.value.permissionFlag, (n) => {
  // 监听节点属性变化并更新
  props.lf.setProperties(objId.value, {
    permissionFlag: Array.isArray(n) ? n.filter(e => e).join('@@') : n
  })
}, { deep: true });
```
:::

### v1.3.5
- 执行升级脚本1.3.5版本[升级脚本warm-flow_x.x.x.sql](https://gitee.com/dromara/warm-flow/tree/master/sql)
- 如果设计器是自己维护的，需要相应调整，可以参考如下
    - 条件表达式前端拼接需要把原本`@@eq@@|flag@@eq@5`格式 改成 `eq|flag|5`,
    - `@@spel@@|#{@user.eval(#flag)}`改成`spel|#{@user.eval(flag)}`
    - 新增默认表达`default|${flag == 5 && flag > 4}`

::: tip 原between.vue：跳转条件下拉框
```vue
<slot name="form-item-task-skipCondition" v-if="skipConditionShow" :model="form" field="skipCondition">
  <el-form-item label="跳转条件">
    <el-input v-model="form.condition" v-if="!spelFlag" placeholder="条件名" style="width: 20%"/>
    <el-select v-model="form.conditionType" placeholder="请选择条件方式" style="width: 35%;margin-left: 1%" @change="changeOper">
      <el-option label="大于" value="gt"/>
      <el-option label="大于等于" value="ge"/>
      <el-option label="等于" value="eq"/>
      <el-option label="不等于" value="ne"/>
      <el-option label="小于" value="lt"/>
      <el-option label="小于等于" value="le"/>
      <el-option label="包含" value="like"/>
      <el-option label="不包含" value="notLike"/>
      <el-option label="spel表达式" value="spel"/>
    </el-select>
    <el-input v-model="form.conditionValue" placeholder="条件值" style="width: 42%;margin-left: 1%;margin-right: 1%;"/>
  </el-form-item>
</slot>
```
:::

::: tip 现between.vue：跳转条件下拉框
```vue
<slot name="form-item-task-skipCondition" v-if="skipConditionShow" :model="form" field="skipCondition">
  <el-form-item label="跳转条件">
    <el-input v-model="form.condition" v-if="!spelFlag" placeholder="条件名" :style="{ width: !spelFlag? '30%' : '0%' }"/>
    <el-select v-model="form.conditionType" placeholder="请选择条件方式" :style="{ width: spelFlag? '18%' : '25%', 'margin-left': '1%' }" @change="changeOper">
      <el-option label="默认" value="default"/>
      <el-option label="spel" value="spel"/>
      <el-option label="大于" value="gt"/>
      <el-option label="大于等于" value="ge"/>
      <el-option label="等于" value="eq"/>
      <el-option label="不等于" value="ne"/>
      <el-option label="小于" value="lt"/>
      <el-option label="小于等于" value="le"/>
      <el-option label="包含" value="like"/>
      <el-option label="不包含" value="notLike"/>
    </el-select>
    <el-input v-model="form.conditionValue" placeholder="条件值" :style="{ width: spelFlag? '80%' : '43%', 'margin-left': '1%' }"/>
  </el-form-item>
</slot>
```
:::

::: tip 原between.vue：跳转条件下拉框js
```javascript
watch(() => form, n => {
  n = n.value;
  let skipCondition = n.skipCondition;
  skipCondition = "@@" + n.conditionType + "@@|";
  if (n.conditionType !== 'spel') {
    skipCondition = skipCondition
            + (n.condition ? n.condition : '') + "@@"
            + n.conditionType + "@@";
  }
  n.skipCondition = skipCondition
          + (n.conditionValue ? n.conditionValue : '')
  if (n) {
    emit('change', n)
  }
}, {deep: true});

function changeOper(obj) {
  spelFlag.value = obj === 'spel';
}

if (props.modelValue?.conditionType === 'spel') spelFlag.value = true;

```
:::

::: tip 现between.vue：跳转条件下拉框
```javascript
watch(() => form, n => {
  n = n.value;
  let skipCondition = '';
  skipCondition = n.conditionType + "|";
  if (!/^spel/.test(n.conditionType) && !/^default/.test(n.conditionType)) {
    skipCondition = skipCondition
            + (n.condition ? n.condition : '') + "|";
  }
  n.skipCondition = skipCondition
          + (n.conditionValue ? n.conditionValue : '')
  if (n) {
    emit('change', n)
  }
}, {deep: true});

function changeOper(obj) {
  spelFlag.value = (obj === 'spel' || obj === 'default');
}

if (props.modelValue?.conditionType === 'spel' || props.modelValue?.conditionType === 'default') {
  spelFlag.value = true;
}
```
:::

::: tip 原/PropertySetting/index.vue：回显js
```javascript
let skipCondition = n.properties.skipCondition
let conditionSpl = skipCondition ? skipCondition.split('@@|') : []
let conditionSplTwo = conditionSpl && conditionSpl.length > 0 ? conditionSpl[1]: []
let condition, conditionType, conditionValue = '';
if (conditionSpl && conditionSpl.length > 0 && conditionSpl[0] === '@@spel') {
  conditionType = 'spel'
  conditionValue = conditionSplTwo
} else if (conditionSpl && conditionSpl.length > 0 && conditionSpl[0] !== '@@spel') {
  condition = conditionSplTwo && conditionSplTwo.length > 0 ? conditionSplTwo.split("@@")[0] : ''
  conditionType = conditionSplTwo && conditionSplTwo.length > 0 ? conditionSplTwo.split("@@")[1] : ''
  conditionValue = conditionSplTwo && conditionSplTwo.length > 0 ? conditionSplTwo.split("@@")[2] : ''
}
```
:::

::: tip 现/PropertySetting/index.vue：回显js
```javascript
let skipCondition = n.properties.skipCondition
let condition, conditionType, conditionValue = ''
if (skipCondition) {
  let conditionSpl = skipCondition.split('|')
  if (skipCondition && (/^spel/.test(skipCondition) || /^default/.test(skipCondition))) {
    conditionType = conditionSpl && conditionSpl.length > 0 ? conditionSpl[0] : ''
    conditionValue = conditionSpl && conditionSpl.length > 1 ? conditionSpl[1] : ''
  } else if (skipCondition) {
    conditionType = conditionSpl && conditionSpl.length > 0 ? conditionSpl[0] : ''
    condition = conditionSpl && conditionSpl.length > 1 ? conditionSpl[1] : ''
    conditionValue = conditionSpl && conditionSpl.length > 2 ? conditionSpl[2] : ''
  }
}
```
:::

### v1.3.4

- 办理人表达式，删除策略前缀，通过$和#区分，需执行1.3.4.[升级脚本warm-flow_x.x.x.sql](https://gitee.com/dromara/warm-flow/tree/master/sql)
- 依赖的groupId：org.dromara，改为org.dromara.warm
- 如果扩展了条件表达式策略
    - 接口或者抽象类前缀由`ExpressionStrategy`改为 `ConditionStrategy`
    - 全局搜索`org.dromara.warm.flow.core.expression` 替换为`org.dromara.warm.flow.core.expression`,然后检查是否正确


### v1.3.3

- 独立设计器放行路径增加`/warm-flow`，如果想要共享后端权限(比如token)，可不增加，并且按照官网流程设计器引入进行设置


### v1.3.1

- 依赖的groupId：io.github.minliuhua，改为org.dromara
- 包名：com.warm， 改为org.dromara.warm
- 节点详情进入改为双击
- 终止操作的流程状态改为更合理的终止状态，如需还想按照原本的自动完成，请使用自定义流程状态
- FlowParams对象删除setXxx(yyy)方法，改为xxx(yyy)方法赋值
- 转办、委派、加签和减签方法，老方法标识即将删除, 请尽快使用新的接口
- 终止免校验权限改为设置ignore字段
- 设计器引入优化
    - 设计器后端放行地址`/warm-flow/**`删除，不再需要
    - 前端加载设计器代理配置,vue.config.js或者nginx中的代理，`/warm-flow-ui/`删除，不再需要
    - iframe中访问设计器接口由`/warm-flow-ui/${definitionId}?disabled=${disabled}`，改为VUE_APP_BASE_API +
      `/warm-flow-ui/index.html?id=${definitionId}&disabled=${disabled}`
    - VUE_APP_BASE_API是前端访问前缀比如`prod-api`

### v1.3.0

- 执行.[升级脚本warm-flow_x.x.x.sql](https://gitee.com/dromara/warm-flow/tree/master/sql)


### v1.2.8

- 本次升级，内置json库snack3方式，改为spi方式加载，业务项目中存在哪种json就会使用哪种的实现，
  支持顺序按顺序加载一种：snack3、jackson、fastjson、gson，并且目前只实现了这四种，可扩展
- 如在未集成snack3库的环境下，还需要使用snack3库，需要单独使用（原组件使用snack3库）
  ```xml
        <dependency>
            <groupId>org.noear</groupId>
            <artifactId>snack3</artifactId>
            <version>3.2.88</version>
        </dependency>
   ```



### v1.2.6

- 执行.[升级脚本warm-flow_x.x.x.sql](https://gitee.com/dromara/warm-flow/tree/master/sql)
- 流程状态字段flow_status改为string类型，业务系统需要对应修改


### v1.2.4

- 执行.[升级脚本warm-flow_x.x.x.sql](https://gitee.com/dromara/warm-flow/tree/master/sql)
- 流程定义表from_custom改为form_custom，from_path改为form_path，涉及到这两个字段的前后段都要修改
- 反显审批流程表单，改为通过task表新增的form_custom和form_path字段
- 只针对mybatis-plus扩展包，其他的扩展包可忽略，多租户和逻辑删除，改为通过mybatis-plus的自带的方式实现(
  可参考官网文章逻辑删除和多租户)，并且流程表的逻辑删除字段都更新为0
- 原本的我发起[warmFlowInitiator], 组件内部不在维护替换，由分派监听器实现替换


### v1.2.1
- 执行.[升级脚本warm-flow_x.x.x.sql](https://gitee.com/dromara/warm-flow/tree/master/sql)


### v1.2.0
- 执行.[升级脚本warm-flow_x.x.x.sql](https://gitee.com/dromara/warm-flow/tree/master/sql)
- 工具包路径调整


### v1.1.9
- 执行.[升级脚本warm-flow_x.x.x.sql](https://gitee.com/dromara/warm-flow/tree/master/sql)
