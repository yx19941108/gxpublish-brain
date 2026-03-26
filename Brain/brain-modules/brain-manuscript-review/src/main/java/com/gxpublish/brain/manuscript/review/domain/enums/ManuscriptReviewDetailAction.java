package com.gxpublish.brain.manuscript.review.domain.enums;

/**
 * 稿件审校详情页动作枚举。
 *
 * <p>v6.26 追加改动：统一详情页动作枚举注释风格，保持按钮分流语义集中定义。</p>
 *
 * @author Codex
 * @since 2026-03-26
 */
public enum ManuscriptReviewDetailAction {

    /**
     * 修改主单。
     */
    MODIFY,

    /**
     * 发起人重新提交。
     */
    RESUBMIT,

    /**
     * 前往审批页。
     */
    GO_APPROVE,

    /**
     * 流程退回。
     */
    BACK,

    /**
     * 草稿态动作。
     */
    DRAFT,

    /**
     * 删除动作。
     */
    DELETE,

    /**
     * 回收站动作。
     */
    RECYCLE
}
