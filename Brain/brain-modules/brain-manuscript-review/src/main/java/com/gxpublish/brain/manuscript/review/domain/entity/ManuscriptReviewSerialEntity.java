package com.gxpublish.brain.manuscript.review.domain.entity;

import java.util.Date;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import lombok.Data;

@Data
@TableName("brain_manuscript_review_serial")
public class ManuscriptReviewSerialEntity {

    @TableId("id")
    private Long id;
    private String tenantId;
    private String processType;
    private String bizDate;
    private Integer currentSerial;
    private Date updateTime;
}
