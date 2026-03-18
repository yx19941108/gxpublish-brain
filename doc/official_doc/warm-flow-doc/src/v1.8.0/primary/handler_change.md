# 办理人变更

::: tip
- 审批业务中经常会出现办理人中途变更的情况，比如增加办理人，任务转办给别人等。

:::

## 1、变更支持的类型
- 转办：任务转给其他人办理
- 委派：求助其他人审批，然后参照他的意见决定是否审批通过
- 加签：办理中途，希望其他人一起参与办理
- 减签：办理中途，希望某些人不参与办理

## 2、接口描述
[接口描述地址](./api.html#_3-1%E3%80%81%E6%B5%81%E7%A8%8B%E8%B7%B3%E8%BD%AC)
</br>

## 3、代码示例

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
