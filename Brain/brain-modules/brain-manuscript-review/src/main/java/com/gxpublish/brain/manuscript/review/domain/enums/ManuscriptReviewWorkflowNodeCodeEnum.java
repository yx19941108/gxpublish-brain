package com.gxpublish.brain.manuscript.review.domain.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 稿件审校流程节点编码枚举。
 *
 * @author Codex
 * @since 2026-03-26
 */
@Getter
@RequiredArgsConstructor
public enum ManuscriptReviewWorkflowNodeCodeEnum {

    /**
     * 二级审批节点。
     */
    SECOND_REVIEW_NODE("second-review-node"),

    /**
     * 三级审批节点。
     */
    FINAL_REVIEW_NODE("final-review-node"),

    /**
     * 结束节点。
     */
    END_NODE("end-node");

    private final String code;
}
