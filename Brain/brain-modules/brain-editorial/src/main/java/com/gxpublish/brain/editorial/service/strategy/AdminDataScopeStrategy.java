package com.gxpublish.brain.editorial.service.strategy;

import org.springframework.stereotype.Component;
import java.util.List;
import java.util.Map;

/**
 * 管理员数据过滤策略: 不进行过滤，可以查看租户内所有记录
 *
 * @author gxpublish
 */
@Component
public class AdminDataScopeStrategy implements EditorialDataScopeStrategy {

    @Override
    public boolean supports(List<String> roleKeys, boolean isAdmin, boolean isApplicant, boolean isApprover) {
        return isAdmin;
    }

    @Override
    public void buildScopeParams(Map<String, Object> params, List<String> roleKeys, Long userId) {
        params.put("scopeType", "ALL");
    }
}
