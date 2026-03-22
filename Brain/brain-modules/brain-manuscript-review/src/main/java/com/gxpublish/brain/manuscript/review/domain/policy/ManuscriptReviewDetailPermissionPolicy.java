package com.gxpublish.brain.manuscript.review.domain.policy;

import java.util.Set;

import com.gxpublish.brain.manuscript.review.domain.enums.ManuscriptReviewDetailAction;

public class ManuscriptReviewDetailPermissionPolicy {

    public ManuscriptReviewDetailPermissionResult resolve(ManuscriptReviewDetailPermissionContext context) {
        if (isReturnedInitiator(context)) {
            return ManuscriptReviewDetailPermissionResult.builder()
                .canModify(true)
                .allowedActions(Set.of(
                    ManuscriptReviewDetailAction.MODIFY,
                    ManuscriptReviewDetailAction.RESUBMIT,
                    ManuscriptReviewDetailAction.BACK
                ))
                .supportsModifyAndSubmitShortcut(false)
                .supportsModifyAndApproveShortcut(false)
                .build();
        }
        if (isCurrentApprover(context)) {
            return ManuscriptReviewDetailPermissionResult.builder()
                .canModify(true)
                .allowedActions(Set.of(
                    ManuscriptReviewDetailAction.MODIFY,
                    ManuscriptReviewDetailAction.GO_APPROVE,
                    ManuscriptReviewDetailAction.BACK
                ))
                .supportsModifyAndSubmitShortcut(false)
                .supportsModifyAndApproveShortcut(false)
                .build();
        }
        return ManuscriptReviewDetailPermissionResult.builder()
            .canModify(false)
            .allowedActions(Set.of(ManuscriptReviewDetailAction.BACK))
            .supportsModifyAndSubmitShortcut(false)
            .supportsModifyAndApproveShortcut(false)
            .build();
    }

    private boolean isReturnedInitiator(ManuscriptReviewDetailPermissionContext context) {
        return context.isReturnedToInitiator()
            && context.getCurrentUserId() != null
            && context.getCurrentUserId().equals(context.getInitiatorUserId());
    }

    private boolean isCurrentApprover(ManuscriptReviewDetailPermissionContext context) {
        return context.getCurrentUserId() != null
            && context.getCurrentApproverUserIds().contains(context.getCurrentUserId());
    }
}
