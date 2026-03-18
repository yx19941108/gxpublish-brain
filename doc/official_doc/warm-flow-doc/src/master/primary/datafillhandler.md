# 数据填充器

::: tip
- 如果觉得内置的id、创建时间和更新时间自动生成规则，不符合业务要求，可通过填充器覆盖

:::

## 1、自定义填充器，并继承DataFillHandler
```java
public class CustomDataFillHandler implements DataFillHandler {

    @Override
    public void idFill(Object object) {
        RootEntity entity = (RootEntity) object;
        if (ObjectUtil.isNotNull(entity)) {
            if (Objects.isNull(entity.getId())) {
                entity.setId(IdUtils.nextId());
            }
        }
    }

    @Override
    public void insertFill(Object object) {
        RootEntity entity = (RootEntity) object;
        if (ObjectUtil.isNotNull(entity)) {
            entity.setCreateTime(ObjectUtil.isNotNull(entity.getCreateTime()) ? entity.getCreateTime() : new Date());
            entity.setUpdateTime(ObjectUtil.isNotNull(entity.getUpdateTime()) ? entity.getUpdateTime() : new Date());

            PermissionHandler permissionHandler = FlowEngine.permissionHandler();
            String handler = null;
            if (permissionHandler != null) {
                handler = permissionHandler.getHandler();
            }
            entity.setCreateBy(StringUtils.isNotEmpty(handler) ? handler : entity.getCreateBy());
            entity.setUpdateBy(StringUtils.isNotEmpty(handler) ? handler : entity.getUpdateBy());
        }
    }

    @Override
    public void updateFill(Object object) {
        RootEntity entity = (RootEntity) object;
        if (ObjectUtil.isNotNull(entity)) {
            entity.setUpdateTime(ObjectUtil.isNotNull(entity.getUpdateTime()) ? entity.getUpdateTime() : new Date());

            PermissionHandler permissionHandler = FlowEngine.permissionHandler();
            String handler = null;
            if (permissionHandler != null) {
                handler = permissionHandler.getHandler();
            }
            entity.setUpdateBy(StringUtils.isNotEmpty(handler) ? handler : entity.getUpdateBy());
        }
    }
}

```

## 2、注入bean
::: code-tabs#shell

@tab:active yaml

```yml
# warm-flow工作流配置
warm-flow:
  # 填充器，内部有默认实现，如果不满足实际业务，可通过此配置自定义实现
  data-fill-handler-path: com.ruoyi.system.handle.CustomDataFillHandler
```

@tab @Bean

```java
@Configuration
public class WarmFlowConfig {

    @Bean
    public DataFillHandler dataFillHandler() {
        return new CustomDataFillHandler();
    }
}
```

@tab @Component

```java
@Component
public class CustomDataFillHandler implements DataFillHandler {

    ...实际代码
}
```

:::

