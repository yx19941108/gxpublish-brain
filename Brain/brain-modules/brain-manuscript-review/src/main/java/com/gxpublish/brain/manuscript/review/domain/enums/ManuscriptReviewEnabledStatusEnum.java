package com.gxpublish.brain.manuscript.review.domain.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 稿件审校资源启停状态枚举。
 *
 * @author Codex
 * @since 2026-03-26
 */
@Getter
@RequiredArgsConstructor
public enum ManuscriptReviewEnabledStatusEnum {

    /**
     * 启用状态。
     */
    ENABLED("1"),

    /**
     * 停用状态。
     */
    DISABLED("0");

    private final String code;
}
