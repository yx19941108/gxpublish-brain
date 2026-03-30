package com.gxpublish.brain.manuscript.review.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import com.gxpublish.brain.common.core.domain.dto.StartProcessDTO;
import com.gxpublish.brain.common.core.domain.event.ProcessEvent;
import com.gxpublish.brain.common.core.domain.event.ProcessTaskEvent;
import com.gxpublish.brain.common.core.exception.ServiceException;
import com.gxpublish.brain.common.core.service.WorkflowService;
import com.gxpublish.brain.manuscript.review.domain.command.SubmitAndStartManuscriptReviewCommand;
import com.gxpublish.brain.manuscript.review.domain.entity.ManuscriptReviewAttachmentEntity;
import com.gxpublish.brain.manuscript.review.domain.entity.ManuscriptReviewFlowConfigEntity;
import com.gxpublish.brain.manuscript.review.domain.entity.ManuscriptReviewHistoryEntity;
import com.gxpublish.brain.manuscript.review.domain.entity.ManuscriptReviewRecordEntity;
import com.gxpublish.brain.manuscript.review.domain.entity.ManuscriptReviewSysOssEntity;
import com.gxpublish.brain.manuscript.review.domain.entity.ManuscriptReviewSystemRoleEntity;
import com.gxpublish.brain.manuscript.review.domain.entity.ManuscriptReviewSystemUserEntity;
import com.gxpublish.brain.manuscript.review.domain.entity.ManuscriptReviewSystemUserRoleEntity;
import com.gxpublish.brain.manuscript.review.domain.enums.ManuscriptReviewProcessType;
import com.gxpublish.brain.manuscript.review.gateway.ManuscriptReviewCurrentUserGateway;
import com.gxpublish.brain.manuscript.review.gateway.ManuscriptReviewSerialGateway;
import com.gxpublish.brain.manuscript.review.mapper.ManuscriptReviewAttachmentMapper;
import com.gxpublish.brain.manuscript.review.mapper.ManuscriptReviewExternalLinkMapper;
import com.gxpublish.brain.manuscript.review.mapper.ManuscriptReviewFlowConfigMapper;
import com.gxpublish.brain.manuscript.review.mapper.ManuscriptReviewHistoryMapper;
import com.gxpublish.brain.manuscript.review.mapper.ManuscriptReviewRecordMapper;
import com.gxpublish.brain.manuscript.review.mapper.ManuscriptReviewSysOssMapper;
import com.gxpublish.brain.manuscript.review.mapper.ManuscriptReviewSystemRoleMapper;
import com.gxpublish.brain.manuscript.review.mapper.ManuscriptReviewSystemUserMapper;
import com.gxpublish.brain.manuscript.review.mapper.ManuscriptReviewSystemUserRoleMapper;
import com.gxpublish.brain.manuscript.review.mapper.ManuscriptReviewVideoMarkerMapper;
import com.gxpublish.brain.workflow.service.IFlwInstanceService;

@Tag("dev")
class ManuscriptReviewFirstRoundGuardrailTest {

    private static final ZoneId BUSINESS_ZONE_ID = ZoneId.of("Asia/Shanghai");

    @Test
    void shouldRejectLegacySubmitWhenCurrentUserLacksInitiatorRole() {
        ServiceFixture fixture = new ServiceFixture(1041L, "000000", 2001L, "user-1041");
        ManuscriptReviewRecordEntity existing = buildRecord(9041L, 1041L);
        existing.setProcessType("AUDIT");
        when(fixture.recordMapper.selectById(9041L)).thenReturn(existing);
        when(fixture.attachmentMapper.selectList(any())).thenReturn(List.of(buildVideoAttachment(7041L, true, 120)));
        when(fixture.externalLinkMapper.selectList(any())).thenReturn(List.of());
        when(fixture.serialGateway.nextSerial(ManuscriptReviewProcessType.AUDIT, "20260321")).thenReturn(41);
        fixture.stubSubmitRouting(false);

        assertThrows(ServiceException.class, () -> fixture.service.submitAndFlowStart(9041L));
    }

