package com.gxpublish.brain.manuscript.review.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import com.gxpublish.brain.common.core.exception.ServiceException;
import com.gxpublish.brain.manuscript.review.controller.request.ManuscriptReviewLedgerQueryRequest;
import com.gxpublish.brain.manuscript.review.controller.response.ManuscriptReviewDetailResponse;
import com.gxpublish.brain.manuscript.review.controller.response.ManuscriptReviewLedgerItemResponse;
import com.gxpublish.brain.manuscript.review.domain.command.CreateManuscriptReviewCommand;
import com.gxpublish.brain.manuscript.review.domain.command.ResubmitManuscriptReviewCommand;
import com.gxpublish.brain.manuscript.review.domain.entity.ManuscriptReviewAttachmentEntity;
import com.gxpublish.brain.manuscript.review.domain.entity.ManuscriptReviewExternalLinkEntity;
import com.gxpublish.brain.manuscript.review.domain.entity.ManuscriptReviewFlowConfigEntity;
import com.gxpublish.brain.manuscript.review.domain.entity.ManuscriptReviewHistoryEntity;
import com.gxpublish.brain.manuscript.review.domain.entity.ManuscriptReviewRecordEntity;
import com.gxpublish.brain.manuscript.review.domain.entity.ManuscriptReviewSystemRoleEntity;
import com.gxpublish.brain.manuscript.review.domain.entity.ManuscriptReviewSystemUserEntity;
import com.gxpublish.brain.manuscript.review.domain.entity.ManuscriptReviewSystemUserRoleEntity;
import com.gxpublish.brain.manuscript.review.domain.entity.ManuscriptReviewVideoMarkerEntity;
import com.gxpublish.brain.manuscript.review.domain.enums.ManuscriptReviewProcessType;
import com.gxpublish.brain.manuscript.review.domain.result.ManuscriptReviewResult;
import com.gxpublish.brain.manuscript.review.gateway.ManuscriptReviewConfigGateway;
import com.gxpublish.brain.manuscript.review.gateway.ManuscriptReviewCurrentUserGateway;
import com.gxpublish.brain.manuscript.review.gateway.ManuscriptReviewResubmitGateway;
import com.gxpublish.brain.manuscript.review.gateway.ManuscriptReviewSerialGateway;
import com.gxpublish.brain.manuscript.review.gateway.ManuscriptReviewUserRoleGateway;
import com.gxpublish.brain.manuscript.review.mapper.ManuscriptReviewAttachmentMapper;
import com.gxpublish.brain.manuscript.review.mapper.ManuscriptReviewExternalLinkMapper;
import com.gxpublish.brain.manuscript.review.mapper.ManuscriptReviewFlowConfigMapper;
import com.gxpublish.brain.manuscript.review.mapper.ManuscriptReviewHistoryMapper;
import com.gxpublish.brain.manuscript.review.mapper.ManuscriptReviewRecordMapper;
import com.gxpublish.brain.manuscript.review.mapper.ManuscriptReviewSystemRoleMapper;
import com.gxpublish.brain.manuscript.review.mapper.ManuscriptReviewSystemUserMapper;
import com.gxpublish.brain.manuscript.review.mapper.ManuscriptReviewSystemUserRoleMapper;
import com.gxpublish.brain.manuscript.review.mapper.ManuscriptReviewVideoMarkerMapper;

@Tag("dev")
class ManuscriptReviewServiceTest {

    private static final ZoneId BUSINESS_ZONE_ID = ZoneId.of("Asia/Shanghai");

