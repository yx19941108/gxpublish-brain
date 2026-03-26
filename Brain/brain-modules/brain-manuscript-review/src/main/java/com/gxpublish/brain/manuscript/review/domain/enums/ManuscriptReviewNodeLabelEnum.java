package com.gxpublish.brain.manuscript.review.domain.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 稿件审校当前节点文案枚举。
 *
 * @author Codex
 * @since 2026-03-26
 */
@Getter
@RequiredArgsConstructor
public enum ManuscriptReviewNodeLabelEnum {

    /**
     * 待一级审批。
     */
    LEVEL_ONE("待一级审批"),

    /**
     * 待二级审批。
     */
    LEVEL_TWO("待二级审批"),

    /**
     * 待三级审批。
     */
    LEVEL_THREE("待三级审批"),

    /**
     * 待发起人处理。
     */
    INITIATOR_PENDING("待发起人处理"),

    /**
     * 流程完成。
     */
    FINISH("流程完成"),

    /**
     * 流程已取消。
     */
    CANCEL("流程已取消"),

    /**
     * 流程已驳回。
     */
    REJECT("流程已驳回");

    private final String label;
}