    @Test
    void shouldRejectIntegratedSubmitWhenCurrentUserLacksInitiatorRole() {
        ServiceFixture fixture = new ServiceFixture(1042L, "000000", 2001L, "user-1042");
        fixture.stubSubmitRouting(false);
        when(fixture.sysOssMapper.selectById(8842L)).thenReturn(buildSysOss(8842L, "https://files.example/submit.pdf", "application/pdf", null));
        when(fixture.serialGateway.nextSerial(ManuscriptReviewProcessType.AUDIT, "20260321")).thenReturn(42);

        SubmitAndStartManuscriptReviewCommand command = SubmitAndStartManuscriptReviewCommand.builder()
            .processType(ManuscriptReviewProcessType.AUDIT)
            .externalManuscriptCode("EXT-9042")
            .title("create-without-role")
            .mediaChannel("channel")
            .submitDepartment("dept")
            .authorName("author")
            .remark("remark")
            .contentBody("content")
            .attachmentResources(List.of(SubmitAndStartManuscriptReviewCommand.SubmitAttachmentResourceCommand.builder()
                .resourceType("ATTACHMENT")
                .displayName("submit.pdf")
                .ossId(8842L)
                .build()))
            .externalLinks(List.of())
            .build();

        assertThrows(ServiceException.class, () -> fixture.service.submitAndFlowStart(command));
    }

    @Test
    void shouldWriteActorIntoSkipLevelOneHistoryWhenCertifiedSubmitSkipsFirstLevel() {
        ServiceFixture fixture = new ServiceFixture(1043L, "000000", 2001L, "user-1043");
        fixture.stubSubmitRouting(true);
        when(fixture.sysOssMapper.selectById(8843L)).thenReturn(buildSysOss(8843L, "https://files.example/certified.pdf", "application/pdf", null));
        when(fixture.serialGateway.nextSerial(ManuscriptReviewProcessType.AUDIT, "20260321")).thenReturn(43);

        fixture.service.submitAndFlowStart(SubmitAndStartManuscriptReviewCommand.builder()
            .processType(ManuscriptReviewProcessType.AUDIT)
            .externalManuscriptCode("EXT-9043")
            .title("certified-submit")
            .mediaChannel("channel")
            .submitDepartment("dept")
            .authorName("author")
            .remark("remark")
            .contentBody("content")
            .attachmentResources(List.of(SubmitAndStartManuscriptReviewCommand.SubmitAttachmentResourceCommand.builder()
                .resourceType("ATTACHMENT")
                .displayName("certified.pdf")
                .ossId(8843L)
                .build()))
            .externalLinks(List.of())
            .build());

        ArgumentCaptor<ManuscriptReviewHistoryEntity> historyCaptor = ArgumentCaptor.forClass(ManuscriptReviewHistoryEntity.class);
        verify(fixture.historyMapper, times(2)).insert(historyCaptor.capture());
        ManuscriptReviewHistoryEntity skipHistory = historyCaptor.getAllValues().get(1);

        assertEquals("SKIP_LEVEL_1", skipHistory.getActionType());
        assertNotNull(skipHistory.getActorUserId());
        assertEquals("user-1043", skipHistory.getActorName());
    }

    @Test
    void shouldWriteActorAndReadableBackHistoryWhenProcessEventReportsBack() {
        ServiceFixture fixture = new ServiceFixture(1044L, "000000", 2001L, "user-1044");
        ManuscriptReviewRecordEntity record = buildPendingApprovalRecord(9044L, 1044L);
        when(fixture.recordMapper.selectById(9044L)).thenReturn(record);

        ProcessEvent processEvent = new ProcessEvent();
        processEvent.setTenantId("000000");
        processEvent.setBusinessId("9044");
        processEvent.setInstanceId(99044L);
        processEvent.setStatus("back");
        processEvent.setNodeCode("first-review-node");
        processEvent.setNodeName("L1 Review");
        processEvent.setParams(java.util.Map.of("handler", "1044", "message", "need-more-context"));

        fixture.service.processHandler(processEvent);

        ArgumentCaptor<ManuscriptReviewHistoryEntity> historyCaptor = ArgumentCaptor.forClass(ManuscriptReviewHistoryEntity.class);
        verify(fixture.historyMapper).insert(historyCaptor.capture());

        assertEquals("BACK", historyCaptor.getValue().getActionType());
        assertNotNull(historyCaptor.getValue().getActorUserId());
        assertNotNull(historyCaptor.getValue().getActorName());
        assertTrue(historyCaptor.getValue().getActionText().contains("user-1044"));
        assertTrue(historyCaptor.getValue().getActionText().contains("L1 Review"));
        assertTrue(historyCaptor.getValue().getActionText().contains("need-more-context"));
    }

