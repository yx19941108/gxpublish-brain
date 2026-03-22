package com.gxpublish.brain.manuscript.review.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Set;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import com.gxpublish.brain.manuscript.review.domain.enums.ManuscriptReviewDetailAction;
import com.gxpublish.brain.manuscript.review.domain.policy.ManuscriptReviewDetailPermissionContext;
import com.gxpublish.brain.manuscript.review.domain.policy.ManuscriptReviewDetailPermissionPolicy;
import com.gxpublish.brain.manuscript.review.domain.policy.ManuscriptReviewDetailPermissionResult;

@Tag("dev")
class ManuscriptReviewDetailPermissionPolicyTest {

    @Test
    void shouldExposeOnlyPrdAllowedActionsForReturnedInitiatorCurrentApproverAndHistoryParticipant() {
        ManuscriptReviewDetailPermissionPolicy policy = new ManuscriptReviewDetailPermissionPolicy();

        ManuscriptReviewDetailPermissionResult returnedInitiator = policy.resolve(
            ManuscriptReviewDetailPermissionContext.builder()
                .currentUserId(1001L)
                .initiatorUserId(1001L)
                .currentApproverUserIds(Set.of(2001L, 2002L))
                .historyParticipantUserIds(Set.of(3001L))
                .returnedToInitiator(true)
                .build()
        );
        assertTrue(returnedInitiator.canModify());
        assertEquals(
            Set.of(
                ManuscriptReviewDetailAction.MODIFY,
                ManuscriptReviewDetailAction.RESUBMIT,
                ManuscriptReviewDetailAction.BACK
            ),
            returnedInitiator.getAllowedActions()
        );
        assertFalse(returnedInitiator.getAllowedActions().contains(ManuscriptReviewDetailAction.GO_APPROVE));
        assertFalse(returnedInitiator.getAllowedActions().contains(ManuscriptReviewDetailAction.DRAFT));
        assertFalse(returnedInitiator.getAllowedActions().contains(ManuscriptReviewDetailAction.DELETE));
        assertFalse(returnedInitiator.getAllowedActions().contains(ManuscriptReviewDetailAction.RECYCLE));
        assertFalse(returnedInitiator.supportsModifyAndSubmitShortcut());
        assertFalse(returnedInitiator.supportsModifyAndApproveShortcut());

        ManuscriptReviewDetailPermissionResult currentApprover = policy.resolve(
            ManuscriptReviewDetailPermissionContext.builder()
                .currentUserId(2001L)
                .initiatorUserId(1001L)
                .currentApproverUserIds(Set.of(2001L, 2002L))
                .historyParticipantUserIds(Set.of(3001L))
                .returnedToInitiator(false)
                .build()
        );
        assertTrue(currentApprover.canModify());
        assertEquals(
            Set.of(
                ManuscriptReviewDetailAction.MODIFY,
                ManuscriptReviewDetailAction.GO_APPROVE,
                ManuscriptReviewDetailAction.BACK
            ),
            currentApprover.getAllowedActions()
        );
        assertFalse(currentApprover.getAllowedActions().contains(ManuscriptReviewDetailAction.RESUBMIT));
        assertFalse(currentApprover.supportsModifyAndApproveShortcut());

        ManuscriptReviewDetailPermissionResult historyParticipant = policy.resolve(
            ManuscriptReviewDetailPermissionContext.builder()
                .currentUserId(3001L)
                .initiatorUserId(1001L)
                .currentApproverUserIds(Set.of(2001L, 2002L))
                .historyParticipantUserIds(Set.of(3001L))
                .returnedToInitiator(false)
                .build()
        );
        assertFalse(historyParticipant.canModify());
        assertEquals(Set.of(ManuscriptReviewDetailAction.BACK), historyParticipant.getAllowedActions());
        assertFalse(historyParticipant.getAllowedActions().contains(ManuscriptReviewDetailAction.MODIFY));
        assertFalse(historyParticipant.getAllowedActions().contains(ManuscriptReviewDetailAction.RESUBMIT));
        assertFalse(historyParticipant.getAllowedActions().contains(ManuscriptReviewDetailAction.GO_APPROVE));
    }
}
