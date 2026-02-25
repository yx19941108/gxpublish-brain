package com.gxpublish.brain.editorial.service.strategy;

import java.util.List;
import java.util.Map;

/**
 * 编辑部审校数据范围策略接口
 * 用于动态决定不同角色在查询审校列表时的数据过滤条件
 *
 * @author gxpublish
 */
public interface EditorialDataScopeStrategy {

    /**
     * 判断当前策略是否支持该用户角色组合
     *
     * @param roleKeys    用户角色Key集合
     * @param isAdmin     是否包含超管/管理员角色
     * @param isApplicant 是否包含发起人角色
     * @param isApprover  是否包含审批人角色
     * @return 是否支持
     */
    boolean supports(List<String> roleKeys, boolean isAdmin, boolean isApplicant, boolean isApprover);

    /**
     * 构建 Mybatis XML 查询参数
     *
     * @param params   查询参数 Map (将被传递给 XML)
     * @param roleKeys 用户角色Key集合
     * @param userId   当前登录用户ID
     */
    void buildScopeParams(Map<String, Object> params, List<String> roleKeys, Long userId);
}
