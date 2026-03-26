package com.gxpublish.brain.manuscript.review.domain.entity;

import java.util.Date;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import lombok.Data;

/**
 * 稿件审校历史实体。
 *
 * @author Codex
 * @since 2026-03-26
 */
@Data
@TableName("brain_manuscript_review_history")
public class ManuscriptReviewHistoryEntity {

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
     * 动作类型编码。
     */
    private String actionType;
    /**
     * 动作文案。
     */
    private String actionText;
    /**
     * 动作人主键。
     */
    private Long actorUserId;
    /**
     * 动作人姓名。
     */
    private String actorName;
    /**
     * 创建时间。
     */
    private Date createTime;
    /**
     * 同一流程内的稳定排序号。
     */
    private Integer sorted;
    /**
     * 扩展 JSON。
     */
    private String extJson;
}
