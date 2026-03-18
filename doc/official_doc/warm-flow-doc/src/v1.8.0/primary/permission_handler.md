# 办理人权限处理器

::: tip
- 审批前通常需要获取当前办理人所拥有的权限，如角色和部门等，办理时会校验的该权限是否是可审批人员  
- 工作流api中很多需要获取当前办理人id，进行保存或者校验  
- 以上业务代码中会出现很多重复代码，为此抽取出这个接口  
- 不强制要求返回返回所有的权限（角色、部门、用户、岗位等），设计器设置的时候有哪些权限就传哪些
 
:::

## 1、PermissionHandler接口

```java
/**
 * 办理人权限处理器
 * 用户获取工作流中用到的permissionFlag和handler
 * permissionFlag: 办理人权限标识，比如用户，角色，部门等，用于校验是否有权限办理任务
 * handler: 当前办理人唯一标识，就是确定唯一用的，如用户id，通常用来入库，记录流程实例创建人，办理人
 *
 * @author shadow
 */
public interface PermissionHandler {

    /**
     * 办理人权限标识，比如用户，角色，部门等，用于校验是否有权限办理任务
     * 后续在{@link FlowParams#getPermissionFlag}  中获取
     * 返回当前用户权限集合
     *
     */
    List<String> permissions();

    /**
     * 获取当前办理人：就是确定唯一用的，如用户id，通常用来入库，记录流程实例创建人，办理人
     * 后续在{@link FlowParams#getHandler()}  中获取
     * @return 当前办理人
     */
    String getHandler();

    /**
     * 转换办理人，比如设计器中预设了能办理的人，如果其中包含角色或者部门id等，可以通过此接口进行转换成用户id
     */
    default List<String> convertPermissions(List<String> permissions) {
        return permissions;
    }

}
```

## 2、编写PermissionHandler实现类

```java
/**
 * 办理人权限处理器（可通过配置文件注入，也可用@Bean/@Component方式）
 *
 * @author shadow
 */
@Component
/**
 * 办理人权限处理器（可通过配置文件注入，也可用@Bean/@Component方式）
 *
 * @author shadow
 */
public class CustomPermissionHandler implements PermissionHandler {

    /**
     * 获取当前操作用户所有权限
     */
    @Override
    public List<String> permissions() {
        // 办理人权限标识，比如用户，角色，部门等, 流程设计时未设置办理人或者ignore为true可不传 [按需传输]
        SysUser sysUser = SecurityUtils.getLoginUser().getUser();
        List<SysRole> roles = sysUser.getRoles();
        List<String> permissionList = StreamUtils.toList(roles, role -> "role:" + role.getRoleId());
        if (sysUser.getUserId() != null) {
            permissionList.add(String.valueOf(sysUser.getUserId()));
        }
        if (sysUser.getDeptId() != null) {
            permissionList.add("dept:" + sysUser.getDeptId());
        }
        return permissionList;
    }

    /**
     * 获取当前办理人
     * @return 当前办理人
     */
    @Override
    public String getHandler() {
        SysUser sysUser = SecurityUtils.getLoginUser().getUser();
        if (sysUser.getUserId() != null) {
            return String.valueOf(sysUser.getUserId());
        }
        return null;
    }

    /**
     * 转换办理人，比如设计器中预设了能办理的人，如果其中包含角色或者部门id等，可以通过此接口进行转换成用户id
     */
    @Override
    public List<String> convertPermissions(List<String> permissions) {
        // 把角色部门转换成用户
        ......
        return permissions;
    }
}

```
