package com.gxpublish.brain.manuscript.review.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gxpublish.brain.common.core.domain.R;
import com.gxpublish.brain.common.core.exception.ServiceException;
import com.gxpublish.brain.manuscript.review.controller.request.ManuscriptReviewLedgerQueryRequest;
import com.gxpublish.brain.manuscript.review.controller.request.ManuscriptReviewSubmitRequest;
import com.gxpublish.brain.manuscript.review.controller.response.ManuscriptReviewDetailResponse;
import com.gxpublish.brain.manuscript.review.controller.response.ManuscriptReviewLedgerItemResponse;
import com.gxpublish.brain.manuscript.review.controller.response.ManuscriptReviewSubmitResponse;
import com.gxpublish.brain.manuscript.review.domain.enums.ManuscriptReviewProcessType;
import com.gxpublish.brain.manuscript.review.gateway.ManuscriptReviewConfigGateway;
import com.gxpublish.brain.manuscript.review.gateway.ManuscriptReviewCurrentUserGateway;
import com.gxpublish.brain.manuscript.review.gateway.ManuscriptReviewResubmitGateway;
import com.gxpublish.brain.manuscript.review.gateway.ManuscriptReviewSerialGateway;
import com.gxpublish.brain.manuscript.review.gateway.ManuscriptReviewUserRoleGateway;
import com.gxpublish.brain.manuscript.review.service.ManuscriptReviewReadableService;
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

    @Test
    void shouldExposeReadableLedgerContractWithoutRawIdentifiersOrEnums() throws Exception {
        ManuscriptReviewReadableService readableService = mock(ManuscriptReviewReadableService.class);
        ManuscriptReviewReadableApiController controller = new ManuscriptReviewReadableApiController(readableService);

        ManuscriptReviewLedgerQueryRequest request = new ManuscriptReviewLedgerQueryRequest();
        request.setKeyword("系统稿件号");
        request.setProcessTypeLabel("审核流程");
        request.setFlowStatusLabel("审批中");
        request.setCurrentNodeLabel("待一级审批");

        ManuscriptReviewLedgerItemResponse item = new ManuscriptReviewLedgerItemResponse();
        item.setReviewId(9001L);
        item.setManuscriptCode("SH20260321001");
        item.setTitle("审校稿件");
        item.setProcessTypeLabel("审核流程");
        item.setMediaChannelLabel("新华社/要闻");
        item.setFlowStatusLabel("审批中");
        item.setCurrentNodeLabel("待一级审批");
        item.setInitiatorName("张三");
        item.setUpdateTime("2026-03-23 10:20:30");
        when(readableService.listLedger(any(ManuscriptReviewLedgerQueryRequest.class))).thenReturn(List.of(item));

        R<List<ManuscriptReviewLedgerItemResponse>> result = controller.ledger(request);

        assertEquals(R.SUCCESS, result.getCode());
        assertEquals(1, result.getData().size());
        assertEquals(9001L, result.getData().get(0).getReviewId());

        ObjectMapper objectMapper = new ObjectMapper();
        String requestJson = objectMapper.writeValueAsString(request);
        String responseJson = objectMapper.writeValueAsString(result);
        assertTrue(responseJson.contains("reviewId"));
        assertFalse(requestJson.contains("AUDIT"));
        assertFalse(responseJson.contains("AUDIT"));
        assertFalse(responseJson.contains("initiatorUserId"));
        assertFalse(responseJson.contains("userName"));
        assertFalse(responseJson.contains("roleKey"));
        assertFalse(responseJson.contains("permissionFlag"));
    }

    @Test
    void shouldReturnEmptyReadableLedgerWhenDatabaseHasNoRows() {
        ManuscriptReviewReadableService readableService = mock(ManuscriptReviewReadableService.class);
        ManuscriptReviewReadableApiController controller = new ManuscriptReviewReadableApiController(readableService);
        when(readableService.listLedger(any(ManuscriptReviewLedgerQueryRequest.class))).thenReturn(List.of());

        R<List<ManuscriptReviewLedgerItemResponse>> result = controller.ledger(new ManuscriptReviewLedgerQueryRequest());

        assertEquals(R.SUCCESS, result.getCode());
        assertTrue(result.getData().isEmpty());
    }

    @Test
    void shouldRejectReadableDetailWhenReviewMissing() {
        ManuscriptReviewReadableService readableService = mock(ManuscriptReviewReadableService.class);
        ManuscriptReviewReadableApiController controller = new ManuscriptReviewReadableApiController(readableService);
        when(readableService.getDetail(9009L)).thenThrow(new ServiceException("稿件审校流程不存在"));

        ServiceException exception = assertThrows(ServiceException.class, () -> controller.detail(9009L));

        assertEquals("稿件审校流程不存在", exception.getMessage());
    }

    @Test
    void shouldExposeReadableDetailContractWithoutTechnicalIdentifiersInBusinessPayload() throws Exception {
        ManuscriptReviewReadableService readableService = mock(ManuscriptReviewReadableService.class);
        ManuscriptReviewReadableApiController controller = new ManuscriptReviewReadableApiController(readableService);

        ManuscriptReviewDetailResponse detail = new ManuscriptReviewDetailResponse();
        detail.setReviewId(9002L);
        detail.setSummaryCard(new ManuscriptReviewDetailResponse.SummaryCard("已退回", "待发起人处理", "张三", "2026-03-23 11:22:33"));
        detail.setManuscriptCard(new ManuscriptReviewDetailResponse.ManuscriptCard(
            "审核流程", "SH20260321002", "EXT-001", "稿件标题", "新华社/要闻", "总编室", "张三、李四", "补充说明", "正文内容"
        ));
        detail.setActionBar(new ManuscriptReviewDetailResponse.ActionBar(true, List.of("修改", "再次提交", "返回")));
        detail.setTimeline(List.of(new ManuscriptReviewDetailResponse.TimelineItem("2026-03-23 11:20:00", "张三新增了流程。")));
        detail.setResources(new ManuscriptReviewDetailResponse.ResourceSection(
            List.of(new ManuscriptReviewDetailResponse.AttachmentItem("送审单.pdf", "https://files.example/a.pdf", 123L, "application/pdf", null, null)),
            List.of(new ManuscriptReviewDetailResponse.ExternalLinkItem("素材参考", "https://example.com/ref", null, null)),
            List.of(new ManuscriptReviewDetailResponse.VideoItem(
                "样片.mp4", "https://files.example/video.mp4", 1024L, "video/mp4", "00:10:00",
                List.of(new ManuscriptReviewDetailResponse.VideoMarkerItem("00:00:05", "00:00:10", "第一处问题", "2026-03-23 11:21:00", null))
            )),
            List.of(new ManuscriptReviewDetailResponse.AttachmentItem("旧附件.pdf", "https://files.example/old.pdf", 111L, "application/pdf", "2026-03-23 11:00:00", null)),
            List.of(new ManuscriptReviewDetailResponse.ExternalLinkItem("旧链接", "https://example.com/old", "2026-03-23 11:05:00", null)),
            List.of(new ManuscriptReviewDetailResponse.VideoMarkerItem("00:00:15", null, "停用标注", "2026-03-23 11:06:00", "2026-03-23 11:07:00"))
        ));
        when(readableService.getDetail(9002L)).thenReturn(detail);

        R<ManuscriptReviewDetailResponse> result = controller.detail(9002L);

        assertEquals(R.SUCCESS, result.getCode());
        assertEquals(9002L, result.getData().getReviewId());
        assertEquals("已退回", result.getData().getSummaryCard().getFlowStatusLabel());
        assertEquals("审核流程", result.getData().getManuscriptCard().getProcessTypeLabel());

        ObjectMapper objectMapper = new ObjectMapper();
        String responseJson = objectMapper.writeValueAsString(result);
        assertTrue(responseJson.contains("reviewId"));
        assertFalse(responseJson.contains("actionType"));
        assertFalse(responseJson.contains("actorUserId"));
        assertFalse(responseJson.contains("roleKey"));
        assertFalse(responseJson.contains("permissionFlag"));
        assertFalse(responseJson.contains("userName"));
    }
}
