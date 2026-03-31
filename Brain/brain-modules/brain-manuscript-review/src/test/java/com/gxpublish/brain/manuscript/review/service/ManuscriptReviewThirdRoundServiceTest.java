package com.gxpublish.brain.manuscript.review.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.lang.reflect.Method;
import java.time.Clock;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.concurrent.atomic.AtomicLong;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.gxpublish.brain.common.core.service.WorkflowService;
import com.gxpublish.brain.manuscript.review.domain.entity.ManuscriptReviewAttachmentEntity;
import com.gxpublish.brain.manuscript.review.domain.entity.ManuscriptReviewExternalLinkEntity;
import com.gxpublish.brain.manuscript.review.domain.entity.ManuscriptReviewHistoryEntity;
import com.gxpublish.brain.manuscript.review.domain.entity.ManuscriptReviewRecordEntity;
import com.gxpublish.brain.manuscript.review.domain.entity.ManuscriptReviewVideoMarkerEntity;
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
class ManuscriptReviewThirdRoundServiceTest {

    private static final ZoneId BUSINESS_ZONE_ID = ZoneId.of("Asia/Shanghai");

    @Test
    void shouldEnableDisabledAttachmentAndAppendNewHistory() throws Exception {
        ThirdRoundFixture fixture = new ThirdRoundFixture(1014L, "000000", 2001L, "tester");
        ManuscriptReviewAttachmentEntity attachment = buildAttachment(7011L, 9014L);
        attachment.setEnabled("0");
        attachment.setDisabledBy(2008L);
        attachment.setDisabledTime(new Date(1774236180000L));
        attachment.setRemark("disabled reason");
        when(fixture.attachmentMapper.selectById(7011L)).thenReturn(attachment);
        when(fixture.externalLinkMapper.selectById(7011L)).thenReturn(null);
        when(fixture.recordMapper.selectById(9014L)).thenReturn(buildEditableRecord(9014L, 1014L));

        invokeThirdRoundMethod(
            fixture.service,
            "enableResource",
            "com.gxpublish.brain.manuscript.review.domain.command.EnableManuscriptReviewResourceCommand",
            "resourceId",
            7011L
        );

        ArgumentCaptor<UpdateWrapper<ManuscriptReviewAttachmentEntity>> wrapperCaptor =
            ArgumentCaptor.forClass(UpdateWrapper.class);
        verify(fixture.attachmentMapper).update(isNull(), wrapperCaptor.capture());
        assertEnableUpdateWrapperSql(wrapperCaptor.getValue().getSqlSet());

        ArgumentCaptor<ManuscriptReviewHistoryEntity> historyCaptor = ArgumentCaptor.forClass(ManuscriptReviewHistoryEntity.class);
        verify(fixture.historyMapper).insert(historyCaptor.capture());
        assertEquals("RESOURCE_ENABLE", historyCaptor.getValue().getActionType());
        assertTrue(historyCaptor.getValue().getActionText().contains("tester"));
        assertTrue(historyCaptor.getValue().getActionText().contains("attachment.pdf"));
    }

    @Test
    void shouldEnableDisabledExternalLinkAndAppendNewHistory() throws Exception {
        ThirdRoundFixture fixture = new ThirdRoundFixture(1016L, "000000", 2001L, "tester");
        ManuscriptReviewExternalLinkEntity externalLink = buildExternalLink(7016L, 9016L);
        externalLink.setEnabled("0");
        externalLink.setDisabledBy(2008L);
        externalLink.setDisabledTime(new Date(1774236180000L));
        externalLink.setRemark("disabled reason");
        when(fixture.attachmentMapper.selectById(7016L)).thenReturn(null);
        when(fixture.externalLinkMapper.selectById(7016L)).thenReturn(externalLink);
        when(fixture.recordMapper.selectById(9016L)).thenReturn(buildEditableRecord(9016L, 1016L));

        invokeThirdRoundMethod(
            fixture.service,
            "enableResource",
            "com.gxpublish.brain.manuscript.review.domain.command.EnableManuscriptReviewResourceCommand",
            "resourceId",
            7016L
        );

        ArgumentCaptor<UpdateWrapper<ManuscriptReviewExternalLinkEntity>> wrapperCaptor =
            ArgumentCaptor.forClass(UpdateWrapper.class);
        verify(fixture.externalLinkMapper).update(isNull(), wrapperCaptor.capture());
        assertEnableUpdateWrapperSql(wrapperCaptor.getValue().getSqlSet());

        ArgumentCaptor<ManuscriptReviewHistoryEntity> historyCaptor = ArgumentCaptor.forClass(ManuscriptReviewHistoryEntity.class);
        verify(fixture.historyMapper).insert(historyCaptor.capture());
        assertEquals("RESOURCE_ENABLE", historyCaptor.getValue().getActionType());
        assertTrue(historyCaptor.getValue().getActionText().contains("tester"));
        assertTrue(historyCaptor.getValue().getActionText().contains("external-link-title"));
    }

