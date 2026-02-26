package com.gxpublish.brain.editorial.service.strategy;

import com.gxpublish.brain.common.core.domain.dto.RoleDTO;
import com.gxpublish.brain.common.satoken.utils.LoginHelper;
import com.gxpublish.brain.editorial.domain.param.EditorialScopeParam;
import com.gxpublish.brain.editorial.enums.EditorialRoleEnum;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;

/**
 * 审校数据过滤范围构建工厂
 * <p>
 * 负责解析当前登录人的角色信息，并产出精确的 {@link EditorialScopeParam} 用于查询过滤
 * </p>
 *
 * @author gxpublish
 */
@Component
public class EditorialDataScopeFactory {

    /**
     * 根据当前登录用户获取对应的数据过滤参数
     *
     * @return 精确过滤参数 EditorialScopeParam
     */
    public EditorialScopeParam buildScopeParams() {
        EditorialScopeParam param = new EditorialScopeParam();

        // 核心管理层最高通行证
        if (LoginHelper.isSuperAdmin()) {
            param.setIsAdmin(true);
            return param;
        }

        // 获取该用户的全量当前生效角色标识列表
        List<String> roleKeys = LoginHelper.getLoginUser().getRoles().stream()
                .map(RoleDTO::getRoleKey)
                .toList();

        // 强匹配超级管理员角色
        boolean isAdmin = roleKeys.stream().anyMatch(r -> r.equals(EditorialRoleEnum.ADMIN.getRoleKey()));
        param.setIsAdmin(isAdmin);

        // 如果不是管理员，深入解析其业务角色
        if (!isAdmin) {
            // 解析发起人角色 (普通及有证双重覆盖)
            boolean isApplicant = roleKeys.stream().anyMatch(r -> r.equals(EditorialRoleEnum.APPLICANT.getRoleKey()) ||
                    r.equals(EditorialRoleEnum.APPLICANT_CER.getRoleKey()));
            param.setIsApplicant(isApplicant);

            // 解析审批人角色可视的精确业务流转状态集
            Set<Integer> visibleStatuses = EditorialRoleEnum.getVisibleStatusesByRoles(roleKeys);

            if (!visibleStatuses.isEmpty()) {
                param.setIsApprover(true);
                param.setVisibleStatuses(visibleStatuses);
            }

            // 绑定用户 ID 用于自己发起的草稿或待办历史记录反向追溯
            param.setUserId(LoginHelper.getUserId());
        }

        return param;
    }
}
