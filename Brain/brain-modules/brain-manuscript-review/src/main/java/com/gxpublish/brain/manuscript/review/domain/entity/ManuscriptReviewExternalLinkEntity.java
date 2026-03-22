package com.gxpublish.brain.manuscript.review.domain.entity;

import java.util.Date;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import lombok.Data;

@Data
@TableName("brain_manuscript_review_external_link")
public class ManuscriptReviewExternalLinkEntity {

    @TableId("id")
    private Long id;
    private String tenantId;
    private Long reviewId;
    private String linkTitle;
    private String linkUrl;
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
