# 设计器集成

::: tip
- 为了方便业务系统快速引入设计器，不需要搬运并且适配等工作  
- 可以按照本文中介绍的，使用设计器，并快速接入业务系统  
- 设计原理采取不分离的方式，把设计器打包的jar包中,以接口和静态资源的方式引入
:::

<div class="yat"><img src="https://foruda.gitee.com/images/1750866311675627647/4fff1881_2218307.png"/></div>
<div class="yat"><div><img src="https://foruda.gitee.com/images/1754530281717340950/b531c256_2218307.png"/></div></div>
<div class="yat"><div><img src="https://foruda.gitee.com/images/1754530582498275502/be3acb55_2218307.png"/></div></div>



## 1. 引入依赖
::: code-tabs#shell

@tab:active springboot

```xml
<dependency>
  <groupId>org.dromara.warm</groupId>
  <artifactId>warm-flow-plugin-ui-sb-web</artifactId>
  <version>版本号</version>
</dependency>
```

@tab solon

```xml
<dependency>
  <groupId>org.dromara.warm</groupId>
  <artifactId>warm-flow-plugin-ui-solon-web</artifactId>
  <version>版本号</version>
</dependency>
```

:::

## 2. 单体项目
### 2.1 后端放行部分路径
> 这个路径需要放行，否则无法访问，`/warm-flow-ui/**`, `/warm-flow/**`

::: code-tabs#shell

@tab:active spring-security

```java
@Bean
protected SecurityFilterChain filterChain(HttpSecurity httpSecurity) throws Exception
{
    return httpSecurity
            .......
            // 注解标记允许匿名访问的url
            .authorizeHttpRequests((requests) -> {
                // 后端请求，静态资源，可匿名访问
                requests.antMatchers("/warm-flow-ui/**", "/warm-flow/**").permitAll()
                        // 除上面外的所有请求全部需要鉴权认证
                        .anyRequest().authenticated();
            })
            ......
            .build();
}
```

@tab sa-token

```java
@Configuration
public class SaTokenConfigure implements WebMvcConfigurer {
    // 注册拦截器
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new SaInterceptor(handle -> StpUtil.checkLogin()))
                .addPathPatterns("/**")
                // 以上是sa-token案例，下面才是需要排除的地址
                .excludePathPatterns("/warm-flow-ui/**", "/warm-flow/**");
    }
}
```

@tab shiro

```java
@Configuration
public class ShiroConfig {

    /**
     * Shiro过滤器配置
     */
    @Bean
    public ShiroFilterFactoryBean shiroFilterFactoryBean(SecurityManager securityManager) {
        CustomShiroFilterFactoryBean shiroFilterFactoryBean = new CustomShiroFilterFactoryBean();
        ......
        // 后端请求，静态资源，可匿名访问
        LinkedHashMap<String, String> filterChainDefinitionMap = new LinkedHashMap<>();
        filterChainDefinitionMap.put("/warm-flow-ui/**", "anon");
        filterChainDefinitionMap.put("/warm-flow/**", "anon");

        shiroFilterFactoryBean.setFilterChainDefinitionMap(filterChainDefinitionMap);
        return shiroFilterFactoryBean;
    }
}
```

:::