    @Test
    void shouldWriteActorRichApprovalHistoryWhenProcessTaskEventMovesToSecondLevel() {
        ServiceFixture fixture = new ServiceFixture(1045L, "000000", 2001L, "user-1045");
        ManuscriptReviewRecordEntity record = buildPendingApprovalRecord(9045L, 1045L);
        when(fixture.recordMapper.selectById(9045L)).thenReturn(record);

        ProcessTaskEvent processTaskEvent = new ProcessTaskEvent();
        processTaskEvent.setTenantId("000000");
        processTaskEvent.setBusinessId("9045");
        processTaskEvent.setInstanceId(99045L);
        processTaskEvent.setStatus("waiting");
        processTaskEvent.setNodeCode("second-review-node");
        processTaskEvent.setNodeName("L2 Review");
        processTaskEvent.setParams(java.util.Map.of("message", "approved-by-l1", "handler", "1045"));

        fixture.service.processTaskHandler(processTaskEvent);

        ArgumentCaptor<ManuscriptReviewHistoryEntity> historyCaptor = ArgumentCaptor.forClass(ManuscriptReviewHistoryEntity.class);
        verify(fixture.historyMapper).insert(historyCaptor.capture());

        assertEquals("APPROVE", historyCaptor.getValue().getActionType());
        assertNotNull(historyCaptor.getValue().getActorUserId());
        assertNotNull(historyCaptor.getValue().getActorName());
        assertTrue(historyCaptor.getValue().getActionText().contains("user-1045"));
        assertTrue(historyCaptor.getValue().getActionText().contains("approved-by-l1"));
    }

    @Test
    void shouldWriteActorRichApprovalHistoryWhenProcessTaskEventMovesToFinalLevel() {
        ServiceFixture fixture = new ServiceFixture(1046L, "000000", 2001L, "user-1046");
        ManuscriptReviewRecordEntity record = buildPendingApprovalRecord(9046L, 1046L);
        when(fixture.recordMapper.selectById(9046L)).thenReturn(record);

        ProcessTaskEvent processTaskEvent = new ProcessTaskEvent();
        processTaskEvent.setTenantId("000000");
        processTaskEvent.setBusinessId("9046");
        processTaskEvent.setInstanceId(99046L);
        processTaskEvent.setStatus("waiting");
        processTaskEvent.setNodeCode("final-review-node");
        processTaskEvent.setNodeName("L3 Review");
        processTaskEvent.setParams(java.util.Map.of("message", "approved-by-l2", "handler", "1046"));

        fixture.service.processTaskHandler(processTaskEvent);

        ArgumentCaptor<ManuscriptReviewHistoryEntity> historyCaptor = ArgumentCaptor.forClass(ManuscriptReviewHistoryEntity.class);
        verify(fixture.historyMapper).insert(historyCaptor.capture());

        assertEquals("APPROVE", historyCaptor.getValue().getActionType());
        assertNotNull(historyCaptor.getValue().getActorUserId());
        assertNotNull(historyCaptor.getValue().getActorName());
        assertTrue(historyCaptor.getValue().getActionText().contains("user-1046"));
        assertTrue(historyCaptor.getValue().getActionText().contains("approved-by-l2"));
    }

    private static ManuscriptReviewRecordEntity buildRecord(Long reviewId, Long initiatorUserId) {
        ManuscriptReviewRecordEntity entity = new ManuscriptReviewRecordEntity();
        entity.setId(reviewId);
        entity.setTenantId("000000");
        entity.setProcessType("AUDIT");
        entity.setTitle("title");
        entity.setMediaChannel("channel");
        entity.setSubmitDepartment("dept");
        entity.setAuthorName("author");
        entity.setContentBody("content");
        entity.setInitiatorUserId(initiatorUserId);
        entity.setInitiatorName("initiator");
        entity.setCreateTime(new Date(1774060800000L));
        entity.setUpdateTime(new Date(1774064400000L));
        return entity;
    }

    private static ManuscriptReviewRecordEntity buildPendingApprovalRecord(Long reviewId, Long initiatorUserId) {
        ManuscriptReviewRecordEntity entity = buildRecord(reviewId, initiatorUserId);
        entity.setFlowStatusLabel("WAITING");
        entity.setCurrentNodeStatus("LEVEL_1");
        entity.setCurrentNodeLabel("L1 Review");
        return entity;
    }

