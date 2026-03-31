package com.gxpublish.brain.manuscript.review.domain.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import lombok.Data;

@Data
@TableName("sys_user")
public class ManuscriptReviewSystemUserEntity {

    @TableId("user_id")
    private Long userId;
    private String tenantId;
    private String userName;
    private String nickName;
    private String status;
    private String delFlag;
}
