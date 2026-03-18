# 接口文档


## DefService流程定义 
### 导入流程输入流
`Definition importIs(is)`： 流程定义的输入流

### 导入流程json字符串 
`Definition importJson(defStr)`：流程定义defJson的json字符串

### 导入流程json对象 
`Definition importDef(defJson)`：流程定义json对象

### 新增流程 
`boolean saveAndInitNode(definition)`：新增流程定义，并初始化流程节点和流程跳转数据

### 只新增流程定义表数据 
`boolean checkAndSave(definition)`：流程定义对象

### 保存流程节点和跳转 
`void saveDef(defJson)`：流程定义json对象

### 导出流程定义json字符串 
`String exportJson(id)`： 导出流程定义(流程定义、流程节点和流程跳转数据)的json字符串

### 获取流程定义全部数据对象 
`Definition getAllDataDefinition(id)`： 获取流程定义全部数据(包含节点和跳转)

### 查询流程设计所需的数据 
`DefJson queryDesign(id)`： 查询流程设计所需的数据，比如流程图渲染

### 根据流程定义code列表查询流程定义
`List<Definition> queryByCodeList(flowCode)`： 根据流程定义code列表查询流程定义

### 根据流程定义code查询流程定义
`List<Definition> getByFlowCode(flowCode)`： 根据流程定义code查询流程定义

### 根据flowCode查询已发布的流程定义
`Definition getPublishByFlowCode(flowCode)`： 根据流程定义code查询已发布的流程定义

### 更新流程定义发布状态 
`void updatePublishStatus(defIds, publishStatus)`：
- defIds: 流程定义id列表 [必传]
- publishStatus: 流程定义发布状态 [必传]

### 删除 
`boolean removeDef(ids)`： 删除流程定义相关数据  

### 发布 
`boolean publish(id)`： 发布流程定义  

### 取消发布 
`boolean unPublish(id)`： 取消发布流程定义  

### 复制流程 
`boolean copyDef(id)`： 复制流程定义   

### 激活流程 
`boolean active(id)`： 激活流程

### 挂起流程 
`boolean unActive(id)`： 挂起流程：流程定义挂起后，相关的流程实例都无法继续流转

## InsService流程实例 

### 开启流程 
`Instance start(businessId, flowParams)`：传入业务id，开启流程实例。flowParams包含如下字段：
- flowCode: 流程编码 [必传]
- handler: 办理人唯一标识，如用户id，用于记录到实例表和历史表 [按需传输]；如果实现了[办理人权限处理器](./permission_handler.md)可不用传
- variable: 流程变量 [按需传输]
- ext: 扩展字段，预留给业务系统使用 [按需传输]
- nextHandler: 执行的下个任务的办理人[按需传输]
- nextHandlerAppend: 个任务处理人配置类型（true-追加，false-覆盖，默认false）[按需传输]
- flowStatus: 流程状态，自定义流程状态[按需传输]

### 根据defIds查询流程实例集合
`List<Instance> listByDefIds(defIds)`：根据流程定义id集合，查询流程实例集合

### 根据defId查询流程实例集合
`List<Instance> getByDefId(definitionId)`：根据流程定义id，查询流程实例集合

### 删除流程实例 
`boolean remove(instanceIds)`：根据实例ids，删除流程

### 激活实例 
`boolean active(Long id)`： 激活实例

### 挂起实例 
`boolean unActive(Long id)`： 挂起实例，流程实例挂起后，该流程实例无法继续流转

