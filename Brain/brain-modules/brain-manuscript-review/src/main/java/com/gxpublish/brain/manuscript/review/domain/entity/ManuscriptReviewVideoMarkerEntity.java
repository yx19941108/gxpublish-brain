package com.gxpublish.brain.manuscript.review.domain.entity;

import java.util.Date;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import lombok.Data;

@Data
@TableName("brain_manuscript_review_video_marker")
public class ManuscriptReviewVideoMarkerEntity {

    @TableId("id")
    private Long id;
    private String tenantId;
    private Long reviewId;
    private Long videoAttachmentId;
    private String startTime;
    private String endTime;
    private Integer startSeconds;
    private Integer endSeconds;
    private String markerNote;
    private String enabled;
    private Long disabledBy;
    private Date disabledTime;
    private Long createDept;
    private Long createBy;
    private Date createTime;
    private Long updateBy;
    private Date updateTime;
    private String remark;
}
