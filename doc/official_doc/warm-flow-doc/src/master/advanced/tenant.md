# 多租户

## 1、Mybatis-plus
::: tip
- Mybatis-plus只支持自身的多租户方式  

:::

::: code-tabs#shell

@tab:active springboot

```java
@Component
public class MpTenantHandler implements TenantLineHandler {

    ThreadLocal<String> threadLocal = new ThreadLocal<>();

    @Override
    public Expression getTenantId() {
        // 返回租户ID的表达式，LongValue 是 JSQLParser 中表示 bigint 类型的 class
        return new LongValue(2);
    }

    @Override
    public String getTenantIdColumn() {
        return threadLocal.get();
    }


    /**
     * 指定租户字段
     * @param tableName 表名
     * @return
     */
    @Override
    public boolean ignoreTable(String tableName) {
        TableInfo tableInfo = TableInfoHelper.getTableInfo(tableName);
        List<TableFieldInfo> fieldList = tableInfo.getFieldList();
        fieldList.forEach(field -> {
            // 如果业务和工作流引擎中的租户字段不一致，可以通过这种方式动态切换
            if (field.getColumn().equals("tenant_id") || field.getColumn().equals("tenant_code")) {
                threadLocal.set(field.getColumn());
            }
        });
        // 获取表字段
        return false;
    }

    /**
     * 如果业务系统不开启租户，使用下面方法，指定流程表才开启
     * @param tableName 表名
     * @return
     */
    @Override
    public boolean ignoreTable(String tableName) {
        // 流程表
        List<String> flowTableName = Arrays.asList("flow_definition", "flow_his_task", "flow_instance", "flow_node"
                ,"flow_skip", "flow_task", "flow_user");
        TableInfo tableInfo = TableInfoHelper.getTableInfo(tableName);
        AtomicBoolean flag = new AtomicBoolean(true);
        if (flowTableName.contains(tableInfo.getTableName())) {
            flag.set(false);
        }
        List<TableFieldInfo> fieldList = tableInfo.getFieldList();
        fieldList.forEach(field -> {
            // 如果业务和工作流引擎中的租户字段不一致，可以通过这种方式动态切换
            if (field.getColumn().equals("tenant_id")) {
                threadLocal.set(field.getColumn());
            }
        });
        // 获取表字段
        return flag.get();
    }

}

@Configuration
public class MybatisPlusConfig {

    @Resource
    private MpTenantHandler mpTenantHandler;

    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        TenantLineInnerInterceptor tenantInterceptor = new TenantLineInnerInterceptor();
        tenantInterceptor.setTenantLineHandler(mpTenantHandler);
        interceptor.addInnerInterceptor(tenantInterceptor);
        return interceptor;
    }
}
```

@tab solon

```java
public class MpTenantHandler implements TenantLineHandler {

    ThreadLocal<String> threadLocal = new ThreadLocal<>();

    @Override
    public Expression getTenantId() {
        // 返回租户ID的表达式，LongValue 是 JSQLParser 中表示 bigint 类型的 class
        return new LongValue(2);
    }

    @Override
    public String getTenantIdColumn() {
        return threadLocal.get();
    }

    @Override
    public boolean ignoreTable(String tableName) {
        TableInfo tableInfo = TableInfoHelper.getTableInfo(tableName);
        List<TableFieldInfo> fieldList = tableInfo.getFieldList();
        fieldList.forEach(field -> {
            // 如果业务和工作流引擎中的租户字段不一致，可以通过这种方式动态切换
            if (field.getColumn().equals("tenant_id") || field.getColumn().equals("tenant_code")) {
                threadLocal.set(field.getColumn());
            }
        });
        // 获取表字段
        return false;
    }
}

@Configuration
public class WarmFlowConfig {
    @Bean(value = "db1", typed = true)
    public DataSource db1(@Inject("${demo.db1}") HikariDataSource ds) {
        return ds;
    }

    /**
     * MybatisPlus全局配置
     *
     * @param globalConfig 数据源
     */
    @Bean
    public void db1_cfg(@Db("db1") MybatisConfiguration cfg,
                        @Db("db1") GlobalConfig globalConfig) {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        // 多租户插件 必须放到第一位
        interceptor.addInnerInterceptor(tenantLineInnerInterceptor());
        cfg.addInterceptor(interceptor);
    }

    /**
     * 多租户插件
     */
    public TenantLineInnerInterceptor tenantLineInnerInterceptor() {
        return new TenantLineInnerInterceptor(new MpTenantHandler());
    }
}
```

:::


## 2、通用多租户
::: code-tabs#shell

@tab:active yaml

```yaml
# warm-flow工作流配置
warm-flow:
  # 全局租户处理器（可通过配置文件注入，也可用@Bean/@Component方式
  tenant_handler_path: org.dromara.warm.flow.core.test.handle.CustomTenantHandler
```

@tab @bean

```java
@Configuration
public class WarmFlowConfig {
    /**
     * 全局租户处理器（可通过配置文件注入，也可用@Bean/@Component方式
     */
    @Bean
    public TenantHandler tenantHandler() {
        return new CustomTenantHandler();
    }
}

```

@tab @Component

```java
/**
 * 全局租户处理器（可通过配置文件注入，也可用@Bean/@Component方式
 *
 * @author warm
 */
@Component
public class CustomTenantHandler implements TenantHandler {


    @Override
    public String getTenantId() {
        // 这里返回系统中的当前办理人的租户ID，一般会有工具类获取
        return "000000";
    }
}
```

:::