    @Test
    void shouldCreateAndSubmitWithoutDraftForNormalApplicant() {
        ServiceFixture fixture = new ServiceFixture(1001L);
        fixture.stubApprovalChainReady(ManuscriptReviewProcessType.AUDIT);
        when(fixture.serialGateway.nextSerial(ManuscriptReviewProcessType.AUDIT, "20260321")).thenReturn(1);
        when(fixture.userRoleGateway.hasCertifiedApplicantRole(1001L)).thenReturn(false);

        ManuscriptReviewResult result = fixture.service.createAndSubmit(
            CreateManuscriptReviewCommand.builder()
                .processType(ManuscriptReviewProcessType.AUDIT)
                .title("测试稿件")
                .content("正文")
                .attachmentCount(1)
                .build()
        );

        assertEquals("SH20260321001", result.getManuscriptCode());
        assertEquals("审批中", result.getFlowStatus());
        assertEquals("待一级审批", result.getCurrentNode());
        verify(fixture.userRoleGateway).hasCertifiedApplicantRole(1001L);
    }

    @Test
    void shouldRejectSubmitWhenTitleMissing() {
        ServiceFixture fixture = new ServiceFixture(1006L);
        fixture.stubApprovalChainReady(ManuscriptReviewProcessType.AUDIT);

        ServiceException exception = assertThrows(ServiceException.class, () -> fixture.service.createAndSubmit(
            CreateManuscriptReviewCommand.builder()
                .processType(ManuscriptReviewProcessType.AUDIT)
                .title("   ")
                .content("正文")
                .attachmentCount(1)
                .build()
        ));

        assertEquals("标题不能为空", exception.getMessage());
    }

    @Test
    void shouldRejectSubmitWhenContentBlank() {
        ServiceFixture fixture = new ServiceFixture(1007L);
        fixture.stubApprovalChainReady(ManuscriptReviewProcessType.AUDIT);

        ServiceException exception = assertThrows(ServiceException.class, () -> fixture.service.createAndSubmit(
            CreateManuscriptReviewCommand.builder()
                .processType(ManuscriptReviewProcessType.AUDIT)
                .title("正文缺失稿件")
                .content("   ")
                .attachmentCount(1)
                .build()
        ));

        assertEquals("正文不能为空", exception.getMessage());
    }

    @Test
    void shouldRejectSubmitWhenAttachmentAndExternalLinkBothMissing() {
        ServiceFixture fixture = new ServiceFixture(1008L);
        fixture.stubApprovalChainReady(ManuscriptReviewProcessType.AUDIT);
        when(fixture.serialGateway.nextSerial(ManuscriptReviewProcessType.AUDIT, "20260321")).thenReturn(1);
        when(fixture.userRoleGateway.hasCertifiedApplicantRole(1008L)).thenReturn(false);

        ServiceException exception = assertThrows(ServiceException.class, () -> fixture.service.createAndSubmit(
            CreateManuscriptReviewCommand.builder()
                .processType(ManuscriptReviewProcessType.AUDIT)
                .title("缺少附件和外链稿件")
                .content("正文")
                .attachmentCount(0)
                .externalLinkCount(0)
                .build()
        ));

        assertEquals("附件或外链至少提供一种", exception.getMessage());
    }

    @Test
    void shouldRejectSubmitWhenExternalLinkUrlProtocolInvalid() {
        ServiceFixture fixture = new ServiceFixture(1013L);
        fixture.stubApprovalChainReady(ManuscriptReviewProcessType.AUDIT);

        ServiceException exception = assertThrows(ServiceException.class, () -> fixture.service.createAndSubmit(
            CreateManuscriptReviewCommand.builder()
                .processType(ManuscriptReviewProcessType.AUDIT)
                .title("外链协议不合法")
                .content("正文")
                .attachmentCount(1)
                .externalLinkUrls(List.of("ftp://a"))
                .build()
        ));

        assertEquals("外链只允许http/https协议", exception.getMessage());
    }

