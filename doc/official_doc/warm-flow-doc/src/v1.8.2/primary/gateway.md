# 网关

::: tip
- 工作流引擎中用于控制流程流向的关键组件，它们通过不同的逻辑和功能来管理流程的执行路径

:::


## 1、网关类型
- 1、互斥网关：会结合[流程变量](variable.md)和[条件表达式](condition.md)来匹配，匹配成功则执行匹配到的分支。
- 2、并行网关: 会将所有分支同时执行，都执行完，到达并行网关终点，才继续往下执行。
- 3、网关最好成对绘制，画了开始开始网关，最好要画结束网关。简单流程图可能没问题，复杂可能会异常


## 2、互斥网关

## 2.1、设计器设置网关的条件

![](https://foruda.gitee.com/images/1754531858724397764/de4b4e75_2218307.png)

## 2.2、通过流程变量，设置变量值

```java {7}
@Override
public int insertTestLeave(TestLeave testLeave, String flowStatus)
{
    FlowParams flowParams = FlowParams.build().flowCode(getFlowType(testLeave));
    // 流程变量
    Map<String, Object> variable = new HashMap<>();
    variable.put("flag", String.valueOf(testLeave.getDay()));
    flowParams.variable(variable);

    Instance instance = insService.start(id, flowParams);
    return instance != null? 1 : 0;
}
```