    @Test
    void shouldEnableDisabledVideoMarkAndAppendNewHistory() throws Exception {
        ThirdRoundFixture fixture = new ThirdRoundFixture(1018L, "000000", 2001L, "tester");
        ManuscriptReviewVideoMarkerEntity marker = buildVideoMarker(7021L, 9015L);
        marker.setEnabled("0");
        marker.setDisabledBy(2008L);
        marker.setDisabledTime(new Date(1774236180000L));
        marker.setRemark("disabled reason");
        when(fixture.videoMarkerMapper.selectById(7021L)).thenReturn(marker);
        when(fixture.recordMapper.selectById(9015L)).thenReturn(buildEditableRecord(9015L, 1018L));

        invokeThirdRoundMethod(
            fixture.service,
            "enableVideoMark",
            "com.gxpublish.brain.manuscript.review.domain.command.EnableManuscriptReviewVideoMarkCommand",
            "markId",
            7021L
        );

        ArgumentCaptor<UpdateWrapper<ManuscriptReviewVideoMarkerEntity>> wrapperCaptor =
            ArgumentCaptor.forClass(UpdateWrapper.class);
        verify(fixture.videoMarkerMapper).update(isNull(), wrapperCaptor.capture());
        assertEnableUpdateWrapperSql(wrapperCaptor.getValue().getSqlSet());

        ArgumentCaptor<ManuscriptReviewHistoryEntity> historyCaptor = ArgumentCaptor.forClass(ManuscriptReviewHistoryEntity.class);
        verify(fixture.historyMapper).insert(historyCaptor.capture());
        assertEquals("VIDEO_MARK_ENABLE", historyCaptor.getValue().getActionType());
        assertTrue(historyCaptor.getValue().getActionText().contains("tester"));
        assertTrue(historyCaptor.getValue().getActionText().contains("video-mark"));
    }

    private static void assertEnableUpdateWrapperSql(String sqlSet) {
        assertTrue(sqlSet.contains("enabled"));
        assertTrue(sqlSet.contains("disabled_by"));
        assertTrue(sqlSet.contains("disabled_time"));
        assertTrue(sqlSet.contains("remark"));
    }

    private static void invokeThirdRoundMethod(Object target,
                                               String methodName,
                                               String commandClassName,
                                               String setterName,
                                               Long targetId) throws Exception {
        Class<?> commandClass = Class.forName(commandClassName);
        Object builder = commandClass.getMethod("builder").invoke(null);
        Method setter = builder.getClass().getMethod(setterName, Long.class);
        setter.invoke(builder, targetId);
        Object command = builder.getClass().getMethod("build").invoke(builder);
        Method method = target.getClass().getMethod(methodName, commandClass);
        method.invoke(target, command);
    }

    private static ManuscriptReviewRecordEntity buildEditableRecord(Long reviewId, Long initiatorUserId) {
        ManuscriptReviewRecordEntity entity = new ManuscriptReviewRecordEntity();
        entity.setId(reviewId);
        entity.setTenantId("000000");
        entity.setProcessType("AUDIT");
        entity.setTitle("review-title");
        entity.setMediaChannel("channel");
        entity.setSubmitDepartment("dept");
        entity.setAuthorName("author");
        entity.setContentBody("content");
        entity.setInitiatorUserId(initiatorUserId);
        entity.setInitiatorName("tester");
        entity.setFlowStatusLabel("已退回");
        entity.setCurrentNodeLabel("待发起人处理");
        entity.setCreateTime(new Date(1774060800000L));
        entity.setUpdateTime(new Date(1774064400000L));
        return entity;
    }

    private static ManuscriptReviewAttachmentEntity buildAttachment(Long resourceId, Long reviewId) {
        ManuscriptReviewAttachmentEntity entity = new ManuscriptReviewAttachmentEntity();
        entity.setId(resourceId);
        entity.setReviewId(reviewId);
        entity.setFileName("attachment.pdf");
        entity.setEnabled("1");
        entity.setIsVideo(false);
        return entity;
    }

    private static ManuscriptReviewVideoMarkerEntity buildVideoMarker(Long markerId, Long reviewId) {
        ManuscriptReviewVideoMarkerEntity entity = new ManuscriptReviewVideoMarkerEntity();
        entity.setId(markerId);
        entity.setReviewId(reviewId);
        entity.setVideoAttachmentId(7004L);
        entity.setStartTime("00:00:05");
        entity.setEndTime("00:01:10");
        entity.setMarkerNote("video-mark");
        entity.setEnabled("1");
        return entity;
    }

    private static ManuscriptReviewExternalLinkEntity buildExternalLink(Long resourceId, Long reviewId) {
        ManuscriptReviewExternalLinkEntity entity = new ManuscriptReviewExternalLinkEntity();
        entity.setId(resourceId);
        entity.setReviewId(reviewId);
        entity.setLinkTitle("external-link-title");
        entity.setLinkUrl("https://example.com");
        entity.setEnabled("1");
        return entity;
    }

    private static Clock fixedBusinessClock() {
        return Clock.fixed(
            LocalDate.of(2026, 3, 21).atStartOfDay(BUSINESS_ZONE_ID).toInstant(),
            BUSINESS_ZONE_ID
        );
    }

    private static final class ThirdRoundFixture {

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

        private ThirdRoundFixture(Long currentUserId, String tenantId, Long currentDeptId, String currentUsername) {
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
    }
}