    @Test
    void shouldRejectSubmitWhenExternalLinkUrlsDuplicateWithinFlow() {
        ServiceFixture fixture = new ServiceFixture(1014L);
        fixture.stubApprovalChainReady(ManuscriptReviewProcessType.AUDIT);

        ServiceException exception = assertThrows(ServiceException.class, () -> fixture.service.createAndSubmit(
            CreateManuscriptReviewCommand.builder()
                .processType(ManuscriptReviewProcessType.AUDIT)
                .title("外链重复")
                .content("正文")
                .attachmentCount(1)
                .externalLinkUrls(List.of("https://a", "https://a"))
                .build()
        ));

        assertEquals("同一流程内URL不允许重复", exception.getMessage());
    }

    @Test
    void shouldRejectResubmitWhenAttachmentAndExternalLinkBothMissing() {
        ServiceFixture fixture = new ServiceFixture(1009L);
        fixture.stubApprovalChainReady(ManuscriptReviewProcessType.AUDIT);
        when(fixture.userRoleGateway.hasCertifiedApplicantRole(1009L)).thenReturn(false);

        ServiceException exception = assertThrows(ServiceException.class, () -> fixture.service.resubmit(
            ResubmitManuscriptReviewCommand.builder()
                .processType(ManuscriptReviewProcessType.AUDIT)
                .title("再次提交缺少附件和外链稿件")
                .content("正文")
                .attachmentCount(0)
                .externalLinkCount(0)
                .build()
        ));

        assertEquals("附件或外链至少提供一种", exception.getMessage());
    }

    @Test
    void shouldRejectResubmitWhenReviewIsNotReturnedToInitiator() {
        ServiceFixture fixture = new ServiceFixture(1011L);
        fixture.stubApprovalChainReady(ManuscriptReviewProcessType.AUDIT);
        when(fixture.userRoleGateway.hasCertifiedApplicantRole(1011L)).thenReturn(false);

        ServiceException exception = assertThrows(ServiceException.class, () -> fixture.service.resubmit(
            ResubmitManuscriptReviewCommand.builder()
                .reviewId(9001L)
                .processType(ManuscriptReviewProcessType.AUDIT)
                .title("测试稿件")
                .content("正文")
                .attachmentCount(1)
                .returnedToInitiator(false)
                .build()
        ));

        assertEquals("当前流程未退回发起人，不能再次提交", exception.getMessage());
        verify(fixture.resubmitGateway).isReturnedToInitiator(9001L, 1011L);
    }

    @Test
    void shouldRejectResubmitWhenGatewayReportsNotReturnedEvenIfCommandClaimsReturnedToInitiator() {
        ServiceFixture fixture = new ServiceFixture(1012L);
        fixture.stubApprovalChainReady(ManuscriptReviewProcessType.AUDIT);
        when(fixture.userRoleGateway.hasCertifiedApplicantRole(1012L)).thenReturn(false);
        when(fixture.resubmitGateway.isReturnedToInitiator(9002L, 1012L)).thenReturn(false);

        ServiceException exception = assertThrows(ServiceException.class, () -> fixture.service.resubmit(
            ResubmitManuscriptReviewCommand.builder()
                .reviewId(9002L)
                .processType(ManuscriptReviewProcessType.AUDIT)
                .title("测试稿件")
                .content("正文")
                .attachmentCount(1)
                .returnedToInitiator(true)
                .build()
        ));

        assertEquals("当前流程未退回发起人，不能再次提交", exception.getMessage());
        verify(fixture.resubmitGateway).isReturnedToInitiator(9002L, 1012L);
    }

    @Test
    void shouldGenerateProofreadCodeWithJdPrefix() {
        ServiceFixture fixture = new ServiceFixture(1002L);
        fixture.stubApprovalChainReady(ManuscriptReviewProcessType.PROOFREAD);
        when(fixture.serialGateway.nextSerial(ManuscriptReviewProcessType.PROOFREAD, "20260321")).thenReturn(7);
        when(fixture.userRoleGateway.hasCertifiedApplicantRole(1002L)).thenReturn(false);

        ManuscriptReviewResult result = fixture.service.createAndSubmit(
            CreateManuscriptReviewCommand.builder()
                .processType(ManuscriptReviewProcessType.PROOFREAD)
                .title("校对稿件")
                .content("正文")
                .attachmentCount(1)
                .build()
        );

        assertEquals("JD20260321007", result.getManuscriptCode());
        assertEquals("待一级审批", result.getCurrentNode());
    }

