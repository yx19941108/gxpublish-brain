# 条件表达式扩展

::: tip
- 当内置的**条件表达式**不满足业务需求时，可进行扩展

:::

<!-- @include: ./expression_open.md -->

## 3、条件表达式

- 扩展需要实现`ConditionStrategy`接口或者继承`ConditionStrategyAbstract`抽象类


### 3.1、条件表达式接口
```java
/**
 * 条件表达式接口
 *
 * @author warm
 */
public interface ConditionStrategy extends ExpressionStrategy<Boolean> {

    /**
     * 条件表达式策略实现类map
     */
    Map<String, ExpressionStrategy<Boolean>> map = new HashMap<>();

    default void setExpression(ExpressionStrategy<Boolean> expressionStrategy) {
        map.put(expressionStrategy.getType(), expressionStrategy);
    }

    static Map<String, ExpressionStrategy<Boolean>> getExpressionMap() {
        return map;
    }
    
    default Boolean isIntercept() {
        return true;
    }
}
```

### 3.2、条件表达式抽象类

```java
/**
 * 条件表达式抽象类，复用部分代码
 *
 * @author warm
 */
public abstract class ConditionStrategyAbstract implements ConditionStrategy {


    /**
     * 执行表达式前置方法 合法性校验
     *
     * @param split    表达式后缀：如flag@@eq@@4
     * @param variable 流程变量
     */
    public void preEval(String[] split, Map<String, Object> variable) {
        AssertUtil.isEmpty(variable, ExceptionCons.NULL_CONDITIONVALUE);
        Object o = variable.get(split[0].trim());
        AssertUtil.isNull(o, ExceptionCons.NULL_CONDITIONVALUE);
    }

    /**
     * 执行表达式
     *
     * @param expression 表达式
     * @param variable   流程变量
     * @return 执行结果
     */
    @Override
    public Boolean eval(String expression, Map<String, Object> variable) {
        String[] split = expression.split(FlowCons.splitAt);
        preEval(split, variable);
        String variableValue = String.valueOf(variable.get(split[0].trim()));
        return afterEval(split, variableValue);
    }

    /**
     * 执行表达式后置方法
     *
     * @param split 如flag@@eq@@4
     * @param value 流程变量值
     * @return 执行结果
     */
    public abstract Boolean afterEval(String[] split, String value);

}
```

### 3.3、条件表达式实现类
```java
/**
 * 条件表达式等于 @@eq@@|flag@@eq@@4
 *
 * @author warm
 */
public class ConditionStrategyEq extends ConditionStrategyAbstract {

    @Override
    public String getType() {
        return "eq";
    }

    @Override
    public Boolean afterEval(String[] split, String value) {
        if (MathUtil.isNumeric(split[2].trim())) {
            return MathUtil.determineSize(value, split[2].trim()) == 0;
        } else {
            return value.equals(split[2].trim());
        }
    }

}
```
