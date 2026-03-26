package com.gxpublish.brain.manuscript.review.domain.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 稿件审校流程类型枚举。
 *
 * <p>v6.26 追加改动：统一流程类型枚举风格，集中维护流程展示名称与稿件号前缀。</p>
 *
 * @author Codex
 * @since 2026-03-26
 */
@Getter
@RequiredArgsConstructor
public enum ManuscriptReviewProcessType {

    /**
     * 审核流程。
     */
    AUDIT("审核流程", "SH"),

    /**
     * 校对流程。
     */
    PROOFREAD("校对流程", "JD");

    /**
     * 流程展示名称。
     */
    private final String displayName;

    /**
     * 稿件号前缀。
     */
    private final String manuscriptCodePrefix;
}