    @Test
    void shouldRejectSubmitWhenApprovalChainConfigMissing() {
        ServiceFixture fixture = new ServiceFixture(1003L);
        when(fixture.configGateway.hasCompleteApprovalChain(ManuscriptReviewProcessType.AUDIT)).thenReturn(false);

        ServiceException exception = assertThrows(ServiceException.class, () -> fixture.service.createAndSubmit(
            CreateManuscriptReviewCommand.builder()
                .processType(ManuscriptReviewProcessType.AUDIT)
                .title("缺配置稿件")
                .content("正文")
                .attachmentCount(1)
                .build()
        ));

        assertEquals("审核流程缺少完整审批链配置", exception.getMessage());
    }

    @Test
    void shouldRejectSubmitWhenApproverRoleHasNoActiveMembers() {
        ServiceFixture fixture = new ServiceFixture(1004L);
        when(fixture.configGateway.hasCompleteApprovalChain(ManuscriptReviewProcessType.AUDIT)).thenReturn(true);
        when(fixture.configGateway.hasActiveApproverMembers(ManuscriptReviewProcessType.AUDIT)).thenReturn(false);

        ServiceException exception = assertThrows(ServiceException.class, () -> fixture.service.createAndSubmit(
            CreateManuscriptReviewCommand.builder()
                .processType(ManuscriptReviewProcessType.AUDIT)
                .title("缺审批人稿件")
                .content("正文")
                .attachmentCount(1)
                .build()
        ));

        assertEquals("审核流程审批角色缺少有效成员", exception.getMessage());
    }

    @Test
    void shouldSkipFirstApprovalForCertifiedApplicant() {
        ServiceFixture fixture = new ServiceFixture(1005L);
        when(fixture.configGateway.hasCompleteApprovalChain(any())).thenReturn(true);
        when(fixture.configGateway.hasActiveApproverMembers(any())).thenReturn(true);
        when(fixture.serialGateway.nextSerial(ManuscriptReviewProcessType.AUDIT, "20260321")).thenReturn(9);
        when(fixture.userRoleGateway.hasCertifiedApplicantRole(1005L)).thenReturn(true);

        ManuscriptReviewResult result = fixture.service.createAndSubmit(
            CreateManuscriptReviewCommand.builder()
                .processType(ManuscriptReviewProcessType.AUDIT)
                .title("持证稿件")
                .content("正文")
                .attachmentCount(1)
                .build()
        );

        assertEquals("审批中", result.getFlowStatus());
        assertEquals("待二级审批", result.getCurrentNode());
        assertEquals("系统判定发起人具备持证资格，自动跳过一级审批", result.getRoutingComment());
    }

    @Test
    void shouldStartSerialFrom001WhenGatewayReturnsZero() {
        ServiceFixture fixture = new ServiceFixture(1010L);
        fixture.stubApprovalChainReady(ManuscriptReviewProcessType.AUDIT);
        when(fixture.serialGateway.nextSerial(ManuscriptReviewProcessType.AUDIT, "20260321")).thenReturn(0);
        when(fixture.userRoleGateway.hasCertifiedApplicantRole(1010L)).thenReturn(false);

        ManuscriptReviewResult result = fixture.service.createAndSubmit(
            CreateManuscriptReviewCommand.builder()
                .processType(ManuscriptReviewProcessType.AUDIT)
                .title("流水号为零稿件")
                .content("正文")
                .attachmentCount(1)
                .build()
        );

        assertEquals("SH20260321001", result.getManuscriptCode());
    }

