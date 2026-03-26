package com.gxpublish.brain.manuscript.review.domain.entity;

import java.util.Date;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import lombok.Data;

/**
 * 稿件审校附件实体。
 *
 * @author Codex
 * @since 2026-03-26
 */
@Data
@TableName("brain_manuscript_review_attachment")
public class ManuscriptReviewAttachmentEntity {

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
     * 流程单主键。
     */
    private Long reviewId;
    /**
     * OSS 主键。
     */
    private Long ossId;
    /**
     * 文件名称。
     */
    private String fileName;
    /**
     * 文件访问地址。
     */
    private String fileUrl;
    /**
     * 文件大小。
     */
    private Long fileSize;
    /**
     * 文件 MIME 类型。
     */
    private String mimeType;
    /**
     * 是否为视频。
     */
    private Boolean isVideo;
    /**
     * 视频时长秒数。
     */
    private Integer videoDurationSeconds;
    /**
     * 启用状态。
     */
    private String enabled;
    /**
     * 停用人主键。
     */
    private Long disabledBy;
    /**
     * 停用时间。
     */
    private Date disabledTime;
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
