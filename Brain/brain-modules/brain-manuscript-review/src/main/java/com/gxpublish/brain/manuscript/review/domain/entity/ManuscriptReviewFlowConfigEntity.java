package com.gxpublish.brain.manuscript.review.domain.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import lombok.Data;

@Data
@TableName("brain_manuscript_review_flow_config")
public class ManuscriptReviewFlowConfigEntity {

    @TableId("id")
    private Long id;
    private String tenantId;
    private String processType;
    private String flowCode;
    private String levelOneRoleKey;
    private String levelTwoRoleKey;
    private String levelThreeRoleKey;
    private String status;
}