    @Test
    void shouldRejectSubmitWhenCurrentUserMissing() {
        ServiceFixture fixture = new ServiceFixture(null);
        fixture.stubApprovalChainReady(ManuscriptReviewProcessType.AUDIT);

        ServiceException exception = assertThrows(ServiceException.class, () -> fixture.service.createAndSubmit(
            CreateManuscriptReviewCommand.builder()
                .processType(ManuscriptReviewProcessType.AUDIT)
                .title("无登录用户稿件")
                .content("正文")
                .attachmentCount(1)
                .build()
        ));

        assertEquals("当前登录用户不存在", exception.getMessage());
    }

    @Test
    void shouldNotExposeApplicantUserIdOnCommands() {
        assertThrows(NoSuchMethodException.class, () -> CreateManuscriptReviewCommand.class.getMethod("getApplicantUserId"));
        assertThrows(NoSuchMethodException.class, () -> ResubmitManuscriptReviewCommand.class.getMethod("getApplicantUserId"));
    }

    @Test
    void shouldReturnEmptyReadableLedgerWhenDatabaseHasNoRows() {
        ReadableFixture fixture = new ReadableFixture(3001L);
        when(fixture.recordMapper.selectList(any())).thenReturn(List.of());

        List<ManuscriptReviewLedgerItemResponse> ledger = fixture.readableService.listLedger(new ManuscriptReviewLedgerQueryRequest());

        assertTrue(ledger.isEmpty());
        verify(fixture.recordMapper).selectList(any());
    }

    @Test
    void shouldRejectDetailWhenReviewRecordMissing() {
        ReadableFixture fixture = new ReadableFixture(3002L);
        when(fixture.recordMapper.selectById(9001L)).thenReturn(null);

        ServiceException exception = assertThrows(ServiceException.class, () -> fixture.readableService.getDetail(9001L));

        assertEquals("稿件审校流程不存在", exception.getMessage());
    }

    @Test
    void shouldAssembleReadableDetailWithNaturalLanguageTimelineAndResourceSections() {
        ReadableFixture fixture = new ReadableFixture(3003L);
        when(fixture.recordMapper.selectById(9002L)).thenReturn(buildReviewRecord());
        when(fixture.attachmentMapper.selectList(any())).thenReturn(List.of(buildCurrentAttachment(), buildCurrentVideo(), buildHistoryAttachment()));
        when(fixture.externalLinkMapper.selectList(any())).thenReturn(List.of(buildCurrentLink(), buildHistoryLink()));
        when(fixture.historyMapper.selectList(any())).thenReturn(List.of(buildCreateHistory(), buildReturnHistory()));
        when(fixture.videoMarkerMapper.selectList(any())).thenReturn(List.of(buildCurrentMarker(), buildHistoryMarker()));

        ManuscriptReviewDetailResponse detail = fixture.readableService.getDetail(9002L);

        assertEquals(9002L, detail.getReviewId());
        assertEquals("已退回", detail.getSummaryCard().getFlowStatusLabel());
        assertEquals("待发起人处理", detail.getSummaryCard().getCurrentNodeLabel());
        assertEquals("审核流程", detail.getManuscriptCard().getProcessTypeLabel());
        assertEquals(List.of("修改", "再次提交", "返回"), detail.getActionBar().getActions());
        assertEquals("张三新增了流程。", detail.getTimeline().get(0).getText());
        assertEquals(1, detail.getResources().getCurrentAttachments().size());
        assertEquals(1, detail.getResources().getCurrentExternalLinks().size());
        assertEquals(1, detail.getResources().getCurrentVideos().size());
        assertEquals(1, detail.getResources().getHistoryAttachments().size());
        assertEquals(1, detail.getResources().getHistoryExternalLinks().size());
        assertEquals(1, detail.getResources().getHistoryVideoMarkers().size());
        assertFalse(detail.getTimeline().get(0).getText().contains("CREATE"));
    }