### 2.2. 前端引入设计器
::: tip
**1、设计器页面入口是访问后端地址(前后端不分离)：`ip:port/warm-flow-ui/index.html?id=${definitionId}&onlyDesignShow=${onlyDesignShow}&Authorization=${token}`**
- definitionId：流程定义id，<span class="red-font">如果没传，则认定是新增流程，会初始化流程节点，否则则是编辑或者查看</span>
- onlyDesignShow：是否独显流程设计，不传默认显示基础信息和流程设计tabs
- token：用户token，[共享后端权限(如token)](./designerIntroduced.html#_6-共享后端权限-如token)
:::

::: code-tabs#shell

@tab:active vue2

```vue
<template>
  <div :style="'height:' + height">
    <iframe :src="url" style="width: 100%; height: 100%"/>
  </div>
</template>
<script>

  export default {
    name: "WarmFlow",
    data() {
      return {
        height: document.documentElement.clientHeight - 94.5 + "px;",
        url: ""
      };
    },
    mounted() {
      this.url = process.env.VUE_APP_BASE_API + `/warm-flow-ui/index.html?id=${definitionId}&onlyDesignShow=${onlyDesignShow}`;
      this.iframeLoaded();
    },
    methods: {
      // iframe监听组件内设计器保存事件
      iframeLoaded() {
        window.onmessage = (event) => {
          console.log(event);
          switch (event.data.method) {
            case "close":
              this.close();
              break;
          }
        }
      },
      close() {
        // 路由参数传递时间戳 来触发页面刷新
        const obj = { path: "/flow/definition", query: { t: Date.now(), pageNum: this.$route.query.pageNum } };
        this.$tab.closeOpenPage(obj);
      }
    }
  };
</script>
```

@tab vue3

```vue
<template>
  <div class="container" ref="container">
    <iframe ref="iframe" :src="iframeUrl" frameborder="0" width="100%" height="100%"></iframe>
  </div>
</template>

<script setup name="WarmFlow">
const { proxy } = getCurrentInstance();
import { onMounted } from 'vue';

const iframeUrl = ref(import.meta.env.VITE_APP_BASE_API + `/warm-flow-ui/index.html?id=${definitionId}&onlyDesignShow=${onlyDesignShow}`);

const iframeLoaded = () => {
  // iframe监听组件内设计器保存事件
  window.onmessage = (event) => {
    switch (event.data.method) {
      case "close":
        close();
        break;
    }
  }
};

/** 关闭按钮 */
function close() {
  // 路由参数传递时间戳 来触发页面刷新
  const obj = { path: "/flow/definition", query: { t: Date.now(), pageNum: this.$route.query.pageNum } };
  this.$tab.closeOpenPage(obj);
}

onMounted(() => {
  iframeLoaded();
});
</script>

<style scoped>
.container {
  width: 100%;
  height: calc(100vh - 84px);
}
</style>

```

@tab 前后端不分离

```java
可以直接访问后端接口加载页面，如：`ip:port/warm-flow-ui/index.html?id=${definitionId}&onlyDesignShow=${onlyDesignShow}`

@Controller
@RequestMapping("/warm-flow")
public class WarmFlowController
{
    @GetMapping()
    public String index(String definitionId, Boolean onlyDesignShow)
    {
        return redirect("/warm-flow-ui/index.html?id=" + definitionId + "&onlyDesignShow=" + onlyDesignShow);
    }
}

或者前端直接访问后端接口，如：`/warm-flow-ui/index.html?id=1839683148936663047&onlyDesignShow=false`
/*打开新的页签，加载设计器*/
function detail(dictId) {
  var url = prefix + '/detail/' + dictId;
  $.modal.openTab("字典数据", "/warm-flow-ui/index.html?id=1839683148936663047&onlyDesignShow=false");
}

```

@tab React

```shell
待完善
```

:::

## 3. 微服务集成
微服务集成原来和单体类似，都是后端放行部分路径，前端加载页面，<span style="color: red;">比如以RuoYi-Cloud为例</span>

### 2.1 后端放行部分路径
> 与单体不同，需要在gateway里面放行

```yaml {12-13}
# 安全配置
security:
  # 不校验白名单
  ignore:
    whites:
      - /auth/logout
      - /auth/login
      - /auth/register
      - /*/v2/api-docs
      - /*/v3/api-docs
      - /csrf
	  - /warm-flow-ui/**
	  - /warm-flow/**
```

<br/>

> 这是RuoYi-Cloud网关服务的过滤器，根据上面的白名单进行放行部分代码

```java
/**
 * 放行白名单配置
 *
 * @author RuoYi
 */
@Configuration
@RefreshScope
@ConfigurationProperties(prefix = "security.ignore")
public class IgnoreWhiteProperties {
    /**
     * 放行白名单配置，网关不校验此处的白名单
     */
    private List<String> whites = new ArrayList<>();

    public List<String> getWhites() {
        return whites;
    }

    public void setWhites(List<String> whites) {
        this.whites = whites;
    }
}

```

```java
/**
 * 网关鉴权
 *
 * @author RuoYi
 */
@Component
public class AuthFilter implements GlobalFilter, Ordered {
    private static final Logger log = LoggerFactory.getLogger(AuthFilter.class);

    // 排除过滤的 uri 地址，nacos自行添加
    @Autowired
    private IgnoreWhiteProperties ignoreWhite;


    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        ServerHttpRequest.Builder mutate = request.mutate();

        String url = request.getURI().getPath();
        // 跳过不需要验证的路径
        if (StringUtils.matches(url, ignoreWhite.getWhites())) {
            return chain.filter(exchange);
        }
        .... 其他校验
        return chain.filter(exchange.mutate().request(mutate.build()).build());
    }
}
```
<br>

> 配置网关路由
```yaml
spring:
  cloud:
    gateway:
      routes:
        # 流程服务
        - id: ruoyi-flow
          uri: lb://ruoyi-flow
          predicates:
            - Path=/flow/**
          filters:
            - StripPrefix=1
```

### 3.2. 前端引入设计器
::: tip
**设计器引入和单体类似，不过要多加一个网关路由`flow`，示例：`ip:port/flow/warm-flow-ui/index.html?id=${definitionId}&onlyDesignShow=${onlyDesignShow}`**

:::

<br>

## 4. 共享后端权限(如token)
- 后端放行路径`/warm-flow-ui/**,/warm-flow/**`，改为只放行一个`/warm-flow-ui/**`
- 在前端加载设计器页面路径后面，追加&Authorization=${token}，token是业务系统的token，可追加多个token
- yml中配置`warm-flow.token-name=Authorization`,每次请求会把token, set到header的`Authorization`上，多个token用逗号分隔
- 请注意：请求中的token的名称或者key，要和后端yml中配置一致
```yml
# warm-flow工作流配置
warm-flow:
  ......
  ## 如果需要工作流共享业务系统权限，默认Authorization，如果有多个token，用逗号分隔
  token-name: Authorization
  ......
```

## 5. 设计器办理人选择框接入
> 给任务节点设置哪些权限的人可以办理，实现接口提供给设计器

### 5.1 办理人权限选择弹框页面

<div><img src="https://foruda.gitee.com/images/1745571554195473679/ca966032_2218307.png"></div>
<div><img src="https://foruda.gitee.com/images/1742804225175791843/02ddc1bd_2218307.png"></div>
<br>

### 5.2 实现接口获取办理人列表数据

#### 5.2.1 HandlerSelectService接口
```java
/**
 * 流程设计器-获取办理人权限设置列表接口
 *
 * @author warm
 */
public interface HandlerSelectService {

  /**
   * 获取办理人权限设置列表tabs页签, 如：用户、角色和部门等
   * @return tabs页签
   */
  List<String> getHandlerType();

  /**
   * 获取办理人权限设置列表结果，如：用户列表、角色列表、部门列表等
   * @param query 查询参数
   * @return 结果
   */
  List<HandlerSelectVo> getHandlerSelect(HandlerQuery query);
}

```
<br>

#### 5.2.2 HandlerSelectServiceImpl实现类

```java
/**
 * 流程设计器-获取办理人权限设置列表接口实现类
 *
 * @author warm
 */
@Component
public class HandlerSelectServiceImpl implements HandlerSelectService {
  @Autowired
  private SysUserMapper userMapper;

  @Autowired
  private SysRoleMapper roleMapper;

  @Autowired
  private SysDeptMapper deptMapper;

  /**
   * 获取办理人权限设置列表tabs页签，如：用户、角色和部门等，可以返回其中一种或者多种，按业务需求决定
   * @return tabs页签
   */
  @Override
  public List<String> getHandlerType() {
    return Arrays.asList("用户", "角色", "部门");
  }

  /**
   * 获取用户列表、角色列表、部门列表等，可以返回其中一种或者多种，按业务需求决定
   * @param query 查询参数
   * @return 结果
   */
  @Override
  public HandlerSelectVo getHandlerSelect(HandlerQuery query) {

    if ("角色".equals(query.getHandlerType())) {
      return getRole(query);
    }

    if ("部门".equals(query.getHandlerType())) {
      return getDept(query);
    }

    if ("用户".equals(query.getHandlerType())) {
      return getUser(query);
    }

    return new HandlerSelectVo();
  }

  /**
   * 获取角色列表
   *
   * @param query 查询条件
   * @return HandlerSelectVo
   */
  private HandlerSelectVo getRole(HandlerQuery query) {
        ......
    // 查询角色列表
    List<SysRole> roleList = roleMapper.selectRoleList(sysRole);
    long total = new PageInfo<>(roleList).getTotal();

    // 业务系统数据，转成组件内部能够显示的数据, total是业务数据总数，用于分页显示
    HandlerFunDto<SysRole> handlerFunDto = new HandlerFunDto<>(roleList, total)
            // 以下设置获取内置变量的Function
            .setStorageId(role -> "role:" + role.getRoleId()) // 前面拼接role:  是为了防止用户、角色的主键重复
            .setHandlerCode(SysRole::getRoleKey) // 权限编码
            .setHandlerName(SysRole::getRoleName) // 权限名称
            .setCreateTime(role -> DateUtils.parseDateToStr(DateUtils.YYYY_MM_DD_HH_MM_SS, role.getCreateTime()));

    return getHandlerSelectVo(handlerFunDto);
  }

  /**
   * 获取用户列表
   *
   * @param query 查询条件
   * @return HandlerSelectVo
   */
  private HandlerSelectVo getDept(HandlerQuery query) {
        ......
    // 查询部门列表
    List<SysDept> deptList = deptMapper.selectDeptList(sysDept);
    long total = new PageInfo<>(deptList).getTotal();

    // 业务系统数据，转成组件内部能够显示的数据, total是业务数据总数，用于分页显示
    HandlerFunDto<SysDept> handlerFunDto = new HandlerFunDto<>(deptList, total)
            .setStorageId(dept -> "dept:" + dept.getDeptId()) // 前面拼接dept:  是为了防止用户、部门的主键重复
            .setHandlerName(SysDept::getDeptName) // 权限名称
            .setCreateTime(dept -> DateUtils.parseDateToStr(DateUtils.YYYY_MM_DD_HH_MM_SS, dept.getCreateTime()));

    return getHandlerSelectVo(handlerFunDto);

  }

  /**
   * 获取用户列表, 同时构建左侧部门树状结构
   *
   * @param query 查询条件
   * @return HandlerSelectVo
   */
  private HandlerSelectVo getUser(HandlerQuery query) {
        ......
    // 查询用户列表
    List<SysUser> userList = userMapper.selectUserList(sysUser);
    long total = new PageInfo<>(userList).getTotal();
    // 查询部门列表，构建树状结构
    List<SysDept> deptList = deptMapper.selectDeptList(new SysDept());

    // 业务系统数据，转成组件内部能够显示的数据, total是业务数据总数，用于分页显示
    HandlerFunDto<SysUser> handlerFunDto = new HandlerFunDto<>(userList, total)
            .setStorageId(user -> user.getUserId().toString())
            .setHandlerCode(SysUser::getUserName) // 权限编码
            .setHandlerName(SysUser::getNickName) // 权限名称
            .setCreateTime(user -> DateUtils.parseDateToStr(DateUtils.YYYY_MM_DD_HH_MM_SS, user.getCreateTime()))
            .setGroupName(user -> user.getDept() != null ? user.getDept().getDeptName() : "");

    // 业务系统机构，转成组件内部左侧树列表能够显示的数据
    TreeFunDto<SysDept> treeFunDto = new TreeFunDto<>(deptList)
            .setId(dept -> dept.getDeptId().toString()) // 左侧树ID
            .setName(SysDept::getDeptName) // 左侧树名称
            .setParentId(dept -> dept.getParentId().toString()); // 左侧树父级ID

    return getHandlerSelectVo(handlerFunDto, treeFunDto);
  }
}
```

## 6. 设计器办理人列表回显
> 回显该节点权限办理人名称，比如上一步选择后保存入库主键，下一次重新打开以下页面

### 6.1 设计器办理人列表页面
<div><img src="https://foruda.gitee.com/images/1745570346631861131/f5ba4bf7_2218307.png" width="700"></div>
<br>

### 6.2 实现接口获取办理人列表回显
#### 6.2.1 HandlerSelectService接口
- `handlerFeedback()`是默认方法，基于以上第四小节中的`getHandlerType`和`getHandlerSelect`实现，但是性能会比较差
- 建议重新实现该接口，通过入库主键集合`storageIds`，重新覆盖查询，下面会会有实现案例

```java {17,21}
/**
 * 流程设计器-获取办理人权限设置列表接口
 *
 * @author warm
 */
public interface HandlerSelectService {

    /**
     * 办理人权限名称回显，兼容老项目，新项目重写提高性能
     *
     * @param storageIds 入库主键集合
     * @return 结果
     */
    default List<HandlerFeedBackVo> handlerFeedback(List<String> storageIds) {
        List<HandlerFeedBackVo> handlerFeedBackVos = new ArrayList<>();
        Map<String, String> authMap = new HashMap<>();
        List<String> handlerTypes = getHandlerType();
        for (String handlerType : handlerTypes) {
            HandlerQuery handlerQuery = new HandlerQuery();
            handlerQuery.setHandlerType(handlerType);
            HandlerSelectVo handlerSelectVo = getHandlerSelect(handlerQuery);
            if (ObjectUtil.isNotNull(handlerSelectVo)) {
                FlowPage<HandlerAuth> handlerAuths = handlerSelectVo.getHandlerAuths();
                List<HandlerAuth> rows = handlerAuths.getRows();
                if (CollUtil.isNotEmpty(rows)) {
                    authMap.putAll(StreamUtils.toMap(rows, HandlerAuth::getStorageId, HandlerAuth::getHandlerName));
                }
            }
        }

        // 遍历storageIds，按照原本的顺序回显名称
        for (String storageId : storageIds) {
            handlerFeedBackVos.add(new HandlerFeedBackVo(storageId
                    , MapUtil.isEmpty(authMap) ? "": authMap.get(storageId)));
        }
        return handlerFeedBackVos;
    }
}

```

#### 6.2.2 HandlerSelectServiceImpl实现类

```java
/**
 * 流程设计器-获取办理人权限设置列表接口实现类
 *
 * @author warm
 */
@Component
public class HandlerSelectServiceImpl implements HandlerSelectService {
    @Resource
    private WarmFlowMapper warmFlowMapper;

    @Override
    public List<HandlerFeedBackVo> handlerFeedback(List<String> storageIds) {
        List<HandlerFeedBackVo> handlerFeedBackVos = new ArrayList<>();
        if (CollUtil.isNotEmpty(storageIds)) {
            List<Long> roleIdList = new ArrayList<>();
            List<Long> deptIdList = new ArrayList<>();
            List<Long> userIdList = new ArrayList<>();

            // 分别过滤出用户、角色和部门的id，分别用集合存储
            for (String storageId : storageIds) {
                if (storageId.startsWith("role:")) {
                    roleIdList.add(Long.valueOf(storageId.replace("role:", "")));
                } else if (storageId.startsWith("dept:")) {
                    deptIdList.add(Long.valueOf(storageId.replace("dept:", "")));
                } else if (MathUtil.isNumeric(storageId)){
                    userIdList.add(Long.valueOf(storageId));
                }
            }

            Map<String, String> authMap = new HashMap<>();
            // 查询角色id对应的名称
            if (CollUtil.isNotEmpty(roleIdList)) {
                // 查询角色列表
                List<SysRole> roleList = warmFlowMapper.selectRoleByIds(roleIdList);
                authMap.putAll(StreamUtils.toMap(roleList, role -> "role:" + role.getRoleId()
                        , SysRole::getRoleName));
            }

            // 查询部门id对应的名称
            if (CollUtil.isNotEmpty(deptIdList)) {
                List<SysDept> deptList = warmFlowMapper.selectDeptByIds(deptIdList);
                authMap.putAll(StreamUtils.toMap(deptList, dept -> "dept:" + dept.getDeptId()
                        , SysDept::getDeptName));
            }

            // 查询用户id对应的名称
            if (CollUtil.isNotEmpty(userIdList)) {
                List<SysUser> userList = warmFlowMapper.selectUserByIds(userIdList);
                authMap.putAll(StreamUtils.toMap(userList, user -> String.valueOf(user.getUserId())
                        , SysUser::getNickName));
            }

            // 遍历storageIds，按照原本的顺序回显名称
            for (String storageId : storageIds) {
                handlerFeedBackVos.add(new HandlerFeedBackVo(storageId
                        , MapUtil.isEmpty(authMap) ? "": authMap.get(storageId)));
            }
        }

        return handlerFeedBackVos;
    }
}
```

<br>

## 7. 基础信息类别
> 流程类别通常是业务系统用来做细分的，比如请假，出差，项目，采购，销售，等等流程

### 7.1 基础信息页面
<div><img src="https://foruda.gitee.com/images/1750865204014479953/75d003e7_2218307.png"></div>
<br>

### 7.2 实现接口获取类别信息
#### 7.2.1 CategoryService接口
- 实现`queryCategory()`接口方法，返回`List<Tree>`集合

```java
/**
 * 分类接口
 *
 * @author warm
 * @since 2025/6/24
 */
public interface CategoryService {

    /**
     * 查询分类
     *
     * @return 分类
     */
    List<Tree> queryCategory();
}

```

#### 7.2.2 CategoryServiceImpl实现类
- 如果返回的数据是树状结构，那请多设置`parentId`字段，组件会自动构建成树状，否则会显示成单选

```java
/**
 * 分类服务
 *
 * @author warm
 * @since 2025/6/24
 */
@Service
public class CategoryServiceImpl implements CategoryService {

    @Override
    public List<Tree> queryCategory() {
        List<Tree> trees = new ArrayList<>();
        trees.add(new Tree("1", "分类1", null, null));
        trees.add(new Tree("1-1", "分类1-1", "1", null));
        trees.add(new Tree("2", "分类2", null, null));
        trees.add(new Tree("2-1", "分类2-1", "2", null));
        trees.add(new Tree("3", "分类3", null, null));

        return trees;
    }
}

public class Tree implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * ID
     */
    private String id;

    /**
     * 名称
     */
    private String name;

    /**
     * 父ID
     */
    private String parentId;
}
```

<br>