    private static ManuscriptReviewAttachmentEntity buildVideoAttachment(Long resourceId, boolean enabled, Integer durationSeconds) {
        ManuscriptReviewAttachmentEntity entity = new ManuscriptReviewAttachmentEntity();
        entity.setId(resourceId);
        entity.setReviewId(9041L);
        entity.setFileName("video.mp4");
        entity.setEnabled(enabled ? "1" : "0");
        entity.setIsVideo(true);
        entity.setVideoDurationSeconds(durationSeconds);
        return entity;
    }

    private static ManuscriptReviewSysOssEntity buildSysOss(Long ossId, String url, String contentType, Integer videoDurationSeconds) {
        ManuscriptReviewSysOssEntity entity = new ManuscriptReviewSysOssEntity();
        entity.setOssId(ossId);
        entity.setUrl(url);
        entity.setExt1("{\"fileSize\":12345,\"contentType\":\"" + contentType + "\",\"videoDurationSeconds\":" + videoDurationSeconds + "}");
        return entity;
    }

    private static Clock fixedBusinessClock() {
        return Clock.fixed(LocalDate.of(2026, 3, 21).atStartOfDay(BUSINESS_ZONE_ID).toInstant(), BUSINESS_ZONE_ID);
    }

    private static final class ServiceFixture {

        private final ManuscriptReviewRecordMapper recordMapper = mock(ManuscriptReviewRecordMapper.class);
        private final ManuscriptReviewAttachmentMapper attachmentMapper = mock(ManuscriptReviewAttachmentMapper.class);
        private final ManuscriptReviewExternalLinkMapper externalLinkMapper = mock(ManuscriptReviewExternalLinkMapper.class);
        private final ManuscriptReviewVideoMarkerMapper videoMarkerMapper = mock(ManuscriptReviewVideoMarkerMapper.class);
        private final ManuscriptReviewHistoryMapper historyMapper = mock(ManuscriptReviewHistoryMapper.class);
        private final ManuscriptReviewFlowConfigMapper flowConfigMapper = mock(ManuscriptReviewFlowConfigMapper.class);
        private final ManuscriptReviewSystemRoleMapper roleMapper = mock(ManuscriptReviewSystemRoleMapper.class);
        private final ManuscriptReviewSysOssMapper sysOssMapper = mock(ManuscriptReviewSysOssMapper.class);
        private final ManuscriptReviewSystemUserRoleMapper userRoleMapper = mock(ManuscriptReviewSystemUserRoleMapper.class);
        private final ManuscriptReviewSystemUserMapper userMapper = mock(ManuscriptReviewSystemUserMapper.class);
        private final ManuscriptReviewSerialGateway serialGateway = mock(ManuscriptReviewSerialGateway.class);
        private final ManuscriptReviewCurrentUserGateway currentUserGateway = mock(ManuscriptReviewCurrentUserGateway.class);
        private final WorkflowService workflowService = mock(WorkflowService.class);
        private final IFlwInstanceService flwInstanceService = mock(IFlwInstanceService.class);
        private final AtomicLong idSequence = new AtomicLong(9000L);
        private final ManuscriptReviewService service;

        private ServiceFixture(Long currentUserId, String tenantId, Long currentDeptId, String currentUsername) {
            when(currentUserGateway.getCurrentUserId()).thenReturn(currentUserId);
            when(currentUserGateway.getCurrentTenantId()).thenReturn(tenantId);
            when(currentUserGateway.getCurrentDeptId()).thenReturn(currentDeptId);
            when(currentUserGateway.getCurrentUsername()).thenReturn(currentUsername);
            this.service = new ManuscriptReviewService(
                recordMapper,
                attachmentMapper,
                externalLinkMapper,
                videoMarkerMapper,
                historyMapper,
                flowConfigMapper,
                roleMapper,
                userRoleMapper,
                userMapper,
                serialGateway,
                currentUserGateway,
                workflowService,
                flwInstanceService,
                sysOssMapper,
                fixedBusinessClock(),
                idSequence::incrementAndGet
            );
        }

