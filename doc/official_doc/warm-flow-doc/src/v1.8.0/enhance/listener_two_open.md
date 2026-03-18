# 监听器表达式扩展

::: tip
- 当内置的**监听器表达式**不满足业务需求时，可进行扩展

:::

<!-- @include: ./expression_open.md -->

## 3. 监听器表达式

### 3.1、监听器表达式接口
```java

/**
 * 监听器表达式策略接口
 *
 * @author warm
 */
public interface ListenerStrategy extends ExpressionStrategy<Boolean> {

    /**
     * 监听器表达式策略实现类集合
     */
    List<ExpressionStrategy<Boolean>> expressionStrategyList = new ArrayList<>();

    default void setExpression(ExpressionStrategy<Boolean> expressionStrategy) {
        expressionStrategyList.add(expressionStrategy);
    }

}
```

### 3.2、监听器表达式实现类

```java
/**
 * spel监听器表达式 #{@user.eval()}
 *
 * @author warm
 */
public class ListenerStrategySpel implements ListenerStrategy {

    @Override
    public String getType() {
        return "#";
    }

    @Override
    public Boolean eval(String expression, Map<String, Object> variable) {
        SpelHelper.parseExpression(expression, variable);
        // 恒返回true，说明匹配上监听器表达式，扩展策略也一定要返回true
        return true;
    }
}
```
