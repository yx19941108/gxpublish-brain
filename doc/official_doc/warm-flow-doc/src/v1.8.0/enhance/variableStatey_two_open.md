# 办理人表达式扩展

::: tip
- 当内置的**办理人变量**不满足业务需求时，可进行扩展

:::

<!-- @include: ./expression_open.md -->

## 3、办理人表达式

- 扩展需要实现`VariableStrategy`接口, 实现`getType和preEval`方法

### 3.1、办理人表达式接口
```java
/**
 * 办理人表达式策略接口
 *
 * @author warm
 */
public interface VariableStrategy extends ExpressionStrategy<List<String>> {

    /**
     * 办理人表达式策略实现类集合
     */
    List<ExpressionStrategy<List<String>>> expressionStrategyList = new ArrayList<>();

    default void setExpression(ExpressionStrategy<List<String>> expressionStrategy) {
        expressionStrategyList.add(expressionStrategy);
    }

    Object preEval(String expression, Map<String, Object> variable);

    @Override
    default List<String> eval(String expression, Map<String, Object> variable) {
        return afterEval(preEval(expression, variable));
    }

    default List<String> afterEval(Object o) {
        if (ObjectUtil.isNotNull(o)) {
            if (o instanceof List) {
                return StreamUtils.toList((List<?>) o, Object::toString);
            }
            if (o instanceof Object[]) {
                return Arrays.stream((Object[]) o).map(Object::toString).collect(Collectors.toList());
            }
            return Collections.singletonList(o.toString());
        }
        return null;
    }
}
```

### 3.2、办理人表达式实现类

```java
/**
 * 默认办理人表达式策略： @@default@@|${flag}
 *
 * @author warm
 */
public class DefaultVariableStrategy implements VariableStrategy {

    @Override
    public String getType() {
        return "$";
    }

    @Override
    public Object preEval(String expression, Map<String, Object> variable) {
        String result = expression.replace("${", "").replace("}", "");
        return variable.get(result);
    }

}
```
