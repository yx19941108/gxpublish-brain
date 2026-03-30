package com.gxpublish.brain.manuscript.review.controller;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import cn.dev33.satoken.annotation.SaIgnore;
import cn.dev33.satoken.annotation.SaCheckPermission;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gxpublish.brain.manuscript.review.controller.request.ManuscriptReviewLedgerQueryRequest;
import com.gxpublish.brain.manuscript.review.controller.request.ManuscriptReviewSubmitRequest;
import com.gxpublish.brain.manuscript.review.controller.ManuscriptReviewApiController.ReviewIdRequest;
import com.gxpublish.brain.manuscript.review.controller.response.ManuscriptReviewDetailResponse;
import com.gxpublish.brain.manuscript.review.controller.response.ManuscriptReviewLedgerItemResponse;

@Tag("dev")
class ManuscriptReviewApiControllerTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void shouldExposeFrozenWorkflowRootMappings() {
        RequestMapping apiMapping = ManuscriptReviewApiController.class.getAnnotation(RequestMapping.class);
        RequestMapping readableMapping = ManuscriptReviewReadableApiController.class.getAnnotation(RequestMapping.class);

        assertArrayEquals(new String[] {"/workflow/manuscript-review"}, apiMapping.value());
        assertArrayEquals(new String[] {"/workflow/manuscript-review"}, readableMapping.value());
    }

    @Test
    void shouldDeclareFirstRoundPermissionAnnotationsOnWriteEndpoints() throws Exception {
        assertPermission("update", "manuscript:review:edit", ManuscriptReviewSubmitRequest.class);
        assertPermission("submitAndFlowStart", "manuscript:review:submit", ManuscriptReviewSubmitRequest.class);
        assertPermission("resubmit", "manuscript:review:resubmit", ReviewIdRequest.class);
    }

    @Test
    void shouldExposeSecondRoundPendingDeleteAndPreviewEndpoints() {
        Method pendingDeleteMethod = Arrays.stream(ManuscriptReviewApiController.class.getDeclaredMethods())
            .filter(method -> "deletePendingResource".equals(method.getName()))
            .findFirst()
            .orElseThrow(() -> new AssertionError("deletePendingResource endpoint should exist for round-2 issue 1"));
        DeleteMapping pendingDeleteMapping = pendingDeleteMethod.getAnnotation(DeleteMapping.class);
        assertTrue(pendingDeleteMapping != null && pendingDeleteMapping.value().length > 0
                && "/resource/pending/{ossId}".equals(pendingDeleteMapping.value()[0]),
            "deletePendingResource should map to /resource/pending/{ossId}");

        Method previewMethod = Arrays.stream(ManuscriptReviewReadableApiController.class.getDeclaredMethods())
            .filter(method -> "previewResource".equals(method.getName()))
            .findFirst()
            .orElseThrow(() -> new AssertionError("previewResource endpoint should exist for round-2 issues 8/4"));
        GetMapping previewMapping = previewMethod.getAnnotation(GetMapping.class);
        assertTrue(previewMapping != null && previewMapping.value().length > 0
                && "/resource/preview/{resourceId}".equals(previewMapping.value()[0]),
            "previewResource should map to /resource/preview/{resourceId}");
        assertTrue(previewMethod.getAnnotation(SaIgnore.class) != null,
            "previewResource should bypass global login filter and rely on preview-specific auth");

        Method previewTicketMethod = Arrays.stream(ManuscriptReviewReadableApiController.class.getDeclaredMethods())
            .filter(method -> "issuePreviewTicket".equals(method.getName()))
            .findFirst()
            .orElseThrow(() -> new AssertionError("issuePreviewTicket endpoint should exist for preview token flow"));
        GetMapping previewTicketMapping = previewTicketMethod.getAnnotation(GetMapping.class);
        assertTrue(previewTicketMapping != null && previewTicketMapping.value().length > 0
                && "/resource/preview-ticket/{resourceId}".equals(previewTicketMapping.value()[0]),
            "issuePreviewTicket should map to /resource/preview-ticket/{resourceId}");
    }

    @Test
    void shouldSerializeFrozenLedgerQueryContract() throws Exception {
        ManuscriptReviewLedgerQueryRequest request = new ManuscriptReviewLedgerQueryRequest();
        request.setKeyword("系统稿件号");
        request.setProcessType("AUDIT");
        request.setMediaChannel("新华社/要闻");
        request.setBusinessStatus("WAITING");
        request.setCurrentNodeCode("LEVEL_1");
        request.setStartTimeFrom("2026-03-21 00:00:00");
        request.setStartTimeTo("2026-03-21 23:59:59");
        request.setPageNum(1);
        request.setPageSize(20);

        String requestJson = objectMapper.writeValueAsString(request);

        assertTrue(requestJson.contains("\"processType\":\"AUDIT\""));
        assertTrue(requestJson.contains("\"mediaChannel\":\"新华社/要闻\""));
        assertTrue(requestJson.contains("\"businessStatus\":\"WAITING\""));
        assertTrue(requestJson.contains("\"currentNodeCode\":\"LEVEL_1\""));
        assertTrue(requestJson.contains("\"startTimeFrom\":\"2026-03-21 00:00:00\""));
        assertTrue(requestJson.contains("\"startTimeTo\":\"2026-03-21 23:59:59\""));
        assertTrue(requestJson.contains("\"pageNum\":1"));
        assertTrue(requestJson.contains("\"pageSize\":20"));
        assertFalse(requestJson.contains("processTypeLabel"));
        assertFalse(requestJson.contains("flowStatusLabel"));
        assertFalse(requestJson.contains("currentNodeLabel"));
    }

    @Test
    void shouldSerializeFrozenSaveRequestWithReadonlySubmitDepartment() throws Exception {
        ManuscriptReviewSubmitRequest request = new ManuscriptReviewSubmitRequest();
        request.setId(9001L);
        request.setProcessType("AUDIT");
        request.setExternalManuscriptCode("EXT-001");
        request.setTitle("审校稿件");
        request.setMediaChannel("新华社/要闻");
        request.setSubmitDepartment("总编室");
        request.setAuthorName("张三、李四");
        request.setRemark("补充说明");
        request.setContentBody("正文内容");

        String requestJson = objectMapper.writeValueAsString(request);

        assertTrue(requestJson.contains("\"submitDepartment\":\"总编室\""));
        assertTrue(requestJson.contains("\"mediaChannel\":\"新华社/要闻\""));
        assertTrue(requestJson.contains("\"contentBody\":\"正文内容\""));
        assertFalse(requestJson.contains("\"content\":"));
        assertFalse(requestJson.contains("\"attachmentCount\":"));
        assertFalse(requestJson.contains("\"externalLinkCount\":"));
    }

    @Test
    void shouldSerializeFrozenLedgerItemWithoutPermissionLeakage() throws Exception {
        ManuscriptReviewLedgerItemResponse item = new ManuscriptReviewLedgerItemResponse();
        item.setId(9001L);
        item.setProcessType("AUDIT");
        item.setProcessTypeLabel("审核流程");
        item.setManuscriptCode("SH20260321001");
        item.setTitle("审校稿件");
        item.setMediaChannel("新华社/要闻");
        item.setBusinessStatus("WAITING");
        item.setBusinessStatusLabel("审批中");
        item.setCurrentNodeCode("LEVEL_1");
        item.setCurrentNodeLabel("待一级审批");
        item.setInitiatorName("张三");
        item.setUpdateTime("2026-03-23 10:20:30");

        String responseJson = objectMapper.writeValueAsString(item);

        assertTrue(responseJson.contains("\"id\":9001"));
        assertTrue(responseJson.contains("\"processType\":\"AUDIT\""));
        assertTrue(responseJson.contains("\"businessStatus\":\"WAITING\""));
        assertTrue(responseJson.contains("\"businessStatusLabel\":\"审批中\""));
        assertTrue(responseJson.contains("\"mediaChannel\":\"新华社/要闻\""));
        assertFalse(responseJson.contains("permissionMatrix"));
        assertFalse(responseJson.contains("canEdit"));
    }

    @Test
    void shouldSerializeFrozenDetailContractWithResourceUrlAndPermissionMatrix() throws Exception {
        ManuscriptReviewDetailResponse detail = new ManuscriptReviewDetailResponse();
        detail.setId(9002L);
        detail.setProcessType("AUDIT");
        detail.setProcessTypeLabel("审核流程");
        detail.setManuscriptCode("SH20260321002");
        detail.setExternalManuscriptCode("EXT-001");
        detail.setTitle("稿件标题");
        detail.setMediaChannel("新华社/要闻");
        detail.setSubmitDepartment("总编室");
        detail.setAuthorName("张三、李四");
        detail.setRemark("补充说明");
        detail.setContentBody("正文内容");
        detail.setContentSummary("正文内容");
        detail.setBusinessStatus("BACK");
        detail.setBusinessStatusLabel("已退回");
        detail.setCurrentNodeCode("RETURN_TO_INITIATOR");
        detail.setCurrentNodeStatus("RETURN_TO_INITIATOR");
        detail.setCurrentNodeLabel("待发起人处理");
        detail.setInitiatorName("张三");
        detail.setFirstSubmitTime("2026-03-21 11:00:00");
        detail.setLatestSubmitTime("2026-03-23 11:22:33");
        detail.setUpdateTime("2026-03-23 11:22:33");
        detail.setAttachmentList(List.of(new ManuscriptReviewDetailResponse.ResourceItemVO(
            1L, 8001L, "ATTACHMENT", "附件", "送审单.pdf", null, "2026-03-23 11:10:00", "https://files.example/a.pdf")));
        detail.setExternalLinkList(List.of(new ManuscriptReviewDetailResponse.ResourceItemVO(
            2L, null, "EXTERNAL_LINK", "外链", "素材参考", "https://example.com/ref", "2026-03-23 11:11:00", null)));
        detail.setVideoList(List.of(new ManuscriptReviewDetailResponse.ResourceItemVO(
            3L, 8003L, "VIDEO", "视频", "样片.mp4", null, "2026-03-23 11:12:00", "https://files.example/video.mp4")));
        detail.setVideoMarkList(List.of(new ManuscriptReviewDetailResponse.VideoMarkItemVO(
            4L, 3L, "00:00:05", "00:00:10", "第一处问题")));
        detail.setTimelineItems(List.of(new ManuscriptReviewDetailResponse.TimelineItemVO(
            "2026-03-23 11:20:00", "WORKFLOW", "流程", "CREATE", "张三新增了流程。", "张三",
            null, null, null, null, null, null, null, null, null)));
        detail.setPermissionMatrix(new ManuscriptReviewDetailResponse.PermissionMatrixVO(
            true, false, false, true, true, true, false, false, null));

        String responseJson = objectMapper.writeValueAsString(detail);

        assertTrue(responseJson.contains("\"id\":9002"));
        assertTrue(responseJson.contains("\"submitDepartment\":\"总编室\""));
        assertTrue(responseJson.contains("\"permissionMatrix\":"));
        assertTrue(responseJson.contains("\"currentNodeStatus\":\"RETURN_TO_INITIATOR\""));
        assertTrue(responseJson.contains("\"ossId\":8001"));
        assertTrue(responseJson.contains("\"resourceUrl\":\"https://files.example/a.pdf\""));
        assertTrue(responseJson.contains("\"resourceUrl\":\"https://files.example/video.mp4\""));
        assertTrue(responseJson.contains("\"resourceId\":3"));
        assertTrue(responseJson.contains("\"externalUrl\":\"https://example.com/ref\""));
        assertFalse(responseJson.contains("\"summaryCard\":"));
        assertFalse(responseJson.contains("\"manuscriptCard\":"));
        assertFalse(responseJson.contains("\"actionBar\":"));
        assertFalse(responseJson.contains("\"resources\":"));
        assertFalse(responseJson.contains("\"fileUrl\":"));
    }

    private static void assertPermission(String methodName, String permission, Class<?>... parameterTypes) throws Exception {
        Method method = ManuscriptReviewApiController.class.getDeclaredMethod(methodName, parameterTypes);
        SaCheckPermission annotation = method.getAnnotation(SaCheckPermission.class);
        assertTrue(annotation != null && annotation.value().length > 0 && permission.equals(annotation.value()[0]),
            methodName + " should declare " + permission);
    }
}
