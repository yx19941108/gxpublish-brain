# 监听器

::: tip 
- 在办理流程过程中，通过监听器，监听办理过程的不同时期，进行业务处理，功能增强。  
- 支持类包名配置和表达式配置。

:::

## 1、监听器小类
- start：开始监听器，任务开始办理时执行，数据初始化，办理人权限设置等
- assignment： 分派办理人监听器，动态修改代办任务信息，但不限于
- finish：完成监听器，当前任务完成后执行，可对业务表更新，消息通知等
- create：创建监听器，任务创建时执行，比如数据初始化

## 2、监听器大类
- 节点监听器：在流程节点中配置，作用范围当前节点，只会执行小类中任意一个
- 流程监听器：在流程定义中配置，作用范围当前流程定义，所有节点会执行小类中任意一个     
- 全局监听器：实现全局监听器四个接口，作用范围当前所有流程，包含可执行所有小类
- 执行顺序：节点监听器 --> 流程监听器 --> 全局监听器

## 3、监听器生命周期图
<div><img src="https://foruda.gitee.com/images/1744189606356147579/9904c854_2218307.png" width="800"></div>


## 4、监听器设置
- 监听器设置：设置节点表的`listener_type`和`listener_path`字段，如果有多个监听器，用`,`分隔，并且两个字段对应  
- listener_type：监听器类型，如`start,assignment,finish,create`等  
- listener_path：监听器路径，支持配置类包名和表达式，如`包名1,表达式1,包名2,表达式2`等  
- 监听器路径：默认支持内置spel表达式，支持扩展，比如：`#{@assignmentExpListener.notify(#listenerVariable)}`  

## 5、匹配规则
- 默认先判断是否是监听器表达式，然后再去尝试加载类路径


## 5、节点和流程监听器
### 5.1、实现以下接口
```java
/**
 * 监听器接口
 */
public interface Listener extends Serializable {

    /** 创建监听器，任务创建时执行 */
    String LISTENER_CREATE = "create";

    /** 开始监听器，任务开始办理时执行 */
    String LISTENER_START = "start";

    /**
     * 完成监听器，当前任务完成后执行
     */
    String LISTENER_FINISH = "finish";

    /** 分派监听器，动态修改代办任务信息 */
    String LISTENER_ASSIGNMENT = "assignment";

    void notify(ListenerVariable variable);
}

```

### 5.2、开始监听器实现类例子
- 通过@Component或者@Bean注解注入到容器
```java
@Component
public class DefStartListener implements Listener {


  private static final Logger log = LoggerFactory.getLogger(DefStartListener.class);

  /**
   * 设置办理人id、所拥有的权限等操作，也可以放到业务代码中办理前设置，或者节点监听器
   * @param listenerVariable 监听器变量
   */
  @Override
  public void notify(ListenerVariable listenerVariable) {
    log.info("流程开始监听器");

    FlowParams flowParams = listenerVariable.getFlowParams();
    LoginUser user = SecurityUtils.getLoginUser();
    // 设置当前办理人id
    flowParams.setHandler(user.getUser().getUserId().toString());

    // 设置办理人所拥有的权限，比如角色、部门、用户等
    List<String> permissionList = flowParams.getPermissionFlag();
    if (StringUtils.isEmpty(permissionList)) {
      permissionList = new ArrayList<>();
    }

    List<SysRole> roles = user.getUser().getRoles();
    if (Objects.nonNull(roles)) {
      permissionList.addAll(roles.stream().map(role -> "role:" + role.getRoleId()).collect(Collectors.toList()));
    }
    permissionList.add("dept:" + SecurityUtils.getLoginUser().getUser().getDeptId());
    permissionList.add(user.getUser().getUserId().toString());
    flowParams.setPermissionFlag(permissionList);

    log.info("流程开始监听器结束......");
  }
}
```

### 5.3、分派监听器
如下图中示例可以很容易实现

<div><img src="/assignmentlistener.jpg" width="550px" height="450px" /></div>


- 注意：
  - 当前节点分派监听器：执行时修改【下个节点配置办理人权限】或者其他
  - 下个节点配置办理人权限策略：可设置自定义权限策略，比如发起人审批，部门领导审批等
```java
@Component
public class AssignmentListener implements Listener {

    private static final Logger log = LoggerFactory.getLogger(AssignmentListener.class);

    @Override
    public void notify(ListenerVariable variable) {
        log.info("分派监听器开始执行......");
        List<Task> tasks = variable.getNextTasks();
        Instance instance = variable.getInstance();
        for (Task task : tasks) {
            List<String> permissionList = task.getPermissionList();
            // 如果设置了发起人审批，则需要动态替换权限标识
            for (int i = 0; i < permissionList.size(); i++) {
                String permission = permissionList.get(i);
                if (StringUtils.isNotEmpty(permission) && permission.contains(FlowCons.WARMFLOWINITIATOR)) {
                    permissionList.set(i, permission.replace(FlowCons.WARMFLOWINITIATOR, instance.getCreateBy()));
                }
            }
        }
        log.info("分派监听器执行结束......");
    }
}
```