    @Test
    void shouldExposeApproveActionWhenCurrentUserBelongsToCurrentApproverRole() {
        ReadableFixture fixture = new ReadableFixture(4002L);
        ManuscriptReviewRecordEntity reviewRecord = buildReviewRecord();
        reviewRecord.setTenantId("000000");
        reviewRecord.setFlowStatusLabel("审批中");
        reviewRecord.setCurrentNodeLabel("待二级审批");
        when(fixture.recordMapper.selectById(9002L)).thenReturn(reviewRecord);
        when(fixture.attachmentMapper.selectList(any())).thenReturn(List.of());
        when(fixture.externalLinkMapper.selectList(any())).thenReturn(List.of());
        when(fixture.historyMapper.selectList(any())).thenReturn(List.of(buildCreateHistory()));
        when(fixture.videoMarkerMapper.selectList(any())).thenReturn(List.of());
        when(fixture.flowConfigMapper.selectOne(any())).thenReturn(buildFlowConfig());
        when(fixture.roleMapper.selectList(any())).thenReturn(List.of(buildApproverRole()));
        when(fixture.userRoleMapper.selectList(any())).thenReturn(List.of(buildUserRole()));
        when(fixture.userMapper.selectList(any())).thenReturn(List.of(buildEnabledUser()));

        ManuscriptReviewDetailResponse detail = fixture.readableService.getDetail(9002L);

        assertEquals(List.of("修改", "去审批", "返回"), detail.getActionBar().getActions());
        assertTrue(detail.getActionBar().isCanModify());
    }

    private static ManuscriptReviewRecordEntity buildReviewRecord() {
        ManuscriptReviewRecordEntity entity = new ManuscriptReviewRecordEntity();
        entity.setId(9002L);
        entity.setProcessType("AUDIT");
        entity.setFlowStatusLabel("已退回");
        entity.setCurrentNodeLabel("待发起人处理");
        entity.setManuscriptCode("SH20260321002");
        entity.setExternalManuscriptCode("EXT-002");
        entity.setTitle("稿件标题");
        entity.setContent("正文内容");
        entity.setNote("补充说明");
        entity.setMediaChannelLabel("新华社/要闻");
        entity.setSubmitterDeptName("总编室");
        entity.setAuthorNames("张三、李四");
        entity.setInitiatorUserId(3003L);
        entity.setInitiatorName("张三");
        entity.setUpdateTime(new Date(1774236153000L));
        return entity;
    }

    private static ManuscriptReviewAttachmentEntity buildCurrentAttachment() {
        ManuscriptReviewAttachmentEntity entity = new ManuscriptReviewAttachmentEntity();
        entity.setId(1L);
        entity.setReviewId(9002L);
        entity.setFileName("送审单.pdf");
        entity.setFileUrl("https://files.example/a.pdf");
        entity.setFileSize(123L);
        entity.setMimeType("application/pdf");
        entity.setEnabled("1");
        entity.setIsVideo(false);
        return entity;
    }

    private static ManuscriptReviewAttachmentEntity buildCurrentVideo() {
        ManuscriptReviewAttachmentEntity entity = new ManuscriptReviewAttachmentEntity();
        entity.setId(2L);
        entity.setReviewId(9002L);
        entity.setFileName("样片.mp4");
        entity.setFileUrl("https://files.example/video.mp4");
        entity.setFileSize(1024L);
        entity.setMimeType("video/mp4");
        entity.setEnabled("1");
        entity.setIsVideo(true);
        entity.setVideoDurationSeconds(600);
        return entity;
    }

    private static ManuscriptReviewAttachmentEntity buildHistoryAttachment() {
        ManuscriptReviewAttachmentEntity entity = new ManuscriptReviewAttachmentEntity();
        entity.setId(3L);
        entity.setReviewId(9002L);
        entity.setFileName("旧附件.pdf");
        entity.setFileUrl("https://files.example/old.pdf");
        entity.setFileSize(111L);
        entity.setMimeType("application/pdf");
        entity.setEnabled("0");
        entity.setIsVideo(false);
        entity.setDisabledTime(new Date(1774235100000L));
        return entity;
    }

