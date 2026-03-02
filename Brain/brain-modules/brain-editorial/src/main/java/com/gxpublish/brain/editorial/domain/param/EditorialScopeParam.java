package com.gxpublish.brain.editorial.domain.param;

import lombok.Data;

import java.util.Set;

/**
 * 编辑部审校专属数据权限控制参数
 * <p>
 * 用于透传判定当前登录人身份所需的各种参数，避免使用不明确的 Map 传参，极简化持久层 SQL。
 * </p>
 */
@Data
public class EditorialScopeParam {

    /**
     * 当前登录用户是否为全权超级管理员
     * 若为 true，则一般放宽所有过滤条件。
     */
    private Boolean isAdmin = false;

    /**
     * 当前登录用户是否具备任何层级的审批人资质（一旦具备，必定触发整数集检索过滤）
     */
    private Boolean isApprover = false;

    /**
     * 当前登录角色映射出的所有可见的精细业务状态集合 (如：[10, 20])
     * 只有在 isApprover 为 true 时此集合才有业务强过滤意义
     */
    private Set<Integer> visibleStatuses;

    /**
     * 提取当前登录人的 ID (用于发起人和关联历史记录的精准回溯)
     */
    private Long userId;

    /**
     * 当前登录用户是否具备发起人资质 (涵盖普通发起人与持证发起人)
     * 用于保证发起人至少能看到自己建的草稿和提的流程
     */
    private Boolean isApplicant = false;
}
