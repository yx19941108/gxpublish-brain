package com.gxpublish.brain.editorial.support;

import com.gxpublish.brain.editorial.enums.EditorialProcessTypeEnum;
import com.gxpublish.brain.editorial.enums.ReviewStatusEnum;
import com.gxpublish.brain.common.core.utils.StringUtils;

import java.util.Map;

/**
 * 审校流程锁定定义。
 */
public final class EditorialReviewWorkflowDefinition {

    public static final String FLOW_CODE = "editorial_review_flow";
    public static final String DETAIL_FORM_PATH = "/editorial/review/detail";
    public static final String FIRST_REVIEW_NODE_CODE = "first-review-node";
    public static final String SECOND_REVIEW_NODE_CODE = "second-review-node";
    public static final String FINAL_REVIEW_NODE_CODE = "final-review-node";
    public static final String FIRST_APPROVER_PERMISSION_VAR = "editorialFirstLevelApprover";
    public static final String SECOND_APPROVER_PERMISSION_VAR = "editorialSecondLevelApprover";
    public static final String FINAL_APPROVER_PERMISSION_VAR = "editorialThirdLevelApprover";

    private static final Map<String, Integer> WAITING_STATUS_BY_NODE_CODE = Map.of(
        FIRST_REVIEW_NODE_CODE, ReviewStatusEnum.WAITING_FIRST.getCode(),
        SECOND_REVIEW_NODE_CODE, ReviewStatusEnum.WAITING_SECOND.getCode(),
        FINAL_REVIEW_NODE_CODE, ReviewStatusEnum.WAITING_FINAL.getCode()
    );

    private EditorialReviewWorkflowDefinition() {
    }

    public static String normalizeProcessType(String processType) {
        return EditorialProcessTypeEnum.normalize(processType);
    }

    public static Integer resolveWaitingStatus(String nodeCode) {
        return WAITING_STATUS_BY_NODE_CODE.get(StringUtils.lowerCase(StringUtils.trim(nodeCode)));
    }

    public static Integer resolveSubmitWaitingStatus(boolean certifiedApplicant) {
        if (certifiedApplicant) {
            return ReviewStatusEnum.WAITING_SECOND.getCode();
        }
        return ReviewStatusEnum.WAITING_FIRST.getCode();
    }
}
