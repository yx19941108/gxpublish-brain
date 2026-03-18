# 自定义流程状态


::: tip
- flowStatus：流程实例表状态，当前流程状态    
- hisStatus：历史任务表状态，过程状态记录，按照自身业务要求，可以语流程实例状态不同  

:::



## 1、开启流程

```java
    public void insertTestLeave(TestLeave testLeave, Integer flowStatus)
    {
        String id = IdUtils.nextIdStr();
        testLeave.setId(id);
        LoginUser user = SecurityUtils.getLoginUser();
        FlowParams flowParams = FlowParams.build().flowCode(getFlowType(testLeave))
                .handler(user.getUser().getUserId().toString());
        
        // 自定义流程状态扩展，flowStatus与hisStatus可以不同
        if (Objects.nonNull(flowStatus)) {
            flowParams.flowStatus(flowStatus).hisStatus(flowStatus);
        }

        Instance instance = insService.start(id, flowParams);
    }
```

## 2、流程跳转

```java
        // 自定义流程状态扩展，flowStatus与hisStatus可以不同
        if (Objects.nonNull(flowStatus)) {
            flowParams.flowStatus(flowStatus).hisStatus(flowStatus);
        }
        Instance instance = insService.skipByInsId(testLeave.getInstanceId(), flowParams);
```

```java
        // 自定义流程状态扩展，flowStatus与hisStatus可以不同
        if (Objects.nonNull(flowStatus)) {
            flowParams.flowStatus(flowStatus).hisStatus(flowStatus);
        }
        Instance instance = taskService.skip(taskId, flowParams);
```

## 3、其他请查阅核心api
