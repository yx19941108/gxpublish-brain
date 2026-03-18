# 流程变量

::: tip
- 流程变量，map类型，用于流程执行中的数据转递  
- 条件表达式中，使用流程变量进行判断执行哪个任务
- 办理人表达式，通过流程变量动态指定办理人
- 在监听器中，可以获取流程变量 
- 在执行流程时，可以设置流程变量，自定义使用

:::

## 设置流程变量
```java
    public void insertTestLeave(TestLeave testLeave, Integer flowStatus)
    {
        String id = IdUtils.nextIdStr();
        testLeave.setId(id);
        LoginUser user = SecurityUtils.getLoginUser();
        FlowParams flowParams = FlowParams.build().flowCode(getFlowType(testLeave))
                .handler(user.getUser().getUserId().toString());
        // 流程变量
        Map<String, Object> variable = new HashMap<>();
        variable.put("testLeave", testLeave);
        variable.put("flag", String.valueOf(testLeave.getDay()));
        flowParams.variable(variable);

        Instance instance = insService.start(id, flowParams);
    }
```

```java
@Component
public class FinishListener implements Listener {
    private static final Logger log = LoggerFactory.getLogger(StartListener.class);

    @Override
    public void notify(ListenerVariable variable) {
        log.info("完成监听器:{}", variable);
        Instance instance = variable.getInstance();
        Map<String, Object> testLeaveMap = variable.getVariable();
        log.info("完成监听器结束......");
    }
}
```

## 存储流程变量
存储在flow_instance表中的variable

<div><img src="https://foruda.gitee.com/images/1743128143393789644/a2b7eef4_2218307.png" width="700px"></div>

## 获取流程变量
```java
Map<String, Object> variableMap = instance.getVariableMap();
```
