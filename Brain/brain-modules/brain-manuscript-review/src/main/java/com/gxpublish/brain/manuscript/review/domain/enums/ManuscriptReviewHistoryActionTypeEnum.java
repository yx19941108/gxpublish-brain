package com.gxpublish.brain.manuscript.review.domain.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 稿件审校历史动作类型枚举。
 *
 * @author Codex
 * @since 2026-03-26
 */
@Getter
@RequiredArgsConstructor
public enum ManuscriptReviewHistoryActionTypeEnum {

    /**
     * 新增提交一体化主动作。
     */
    CREATE("CREATE"),

    /**
     * 主单修改。
     */
    UPDATE("UPDATE"),

    /**
     * 再次提交。
     */
    RESUBMIT("RESUBMIT"),

    /**
     * 资源新增。
     */
    RESOURCE_ADD("RESOURCE_ADD"),

    /**
     * 资源停用。
     */
    RESOURCE_DISABLE("RESOURCE_DISABLE"),

    /**
     * 视频标注新增。
     */
    VIDEO_MARK_ADD("VIDEO_MARK_ADD"),

    /**
     * 视频标注停用。
     */
    VIDEO_MARK_DISABLE("VIDEO_MARK_DISABLE"),

    /**
     * 持证跳过一级审批。
     */
    SKIP_LEVEL_1("SKIP_LEVEL_1"),

    /**
     * 审批通过。
     */
    APPROVE("APPROVE"),

    /**
     * 流程退回。
     */
    BACK("BACK"),

    /**
     * 流程完成。
     */
    FINISH("FINISH"),

    /**
     * 流程驳回。
     */
    REJECT("REJECT"),

    /**
     * 流程撤销。
     */
    CANCEL("CANCEL");

    private final String code;
}
