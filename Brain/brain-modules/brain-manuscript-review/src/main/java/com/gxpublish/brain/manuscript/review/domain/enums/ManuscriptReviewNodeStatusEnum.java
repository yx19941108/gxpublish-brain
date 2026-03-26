package com.gxpublish.brain.manuscript.review.domain.enums;

import java.util.Arrays;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 稿件审校当前节点状态枚举。
 *
 * <p>统一承接持久化状态码、对外展示文案以及 workflow 原始节点名，
 * 避免读写侧直接依赖 workflow 节点名导致权限和时间线口径漂移。</p>
 *
 * @author Codex
 * @since 2026-03-26
 */
@Getter
@RequiredArgsConstructor
public enum ManuscriptReviewNodeStatusEnum {

    /**
     * 待一级审批。
     */
    LEVEL_1("LEVEL_1", "待一级审批", "一级审批", "first-review-node"),

    /**
     * 待二级审批。
     */
    LEVEL_2("LEVEL_2", "待二级审批", "二级审批", "second-review-node"),

    /**
     * 待三级审批。
     */
    LEVEL_3("LEVEL_3", "待三级审批", "三级审批", "final-review-node"),

    /**
     * 待发起人处理。
     */
    RETURN_TO_INITIATOR("RETURN_TO_INITIATOR", "待发起人处理", null, null),

    /**
     * 流程完成。
     */
    FLOW_FINISHED("FLOW_FINISHED", "流程完成", null, "end-node"),

    /**
     * 流程已取消。
     */
    FLOW_CANCELED("FLOW_CANCELED", "流程已取消", null, null),

    /**
     * 流程已驳回。
     */
    FLOW_REJECTED("FLOW_REJECTED", "流程已驳回", null, null);

    private final String code;
    private final String label;
    private final String workflowNodeName;
    private final String workflowNodeCode;

    public static ManuscriptReviewNodeStatusEnum fromCode(String code) {
        if (code == null) {
            return null;
        }
        return Arrays.stream(values())
            .filter(item -> item.code.equals(code))
            .findFirst()
            .orElse(null);
    }

    public static ManuscriptReviewNodeStatusEnum fromAnyLabel(String label) {
        if (label == null) {
            return null;
        }
        return Arrays.stream(values())
            .filter(item -> item.label.equals(label) || label.equals(item.workflowNodeName))
            .findFirst()
            .orElse(null);
    }

    public static ManuscriptReviewNodeStatusEnum fromWorkflowTask(String nodeCode, String nodeName) {
        if (nodeCode != null) {
            ManuscriptReviewNodeStatusEnum matchedByCode = Arrays.stream(values())
                .filter(item -> nodeCode.equals(item.workflowNodeCode))
                .findFirst()
                .orElse(null);
            if (matchedByCode != null) {
                return matchedByCode;
            }
        }
        return fromAnyLabel(nodeName);
    }
}
