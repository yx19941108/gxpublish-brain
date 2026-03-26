package com.gxpublish.brain.manuscript.review.domain.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 稿件审校业务流程状态文案枚举。
 *
 * @author Codex
 * @since 2026-03-26
 */
@Getter
@RequiredArgsConstructor
public enum ManuscriptReviewFlowStatusEnum {

    /**
     * 审批中。
     */
    WAITING("审批中"),

    /**
     * 已退回。
     */
    BACK("已退回"),

    /**
     * 已取消。
     */
    CANCEL("已取消"),

    /**
     * 已完成。
     */
    FINISH("已完成"),

    /**
     * 已驳回。
     */
    REJECT("已驳回");

    private final String label;
}
