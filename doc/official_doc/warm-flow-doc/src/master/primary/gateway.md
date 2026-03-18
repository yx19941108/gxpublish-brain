# 网关

::: tip
- 工作流引擎中用于控制流程流向的关键组件，它们通过不同的逻辑和功能来管理流程的执行路径

:::


## 1、网关类型
- 1、互斥网关：会结合[流程变量](variable.md)和[条件表达式](condition.md)来匹配，<span class="red-no-bg">跳转条件匹配</span>的，则取<span class="red-no-bg">任意第一条</span>，否则取跳转<span class="red-no-bg">条件为空</span>的任意一条去执行。
  到达网关终点，<span class="red-no-bg">不做限制</span>直接往下执行。
- 2、并行网关: 会将<span class="red-no-bg">所有分支</span>同时执行。当<span class="red-no-bg">所有分支</span>都执行完，到达并行网关终点，才继续往下执行。
- 2、包容网关: 会结合[流程变量](variable.md)和[条件表达式](condition.md)来匹配，<span class="red-no-bg">有跳转条件</span>的分支，但是跳转条件<span class="red-no-bg">不匹配</span>的不执行，<span class="red-no-bg">跳转条件为空</span>的分支默认执行。
  当<span class="red-no-bg">正在执行的分支</span>都执行完，到达包容网关终点，才继续往下执行。
- 3、网关最好成对绘制，画了开始网关，最好要画对应的结束网关。<span class="red-no-bg">互斥网关</span>简单流程图可能没问题，复杂可能会异常。<span class="red-no-bg">包容和并行网关</span>一定要成对出现。


## 2、互斥网关/包含网关

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

