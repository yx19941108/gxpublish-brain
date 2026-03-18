# 快速开始

::: tip
**在开始之前，我们假定您已经:**  

- 熟悉 Java 环境配置及其开发  

- 熟悉 关系型 数据库，比如 MySQL  

- 熟悉 Spring Boot或者Solon 及相关框架  

- 熟悉 Java 构建工具，比如 Maven  
:::

## **1、导入sql，按需求执行**

- 开始学习前，请先了解<span class="big-font">[表结构](./table.md)</span>，不迷路
- 首次导入，先创建数据库，找到对应数据库的全量脚本<span class="big-font">[warm-flow-all.sql](https://gitee.com/dromara/warm-flow/tree/master/sql/mysql)</span>，执行  
- 如果版本更新，找到对应数据库的更新版本，比如xx-upgrade，<span class="big-font">[warm-flow_x.x.x.sql](https://gitee.com/dromara/warm-flow/tree/master/sql/mysql/v1-upgrade)</span>，执行

<table>
    <tbody>
        <tr>
            <td><img src="https://foruda.gitee.com/images/1724349579810152906/15af22df_2218307.png" height="180px"/></td>
            <td><img src="https://foruda.gitee.com/images/1724349629546024487/f32625d9_2218307.png" height="180px"/></td>
        </tr>
    </tbody>
</table>

<style scoped>
td {
  width: 50%;
}
</style>

## **2、官网流程定义案例xml**
<span class="big-font">[官网流程定义案例xml](https://gitee.com/dromara/warm-flow-test/tree/master/warm-flow-core-test/src/main/resources)</span>

<span class="red-font">有典型的流程案例，可以发给我json文件</span>


## **3、maven依赖**
### **3.1、mybatis**

::: code-tabs#shell

@tab:active springboot2

```xml
<dependency>
    <groupId>org.dromara.warm</groupId>
    <artifactId>warm-flow-mybatis-sb-starter</artifactId>
    <version>最新版本</version>
</dependency>
```

@tab springboot3

```xml
<dependency>
    <groupId>org.dromara.warm</groupId>
    <artifactId>warm-flow-mybatis-sb3-starter</artifactId>
    <version>最新版本</version>
</dependency>
```

@tab solon

```xml
<dependency>
    <groupId>org.dromara.warm</groupId>
    <artifactId>warm-flow-mybatis-solon-plugin</artifactId>
    <version>最新版本</version>
</dependency>
```

:::


### **3.2、mybatis-plus**

::: code-tabs#shell

@tab:active springboot2

```xml
<dependency>
    <groupId>org.dromara.warm</groupId>
    <artifactId>warm-flow-mybatis-plus-sb-starter</artifactId>
    <version>最新版本</version>
</dependency>
```

@tab springboot3

```xml
<dependency>
    <groupId>org.dromara.warm</groupId>
    <artifactId>warm-flow-mybatis-plus-sb3-starter</artifactId>
    <version>最新版本</version>
</dependency>
```

@tab solon

```xml
<dependency>
    <groupId>org.dromara.warm</groupId>
    <artifactId>warm-flow-mybatis-plus-solon-plugin</artifactId>
    <version>最新版本</version>
</dependency>
```

:::


### **3.3、jpa**

<span class="big-font">[https://gitee.com/vanlin/warm-flow-jpa.git](https://gitee.com/vanlin/warm-flow-jpa.git)</span>


### **3.4、mybatis-flex**

<span class="big-font">[https://gitee.com/rigangxia/warm-flow-mybatis-flex.git](https://gitee.com/rigangxia/warm-flow-mybatis-flex.git)</span>


### **3.5、easy-query**

<span class="big-font">[https://gitee.com/link2fun/warm-flow-easy-query.git](https://gitee.com/link2fun/warm-flow-easy-query.git)</span>

### **3.6、BeetlSql**

<span class="big-font">[https://gitee.com/smartcity/warm-flow-beetlsql-solon.git](https://gitee.com/smartcity/warm-flow-beetlsql-solon.git)</span>


> **有想扩展其他orm框架和数据库的可加qq群联系群主**

## **4、代码示例**

> <span class="big-font">详细案例测试代码[warm-flow-test](https://gitee.com/dromara/warm-flow-test)项目中，warm-flow-xxx-test模块的测类</span>

<br>

**以下为简短案例：**

::: code-tabs#shell

@tab:active 部署流程

```java
public void deployFlow() throws Exception {
    String path = "/Users/minliuhua/Desktop/mdata/file/IdeaProjects/min/hh-vue/hh-admin/src/main/resources/leaveFlow-serial.xml";
    System.out.println("已部署流程的id：" + defService.importIs(new FileInputStream(path)).getId());
}
```

@tab 发布流程

```java
public void publish() throws Exception {
    defService.publish(1212437969554771968L);
}
```

@tab 开启流程

```java
public void startFlow() {
    System.out.println("已开启的流程实例id：" + insService.start("1", getUser()).getId());
}
```

@tab 流程流转

```java
public void skipFlow() throws Exception {
    // 通过实例id流转
    Instance instance = insService.skipByInsId(1219286332141080576L, getUser().skipType(SkipType.PASS.getKey())
            .permissionFlag(Arrays.asList("role:1", "role:2")));
    System.out.println("流转后流程实例：" + instance.toString());

//        // 通过任务id流转
//        Instance instance = insService.skip(1219286332145274880L, getUser().skipType(SkipType.PASS.getKey())
//                .permissionFlag(Arrays.asList("role:1", "role:2")));
//        System.out.println("流转后流程实例：" + instance.toString());
}

public void skipAnyNode() throws Exception {
    // 跳转到指定节点
    Instance instance = insService.skip(1219286332145274880L, getUser().skipType(SkipType.PASS.getKey())
            .permissionFlag(Arrays.asList("role:1", "role:2")).nodeCode("4"));
    System.out.println("流转后流程实例：" + instance.toString());
}
```

:::

## **5、设计器引入**
> <span class="big-font">通过jar包引入：[文档地址](./designerIntroduced.md)</span>

