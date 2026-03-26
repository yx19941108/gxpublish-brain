package com.gxpublish.brain.manuscript.review.domain.enums;

import java.util.Arrays;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 稿件审校资源类型枚举。
 *
 * @author Codex
 * @since 2026-03-26
 */
@Getter
@RequiredArgsConstructor
public enum ManuscriptReviewResourceTypeEnum {

    /**
     * 附件。
     */
    ATTACHMENT("ATTACHMENT", "附件", false),

    /**
     * 视频。
     */
    VIDEO("VIDEO", "视频", true),

    /**
     * 外链。
     */
    EXTERNAL_LINK("EXTERNAL_LINK", "外链", false);

    private final String code;
    private final String label;
    private final boolean video;

    /**
     * 根据资源类型编码解析枚举。
     *
     * @param code 资源类型编码
     * @return 资源类型枚举
     */
    public static ManuscriptReviewResourceTypeEnum fromCode(String code) {
        return Arrays.stream(values())
            .filter(item -> item.code.equalsIgnoreCase(code))
            .findFirst()
            .orElse(null);
    }
}
