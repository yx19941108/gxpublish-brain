package com.gxpublish.brain.editorial.service.strategy;

import org.springframework.stereotype.Component;
import java.util.List;
import java.util.Map;

/**
 * 发起人数据过滤策略: 只能查看自己发起的记录
 *
 * @author gxpublish
 */
@Component
public class ApplicantDataScopeStrategy implements EditorialDataScopeStrategy {

    @Override
    public boolean supports(List<String> roleKeys, boolean isAdmin, boolean isApplicant, boolean isApprover) {
        return !isAdmin && isApplicant && !isApprover;
    }

    @Override
    public void buildScopeParams(Map<String, Object> params, List<String> roleKeys, Long userId) {
        params.put("scopeType", "APPLICANT");
        params.put("userId", userId);
    }
}
