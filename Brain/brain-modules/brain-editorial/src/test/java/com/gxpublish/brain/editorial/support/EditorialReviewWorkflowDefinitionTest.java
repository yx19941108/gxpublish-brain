package com.gxpublish.brain.editorial.support;

import com.gxpublish.brain.common.core.exception.ServiceException;
import com.gxpublish.brain.editorial.enums.ReviewStatusEnum;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

@Tag("dev")
class EditorialReviewWorkflowDefinitionTest {

    @Test
    void shouldNormalizeSupportedProcessType() {
        assertEquals("AUDIT", EditorialReviewWorkflowDefinition.normalizeProcessType(" audit "));
        assertEquals("PROOFREAD", EditorialReviewWorkflowDefinition.normalizeProcessType("proofread"));
    }

    @Test
    void shouldRejectUnsupportedProcessType() {
        assertThrows(ServiceException.class,
            () -> EditorialReviewWorkflowDefinition.normalizeProcessType("publish"));
    }

    @Test
    void shouldResolveWaitingStatusByLockedNodeCodeOnly() {
        assertEquals(ReviewStatusEnum.WAITING_FIRST.getCode(),
            EditorialReviewWorkflowDefinition.resolveWaitingStatus("first-review-node"));
        assertEquals(ReviewStatusEnum.WAITING_SECOND.getCode(),
            EditorialReviewWorkflowDefinition.resolveWaitingStatus("second-review-node"));
        assertEquals(ReviewStatusEnum.WAITING_FINAL.getCode(),
            EditorialReviewWorkflowDefinition.resolveWaitingStatus("final-review-node"));
        assertNull(EditorialReviewWorkflowDefinition.resolveWaitingStatus("dept-audit-node"));
    }

    @Test
    void shouldSkipFirstWaitingStatusForCertifiedApplicant() {
        assertEquals(ReviewStatusEnum.WAITING_FIRST.getCode(),
            EditorialReviewWorkflowDefinition.resolveSubmitWaitingStatus(false));
        assertEquals(ReviewStatusEnum.WAITING_SECOND.getCode(),
            EditorialReviewWorkflowDefinition.resolveSubmitWaitingStatus(true));
    }
}
