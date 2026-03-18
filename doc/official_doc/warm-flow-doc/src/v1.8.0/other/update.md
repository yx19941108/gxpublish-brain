# 计划/日志


## 未来发布计划

### vx.x.x

- 事件
- 流程图合法性检验
- 子流程
- 会签和票签通过率策略，支持扩展
- 重启流程
- 动态表单 @晓华
- 会签功能支持顺序会签
- 动态修改流程
- 并行任务驳回支持多个任务


## 开发中计划
### v1.8.x  2025-09-xx
- 节点扩展属性小类增加选择弹框类型
- 存入数据库的json格式去掉空格保存
- 包容网关
- 设计器可支持新增额外的文本
- ...


## 更新日志

### v1.8.1 2025-08-22
- [style] 优化代码样式,添加注释
- [fix] WarmDaoImpl.selectPage 返回异常，返回的 Page 对象体都是 1,10
- [fix] 修复流程设计器流程分类不显示的bug
- [fix] 流程定义删除监听器，更新后失败
- [fix] 修复流程图下载后，没有100%还原


### v1.8.0  2025-08-08
- [升级指南](./upgrade_guide.md#v1-8-0)
- [feat] 仿钉钉设计器 @晓华
- [feat] 如果跳转线往回画，默认给他退回跳转类型
- [feat] 新增获取第一个中间节点接口，优化查询次数
- [feat] 设计器经典模式节点和边文字可以自由拖动
- [feat] 设计器页面中的可以访问基础信息和流程设计，或者可以单独访问流程设计
- [feat] 增加流程图渲染顶部名称控制显示隐藏    @may
- [feat] 中间节点增加用户图标
- [feat] NodeService新增获取第一个中间节点(申请人节点)的接口`getFirstBetweenNode`
- [update] 设计器不用通过外部传入得disable判断是否可以编辑，直接通过是否发布状态判断，是否可以编辑
- [update] 独立访问流程设计页面，保存时不更新流程定义数据，只更改节点和跳转表信息
- [refactor] 将Date 属性替换成 Instant 以适应国际时区处理问题，移除多余的导包
- [style] 优化代码格式和注释情况,纠错未标注重载情况的方法
- [fix] 修复部门名称搜索不生效问题   @QS
- [fix] like/notNike 条件表达式逻辑修复
- [fix] active/unActive 检查流程是否存在, 避免空指针   @xloouis
- [fix] 修复基础数据中表单路径和监听器保存异常问题
- [fix] 办理人列表回显，不实现也可以返回
- [fix] 前后端获取前置或者后置节点的方法，解决出现死循环问题
- [fix] 修复v-for 缺少key属性的错误   @QS
- [fix] 修复审批流程表单中key值错误的问题   @QS
- [fix] 修复no-mutating-props报错问题: props.logicJson.flowName = flowName会造成Eslint报出no-mutating-props问题，emit已经通知父组件更改名字了   @QS
- [fix] like/notNike 条件表达式逻辑修复   @xloouis
- [fix] 删除流程实例，如果流程已结束，会删除失败
- [fix] 修复设计器基础信息必填未填，也可以切换到流程设计tab
- [fix] 修复未绘制流程图，不可发布
- [fix] 修复TaskService.revoke权限验证if (flowParams.isIgnore())没有取反
- [fix] 流程设计器浏览器有告警且钉钉样式，流程设计器回显不正常

### v1.7.7  2025-07-02
- [fix] 修复每次打开流程图，都是初始节点

### v1.7.6  2025-06-30

- [升级指南](./upgrade_guide.md#v1-7-6)
- [feat] 设计器新增流程定义json下载
- [feat] 设置设计器基础信息分类下拉接口
- [feat] 查询设计器接口queryDef改为同时支持新增和修改
- [feat] 新增设计器模式字段，为仿钉钉设计器做准备
- [update] 获取流程图接口返回值defJson增加instance字段
- [update] 结束节点边框加粗
- [update] 流程图提示信息，改成单价节点触发，点击空白关闭，并且提示框可选中
- [update] 设计器界面重构，支持新增流程定义基础数据
- [remove] DefService的saveAndInitNode接口标识即将删除，改用checkAndSave接口，初始化节点改为前端初始化
- [remove] 删除无用的TreeSelection.java类
- [remove] 删除生成流程图片相关的代码

### v1.7.4  2025-06-19

- [升级指南](./upgrade_guide.md#v1-7-4)
- [feat] 新增下载流程图功能
- [feat] start和skip或者其衍生的方法，新增指定下个任务办理人flowParams.nextHandler @感谢 xiarigang
- [feat] 新增流程图明暗主题  @感谢 Lina Bell
- [feat] 流程图新增通过访问路径设置网格显隐
- [feat] 流程图新增悬浮提示, 支持自定义内容和样式
- [feat] 新增顶部提示信息和右下角log
- [update] 调整按钮布局
- [update] 设计器打开自动居中
- [update] 设计器base文件夹合并到logic-flow文件夹里
- [update] 复制流程对象，不需要复制id、createTime和updateTime
- [remove] 删除ChartService的chartIns接口
- [remove] 删除ChartService的chartDef接口
- [remove] 删除ChartService的chartInsObj接口
- [remove] 删除ChartService的chartDefObj接口
- [remove] 删除HisTaskService的getNoReject接口
- [remove] 删除InsService的skipByInsId接口
- [remove] 删除InsService的termination接口

### v1.7.3  2025-05-29

- [升级指南](./upgrade_guide.md#v1-7-3)
- [feat] 新版流程图通过前端渲染 @may
- [perf] 美化流程设计器ui
- [feat] 办理人权限处理器，新增办理人转换接口，比如角色转用户
- [update] 优化报错提醒
- [update] 添加Version适配当前设计器的行为  @WestFarmer
- [update] 组件内置访问请求/warm-flow-ui/token-name改成/warm-flow-ui/config

### v1.7.2  2025-05-13

- [升级指南](./upgrade_guide.md#v1-7-2)
- [feat] 开启流程实例，新增流程定义是否存在校验
- [feat] [新增合同签订流程案例](https://gitee.com/dromara/warm-flow-test/blob/master/warm-flow-core-test/src/main/resources/contract_process.json)
- [feat] [新增企业采购流程案例](https://gitee.com/dromara/warm-flow-test/blob/master/warm-flow-core-test/src/main/resources/procurement_steps.json)
- [update] mybatis-plus逻辑删除，删除值和未删除值强制设置为0和1
- [update] 合并流程变量方法改成public修饰
- [update] 如果没有网关，是驳回跳转线，跳转条件会默认加上`undefined@@|`
- [fix] 修复办理人列表中只有一个办理人时，删除按钮消失的问题
- [fix] 修复设计器办理人默认为“用户”问题
- [fix] 修复办理人列表名称回显报错问题

### v1.7.0  2025-04-28

- [升级指南](./upgrade_guide.md#v1-7-0)
- [feat] 待办任务表新增流程状态字段，避免并行网关中，不同点任务流程状态都是相同
- [feat] 新增撤销功能  @xiarigang @晓华
- [feat] 新增驳回到上一个任务 @xiarigang @晓华
- [feat] 新增拿回功能
- [feat] 设计器中间节点设置，办理人输入改成列表，可以反显中文名称
- [feat] TaskService新增pass：流程通过(自定义流程状态)
- [feat] TaskService新增passAtWill：流程任意通过(自定义流程状态)
- [feat] TaskService新增reject：流程退回(自定义流程状态)
- [feat] TaskService新增rejectWill：流程任意退回(自定义流程状态)
- [feat] TaskService新增rejectLastByInsId：驳回上一个任务
- [feat] TaskService新增rejectLast：驳回上一个任务
- [feat] TaskService新增revoke：撤销
- [feat] TaskService新增taskBackByInsId：拿回到最近办理的任务
- [feat] TaskService新增taskBack：拿回到最近办理的任务
- [feat] TaskService新增skipByInsId：根据实例id，流程跳转
- [feat] TaskService新增getByInsId：根据流程实例id获取流程任务集合
- [feat] TaskService新增getByInsIdAndNodeCodes：根据流程实例id和节点code集合获取流程任务集合
- [feat] DefService新增getByFlowCode: 根据流程定义code查询流程定义
- [feat] InsService新增getByDefId: 根据流程定义id，查询流程实例集合
- [feat] NodeService新增getByDefId: 根据流程定义id，查询流程节点集合
- [feat] NodeService新增getStartNode：根据流程定义id获取开始节点
- [feat] NodeService新增getBetweenNode：根据流程定义id获取中间节点集合
- [feat] NodeService新增getEndNode：根据流程定义id获取结束节点
- [feat] NodeService新增getByDefIdAndNodeCode：根据流程定义id和节点编码获取流程节点
- [feat] SkipService新增getByDefId：根据流程定义id查询节点跳转线
- [feat] SkipService新增getByDefIdAndNowNodeCode：根据流程定义id和节点编码查询节点跳转线
- [feat] DefService新增getPublishByFlowCode：根据流程定义code查询已发布的流程定义
- [feat] 增加源码对应文档的地址@see注释
- [perf] 优化nodeService.getNextNodeList重复代码，提升性能
- [refactor] 重构部分代码
- [update] HisTaskService的getNoReject接口标识为即将删除
- [update] InsService的skipByInsId接口标识为即将删除，请使用TaskService.skipByInsId代替
- [update] 设计器驳回指定节点，过滤掉开始节点
- [fix] 修复如果有long类型的时候，判断会出现问题
- [fix] 修复互斥网关时，存在执行多个任务情况
- [fix] 修复开启流程直接结束时，不能正确完成流程问题
- [remove] 删除审批消息字数校验
- [remove] TaskService删除removeAndUser
- [remove] UserService删除setSkipUser


### v1.6.10  2025-04-13

- [update] 处理扩展节点多选反显
- [update] 删除多余的注释
- [fix] Page未实现Serializable,导致Dubbo3.2+ 版本中默认的 STRICT严格检查报错  @yuegc
- [fix] 修复双击中间节点后，再点击跳转线不打开右边抽屉的问题  @Vittery
- [fix] 修复当图形出现在负坐标的时候，开始、结束、互斥网关节点文字没有显示在正确位置的问题
- [remove] 删除办理人表达式抽象类

### v1.6.8  2025-03-19

- [升级指南](./upgrade_guide.md#v1-6-8)
- [fix] 流程复制后，丢失原有的，驳回到指定节点配置信息
- [fix] 流程图退回状态设置，缺少判空
- [fix] 给flow_user表associated字段增加普通索引，提升查询和更新性能
- [fix] 修复目标节点有多个节点指向它，当有并行网关时，导致不生成待办任务问题
- [fix] 修复公共API排序升序无效

### v1.6.7  2025-03-03

- [升级指南](./upgrade_guide.md#v1-6-7)
- [feat] 设计器支持节点扩展属性设置
- [feat] 流程图扩展，新增接口，方便追加文字
- [feat] 流程状态支持颜色支持自定义
- [update] 节点表版本号字段标识下个版本删除
- [update] Jackson反序列化时忽略未知字段
- [update] 删除部分代码，调整注释
- [update] 修改当票签和会签节点时，注意事项描述
- [fix] 规范solon,api注解 防止某些情况获取不到方法参数名
- [fix] 删除流程实例的时候，办理用户不存在，导致删除失败
- [fix] #IBP397：修复当设计流程，开始节点出现再负坐标时，文字名称未显示
- [fix] #IBP3LK：修复开启流程，流程图第一个节点不是待办颜色
- [fix] 网关节点编辑文字报错处理
- [remove] 移除流程定义xml导入导出方式
- [remove] 移除多余的skip_Any_Node字段
- [style] 常量改成大写和下划线


### v1.6.6  2025-01-23

- [升级指南](./upgrade_guide.md#v1-6-6)
- [feat] 导入、导出和保存等新增json格式支持DefService.importIs/importJson/importDef/saveDef/exportJson
- [feat] 新增获取后置节点方法NodeService.suffixNodeList
- [feat] 新增网关直连和测试案例
- [feat] 流程图右上角新增完成状态颜色示例
- [feat] 新增流程图查询接口和扩展接口ChartService
- [feat] 新增历史表数据同步为新的流程图元数据
- [feat] 新增sqlserver全量脚本
- [update] 导入、导出和保存xml格式标识为即将删除，请参照hh-vue切换json的api
- [update] FlowFactory修改为FlowEngine
- [update] 历史表目标节点编码和目标节点名称字段长度改为200
- [update] 通过或者退回到并行网关，开启多个任务，改为只产生一条历史记录
- [update] 退回或者任务完成，其他需要被删除的任务不需要记录历史表，因为已经存在退回记录，不需要重复记录
- [update] 转办、委派、加签和减签，改为只产生一条历史记录
- [update] 批量保存改为默认1000条一批
- [update] 流程设计保存，增加遮罩层
- [refactor] 流程图绘制调整重构
- [refactor] 移除mybatis-flex,easy-query和jpa的扩展包，独立成项目，由专门人维护
- [refactor] 实体类和dao获取改为通过反射，解耦orm-core包
- [refactor] 重构获取前置节点方法NodeService.previousNodeList
- [fix] 修复退回时存在其他代办任务，未删除的问题
- [fix] 修复流程退回目标节点前存在并行网关，导致不生成代办任务的问题
- [fix] 修复条件表达式中如果有`|`或导致错误分隔的问题
- [fix] 修复绘制流程图，错误判断同一条录像的key
- [fix] 修复结束节点还执行创建监听器的问题
- [remove] 移除DefService获取流程图api，由ChartService中chartIns和chartDef代替
- [remove] 删除前端log打印
- [remove] 移除oracle和postgresql升级脚本，后续只提供mysql升级脚本，所有的全量脚本，其他升级脚本的自行转换

### v1.3.8 2025-01-07

- [fix] 修复最新设计器代码未复制到到jar包

### v1.3.7 2024-12-31

- [升级指南](./upgrade_guide.md#v1-3-7)
- [fix] 修复设计器驳回指定节点显示异常问题
- [fix] 流程实例查询SQL BUG

### v1.3.6 2024-12-23

- [fix] 修复设计器驳回指定节点显示异常问题

### v1.3.5 2024-12-20

- [升级指南](./upgrade_guide.md#v1-3-5)
- [feat] 新增获取所有前置节点接口
- [feat] 设计器新增设置驳回指定节点
- [feat] 条件表达式新增默认策略`default|${flag == 5 && flag > 4}`
- [feat] 新增mybatis-plus关闭逻辑删除案例
- [update] 退回不校验是否办理过
- [update] 复制流程版本号支持自动递增
- [update] 结点命名全部改成节点
- [refactor] 导入流程方法拆成两个方法，读取is流和导入实体类，insertFlow改为公共方法
- [refactor] 条件表达式原本太繁琐，进行精简, `@@eq@@|flag@@eq@5` --> `eq|flag|5`
- [refactor] 任意跳转，改成退回选择目标节点，票签必填，修改字段名称

### v1.3.4 2024-11-25

- [升级指南](./upgrade_guide.md#v1-3-4)
- [feat] 新增监听器spel表达式，并且支持扩展
- [feat] 增加全局监听器，针对整个系统，通过接口接入方式
- [feat] 新增审批前获取当前办理人接口，类似satoken方式 @huangjian
- [feat] 流程变量表达式支持替换集合 @huangjian
- [feat] 设计器引入，新增支持solon
- [feat] 新增创建流程定义，默认初始化节点
- [feat] 新增根据流程定义id集合，查询流程实例集合api
- [update] 监听器优化配置（类中配置增加类型接口，或者优化页面配置）
- [update] 重新定义监听器名称，原全局监听器改名为流程监听器，局部监听器改名为节点监听器
- [update] 已经开启过审批任务的不可取消发布和删除
- [update] 转办、委派、加签和减签，增加参数合法性校验
- [update] 修改流程变量传递方式，可通过办理人表达式或者分派监听器，初始化后续所有办理人
- [update] 加载handler取消懒加载，重构test项目
- [update] 办理人表达式，删除策略前缀，通过$和#区分
- [update] 流程版本号默认改完自动递增，不接收外部设置
- [update] 修改项目的groupId
- [refactor] 重构条件表达式和办理人表达式
- [remove] 移除权限监听器

### v1.3.3 2024-11-12
- [升级指南](./upgrade_guide.md#v1-3-3)
- [feat] 新增支持接入业务系统token，支持多token
- [update] 办理人选择tabs切换
- [update] 没有左侧树状选择数据时，左侧隐藏
- [fix] 统一修复分页bug
- [fix] 修复删除流程实例信息，未删除办理人信息 @xiarigang
- [fix] 修改【Bug】 会签节点委派 @vanlin
- [fix] 统一修复分页bug

### v1.3.1 2024-11-01

- [升级指南](./upgrade_guide.md#v1-3-1)
- [feat] 新增boot3+java17支持
- [feat] 流程设计器新增快捷键支持
- [feat] 新增流程状态枚举（终止、作废、撤销和取回）
- [feat] 新增转办、委派、加签和减签方法，老方法标识即将删除，接入监听器
- [update] 终止流程状态改为更合理的终止状态
- [update] 流程复制克隆改set/get赋值
- [refactor] 重构skip等方法通用校验
- [perf] 流程图清晰度调整
- [fix] 流程图查询异常处理
- [fix] 修复历史记录表，创建时间和更新时间一样的问题
- [remove] FlowParams对象删除setXxx(yyy)方法，改为xxx(yyy)方法赋值

### v1.3.0 2024-10-23

- [feat] 设计器独立 @zhen
- [feat] 使用jar引入方式引入设计器
- [feat] 新增办理人表达式流程案例
- [feat] 新增方法，获取流程变量的map类型
- [update] 节点线条保存关联名称
- [fix] 修复mybatis扩展包中，flowStatus变量书写错误的问题
- [fix] 更新时间有值时，取更新时间，不是创建时间
- [fix] 修复mybatis-plus扩展包，配置了其他id策略不生效的问题

### v1.2.8 2024-09-25

- [升级指南](./upgrade_guide.md#v1-2-8)
- [feat] json库支持snack3、jackson、fastjson和gson，并且支持扩展
- [feat] 增加办理人表达式，支持${xxx}替换和spel，并支持扩展
- [feat] ListenerVariable监听器变量新增FlowParams字段，方便开始监听器全局传递参数
- [feat] 终止新增对开始和完成监听器的支持
- [update] springboot项目的条件表达式默认支持spel
- [update] 历史记录改为单条保存，删除重复代码
- [update] 修改FlowUserDao的bean名称
- [update] 中间节点拆分为或签，会签，票签
- [fix] 修复历史记录创建时间相等，导致流程图渲染异常
- [fix]修复Mybatis逻辑删除变成真实删除的缺陷 @xiarigang
- [refactor] 重构id生成器，支持orm默认策略，删除数据填充默认实现类，改为匿名类

### v1.2.7 2024-09-03

- [update] 历史任务表流程状态支持外部传入
- [update] 修改办理人接口，当未设置办理人时，不做权限校验
- [update] ModifyHandler增加链式调用
- [fix] 修复流程监听器导出失败的问题

### v1.2.6 2024-08-28

- [升级指南](./upgrade_guide.md#v1-2-6)
- [feat] 增加获取下个节点集合api @xiarigang
- [feat] 流程监听器 @xiaoxiaoliu889
- [feat] id内存策略新增14、15位雪花算法支持
- [feat] 流程激活和挂起案例 @xiaoxiaoliu889
- [feat] 增加基于流程定义Id获取流程图 @xiaoxiaoliu889
- [update] 流程状态改成字符串类型 @xiarigang
- [update] 测试模块拆分独立仓库
- [update] modes-sb删除加载配置文件，改为有上层jar加载
- [update] flex solon版本yml弄错了，config调整
- [refactor] 流程版本号生成逻辑重构 @xiaoxiaoliu889
- [fix] 修复deleteByTaskIds 中的根据无法正确删除user数据
- [fix] 修复 jpa solon注解问题 @vanlin
- [fix] 修复 并行网关三个任务分支的时候，错误结束流程的问题

### v1.2.4 2024-08-14

- [升级指南](./upgrade_guide.md#v1-2-4)
- [feat] 激活和挂起 @xiaoxiaoliu889
- [feat] 不同节点也支持配置审批表单路径 @vanlin
- [feat] 支持接收外部流程状态，支持流程状态扩展 @vanlin
- [feat] 新增spel条件表达式，新增可通过SPI机制加载条件表达式
- [feat] 新增分派监听器，支持代办任务中办理人等动态修改 @liangli
- [feat] 新增Easy-Query框架支持 @link2fun
- [feat] 新增Mybatis-Flex的solon扩展包 @xiarigang
- [feat] 新增Jpa的solon扩展包 @vanlin
- [feat] 历史表新增跳转类型，记录跳转类型 @vanlin
- [feat] 增加组件加载，yml控制开关 @疯狂的狮子Li
- [update] 之前强依赖流程状态的通过的，改为跳转类型，历史数据考虑如何处理 @vanlin
- [update] 之前所有保存流程状态地方，全部提供可接受外部传入 @vanlin
- [update] 流程开启，校验节点是否发布，提示语增加流程编码
- [update] 删除校验是否任意跳转
- [update] 修改扩展字段ext注释，删除FlowConfigUtil多余的代码
- [update] SqlSessionFactory改为构造函数引入
- [update] 替换异常类，UtilException高版本不兼容
- [update] from_custom改为form_custom，from_path改为form_path
- [remove] 移除节点前置执行权限处理器
- [remove] 删除cooperateAutoPass方法
- [update] 代办改为待办
- [refactor] 重构测试模块，完善mybatis-plus多租户和逻辑删除使用方式
- [refactor] 重构solon和插件模块
- [fix] 修复加签批量提交报错
- [fix] 修复TaskServiceImpl#handleDepute方法中删除受托人传参数错误
- [fix] 修复 JPA flowUserDao bug @vanlin
- [fix] 流程第三个节点为网关时无法渲染颜色
- [fix] 修复查看流程图模糊的问题 @erfeijiadao
- [fix] 修復开始节点直连网关，流程图渲染有问题
- [fix] 修复不能退回，未完成过任务
- [fix] 修复流程定义和流程实例相同，处于非结束节点，流程实例不能存在相同的业务id
- [fix] 修复不能退回，未完成过任务
- [fix] 删除不必要的··符号，修复postgresql与mysql关键词符号问题。
- [fix] 修复流程已完成，流程图结束节点显示为黑色
- [fix] 修复已办任务查询审批想起无效问题
- [fix] 设置开始节点 skip_type = PASS
- [fix] 为SpringUtil指定bean name,解决 LocalContainerEntityManagerFactoryBean 依赖问题
- [chore] 升级dom4j为安全版本2.1.3

### v1.2.3 2024-06-28

- [fix] 修复更新拼上了多余的条件
- [fix] 修复保存流程xml报错问题

### v1.2.1 2024-06-28

- [升级指南](./upgrade_guide.md#v1-2-1)
- [feat] 新增mybatis-flex扩展包 @xiarigang
- [feat] 新增抄送演示案例 @adru*
- [feat] 历史记录表新增ext扩展字段，方便保存历史过程数据 @adru*
- [update] 保存下一个节点办理人时，不校验是否有审批人
- [update] 调整扩展包配置，新增测试模块
- [update] 票签,会签，将待办任务的创建时间赋予历史任务开始时间 @liutao
- [update] 调整mybatis-plus多租户和软件删除处理
- [update] 修改任务历史表时间定义，任务审批通过，将待办任务的创建时间赋予历史任务开始时间 @liutao*
- [update] 修正pg的sql文件名，新增1.2.1增量sql @liutao
- [update] 会签票签，开始时间记录代表任务的创建时间
- [remove] 删除监听器自定义参数,修改扩展字段名称
- [fix] 监听器问题修复 @liangli
- [fix] solon插件包增加userMapper.xml加载
- [fix] 修复填充器不接收外部设置的时间
- [fix] 修复userMapper.xml中updateLogic的某个负值错误

### v1.2.0  2024-06-13

- [升级指南](./upgrade_guide.md#v1-2-0)
- 待办表解偶用户，新增用户表（查询方式需要改动）
- 会签，票签
- 加减签
- 转办完善
- jpa扩展
- oracle适配
- pg适配

### v1.1.9  2024-05-08

- [升级指南](./upgrade_guide.md#v1-1-9)
- orm支持mybatis-plus扩展
- 多租户字段隔离提供全局配置，自动获取
- 增加软删除可以配置化
- 新增三个测试模块
- 修复设置默认填充器时候，判空错误

### v1.1.7 2024-04-27

- 启动流程时，支持第二个节点为网关节点的情况
- 开始监听器和完成监听器新增返回当前任务和新建任务集合
- 修复终止流程bug

### v1.1.6 2024-04-23

- 支持转办功能
- 任务自动流转到创建人
- 考虑流程终止功能
- 修复流程流转异常
- 实体类支持序列化
- 修复java17以上@Resource包路径变更的问题

### v1.1.5 2024-04-17

- 支持自定义填充
- 新增配置文件，部分功能可配置
- 重构代码，insService.skip标识即将删除，改用taskService.skip
- 引入日志门面
- 修复已经设置后续节点动态权限后，办理时未生效问题；
- 枚举扩展getByKey方法 @Holly_Git
- 调整实例类结构，方便链式写法
- 修复并行网关后面没有中间节点
- 修复开始任务记录待办，为保存流程状态
- 新增链式查询排序提供id排序
- 新增历史任务记录结束节点
- 新增赋值流程记录创建更新时间
- 优化表实体类链式赋值
- 待办已办查询标记为即将删除, 已挪到业务系统中

### v1.1.4 2024-04-07

- 修复监听器部分判空bug 感谢@Holly_Git
- 新增创建任务监听器
- 修改flow_node监听类型和监听路径字段长度
- 新增监听器生命周期概念，完善文档
- 重构流程开启流程和流程办理代码
- 开始节点也能记录到历史任务记录中

### v1.1.3 2024-04-02

- 新增权限监听器，办理中动态设置权限 感谢@Holly

### v1.1.2 2024-03-27

- 流程定义新增复制按钮
- 补齐sql脚本，完善文档
- 跳转条件获取方式变更为流程变量 感谢@Holly
- 监听器变量新增返回节点信息 感谢@Holly
- 新增根据流程定义和当前节点code获取下一节点api接口. 感谢@Holly
- 删除多余的字段和代码

### v1.1.1 2024-03-22

- [fix] 修复xml脚本中遗漏伪命的问题，完善sql

### v1.1.0 2024-03-21

- 可以跳转指定节点
- 增加全局变量
- 增加监听器
- 重构代码，解偶orm，方便扩展不同orm和数据，新增代码示例
- 修复并行网关流程流程图显示错误问题

### v1.0.4 2024-03-06

- [update] 美化后台返回的流程图

### v1.0.3 2024-03-05

- [update] 重构代码，解偶orm操作，方便扩展其他orm框架

### v1.0.2 2023-12-31

- [fix] 修复solon版本业务系统可不用单独加载warm-flow的xml

### v1.0.0 2023-12-28

- 完善流程设计器和流程图，新增vue3版本
- 放弃js引擎，自研条件表达式
- 新增支持条件表达式
- 可退回到任意节点
- 支持生成流程图
- 流程设计器开发
- 互斥网关，并行网关（会签、或签）功能开发
- 抽离spring生态依赖，支持solon，并且保持事务与业务系统一致
- 支持待办任务和已办任务，通过权限标识过滤数据
- 新增多租户模式
- 原生提供排序
- 原生提供分页查询
- 项目上传中央仓库
- 工作流模块抽取为独立项目，提供集成方式
- 已办任务和任务记录，审批页面中包含业务详情页面
- 提供待办任务、提供角色、部门等权限配置
- 提供流程配置界面
- 流程配置文件改为表单填报方式
- 用户角色抽取出来
- 整理流程表，调整表名和字段名
