package com.gxpublish.brain.editorial.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 稿件三审三校全量业务角色与可见进度状态绑定枚举
 * <p>
 * 统管该业务板块出现的所有角色并实现字面量防硬编码，同时内聚实现了基于角色的审批状态可见度计算。
 * </p>
 */
@Getter
@AllArgsConstructor
public enum EditorialRoleEnum {

    /**
     * 基础特权控制：系统超级管理员
     */
    ADMIN("admin", null),

    /**
     * 发起人控制：普通编辑发起人角色
     */
    APPLICANT("editorial_review_applicant", null),

    /**
     * 发起人控制：具备职业资格证书的编辑发起人角色
     */
    APPLICANT_CER("editorial_review_applicant_has_certificate", null),

    /**
     * 审批流转：一级编辑室主任审批角色，可见“待一审”的稿件
     */
    FIRST_APPROVER("editorial_first_level_approver", ReviewStatusEnum.WAITING_FIRST.getCode()),

    /**
     * 审批流转：二级总编室复审角色，可见“待二审”的稿件
     */
    SECOND_APPROVER("editorial_second_level_approver", ReviewStatusEnum.WAITING_SECOND.getCode()),

    /**
     * 审批流转：三级社领导终审角色，可见“待终审”的稿件
     */
    FINAL_APPROVER("editorial_third_level_approver", ReviewStatusEnum.WAITING_FINAL.getCode());

    /**
     * 实际数据库 sys_role 表的角色标识（完全强对应）
     */
    private final String roleKey;

    /**
     * 该角色所负责监管的精细待办业务状态码（review_status）
     * (若该角色并非通过某个特定的待办状态来驱动可见性，如发起人/管理员，则为 null)
     */
    private final Integer statusCode;

    /**
     * 核心亮点逻辑：根据当前登录用户所拥有的角色列表，自动解析出此用户所有的可见待办业务状态集合
     * (已运用绝对相等的 equals 进行匹配防御，根除子串越权隐患)
     *
     * @param userRoleKeys 当前登录用户持有的所有真实角色串
     * @return 最终应可见的目标业务状态 IN 指向集合（如包含 10、20）
     */
    public static Set<Integer> getVisibleStatusesByRoles(List<String> userRoleKeys) {
        Set<Integer> visibleStatuses = new HashSet<>();
        for (String roleKey : userRoleKeys) {
            for (EditorialRoleEnum enumItem : values()) {
                if (enumItem.getStatusCode() != null && roleKey.equals(enumItem.getRoleKey())) {
                    visibleStatuses.add(enumItem.getStatusCode());
                }
            }
        }
        return visibleStatuses;
    }
}
