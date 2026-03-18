# 配置yml和定义json

## yml配置文件

::: code-tabs#shell

@tab:active springboot

```yml
# warm-flow工作流配置
warm-flow:
  # 是否开启工作流，默认true
  enabled: true
  # 是否显示banner图，默认是
  banner: true
  # 是否开启设计器ui，默认true
  ui: true
  # id生成器类型, 不填默认为orm扩展自带生成器或者warm-flow内置的19位雪花算法, SnowId14:14位，SnowId15:15位， SnowFlake19：19位
  key_type: SnowId19
  # 填充器，内部有默认实现，如果不满足实际业务，可通过此配置自定义实现
  data-fill-handler-path: com.ruoyi.system.handle.CustomDataFillHandler
  # 全局租户处理器，有多租户需要，可以配置自定义实现
  tenant_handler_path: com.ruoyi.system.handle.CustomTenantHandler
  # 是否开启逻辑删除（orm框架本身不支持逻辑删除，可通过这种方式开启）
  logic_delete: true
  # 逻辑删除字段值（开启后默认为2）
  logic_delete_value: 2
  ## 如果需要工作流共享业务系统权限，默认Authorization，如果有多个token，用逗号分隔
  token-name: Authorization1
  # 逻辑未删除字段（开启后默认为0）
  logic_not_delete_value: 0
  # 是否显示流程图顶部文字，默认为true：显示
  top-text-show: true
  # 当使用JPA时指定JpaPersistenceProvider
  jpa_persistence_provider: org.springframework.orm.jpa.vendor.SpringHibernateJpaPersistenceProvider
  # 内部已实现自动获取，失效时使用此配置（在使用mybatis扩展包时, 由于各数据库sql语句存在差异, 通过此配置兼容，默认为mysql）
  data_source_type: mysql
```

@tab solon

```yaml
# warm-flow工作流配置
warm-flow:
  # 是否开启工作流，默认true
  enabled: true
  # 是否显示banner图，默认是
  banner: true
  # 是否开启设计器ui，默认true
  ui: true
  # id生成器类型, 不填默认为orm扩展自带生成器或者warm-flow内置的19位雪花算法, SnowId14:14位，SnowId15:15位， SnowFlake19：19位
  keyType: SnowId19
  # 填充器，内部有默认实现，如果不满足实际业务，可通过此配置自定义实现
  dataFillHandlerPath: com.ruoyi.system.handle.CustomDataFillHandler
  # 全局租户处理器，有多租户需要，可以配置自定义实现
  tenantHandlerPath: com.ruoyi.system.handle.CustomTenantHandler
  # 是否开启逻辑删除（orm框架本身不支持逻辑删除，可通过这种方式开启）
  logicDelete: true
  # 逻辑删除字段值（开启后默认为2）
  logicDeleteValue: 2
  ## 如果需要工作流共享业务系统权限，默认Authorization，如果有多个token，用逗号分隔
  tokenName: Authorization1
  # 逻辑未删除字段（开启后默认为0）
  logicNotDeleteValue: 0
  # 是否显示流程图顶部文字，默认为true：显示
  topTextShow: true
  # 内部已实现自动获取，失效时使用此配置（在使用mybatis扩展包时, 由于各数据库sql语句存在差异, 通过此配置兼容，默认为mysql）
  data_source_type: mysql
```

:::

## 流程定义json

```yaml
{
  "flowCode": "leaveFlow-serial3",                  -- 流程编码       
  "flowName": "串行-驳回互斥",						-- 流程名称
  "category": "请假",								-- 流程类别  
  "formCustom": "N",                                -- 审批表单是否自定义（Y是 N否）
  "formPath": "system/leave/approve",               -- 审批表单路径
  "listenerPath": "x.x@@x.x@@x.x",                  -- 流程监听器路径，全限定名
  "listenerType": "start,assignment,finish",        -- 流程监听器类型
  "version": "1",                                   -- 流程版本
  "ext": "xxx",                                     -- 扩展字段，预留给业务系统使用
  "nodeList": [                                     -- 流程节点集合
    {                                               
      "coordinate": "380,200|380,200",              -- 流程节点坐标
      "nodeCode": "2",                              -- 节点编码，definitionId+nodeCode唯一
      "nodeName": "组长审批",                        -- 节点名称
      "nodeRatio": 0.000,                           -- 流程签署比例值, 0:或签，0-100：票签，100：会签
      "nodeType": 1,                                -- 节点类型（0开始节点 1中间节点 2结束节点 3互斥网关 4并行网关）
      "permissionFlag": "1@@role:1",                 -- 权限标识（权限类型:权限标识，可以多个，用@@隔开)
	  "anyNodeSkip": "1",                           -- 任意结点跳转，目标节点编码
      "listenerPath": "x.x@@x.x@@x.x",              -- 节点监听器路径，全限定名
      "listenerType": "start,assignment,finish",    -- 节点监听器类型
	  "formCustom": "N",                            -- 审批表单是否自定义（Y是 2否），不同节点可设置不同审批页面
      "formPath": "system/leave/approve",           -- 审批表单路径
	  "ext": "xxx",                                 -- 节点扩展属性
      "skipList": [                                 -- 跳转线集合
        {                                          
          "coordinate": "430,200;550,200",          -- 流程跳转坐标
          "nowNodeCode": "2",                       -- 当前流程节点的编码
          "nextNodeCode": "3",                      -- 下一个流程节点的编码
          "skipName": "xx",                         -- 跳转名称
          "skipType": "PASS"                        -- 跳转类型（PASS审批通过 REJECT退回）
          "skipCondition": "gt@@flag|4",            -- 跳转条件
        }
      ]
    },
  ]
}
```
