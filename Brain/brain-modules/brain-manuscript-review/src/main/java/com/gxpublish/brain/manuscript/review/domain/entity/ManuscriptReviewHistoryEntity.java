package com.gxpublish.brain.manuscript.review.domain.entity;

import java.util.Date;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import lombok.Data;

@Data
@TableName("brain_manuscript_review_history")
public class ManuscriptReviewHistoryEntity {

    @TableId("id")
    private Long id;
    private String tenantId;
    private Long reviewId;
    private String actionType;
    private String actionText;
    private Long actorUserId;
    private String actorName;
    private Date createTime;
    private String extJson;
}
