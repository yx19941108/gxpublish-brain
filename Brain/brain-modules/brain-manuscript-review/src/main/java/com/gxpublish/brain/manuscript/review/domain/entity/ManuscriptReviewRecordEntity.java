package com.gxpublish.brain.manuscript.review.domain.entity;

import java.util.Date;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import lombok.Data;

@Data
@TableName("brain_manuscript_review")
public class ManuscriptReviewRecordEntity {

    @TableId("id")
    private Long id;
    private String tenantId;
    private String processType;
    private String flowCode;
    private Long flowInstanceId;
    private String flowStatusLabel;
    private String currentNodeLabel;
    private String manuscriptCode;
    private String externalManuscriptCode;
    private String title;
    private String content;
    private String note;
    private Long mediaChannelDictCode;
    private String mediaChannelLabel;
    private Long submitterDeptId;
    private String submitterDeptName;
    private String authorNames;
    private Long initiatorUserId;
    private String initiatorName;
    private Long createDept;
    private Long createBy;
    private Date createTime;
    private Long updateBy;
    private Date updateTime;
    private String remark;
}
