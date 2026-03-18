# 条件表达式扩展


## 1、表达式公共接口

`ExpressionStrategy`接口，是表达式策略类接口，抽离公共方法
- 1、`getType()`: 表达式策略类型，也是表达式的前缀  
- 2、`isIntercept()`: 执行表达式之前，是否截取表达式前缀，然后在进行执行，默认不截取  
- 3、`eval()`: 执行表达式  
- 4、`setExpression()`: 设置新增的表达式，方便扩展  

```java
/**
 * 表达式策略类接口
 *
 * @author warm
 */
public interface ExpressionStrategy<T> {

    /**
     * 获取策略类型
     *
     * @return 类型
     */
    String getType();

    /**
     * 当选择截取，并且希望拼接上某些字符串，在进行截取
     *
     * @return 类型
     */
    default String interceptStr() {
        return "";
    }

    /**
     * 执行表达式
     *
     * @param expression 表达式
     * @param variable   流程变量
     * @return 执行结果
     */
    T eval(String expression, Map<String, Object> variable);


    /**
     * 设置表达式
     * @param expressionStrategy 表达式
     */
    void setExpression(ExpressionStrategy<T> expressionStrategy);

}
```

## 2、注册表达式实现类
- 通过这个方法进行注册ExpressionUtil.setExpression

```java
ExpressionUtil.setExpression(new ExpressionStrategyEq());
```
