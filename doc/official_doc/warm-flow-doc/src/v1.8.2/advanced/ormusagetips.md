# orm扩展包使用技巧

::: tip
- 组件本身提供常见并且基础的api，如果满足不了需求，可以使用orm自身的api

:::

## 1、mybatis-plus

**获取组件中的mapper，使用mybaits-plus的自带方法**
```java

// 第一种方式
@Resource
private FlowTaskMapper taskMapper;

// 第二种方式
FlowTaskMapper taskMapper = FrameInvoker.getBean(FlowTaskMapper.class);
```
