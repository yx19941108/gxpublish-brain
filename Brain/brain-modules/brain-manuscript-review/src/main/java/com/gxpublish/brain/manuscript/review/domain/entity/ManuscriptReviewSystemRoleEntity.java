package com.gxpublish.brain.manuscript.review.domain.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import lombok.Data;

@Data
@TableName("sys_role")
public class ManuscriptReviewSystemRoleEntity {

    @TableId("role_id")
    private Long roleId;
    private String tenantId;
    private String roleKey;
    private String status;
    private String delFlag;
}