## TaskService待办任务 
### 流程通过 
`Instance pass(taskId, message, variable)`
> <span class="red-no-bg">使用前提是实现，{@link PermissionHandler#permissions()}和{@link PermissionHandler#getHandler()}</span>
> <span class="red-no-bg">工作流内部会获取办理人权限标识（permissionFlag）和办理人唯一标识（handler）</span>
- taskId: 流程任务id [必传]
- message: 审批意见 [按需传输]
- variable: 流程变量 [按需传输]

### 流程任意通过 
`Instance passAtWill(taskId, nodeCode, message, variable)`
> <span class="red-no-bg">使用前提是实现，{@link PermissionHandler#permissions()}和{@link PermissionHandler#getHandler()}</span>
> <span class="red-no-bg">工作流内部会获取办理人权限标识（permissionFlag）和办理人唯一标识（handler）</span>
- taskId: 流程任务id [必传]
- nodeCode: 如果指定节点,可[任意跳转]到对应节点 [[必传]]
- message: 审批意见 [按需传输]
- variable: 流程变量 [按需传输]

### 流程通过，自定义流程状态 
`Instance pass(taskId, message, variable, flowStatus, hisStatus)`
> <span class="red-no-bg">使用前提是实现，{@link PermissionHandler#permissions()}和{@link PermissionHandler#getHandler()}</span>
> <span class="red-no-bg">工作流内部会获取办理人权限标识（permissionFlag）和办理人唯一标识（handler）</span>
- taskId: 流程任务id [必传]
- message: 审批意见 [按需传输]
- variable: 流程变量 [按需传输]
- flowStatus: 流程状态，自定义流程状态 [按需传输]
- hisStatus: 历史任务表状态，自定义流程状态 [按需传输]

### 流程任意通过，自定义流程状态 
`Instance passAtWill(taskId, nodeCode, message, variable, flowStatus, hisStatus)`
> <span class="red-no-bg">使用前提是实现，{@link PermissionHandler#permissions()}和{@link PermissionHandler#getHandler()}</span>
> <span class="red-no-bg">工作流内部会获取办理人权限标识（permissionFlag）和办理人唯一标识（handler）</span>
- taskId: 流程任务id [必传]
- nodeCode: 如果指定节点,可[任意跳转]到对应节点，严禁任意退回选择后置节点 [[必传]]
- message: 审批意见 [按需传输]
- variable: 流程变量 [按需传输]
- flowStatus: 流程状态，自定义流程状态 [按需传输]
- hisStatus: 历史任务表状态，自定义流程状态 [按需传输]

### 流程退回
`Instance reject(taskId, message, variable)`
> <span class="red-no-bg">使用前提是实现，{@link PermissionHandler#permissions()}和{@link PermissionHandler#getHandler()}</span>
> <span class="red-no-bg">工作流内部会获取办理人权限标识（permissionFlag）和办理人唯一标识（handler）</span>
- taskId: 流程任务id [必传]
- message: 审批意见 [按需传输]
- variable: 流程变量 [按需传输]

### 流程任意退回
`Instance rejectAtWill(taskId, nodeCode, message, variable)`
> <span class="red-no-bg">使用前提是实现，{@link PermissionHandler#permissions()}和{@link PermissionHandler#getHandler()}</span>
> <span class="red-no-bg">工作流内部会获取办理人权限标识（permissionFlag）和办理人唯一标识（handler）</span>
- taskId: 流程任务id [必传]
- nodeCode: 如果指定节点,可[任意跳转]到对应节点，严禁任意退回选择后置节点 [[必传]]
- message: 审批意见 [按需传输]
- variable: 流程变量 [按需传输]

### 流程退回，自定义流程状态
`Instance reject(taskId, message, variable, flowStatus, hisStatus)`
> <span class="red-no-bg">使用前提是实现，{@link PermissionHandler#permissions()}和{@link PermissionHandler#getHandler()}</span>
> <span class="red-no-bg">工作流内部会获取办理人权限标识（permissionFlag）和办理人唯一标识（handler）</span>
- taskId: 流程任务id [必传]
- message: 审批意见 [按需传输]
- variable: 流程变量 [按需传输]
- flowStatus: 流程状态，自定义流程状态 [按需传输]
- hisStatus: 历史任务表状态，自定义流程状态 [按需传输]

### 流程任意退回，自定义流程状态
`Instance rejectAtWill(taskId, nodeCode, message, variable, flowStatus, hisStatus)`
> <span class="red-no-bg">使用前提是实现，{@link PermissionHandler#permissions()}和{@link PermissionHandler#getHandler()}</span>
> <span class="red-no-bg">工作流内部会获取办理人权限标识（permissionFlag）和办理人唯一标识（handler）</span>
- taskId: 流程任务id [必传]
- nodeCode: 如果指定节点,可[任意跳转]到对应节点，严禁任意退回选择后置节点 [[必传]]
- message: 审批意见 [按需传输]
- variable: 流程变量 [按需传输]
- flowStatus: 流程状态，自定义流程状态 [按需传输]
- hisStatus: 历史任务表状态，自定义流程状态 [按需传输]

### 流程跳转 
`Instance skip(taskId, flowParams)`：传入流程任务id，流程跳转。flowParams包含如下字段：
- skipType: 跳转类型(PASS审批通过 REJECT退回) [必传]
- nodeCode: 如果指定节点,可[任意跳转]到对应节点，严禁任意退回选择后置节点 [按需传输]
- permissionFlag: 办理人权限标识，比如用户，角色，部门等，用于校验是否有权限办理 [按需传输]；满足任一情况可以不传：流程设计时未设置办理人、ignore为true、实现了[办理人权限处理器](./permission_handler.md)
- message: 审批意见 [按需传输]
- handler: 办理人唯一标识，如用户id，用于记录历史表 [按需传输]；如果实现了[办理人权限处理器](./permission_handler.md)可不用传
- variable: 流程变量 [按需传输]
- nextHandler: 执行的下个任务的办理人[按需传输]
- nextHandlerAppend: 个任务处理人配置类型（true-追加，false-覆盖，默认false）[按需传输]
- flowStatus: 流程状态，自定义流程状态 [按需传输]
- ignore: 忽略权限校验（比如管理员不校验），默认不忽略 [按需传输]

### 根据流程实例id流程跳转 
`Instance skipByInsId(instanceId, flowParams)`：传入流程实例id，流程跳转。flowParams包含如下字段：
- skipType: 跳转类型(PASS审批通过 REJECT退回) [必传]
- nodeCode: 如果指定节点,可[任意跳转]到对应节点，严禁任意退回选择后置节点 [按需传输]
- permissionFlag: 办理人权限标识，比如用户，角色，部门等，用于校验是否有权限办理 [按需传输]；满足任一情况可以不传：流程设计时未设置办理人、ignore为true、实现了[办理人权限处理器](./permission_handler.md)
- message: 审批意见 [按需传输]
- handler: 办理人唯一标识，如用户id，用于记录历史表 [按需传输]；如果实现了[办理人权限处理器](./permission_handler.md)可不用传
- variable: 流程变量 [按需传输]
- nextHandler: 执行的下个任务的办理人[按需传输]
- nextHandlerAppend: 个任务处理人配置类型（true-追加，false-覆盖，默认false）[按需传输]
- flowStatus: 流程状态，自定义流程状态 [按需传输]
- ignore: 办理是忽略权限校验，默认不忽略（true：忽略，false：不忽略）[按需传输]

### 驳回上一个任务 
`Instance rejectLastByInsId(instanceId, flowParams)`：传入流程实例id，驳回上一个任务。flowParams包含如下字段：
- permissionFlag: 办理人权限标识，比如用户，角色，部门等，用于校验是否有权限办理 [按需传输]；满足任一情况可以不传：流程设计时未设置办理人、ignore为true、实现了[办理人权限处理器](./permission_handler.md)
- message: 审批意见 [按需传输]
- handler: 办理人唯一标识，如用户id，用于记录历史表 [按需传输]；如果实现了[办理人权限处理器](./permission_handler.md)可不用传
- variable: 流程变量 [按需传输]
- nextHandler: 执行的下个任务的办理人[按需传输]
- nextHandlerAppend: 个任务处理人配置类型（true-追加，false-覆盖，默认false）[按需传输]
- flowStatus: 流程状态，自定义流程状态 [按需传输]
- ignore: 办理是忽略权限校验，默认不忽略（true：忽略，false：不忽略）[按需传输]

### 驳回上一个任务 
`Instance rejectLast(taskId, flowParams)`：传入流程任务id，驳回上一个任务。flowParams包含如下字段：
- permissionFlag: 办理人权限标识，比如用户，角色，部门等，用于校验是否有权限办理 [按需传输]；满足任一情况可以不传：流程设计时未设置办理人、ignore为true、实现了[办理人权限处理器](./permission_handler.md)
- message: 审批意见 [按需传输]
- handler: 办理人唯一标识，如用户id，用于记录历史表 [按需传输]；如果实现了[办理人权限处理器](./permission_handler.md)可不用传
- variable: 流程变量 [按需传输]
- nextHandler: 执行的下个任务的办理人[按需传输]
- nextHandlerAppend: 个任务处理人配置类型（true-追加，false-覆盖，默认false）[按需传输]
- flowStatus: 流程状态，自定义流程状态 [按需传输]
- ignore: 忽略权限校验（比如管理员不校验），默认不忽略 [按需传输]

### 根据insId拿回到最近办理的任务
`Instance taskBackByInsId(instanceId, flowParams)`：传入流程实例id，拿回到最近办理的任务。flowParams包含如下字段：
- message: 审批意见 [按需传输]
- handler: 办理人唯一标识，如用户id，用于记录历史表 [按需传输]；如果实现了[办理人权限处理器](./permission_handler.md)可不用传
- variable: 流程变量 [按需传输]
- nextHandler: 执行的下个任务的办理人[按需传输]
- nextHandlerAppend: 个任务处理人配置类型（true-追加，false-覆盖，默认false）[按需传输]
- flowStatus: 流程状态，自定义流程状态 [按需传输]

### 根据taskId拿回到最近办理的任务
`Instance taskBack(taskId, flowParams)`：传入流程任务id，拿回到最近办理的任务。flowParams包含如下字段：
- message: 审批意见 [按需传输]
- handler: 办理人唯一标识，如用户id，用于记录历史表 [按需传输]；如果实现了[办理人权限处理器](./permission_handler.md)可不用传
- variable: 流程变量 [按需传输]
- nextHandler: 执行的下个任务的办理人[按需传输]
- nextHandlerAppend: 个任务处理人配置类型（true-追加，false-覆盖，默认false）[按需传输]
- flowStatus: 流程状态，自定义流程状态 [按需传输]

### 撤销 
`Instance revoke(instanceId, flowParams)`：传入流程实例id，撤销到第一个中间节点。flowParams包含如下字段：
- message: 审批意见 [按需传输]
- handler: 办理人唯一标识，如用户id，用于记录历史表 [按需传输]；如果实现了[办理人权限处理器](./permission_handler.md)可不用传
- variable: 流程变量 [按需传输]
- nextHandler: 执行的下个任务的办理人[按需传输]
- nextHandlerAppend: 个任务处理人配置类型（true-追加，false-覆盖，默认false）[按需传输]
- flowStatus: 流程状态，自定义流程状态 [按需传输]
- ignore: 忽略权限校验（比如管理员不校验），默认不忽略 [按需传输]

### 根据流程实例id终止流程
`Instance terminationByInsId(instanceId, flowParams)`：传入流程任务id，终止流程。flowParams包含如下字段：
- message: 审批意见 [按需传输]
- handler: 办理人唯一标识，如用户id，用于记录历史表 [按需传输]；如果实现了[办理人权限处理器](./permission_handler.md)可不用传
- flowStatus: 流程状态，自定义流程状态 [按需传输]
- permissionFlag: 办理人权限标识，比如用户，角色，部门等，用于校验是否有权限办理 [按需传输]；满足任一情况可以不传：流程设计时未设置办理人、ignore为true、实现了[办理人权限处理器](./permission_handler.md)
- ignore: 忽略权限校验（比如管理员不校验），默认不忽略 [按需传输]


### 终止流程 
`Instance termination(taskId, flowParams)`：传入流程任务id，终止流程。flowParams包含如下字段：
- message: 审批意见 [按需传输]
- handler: 办理人唯一标识，如用户id，用于记录历史表 [按需传输]；如果实现了[办理人权限处理器](./permission_handler.md)可不用传
- flowStatus: 流程状态，自定义流程状态 [按需传输]
- permissionFlag: 办理人权限标识，比如用户，角色，部门等，用于校验是否有权限办理 [按需传输]；满足任一情况可以不传：流程设计时未设置办理人、ignore为true、实现了[办理人权限处理器](./permission_handler.md)
- ignore: 忽略权限校验（比如管理员不校验），默认不忽略 [按需传输]

### 转办 
`boolean transfer(taskId, flowParams)`：转办, 默认删除当前办理用户权限，转办后，当前办理不可办理。flowParams包含如下字段：
- handler: 当前办理人唯一标识，如用户id，用于记录历史表; 如果通过办理人权限处理器{@link PermissionHandler#getHandler()}传入了，就不需要传 [按需传输]
- permissionFlag: 办理人权限标识，比如用户，角色，部门等，用于校验是否有权限办理 [按需传输]；满足任一情况可以不传：流程设计时未设置办理人、ignore为true、实现了[办理人权限处理器](./permission_handler.md)
- addHandlers: 转办对象 [必传]
- message: 审批意见 [按需传输]
- ignore: 忽略权限校验（比如管理员不校验），默认不忽略 [按需传输]

### 委派 
`boolean depute(taskId, flowParams)`：委派, 默认删除当前办理用户权限，委派后审批完, 重新回到当前办理人。flowParams包含如下字段：
- handler: 当前办理人唯一标识，如用户id，用于记录历史表; 如果通过办理人权限处理器{@link PermissionHandler#getHandler()}传入了，就不需要传 [按需传输]
- permissionFlag: 办理人权限标识，比如用户，角色，部门等，用于校验是否有权限办理 [按需传输]；满足任一情况可以不传：流程设计时未设置办理人、ignore为true、实现了[办理人权限处理器](./permission_handler.md)
- addHandlers: 委托对象 [必传]
- message: 审批意见 [按需传输]
- ignore: 忽略权限校验（比如管理员不校验），默认不忽略 [按需传输]

### 加签 
`boolean addSignature(taskId, flowParams)`：加签，增加办理人。flowParams包含如下字段：
- handler: 当前办理人唯一标识，如用户id，用于记录历史表; 如果通过办理人权限处理器{@link PermissionHandler#getHandler()}传入了，就不需要传 [按需传输]
- permissionFlag: 办理人权限标识，比如用户，角色，部门等，用于校验是否有权限办理 [按需传输]；满足任一情况可以不传：流程设计时未设置办理人、ignore为true、实现了[办理人权限处理器](./permission_handler.md)
- addHandlers: 加签对象 [必传]
- message: 审批意见 [按需传输]
- ignore: 忽略权限校验（比如管理员不校验），默认不忽略 [按需传输]

### 减签 
`boolean reductionSignature(taskId, flowParams)`：减签，减少办理人。flowParams包含如下字段：
- handler: 当前办理人唯一标识，如用户id，用于记录历史表; 如果通过办理人权限处理器{@link PermissionHandler#getHandler()}传入了，就不需要传 [按需传输]
- permissionFlag: 办理人权限标识，比如用户，角色，部门等，用于校验是否有权限办理 [按需传输]；满足任一情况可以不传：流程设计时未设置办理人、ignore为true、实现了[办理人权限处理器](./permission_handler.md)
- reductionHandlers: 减少办理人 [必传]
- message: 审批意见 [按需传输]
- ignore: 忽略权限校验（比如管理员不校验），默认不忽略 [按需传输]

### 修改办理人 
`boolean updateHandler(taskId, flowParams)`：传入流程任务id，修改办理人
- handler: 办理人唯一标识，如用户id，用于记录历史表 [按需传输]；如果实现了[办理人权限处理器](./permission_handler.md)可不用传
- permissionFlag: 用户所拥有的权限标识 [按需传输，ignore为false，则必传]
- addHandlers: 增加办理人：加签，转办，委托 [按需传输]
- reductionHandlers: 减少办理人：减签，委托 [按需传输]
- message: 审批意见 [按需传输]
- cooperateType: 协作方式(2转办 3委派 6加签 7减签）[按需传输]
- ignore: 转办忽略权限校验（true：忽略，false：不忽略）[按需传输]

### 根据流程实例id获取流程任务集合
`getByInsId(instanceId)`：根据流程实例id获取流程任务集合

### 根据流程实例id和节点code集合获取流程任务集合
`getByInsIdAndNodeCodes(instanceId, nodeCodes)`：根据流程实例id和节点code集合获取流程任务集合

## NodeService流程节点 
### 获取已发布流程节点 
`getPublishByFlowCode(flowCode)`：根据流程编码获取已发布流程节点集合

### 获取前置节点 
`previousNodeList(nodeId)`：根据节点id获取所有的前置节点集合
- nodeId: 节点id [必传]

### 获取前置节点 
`previousNodeList(definitionId, nowNodeCode)`：根据流程定义id和当前节点code获取所有的前置节点集合
- definitionId: 流程定义id [必传]
- nowNodeCode: 当前节点code [必传]

### 获取后置节点 
`suffixNodeList(nodeId)`：根据节点id获取所有的后置节点集合
- nodeId: 节点id [必传]

### 获取后置节点 
`suffixNodeList(definitionId, nowNodeCode)`：根据流程定义id和当前节点code获取所有的后置节点集合
- definitionId: 流程定义id [必传]
- nowNodeCode: 当前节点code [必传]
- 
### 获取下一节点,不一定是后置节点，如果是通过就是后置，如果是驳回就取前置节点-含流程变量过滤
`getNextNodeList(definitionId, nowNodeCode, anyNodeCode, skipType, variable)`：根据流程定义和当前节点code获取下一节点,如是网关跳过取下一节点,并行网关返回多个节点
- definitionId: 流程定义id [必传]
- nowNodeCode: 当前节点code [必传]
- skipType: 跳转类型（PASS审批通过 REJECT退回） [必传]
- anyNodeCode: anyNodeCode不为空，则可跳转anyNodeCode节点（优先级最高） [按需传输]
- variable: 流程变量,下一个节点是网关需要判断跳转条件,并行网关返回多个节点 [按需传输]

### 根据流程定义id获取流程节点集合
`getByDefId(definitionId)`：根据流程定义id获取流程节点集合

### 根据defId和节点编码获取流程节点
`getByDefIdAndNodeCode(definitionId, nodeCode)`：根据流程定义id获取流程节点集合

### 根据流程定义id获取开始节点
`getStartNode(definitionId)`：根据流程定义id获取开始节点

### 根据流程定义id获取中间节点集合
`getBetweenNode(definitionId)`：根据流程定义id获取中间节点集合

### 根据流程定义id获取结束节点
`getEndNode(definitionId)`：根据流程定义id获取结束节点

## SkipService节点跳转关联Service接口

### 批量删除节点跳转关联
`deleteSkipByDefIds(defIds)`：批量删除节点跳转关联

### 根据流程定义id查询节点跳转线
`getByDefId(definitionId)`：根据流程定义id查询节点跳转线

### 根据流程定义id和节点编码查询节点跳转线
`getByDefIdAndNowNodeCode(definitionId, nowNodeCode)`：根据流程定义id和节点编码查询节点跳转线

## HisTaskService历史记录 
### 根据任务id和协作类型查询 
`listByTaskIdAndCooperateTypes(taskId, Integer... cooperateTypes)`：根据任务id和协作类型查询
- taskId: taskId [必传]
- cooperateTypes: 协作类型集合 [按需传输]

### 根据实例Id和节点编码查询 
`getByInsAndNodeCodes(instanceId, nodeCodes)`：根据实例Id和节点编码查询
- instanceId: 实例Id [必传]
- nodeCodes: 节点编码 [按需传输]

### 根据流程实例Ids删除 
`boolean deleteByInsIds(instanceIds)`：根据流程实例Ids删除

## 公共api接口 
### 根据id查询 
`getById(id)`：根据id查询
- id: 主键

### 根据ids主键集合查询 
`getByIds(ids)`：根据ids主键集合查询
- ids: 主键集合

### 分页查询 
`getById(entity, page)`：分页查询
- entity: 查询实体
- page: 分页对象，支持设置排序字段

### 查询列表 
`list(entity)`：查询列表
- entity: 查询实体

### 查询列表，可排序 
`list(entity, query)`：查询列表，可排序
- entity: 查询实体
- query: 查询代理层处理，支持设置排序字段

### 查询一条记录 
`getOne(entity)`：查询一条记录
- entity 查询实体

### 获取总数量 
`selectCount(entity)`：获取总数量
- entity: 查询实体

### 判断是否存在 
`exists(entity)`：判断是否存在
- entity: 查询实体

### 新增 
`save(entity)`：新增
- entity: 实体

### 根据id修改 
`updateById(entity)`：根据id修改
- entity: 实体

### 根据id删除 
`removeById(id)`：根据id删除
- id: 实体

### 根据entity删除 
`remove(entity)`：根据entity删除
- entity: 实体

### 根据ids批量删除 
`removeByIds(ids)`：根据ids批量删除
- ids: 实体

### 批量新增 
`saveBatch(list)`：批量新增
- list: 实体集合

### 批量新增 
`saveBatch(list, batchSize)`：批量新增
- list: 需要插入的集合数据
- batchSize: 插入大小

### 批量更新 
`updateBatch(list)`：批量更新
- list: 集合数据

### id设置正序排列 
`orderById()`：id设置正序排列

### 创建时间设置正序排列 
`orderByCreateTime()`：创建时间设置正序排列

### 更新时间设置正序排列 
`orderByUpdateTime()`：更新时间设置正序排列

### 设置正序排列 
`orderByAsc(orderByField)`：设置正序排列
- orderByField: 排序字段

### 设置倒序排列 
`orderByDesc(orderByField)`：设置倒序排列
- orderByField: 排序字段

### 用户自定义排序方案 
`orderBy(orderByField)`：用户自定义排序方案
- orderByField: 排序字段
