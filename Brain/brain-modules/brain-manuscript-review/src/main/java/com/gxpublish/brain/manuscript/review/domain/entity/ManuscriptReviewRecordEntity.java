package com.gxpublish.brain.manuscript.review.domain.entity;

import java.util.Date;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import lombok.Data;

/**
 * 稿件审校主单实体。
 *
 * @author Codex
 * @since 2026-03-26
 */
@Data
@TableName("brain_manuscript_review")
public class ManuscriptReviewRecordEntity {

    /**
     * 主键。
     */
    @TableId("id")
    private Long id;
    /**
     * 租户编号。
     */
    private String tenantId;
    /**
     * 流程类型编码。
     */
    private String processType;
    /**
     * 流程定义编码。
     */
    private String flowCode;
    /**
     * 流程实例主键。
     */
    private Long flowInstanceId;
    /**
     * 流程状态文案。
     */
    private String flowStatusLabel;
    /**
     * 当前节点状态码。
     */
    private String currentNodeStatus;
    /**
     * 当前节点文案。
     */
    private String currentNodeLabel;
    /**
     * 系统稿件号。
     */
    private String manuscriptCode;
    /**
     * 外部稿件编号。
     */
    private String externalManuscriptCode;
    /**
     * 标题。
     */
    private String title;
    /**
     * 媒体栏目。
     */
    private String mediaChannel;
    /**
     * 报送部门。
     */
    private String submitDepartment;
    /**
     * 作者。
     */
    private String authorName;
    /**
     * 备注说明。
     */
    private String remarkText;
    /**
     * 正文内容。
     */
    private String contentBody;
    /**
     * 首次提交时间。
     */
    private Date firstSubmitTime;
    /**
     * 最近一次提交时间。
     */
    private Date latestSubmitTime;
    /**
     * 兼容旧字段的正文内容。
     */
    private String content;
    /**
     * 兼容旧字段的备注。
     */
    private String note;
    /**
     * 媒体栏目字典编码。
     */
    private Long mediaChannelDictCode;
    /**
     * 媒体栏目展示文案。
     */
    private String mediaChannelLabel;
    /**
     * 报送部门主键。
     */
    private Long submitterDeptId;
    /**
     * 报送部门名称。
     */
    private String submitterDeptName;
    /**
     * 作者列表展示文案。
     */
    private String authorNames;
    /**
     * 发起人用户主键。
     */
    private Long initiatorUserId;
    /**
     * 发起人姓名。
     */
    private String initiatorName;
    /**
     * 创建部门主键。
     */
    private Long createDept;
    /**
     * 创建人主键。
     */
    private Long createBy;
    /**
     * 创建时间。
     */
    private Date createTime;
    /**
     * 更新人主键。
     */
    private Long updateBy;
    /**
     * 更新时间。
     */
    private Date updateTime;
    /**
     * 通用备注字段。
     */
    private String remark;
}
