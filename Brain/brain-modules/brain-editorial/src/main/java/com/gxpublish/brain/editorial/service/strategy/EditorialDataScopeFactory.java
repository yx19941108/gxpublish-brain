package com.gxpublish.brain.editorial.service.strategy;

import com.gxpublish.brain.common.core.domain.dto.RoleDTO;
import com.gxpublish.brain.common.satoken.utils.LoginHelper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 编辑部审校数据范围策略工厂
 *
 * @author gxpublish
 */
@Component
@RequiredArgsConstructor
public class EditorialDataScopeFactory {

    private final List<EditorialDataScopeStrategy> strategies;

    /**
     * 根据当前登录用户获取对应的数据过滤策略
     *
     * @return 匹配的策略实例
     */
    public EditorialDataScopeStrategy getStrategy() {
        if (LoginHelper.isSuperAdmin()) {
            return getAdminStrategy();
        }

        List<String> roleKeys = LoginHelper.getLoginUser().getRoles().stream()
                .map(RoleDTO::getRoleKey)
                .toList();

        boolean isAdmin = roleKeys.stream().anyMatch(r -> r.contains("admin"));
        boolean isApplicant = roleKeys.stream().anyMatch(r -> r.contains("applicant"));
        boolean isApprover = roleKeys.stream().anyMatch(r -> r.contains("approver"));

        return strategies.stream()
                .filter(s -> s.supports(roleKeys, isAdmin, isApplicant, isApprover))
                .findFirst()
                .orElseGet(this::getAdminStrategy);
    }

    private EditorialDataScopeStrategy getAdminStrategy() {
        return strategies.stream()
                .filter(s -> s instanceof AdminDataScopeStrategy)
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Admin strategy not found"));
    }
}
