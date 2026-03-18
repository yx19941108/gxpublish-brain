# id生成器

::: tip
- 如果觉得内置的id，不符合业务要求，可通过修改配置文件，切换id生成器

:::

## 1. 功能介绍和使用



```yaml
# warm-flow工作流配置
warm-flow:
  # id生成器类型, 不填默认为orm扩展自带生成器或者warm-flow内置的19位雪花算法, 
  # SnowId14:14位，SnowId15:15位， SnowFlake19：19位
  key_type: SnowId19
```



## 2. 精度问题常见解决思路

[id精度丢失](../other/troubleshooting.html#_1、id精度丢失)