### 5.4、完成监听器实现类例子
```java
@Component
public class DefFinishListener implements Listener {


  private static final Logger log = LoggerFactory.getLogger(DefFinishListener.class);

  @Resource
  private TestLeaveMapper testLeaveMapper;

  /**
   * 业务表新增或者更新操作，也可以放到业务代码中办理完成后，或者节点监听器
   * @param listenerVariable 监听器变量
   */
  @Override
  public void notify(ListenerVariable listenerVariable) {
    log.info("流程完成监听器");
    Instance instance = listenerVariable.getInstance();
    Map<String, Object> variable = listenerVariable.getVariable();
    if (StringUtils.isNotNull(variable)) {
        String businessId = instance.getBusinessId();
        Object businessType = variable.get("businessType");
        /** 如果{@link com.ruoyi.system.service.impl.TestLeaveServiceImpl}中更新了，这里就不用更新了*/
        // 更新业务数据
        if ("testLeave".equals(businessType)) {
            // 可以统一使用一个流程监听器，不同实体类，不同的操作
            TestLeave testLeave = testLeaveMapper.selectTestLeaveById(businessId);
            if (ObjectUtil.isNull(testLeave)) {
                testLeave = (TestLeave) variable.get("businessData");
            }
            testLeave.setNodeCode(instance.getNodeCode());
            testLeave.setNodeName(instance.getNodeName());
            testLeave.setNodeType(instance.getNodeType());
            testLeave.setFlowStatus(instance.getFlowStatus());
            // 如果没有实例id，说明是新增
            if (ObjectUtil.isNull(testLeave.getInstanceId())) {
                testLeave.setInstanceId(instance.getId());
                testLeaveMapper.insertTestLeave(testLeave);
                testLeave.setCreateTime(DateUtils.getNowDate());
                // 新增抄送人方法，也可发送通知
                if (StringUtils.isNotNull(testLeave.getAdditionalHandler())) {
                    List<User> users = FlowEngine.userService().structureUser(instance.getId()
                            , testLeave.getAdditionalHandler(), "4");
                    FlowEngine.userService().saveBatch(users);
                }
            } else {
                testLeave.setUpdateTime(DateUtils.getNowDate());
                testLeaveMapper.updateTestLeave(testLeave);
            }
        }
    }

    log.info("流程完成监听器结束......");
  }
}
```

### 5.5、创建监听器
- 就是在下一个任务生成前执行，比如创建任务前需要初始化信息或者校验数据是否合法

### 5.6、页面配置全局或节点监听器
#### 5.6.1、节点监听器（流程节点配置）

<div><img src="https://foruda.gitee.com/images/1732545153700629064/3183155f_2218307.png" width="500"></div>


#### 5.6.1、流程监听器（流程定义配置）

<div><img src="https://foruda.gitee.com/images/1732548175204139076/1f88c928_2218307.png" width="600px"></div>

## 6、全局监听器

- 全局监听器，只需要实现GlobalListener接口, 按照实际业务需求选择实现一个方法或者多个方法。

```java
/**
 * 全局监听器: 整个系统只有一个，任务开始、分派、完成和创建时期执行
 *
 * @author warm
 * @since 2024/11/17
 */
@Component
public class CustomGlobalListener implements GlobalListener {

    private static final Logger log = LoggerFactory.getLogger(CustomGlobalListener.class);

    /**
     * 开始监听器，任务开始办理时执行
     * @param listenerVariable 监听器变量
     */
    public void start(ListenerVariable listenerVariable) {
        log.info("全局开始监听器开始执行......");

        log.info("全局开始监听器执行结束......");

    }

    /**
     * 分派监听器，动态修改代办任务信息
     * @param listenerVariable  监听器变量
     */
    public void assignment(ListenerVariable listenerVariable) {
        log.info("全局分派监听器开始执行......");

        log.info("全局分派监听器执行结束......");
    }

    /**
     * 完成监听器，当前任务完成后执行
     * @param listenerVariable  监听器变量
     */
    public void finish(ListenerVariable listenerVariable) {
        log.info("全局完成监听器开始执行......");

        log.info("全局完成监听器执行结束......");
    }

    /**
     * 创建监听器，任务创建时执行
     * @param listenerVariable  监听器变量
     */
    public void create(ListenerVariable listenerVariable) {
        log.info("全局创建监听器开始执行......");

        log.info("全局创建监听器执行结束......");
    }

}
```

## 7、监听器参数使用

- 页面配置监听器时加上类路径

<div><img src="https://foruda.gitee.com/images/1732548102324679120/544ff483_2218307.png" width="600px"></div>

```java
    public void notify(ListenerVariable variable) {
        Instance instance = variable.getInstance();
        Map<String, Object> variableMap = variable.getVariable();
        // 拿到json后使用序列化可以拿到配置信息
        Map<String, Object> variableMap = variable.getVariable();
        if (MapUtil.isNotEmpty(variableMap)) {
            Object o = variableMap.get(FlowCons.WARM_LISTENER_PARAM);
            HashMap hashMap = JSONObject.parseObject(JSONObject.toJSONString(o), HashMap.class);
        }
        log.info("创建监听器结束");
    }
```
