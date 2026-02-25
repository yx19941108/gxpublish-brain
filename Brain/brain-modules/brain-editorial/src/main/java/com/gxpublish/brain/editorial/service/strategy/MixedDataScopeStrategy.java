package com.gxpublish.brain.editorial.service.strategy;

import org.springframework.stereotype.Component;
import java.util.List;
import java.util.Map;

/**
 * 混合身份数据过滤策略: 兼具发起人与某级别审批人的身份，结合两种查询范围
 *
 * @author gxpublish
 */
@Component
public class MixedDataScopeStrategy implements EditorialDataScopeStrategy {

    @Override
    public boolean supports(List<String> roleKeys, boolean isAdmin, boolean isApplicant, boolean isApprover) {
        return !isAdmin && isApplicant && isApprover;
    }

    @Override
    public void buildScopeParams(Map<String, Object> params, List<String> roleKeys, Long userId) {
        params.put("scopeType", "MIXED");
        params.put("userId", userId != null ? userId.toString() : null);
        params.put("roleKeys", roleKeys);
    }
}
