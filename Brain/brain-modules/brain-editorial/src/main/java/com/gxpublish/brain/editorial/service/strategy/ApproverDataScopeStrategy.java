package com.gxpublish.brain.editorial.service.strategy;

import org.springframework.stereotype.Component;
import java.util.List;
import java.util.Map;

/**
 * 审批人数据过滤策略: 只能查看待自己审批和已审批的历史记录
 *
 * @author gxpublish
 */
@Component
public class ApproverDataScopeStrategy implements EditorialDataScopeStrategy {

    @Override
    public boolean supports(List<String> roleKeys, boolean isAdmin, boolean isApplicant, boolean isApprover) {
        return !isAdmin && !isApplicant && isApprover;
    }

    @Override
    public void buildScopeParams(Map<String, Object> params, List<String> roleKeys, Long userId) {
        params.put("scopeType", "APPROVER");
        params.put("userId", userId != null ? userId.toString() : null);
        params.put("roleKeys", roleKeys);
    }
}
