# 办理人表达式

::: tip
- 业务中，经常会出现设计流程时，还不确定具体办理人是谁，就需要后续能够动态设置办理人功能，此时办理人表达式就派上用场了  

:::

## 1、特点
- 1、内置常见表达式，同时支持功能强大的spel表达，支持扩展
- 2、支持一对一替换，同时支持多对一的替换，替换集合


## 2、内置表达式
- 1、默认办理人变量策略: `${handler1}`， `$`前缀表示默认办理人变量策略
- 2、spel办理人变量策略: `#{@user.evalVar(#handler2)}`，`#`前缀表示spel办理人变量策略

```java
 @SpringBootTest
public class VariableTest {

    /**
     * 办理人表达式测试
     */
    @Test
    public void testVariable() {
        List<Task> addTasks = new ArrayList<>();
        addTasks.add(FlowEngine.newTask().setPermissionList(Arrays.asList("${handler1}"
                , "#{@user.evalVar(#handler2)}", "${handler3}", "#{@user.evalVar(#handler4)}"
                , "#{@user.evalVarEntity(#handler5)}", "role:1", "1")));
        FlowParams flowParams = new FlowParams();
        Map<String, Object> variable = new HashMap<>();
        variable.put("handler1", Arrays.asList(4, "5", 100L));
        variable.put("handler2", 12L);
        variable.put("handler3", new Object[]{9, "10", 102L});
        variable.put("handler4", "15");
        Task task = FlowEngine.newTask();
        variable.put("handler5", task.setId(55L));

        ExpressionUtil.evalVariable(addTasks, variable);
        addTasks.forEach(p -> p.getPermissionList().forEach(System.out::println));
    }
}
```

## 3、匹配规则
- 1、默认按照注入策略顺序，倒叙匹配。比如最后注入spel策略，就先遍历spel策略，匹配上就执行。

## 4、变量替换时机
- 1、流程设计时，本节点配置办理人表达式
- 2、本节点前任意节点办理时设置，传入变量
- 3、办理完成会生成本节点任务，并且替换`flow_user`表中的表达式

> 比如B-->C, C任务设置办理人变量为`${handler1}`，B任务或者之前任务办理时传入变量`handler1=100`，则C节点办理人变量为100

## 5、可实现的效果
如下图中示例可以很容易实现 

<div><img src="/assignmentlistener.jpg" width="550px" height="450px" /></div>

## 6、条件表达式和办理人表达式区别

- 使用地方：条件表达式网关中用到，办理人表达式在办理人列表中用到。
- 替换时机：条件表达式是当前节点传入变量替换，办理人表达式在本节点前任意节点办理时替换。
- 作用：前者为了决定执行哪条节点任务，后者觉得谁可以办理。

## 7、办理人选择项接口
- 通过此接口可以给办理人选择，增加默认选项比如发起人、部门领导审批之类的（待开发）