    private static ManuscriptReviewExternalLinkEntity buildCurrentLink() {
        ManuscriptReviewExternalLinkEntity entity = new ManuscriptReviewExternalLinkEntity();
        entity.setReviewId(9002L);
        entity.setLinkTitle("素材参考");
        entity.setLinkUrl("https://example.com/ref");
        entity.setEnabled("1");
        return entity;
    }

    private static ManuscriptReviewExternalLinkEntity buildHistoryLink() {
        ManuscriptReviewExternalLinkEntity entity = new ManuscriptReviewExternalLinkEntity();
        entity.setReviewId(9002L);
        entity.setLinkTitle("旧链接");
        entity.setLinkUrl("https://example.com/old");
        entity.setEnabled("0");
        entity.setDisabledTime(new Date(1774235160000L));
        return entity;
    }

    private static ManuscriptReviewHistoryEntity buildCreateHistory() {
        ManuscriptReviewHistoryEntity entity = new ManuscriptReviewHistoryEntity();
        entity.setReviewId(9002L);
        entity.setActionType("CREATE");
        entity.setActionText("张三新增了流程。");
        entity.setActorUserId(3003L);
        entity.setActorName("张三");
        entity.setCreateTime(new Date(1774234800000L));
        return entity;
    }

    private static ManuscriptReviewHistoryEntity buildReturnHistory() {
        ManuscriptReviewHistoryEntity entity = new ManuscriptReviewHistoryEntity();
        entity.setReviewId(9002L);
        entity.setActionType("RETURN_TO_INITIATOR");
        entity.setActionText("李四退回给发起人：请补充说明。");
        entity.setActorUserId(3004L);
        entity.setActorName("李四");
        entity.setCreateTime(new Date(1774236000000L));
        return entity;
    }

    private static ManuscriptReviewVideoMarkerEntity buildCurrentMarker() {
        ManuscriptReviewVideoMarkerEntity entity = new ManuscriptReviewVideoMarkerEntity();
        entity.setReviewId(9002L);
        entity.setVideoAttachmentId(2L);
        entity.setStartTime("00:00:05");
        entity.setEndTime("00:00:10");
        entity.setMarkerNote("第一处问题");
        entity.setEnabled("1");
        entity.setCreateTime(new Date(1774236060000L));
        return entity;
    }

    private static ManuscriptReviewVideoMarkerEntity buildHistoryMarker() {
        ManuscriptReviewVideoMarkerEntity entity = new ManuscriptReviewVideoMarkerEntity();
        entity.setReviewId(9002L);
        entity.setVideoAttachmentId(2L);
        entity.setStartTime("00:00:15");
        entity.setMarkerNote("停用标注");
        entity.setEnabled("0");
        entity.setCreateTime(new Date(1774236120000L));
        entity.setDisabledTime(new Date(1774236180000L));
        return entity;
    }

    private static ManuscriptReviewFlowConfigEntity buildFlowConfig() {
        ManuscriptReviewFlowConfigEntity entity = new ManuscriptReviewFlowConfigEntity();
        entity.setTenantId("000000");
        entity.setProcessType("AUDIT");
        entity.setStatus("0");
        entity.setLevelTwoRoleKey("role:manuscript-review:l2");
        return entity;
    }

    private static ManuscriptReviewSystemRoleEntity buildApproverRole() {
        ManuscriptReviewSystemRoleEntity entity = new ManuscriptReviewSystemRoleEntity();
        entity.setRoleId(7002L);
        entity.setTenantId("000000");
        entity.setRoleKey("role:manuscript-review:l2");
        entity.setStatus("0");
        entity.setDelFlag("0");
        return entity;
    }