        private void stubSubmitRouting(boolean certifiedCurrentUser) {
            ManuscriptReviewFlowConfigEntity flowConfig = new ManuscriptReviewFlowConfigEntity();
            flowConfig.setTenantId("000000");
            flowConfig.setProcessType("AUDIT");
            flowConfig.setFlowCode("manuscript_review_audit_flow");
            flowConfig.setLevelOneRoleKey("manuscript_review_level_1_approver");
            flowConfig.setLevelTwoRoleKey("manuscript_review_level_2_approver");
            flowConfig.setLevelThreeRoleKey("manuscript_review_level_3_approver");
            flowConfig.setStatus("0");
            when(flowConfigMapper.selectOne(any())).thenReturn(flowConfig);

            ManuscriptReviewSystemRoleEntity levelOneRole = buildRole(7101L, "manuscript_review_level_1_approver");
            ManuscriptReviewSystemRoleEntity levelTwoRole = buildRole(7102L, "manuscript_review_level_2_approver");
            ManuscriptReviewSystemRoleEntity levelThreeRole = buildRole(7103L, "manuscript_review_level_3_approver");
            ManuscriptReviewSystemRoleEntity certifiedRole = buildRole(7104L, "manuscript_review_certified_initiator");
            ManuscriptReviewSystemRoleEntity initiatorRole = buildRole(7105L, "manuscript_review_initiator");
            when(roleMapper.selectOne(any())).thenReturn(initiatorRole, certifiedRole, levelOneRole, levelTwoRole, levelThreeRole, certifiedRole);

            ManuscriptReviewSystemUserRoleEntity levelOneUserRole = buildUserRole(8101L, 7101L);
            ManuscriptReviewSystemUserRoleEntity levelTwoUserRole = buildUserRole(8102L, 7102L);
            ManuscriptReviewSystemUserRoleEntity levelThreeUserRole = buildUserRole(8103L, 7103L);
            ManuscriptReviewSystemUserRoleEntity certifiedUserRole = buildUserRole(currentUserGateway.getCurrentUserId(), 7104L);
            when(userRoleMapper.selectList(any())).thenReturn(
                List.of(),
                certifiedCurrentUser ? List.of(certifiedUserRole) : List.of(),
                List.of(levelOneUserRole),
                List.of(levelTwoUserRole),
                List.of(levelThreeUserRole),
                certifiedCurrentUser ? List.of(certifiedUserRole) : List.of()
            );

            ManuscriptReviewSystemUserEntity levelOneUser = buildEnabledUser(8101L);
            ManuscriptReviewSystemUserEntity levelTwoUser = buildEnabledUser(8102L);
            ManuscriptReviewSystemUserEntity levelThreeUser = buildEnabledUser(8103L);
            ManuscriptReviewSystemUserEntity certifiedUser = buildEnabledUser(currentUserGateway.getCurrentUserId());
            when(userMapper.selectList(any())).thenReturn(
                certifiedCurrentUser ? List.of(certifiedUser) : List.of(),
                List.of(levelOneUser),
                List.of(levelTwoUser),
                List.of(levelThreeUser),
                certifiedCurrentUser ? List.of(certifiedUser) : List.of()
            );
            when(workflowService.startCompleteTask(any(StartProcessDTO.class))).thenReturn(true);
            when(workflowService.getInstanceIdByBusinessId(any())).thenReturn(99001L);
        }

        private static ManuscriptReviewSystemRoleEntity buildRole(Long roleId, String roleKey) {
            ManuscriptReviewSystemRoleEntity entity = new ManuscriptReviewSystemRoleEntity();
            entity.setRoleId(roleId);
            entity.setTenantId("000000");
            entity.setRoleKey(roleKey);
            entity.setStatus("0");
            entity.setDelFlag("0");
            return entity;
        }

        private static ManuscriptReviewSystemUserRoleEntity buildUserRole(Long userId, Long roleId) {
            ManuscriptReviewSystemUserRoleEntity entity = new ManuscriptReviewSystemUserRoleEntity();
            entity.setUserId(userId);
            entity.setRoleId(roleId);
            return entity;
        }

        private static ManuscriptReviewSystemUserEntity buildEnabledUser(Long userId) {
            ManuscriptReviewSystemUserEntity entity = new ManuscriptReviewSystemUserEntity();
            entity.setUserId(userId);
            entity.setTenantId("000000");
            entity.setStatus("0");
            entity.setDelFlag("0");
            return entity;
        }
    }
}
