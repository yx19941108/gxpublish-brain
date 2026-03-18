# 或、票、会签

::: tip 
- 审批过程中可能存在，一人或者多人审批的情况，不同情况对应不同协作类型（会签、票签和或签）

:::

## 1、流程协作规则
> 或签：一名办理人审批通过，审批节点才会通过  
> 票签：满足设定的通过策略，部分审批人审批通过，审批节点才会通过  
> 会签：所有审批人审批通过，审批节点才会通过

## 2、注意事项
> 票签和会签会会根据用户数量计算通过率或者通过策略等，如果节点配置的是角色，这种情况不好统计，要解决这种问题，请把角色全部转成用户id-[转换办理人](./permission_handler.html)

## 3、使用演示
### 3.1、或签
<div><img src="https://foruda.gitee.com/images/1754532248385621301/50101415_2218307.png"/></div>

### 3.2、票签
- **票签策略**
  - 通过率：通过率在(0.001-100)这之间，也就是审批通过人数占比总人数比例大于这个通过率，审批流程才往下执行，反之提前不满足通过率，直接驳回
  - 固定通过人数：类型为正整数，审批通过人数大于等于这个人数，审批流程才往下执行，反之提前不满足通过人数，直接驳回
  - 固定驳回人数：类型为正整数，审批驳回人数大于等于这个人数，审批流程才往下执行，反之提前不满足驳回人数，直接驳回
  - 默认表达式：格式如: `${flag > 4}`，如果`${}`内部返回的结果为true，则通过，否则驳回
  - spel表达式：格式如: `#{@user.eval(#flag)}`，如果`#{}`内部返回的结果为true，则通过，否则驳回

<table>
    <tbody>
        <tr>
            <td><img src="https://foruda.gitee.com/images/1763080128080284358/a86fdeb3_2218307.png"/></td>
        </tr>
        <tr>
            <td><img src="https://foruda.gitee.com/images/1763080154885581373/e5a209ea_2218307.png"/></td>
        </tr>
        <tr>
            <td><img src="https://foruda.gitee.com/images/1763080173837682535/c90154b9_2218307.png"/></td>
        </tr>
        <tr>
            <td><img src="https://foruda.gitee.com/images/1763080204929428483/034dd696_2218307.png"/></td>
        </tr>
        <tr>
            <td><img src="https://foruda.gitee.com/images/1763080220404138371/60a850e0_2218307.png"/></td>
        </tr>
    </tbody>
</table>

### 3.3、会签
<div><img src="https://foruda.gitee.com/images/1754532261962167543/23ce23da_2218307.png"/></div>

## 4、或签策略注意事项
> 通过策略特别需要讲一下<span class="red-font">默认表达式和spel表达</span>，因为不管通过什么方式，通常需要知道已通过的
<span class="red-font">人数和总人数</span>等，才能在自己的表达式中判断是否满足条件，那么<span class="red-font">如何获取</span>已通过和总人数呢？

- 组件在执行表达式的时候会注入内置变量，如下：
    - skipType：跳过类型
    - passNum：通过数量
    - rejectNum：拒绝数量
    - todoNum：待处理数量
    - allNum：总人数
    - passList：通过历史任务列表
    - rejectList：拒绝历史任务列表
    - todoList：待处理用户列表
- 在表达式中获取对应的变量，可以获取部分变量，spel表达式只是示例，实际使用中按需设置参数，参数可以以上变量之外的，只要自己在流程变量中设置了就行
  - 默认表达式：`${passNum * 1.0 / allNum > 0.5}`，加上这个`* 1.0`，防止大于号左边0.x变成0去比较
  - spel表达式：`#{@voteSignService.eval(#skipType, #passNum, #rejectNum, #todoNum, #allNum, #passList, #rejectList, #todoList)}`
  

```java
@Component("voteSignService")
public class VoteSignService {

    /**
     * 票签通过率计算
     *
     * @param skipType 跳转类型
     * @param passNum  审批通过人数
     * @param rejectNum 审批驳回人数
     * @param todoNum  待处理人数
     * @param allNum  总人数
     * @param passList List<HisTask> 通过历史任务列表，HisTask中approver字段是审批人的唯一标识
     * @param rejectList List<HisTask> 拒绝历史任务列表，HisTask中approver字段是审批人的唯一标识
     * @param todoList 待处理用户列表
     * @return  boolean
     */
    public boolean eval(String skipType, Integer passNum, Integer rejectNum, Integer todoNum, Integer allNum
            , List<HisTask> passList, List<HisTask> rejectList, List<User> todoList ) {

        log.info("跳过类型: {}", skipType);
        log.info("通过数量: {}", passNum);
        log.info("拒绝数量: {}", rejectNum);
        log.info("待处理数量: {}", todoNum);
        log.info("总人数: {}", allNum);
        log.info("通过历史任务列表: {}", passList);
        log.info("拒绝历史任务列表: {}", rejectList);
        log.info("待处理用户列表: {}", todoList);
        log.info("开始票签通过率计算......");

        return true;
    }

}
```

## 5、入库值
- 他们都是保存在flow_node表的node_ratio字段中  
- 0就是或签，0-100就是票签，100就是会签
