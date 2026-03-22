package com.gxpublish.brain.manuscript.review.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.LocalDate;
import java.time.ZoneId;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gxpublish.brain.common.core.domain.R;
import com.gxpublish.brain.manuscript.review.controller.request.ManuscriptReviewSubmitRequest;
import com.gxpublish.brain.manuscript.review.controller.response.ManuscriptReviewSubmitResponse;
import com.gxpublish.brain.manuscript.review.domain.enums.ManuscriptReviewProcessType;
import com.gxpublish.brain.manuscript.review.gateway.ManuscriptReviewConfigGateway;
import com.gxpublish.brain.manuscript.review.gateway.ManuscriptReviewCurrentUserGateway;
import com.gxpublish.brain.manuscript.review.gateway.ManuscriptReviewResubmitGateway;
import com.gxpublish.brain.manuscript.review.gateway.ManuscriptReviewSerialGateway;
import com.gxpublish.brain.manuscript.review.gateway.ManuscriptReviewUserRoleGateway;
import com.gxpublish.brain.manuscript.review.service.ManuscriptReviewService;

@Tag("dev")
class ManuscriptReviewApiControllerTest {

    private static final ZoneId BUSINESS_ZONE_ID = ZoneId.of("Asia/Shanghai");

    @Test
    void shouldExposeReadableSubmitContractWithoutApplicantUserId() throws Exception {
        ManuscriptReviewConfigGateway configGateway = mock(ManuscriptReviewConfigGateway.class);
        ManuscriptReviewSerialGateway serialGateway = mock(ManuscriptReviewSerialGateway.class);
        ManuscriptReviewUserRoleGateway userRoleGateway = mock(ManuscriptReviewUserRoleGateway.class);
        ManuscriptReviewResubmitGateway resubmitGateway = mock(ManuscriptReviewResubmitGateway.class);
        ManuscriptReviewCurrentUserGateway currentUserGateway = mock(ManuscriptReviewCurrentUserGateway.class);
        when(configGateway.hasCompleteApprovalChain(ManuscriptReviewProcessType.AUDIT)).thenReturn(true);
        when(configGateway.hasActiveApproverMembers(ManuscriptReviewProcessType.AUDIT)).thenReturn(true);
        when(serialGateway.nextSerial(ManuscriptReviewProcessType.AUDIT, "20260321")).thenReturn(1);
        when(userRoleGateway.hasCertifiedApplicantRole(3001L)).thenReturn(false);
        when(currentUserGateway.getCurrentUserId()).thenReturn(3001L);

        ManuscriptReviewService manuscriptReviewService = new ManuscriptReviewService(
            configGateway,
            serialGateway,
            userRoleGateway,
            resubmitGateway,
            currentUserGateway,
            Clock.fixed(LocalDate.of(2026, 3, 21).atStartOfDay(BUSINESS_ZONE_ID).toInstant(), BUSINESS_ZONE_ID)
        );
        ManuscriptReviewApiController controller = new ManuscriptReviewApiController(manuscriptReviewService);

        ManuscriptReviewSubmitRequest request = new ManuscriptReviewSubmitRequest();
        request.setProcessType(ManuscriptReviewProcessType.AUDIT);
        request.setTitle("测试稿件");
        request.setContent("正文");
        request.setAttachmentCount(1);

        R<ManuscriptReviewSubmitResponse> result = controller.submit(request);

        assertEquals(R.SUCCESS, result.getCode());
        assertEquals("SH20260321001", result.getData().getManuscriptCode());
        assertEquals("审批中", result.getData().getFlowStatus());
        assertEquals("待一级审批", result.getData().getCurrentNode());

        ObjectMapper objectMapper = new ObjectMapper();
        String requestJson = objectMapper.writeValueAsString(request);
        String responseJson = objectMapper.writeValueAsString(result);
        assertFalse(requestJson.contains("applicantUserId"));
        assertFalse(responseJson.contains("applicantUserId"));
    }
}
