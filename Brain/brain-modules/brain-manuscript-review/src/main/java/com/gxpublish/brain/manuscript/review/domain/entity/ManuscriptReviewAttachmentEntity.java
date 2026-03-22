package com.gxpublish.brain.manuscript.review.domain.entity;

import java.util.Date;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import lombok.Data;

@Data
@TableName("brain_manuscript_review_attachment")
public class ManuscriptReviewAttachmentEntity {

    @TableId("id")
    private Long id;
    private String tenantId;
    private Long reviewId;
    private Long ossId;
    private String fileName;
    private String fileUrl;
    private Long fileSize;
    private String mimeType;
    private Boolean isVideo;
    private Integer videoDurationSeconds;
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
