package com.gxpublish.brain.manuscript.review.domain.entity;

import com.baomidou.mybatisplus.annotation.TableName;

import lombok.Data;

@Data
@TableName("sys_user_role")
public class ManuscriptReviewSystemUserRoleEntity {

    private Long userId;
    private Long roleId;
}