    private static ManuscriptReviewSystemUserRoleEntity buildUserRole() {
        ManuscriptReviewSystemUserRoleEntity entity = new ManuscriptReviewSystemUserRoleEntity();
        entity.setUserId(4002L);
        entity.setRoleId(7002L);
        return entity;
    }

    private static ManuscriptReviewSystemUserEntity buildEnabledUser() {
        ManuscriptReviewSystemUserEntity entity = new ManuscriptReviewSystemUserEntity();
        entity.setUserId(4002L);
        entity.setStatus("0");
        entity.setDelFlag("0");
        return entity;
    }

    private static Clock fixedBusinessClock() {
        return Clock.fixed(
            LocalDate.of(2026, 3, 21).atStartOfDay(BUSINESS_ZONE_ID).toInstant(),
            BUSINESS_ZONE_ID
        );
    }

    private static final class ServiceFixture {

        private final ManuscriptReviewConfigGateway configGateway = mock(ManuscriptReviewConfigGateway.class);
        private final ManuscriptReviewSerialGateway serialGateway = mock(ManuscriptReviewSerialGateway.class);
        private final ManuscriptReviewUserRoleGateway userRoleGateway = mock(ManuscriptReviewUserRoleGateway.class);
        private final ManuscriptReviewResubmitGateway resubmitGateway = mock(ManuscriptReviewResubmitGateway.class);
        private final ManuscriptReviewCurrentUserGateway currentUserGateway = mock(ManuscriptReviewCurrentUserGateway.class);
        private final ManuscriptReviewService service;

        private ServiceFixture(Long currentUserId) {
            when(currentUserGateway.getCurrentUserId()).thenReturn(currentUserId);
            this.service = new ManuscriptReviewService(
                configGateway,
                serialGateway,
                userRoleGateway,
                resubmitGateway,
                currentUserGateway,
                fixedBusinessClock()
            );
        }

        private void stubApprovalChainReady(ManuscriptReviewProcessType processType) {
            when(configGateway.hasCompleteApprovalChain(processType)).thenReturn(true);
            when(configGateway.hasActiveApproverMembers(processType)).thenReturn(true);
        }
    }

    private static final class ReadableFixture {

        private final ManuscriptReviewRecordMapper recordMapper = mock(ManuscriptReviewRecordMapper.class);
        private final ManuscriptReviewAttachmentMapper attachmentMapper = mock(ManuscriptReviewAttachmentMapper.class);
        private final ManuscriptReviewExternalLinkMapper externalLinkMapper = mock(ManuscriptReviewExternalLinkMapper.class);
        private final ManuscriptReviewHistoryMapper historyMapper = mock(ManuscriptReviewHistoryMapper.class);
        private final ManuscriptReviewVideoMarkerMapper videoMarkerMapper = mock(ManuscriptReviewVideoMarkerMapper.class);
        private final ManuscriptReviewFlowConfigMapper flowConfigMapper = mock(ManuscriptReviewFlowConfigMapper.class);
        private final ManuscriptReviewSystemRoleMapper roleMapper = mock(ManuscriptReviewSystemRoleMapper.class);
        private final ManuscriptReviewSystemUserRoleMapper userRoleMapper = mock(ManuscriptReviewSystemUserRoleMapper.class);
        private final ManuscriptReviewSystemUserMapper userMapper = mock(ManuscriptReviewSystemUserMapper.class);
        private final ManuscriptReviewCurrentUserGateway currentUserGateway = mock(ManuscriptReviewCurrentUserGateway.class);
        private final ManuscriptReviewReadableService readableService;

        private ReadableFixture(Long currentUserId) {
            when(currentUserGateway.getCurrentUserId()).thenReturn(currentUserId);
            this.readableService = new ManuscriptReviewReadableService(
                recordMapper,
                attachmentMapper,
                externalLinkMapper,
                historyMapper,
                videoMarkerMapper,
                flowConfigMapper,
                roleMapper,
                userRoleMapper,
                userMapper,
                currentUserGateway
            );
        }
    }
}
