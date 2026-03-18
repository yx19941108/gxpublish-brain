# 办理人变更

::: tip
- 审批任务的办理人，通常是在流程设计器中预先设定好办理人，那如果想要在办理过程中修改办理人呢？

:::

## 1、变更的时机
- 1、修改下个任务的办理人
  - <span class="red-no-bg">动态指定办理人：</span> 办理人表达式(或[分派监听器](./listener.html#_5-3、分派监听器))，动态修改下个任务的办理人
  - <span class="red-no-bg">角色|部门id转用户id：</span> 办理人权限处理器转换接口，角色/部门id转成用户id
  - <span class="red-no-bg">跳转接口FlowParams设置nextHandler：</span> 比如skip的FlowParams(nextHandler、nextHandlerAppend)设置值，直接指定下个任务的办理人
- 2、修改当前任务的办理人
  - <span class="red-no-bg">转办：</span> 任务转给其他人办理
  - <span class="red-no-bg">委派：</span> 求助其他人审批，然后参照他的意见决定是否审批通过
  - <span class="red-no-bg">加签：</span> 办理中途，希望其他人一起参与办理
  - <span class="red-no-bg">减签：</span> 办理中途，希望某些人不参与办理

    
## 2、动态指定办理人

**背景**

审批任务的办理人，通常是在流程设计器中预先设定好办理人，那如果想要在办理过程中指定办理人呢？

**解决思路**

- 1、流程设计时，需要动态指定办理人的节点，配置办理人表达式`${handler1}`
- 2、本节点前任意节点办理时设置，在流程变量中传入`${handler1}`的值
- 3、办理完成会生成本节点任务，并且替换`flow_user`表中的表达式



<div><img src="https://foruda.gitee.com/images/1745558346409798689/0bc86581_2218307.png" width="500" /></div>

后端代码设置变量

```java
// 流程变量
Map<String, Object> variable = new HashMap<>();
variable.put("handler1", "100");
flowParams.variable(variable);

Instance instance = insService.skipByInsId(testLeave.getInstanceId(), flowParams);
```



**高级玩法**

- 支持动态指定一群人
- 支持spel表达式
- 支持表达式扩展



把如上代码`"100"`改成`Arrays.asList(4, "5", 100L)`，就可以动态指定一群人

```java
// 流程变量
Map<String, Object> variable = new HashMap<>();
variable.put("handler1", Arrays.asList(4, "5", 100L));
flowParams.variable(variable);

Instance instance = insService.skipByInsId(testLeave.getInstanceId(), flowParams);
```
<br>

比如设计器配置了`#{@user.evalVar(#handler2)}`spel表达式，`#handler2`是方法入参，通过流程变量传递，就会表达式，执行`user.evalVar`方法

```java
/**
 * 用户类
 */
@Component("user")
public class User {

    /**
     * spel办理人表达式
     * @param handler2 办理人
     * @return String
     */
    public String evalVar(String handler2) {
        return handler2;
    }
}

// 流程变量
Map<String, Object> variable = new HashMap<>();
variable.put("handler2", "101");
flowParams.variable(variable);

Instance instance = insService.skipByInsId(testLeave.getInstanceId(), flowParams);
```

## 3、角色|部门id转用户id

```java

@Component
/**
 * 办理人权限处理器（可通过配置文件注入，也可用@Bean/@Component方式）
 *
 * @author shadow
 */
public class CustomPermissionHandler implements PermissionHandler {

    /**
     * 转换办理人，比如设计器中预设了能办理的人，如果其中包含角色或者部门id等，可以通过此接口进行转换成用户id
     * permissions：{role:1,dept:1}
     */
    @Override
    public List<String> convertPermissions(List<String> permissions) {
        // 把角色部门转换成用户
        // permissions：{role:1,dept:1} ---> {1,2,100}
        ......
        return "{1,2,100}";
    }
}

```

## 4、跳转接口FlowParams设置nextHandler
在[接口文档中](../primary/api.html#流程跳转)，skip方法的flowParams入参中有(nextHandler, nextHandlerAppend)，设置了nextHandler，会跳转到指定节点，
并把nextHandler的值作为下一个任务的办理人。同理只要接口中有nextHandler，就可以设置。

`Instance skip(taskId, flowParams)`：传入流程任务id，流程跳转。flowParams包含如下字段：
- skipType: 跳转类型(PASS审批通过 REJECT退回) [必传]
- nodeCode: 如果指定节点,可[任意跳转]到对应节点，严禁任意退回选择后置节点 [按需传输]
- permissionFlag: 办理人权限标识，比如用户，角色，部门等，用于校验是否有权限办理 [按需传输]；满足任一情况可以不传：流程设计时未设置办理人、ignore为true、实现了[办理人权限处理器](./permission_handler.md)
- message: 审批意见 [按需传输]
- handler: 办理人唯一标识，如用户id，用于记录历史表 [按需传输]；如果实现了[办理人权限处理器](./permission_handler.md)可不用传
- variable: 流程变量 [按需传输]
- nextHandler: 执行的下个任务的办理人[按需传输]
- nextHandlerAppend: 个任务处理人配置类型（true-追加，false-覆盖，默认false）[按需传输]
- flowStatus: 流程状态，自定义流程状态 [按需传输]
- ignore: 忽略权限校验（比如管理员不校验），默认不忽略 [按需传输]

## 5、转办|委派|加签|减签

[接口描述地址](../primary/api.html#转办)
</br>

::: code-tabs#shell

@tab:active 转办

```java
public void transfer(TaskService taskService) {

    taskService.transfer(getTaskId(), new FlowParams()
            .handler("1")
            .permissionFlag(Arrays.asList("role:1", "role:2", "user:1"))
            .addHandlers(Arrays.asList("1","2"))
            .message("转办"));
}
```

@tab 委派

```java
public void depute(TaskService taskService){
    taskService.transfer(getTaskId(), new FlowParams()
            .handler("1")
            .permissionFlag(Arrays.asList("role:1", "role:2", "user:1"))
            .addHandlers(Arrays.asList("1","2"))
            .message("委派"));
}
```

@tab 加签

```java
public void addSignature(TaskService taskService){
    taskService.addSignature(getTaskId(), new FlowParams()
            .handler("1")
            .permissionFlag(Arrays.asList("role:1", "role:2", "user:1"))
            .addHandlers(Arrays.asList("1","2"))
            .message("加签"));
}
```

@tab 减签

```java
public void reductionSignature(TaskService taskService){
    taskService.reductionSignature(getTaskId(), new FlowParams()
            .handler("1")
            .permissionFlag(Arrays.asList("role:1", "role:2", "user:1"))
            .reductionHandlers(Arrays.asList("1","2"))
            .message("减签"));
}
```

:::
