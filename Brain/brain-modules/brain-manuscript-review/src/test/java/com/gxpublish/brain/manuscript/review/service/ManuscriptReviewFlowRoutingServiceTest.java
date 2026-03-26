package com.gxpublish.brain.manuscript.review.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
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

import com.gxpublish.brain.common.core.exception.ServiceException;
import com.gxpublish.brain.manuscript.review.domain.command.ResubmitManuscriptReviewCommand;
import com.gxpublish.brain.manuscript.review.domain.entity.ManuscriptReviewAttachmentEntity;
import com.gxpublish.brain.manuscript.review.domain.entity.ManuscriptReviewFlowConfigEntity;
import com.gxpublish.brain.manuscript.review.domain.entity.ManuscriptReviewHistoryEntity;
import com.gxpublish.brain.manuscript.review.domain.entity.ManuscriptReviewRecordEntity;
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
import com.gxpublish.brain.manuscript.review.mapper.ManuscriptReviewSystemRoleMapper;
import com.gxpublish.brain.manuscript.review.mapper.ManuscriptReviewSysOssMapper;
import com.gxpublish.brain.manuscript.review.mapper.ManuscriptReviewSystemUserMapper;
import com.gxpublish.brain.manuscript.review.mapper.ManuscriptReviewSystemUserRoleMapper;
import com.gxpublish.brain.manuscript.review.mapper.ManuscriptReviewVideoMarkerMapper;

@Tag("dev")
class ManuscriptReviewFlowRoutingServiceTest {

    private static final ZoneId BUSINESS_ZONE_ID = ZoneId.of("Asia/Shanghai");
    private static final String FLOW_CODE_AUDIT = "manuscript_review_audit_flow";
    private static final String ROLE_KEY_LEVEL_1 = "manuscript_review_level_1_approver";
    private static final String ROLE_KEY_LEVEL_2 = "manuscript_review_level_2_approver";
    private static final String ROLE_KEY_LEVEL_3 = "manuscript_review_level_3_approver";
    private static final String ROLE_KEY_CERTIFIED = "manuscript_review_certified_initiator";

    @Test
    void shouldRouteSubmitToLevelOneForNormalInitiator() {
        FlowFixture fixture = new FlowFixture(1001L, "张三");
        ManuscriptReviewRecordEntity existing = buildDraftRecord(9101L);
        when(fixture.recordMapper.selectById(9101L)).thenReturn(existing);
        when(fixture.attachmentMapper.selectList(any())).thenReturn(List.of(buildAttachment(8101L)));
        when(fixture.externalLinkMapper.selectList(any())).thenReturn(List.of());
        when(fixture.serialGateway.nextSerial(ManuscriptReviewProcessType.AUDIT, "20260321")).thenReturn(7);
        fixture.stubSubmitRouting(false);

        String manuscriptCode = fixture.service.submitAndFlowStart(9101L);

        assertEquals("SH20260321007", manuscriptCode);
        ArgumentCaptor<ManuscriptReviewRecordEntity> recordCaptor = ArgumentCaptor.forClass(ManuscriptReviewRecordEntity.class);
        verify(fixture.recordMapper).updateById(recordCaptor.capture());
        ManuscriptReviewRecordEntity updated = recordCaptor.getValue();
        assertEquals(FLOW_CODE_AUDIT, updated.getFlowCode());
        assertEquals("审批中", updated.getFlowStatusLabel());
        assertEquals("待一级审批", updated.getCurrentNodeLabel());
        assertNotNull(updated.getFirstSubmitTime());
        assertNotNull(updated.getLatestSubmitTime());

        ArgumentCaptor<ManuscriptReviewHistoryEntity> historyCaptor = ArgumentCaptor.forClass(ManuscriptReviewHistoryEntity.class);
        verify(fixture.historyMapper).insert(historyCaptor.capture());
        assertEquals("SUBMIT", historyCaptor.getValue().getActionType());
        assertEquals("张三提交了审校流程单。", historyCaptor.getValue().getActionText());
    }

    @Test
    void shouldSkipLevelOneAndWriteSystemHistoryWhenInitiatorIsCertified() {
        FlowFixture fixture = new FlowFixture(1001L, "张三");
        ManuscriptReviewRecordEntity existing = buildDraftRecord(9102L);
        when(fixture.recordMapper.selectById(9102L)).thenReturn(existing);
        when(fixture.attachmentMapper.selectList(any())).thenReturn(List.of(buildAttachment(8102L)));
        when(fixture.externalLinkMapper.selectList(any())).thenReturn(List.of());
        when(fixture.serialGateway.nextSerial(ManuscriptReviewProcessType.AUDIT, "20260321")).thenReturn(8);
        fixture.stubSubmitRouting(true);

        fixture.service.submitAndFlowStart(9102L);

        ArgumentCaptor<ManuscriptReviewRecordEntity> recordCaptor = ArgumentCaptor.forClass(ManuscriptReviewRecordEntity.class);
        verify(fixture.recordMapper).updateById(recordCaptor.capture());
        assertEquals("待二级审批", recordCaptor.getValue().getCurrentNodeLabel());

        ArgumentCaptor<ManuscriptReviewHistoryEntity> historyCaptor = ArgumentCaptor.forClass(ManuscriptReviewHistoryEntity.class);
        verify(fixture.historyMapper, org.mockito.Mockito.times(2)).insert(historyCaptor.capture());
        List<ManuscriptReviewHistoryEntity> histories = historyCaptor.getAllValues();
        assertEquals("SUBMIT", histories.get(0).getActionType());
        assertEquals("SKIP_LEVEL_1", histories.get(1).getActionType());
        assertEquals("系统判定发起人具备持证资格，自动跳过一级审批", histories.get(1).getActionText());
    }

    @Test
    void shouldRerouteReturnedRecordOnResubmit() {
        FlowFixture fixture = new FlowFixture(1001L, "张三");
        ManuscriptReviewRecordEntity existing = buildDraftRecord(9103L);
        existing.setManuscriptCode("SH20260321009");
        existing.setFirstSubmitTime(new Date(1774060800000L));
        existing.setLatestSubmitTime(new Date(1774064400000L));
        existing.setFlowStatusLabel("已退回");
        existing.setCurrentNodeLabel("待发起人处理");
        when(fixture.recordMapper.selectById(9103L)).thenReturn(existing);
        when(fixture.attachmentMapper.selectList(any())).thenReturn(List.of(buildAttachment(8103L)));
        when(fixture.externalLinkMapper.selectList(any())).thenReturn(List.of());
        fixture.stubSubmitRouting(true);

        fixture.service.resubmit(ResubmitManuscriptReviewCommand.builder().reviewId(9103L).build());

        ArgumentCaptor<ManuscriptReviewRecordEntity> recordCaptor = ArgumentCaptor.forClass(ManuscriptReviewRecordEntity.class);
        verify(fixture.recordMapper).updateById(recordCaptor.capture());
        assertEquals("审批中", recordCaptor.getValue().getFlowStatusLabel());
        assertEquals("待二级审批", recordCaptor.getValue().getCurrentNodeLabel());

        ArgumentCaptor<ManuscriptReviewHistoryEntity> historyCaptor = ArgumentCaptor.forClass(ManuscriptReviewHistoryEntity.class);
        verify(fixture.historyMapper, org.mockito.Mockito.times(2)).insert(historyCaptor.capture());
        assertEquals("RESUBMIT", historyCaptor.getAllValues().get(0).getActionType());
        assertEquals("SKIP_LEVEL_1", historyCaptor.getAllValues().get(1).getActionType());
    }

    @Test
    void shouldRejectSubmitWhenApprovalChainConfigMissing() {
        FlowFixture fixture = new FlowFixture(1001L, "张三");
        when(fixture.recordMapper.selectById(9104L)).thenReturn(buildDraftRecord(9104L));
        when(fixture.attachmentMapper.selectList(any())).thenReturn(List.of(buildAttachment(8104L)));
        when(fixture.externalLinkMapper.selectList(any())).thenReturn(List.of());
        when(fixture.flowConfigMapper.selectOne(any())).thenReturn(null);

        ServiceException exception = assertThrows(ServiceException.class, () -> fixture.service.submitAndFlowStart(9104L));

        assertEquals("审校流程审批链配置缺失", exception.getMessage());
    }

    @Test
    void shouldRejectSubmitWhenCertifiedRoleCannotBeResolved() {
        FlowFixture fixture = new FlowFixture(1001L, "张三");
        when(fixture.recordMapper.selectById(9105L)).thenReturn(buildDraftRecord(9105L));
        when(fixture.attachmentMapper.selectList(any())).thenReturn(List.of(buildAttachment(8105L)));
        when(fixture.externalLinkMapper.selectList(any())).thenReturn(List.of());
        when(fixture.flowConfigMapper.selectOne(any())).thenReturn(buildFlowConfig());
        when(fixture.roleMapper.selectOne(any())).thenReturn(
            buildRole(7101L, ROLE_KEY_LEVEL_1),
            buildRole(7102L, ROLE_KEY_LEVEL_2),
            buildRole(7103L, ROLE_KEY_LEVEL_3),
            null
        );
        when(fixture.userRoleMapper.selectList(any())).thenReturn(
            List.of(buildUserRole(8101L, 7101L)),
            List.of(buildUserRole(8102L, 7102L)),
            List.of(buildUserRole(8103L, 7103L))
        );
        when(fixture.userMapper.selectList(any())).thenReturn(
            List.of(buildEnabledUser(8101L)),
            List.of(buildEnabledUser(8102L)),
            List.of(buildEnabledUser(8103L))
        );

        ServiceException exception = assertThrows(ServiceException.class, () -> fixture.service.submitAndFlowStart(9105L));

        assertEquals("持证发起人角色解析失败", exception.getMessage());
    }

    private static ManuscriptReviewRecordEntity buildDraftRecord(Long reviewId) {
        ManuscriptReviewRecordEntity entity = new ManuscriptReviewRecordEntity();
        entity.setId(reviewId);
        entity.setTenantId("000000");
        entity.setProcessType("AUDIT");
        entity.setTitle("稿件标题");
        entity.setMediaChannel("新华社/要闻");
        entity.setSubmitDepartment("总编室");
        entity.setAuthorName("张三");
        entity.setContentBody("正文内容");
        entity.setInitiatorUserId(1001L);
        entity.setInitiatorName("张三");
        entity.setCreateTime(new Date(1774060800000L));
        entity.setUpdateTime(new Date(1774064400000L));
        return entity;
    }

    private static ManuscriptReviewAttachmentEntity buildAttachment(Long resourceId) {
        ManuscriptReviewAttachmentEntity entity = new ManuscriptReviewAttachmentEntity();
        entity.setId(resourceId);
        entity.setReviewId(9101L);
        entity.setFileName("送审单.pdf");
        entity.setEnabled("1");
        entity.setIsVideo(false);
        return entity;
    }

    private static ManuscriptReviewFlowConfigEntity buildFlowConfig() {
        ManuscriptReviewFlowConfigEntity entity = new ManuscriptReviewFlowConfigEntity();
        entity.setTenantId("000000");
        entity.setProcessType("AUDIT");
        entity.setFlowCode(FLOW_CODE_AUDIT);
        entity.setLevelOneRoleKey(ROLE_KEY_LEVEL_1);
        entity.setLevelTwoRoleKey(ROLE_KEY_LEVEL_2);
        entity.setLevelThreeRoleKey(ROLE_KEY_LEVEL_3);
        entity.setStatus("0");
        return entity;
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

    private static Clock fixedBusinessClock() {
        return Clock.fixed(LocalDate.of(2026, 3, 21).atStartOfDay(BUSINESS_ZONE_ID).toInstant(), BUSINESS_ZONE_ID);
    }

    private static final class FlowFixture {

        private final Long currentUserId;
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
        private final com.gxpublish.brain.common.core.service.WorkflowService workflowService =
            mock(com.gxpublish.brain.common.core.service.WorkflowService.class);
        private final com.gxpublish.brain.workflow.service.IFlwInstanceService flwInstanceService =
            mock(com.gxpublish.brain.workflow.service.IFlwInstanceService.class);
        private final ManuscriptReviewService service;

        private FlowFixture(Long currentUserId, String currentUsername) {
            this.currentUserId = currentUserId;
            when(currentUserGateway.getCurrentUserId()).thenReturn(currentUserId);
            when(currentUserGateway.getCurrentTenantId()).thenReturn("000000");
            when(currentUserGateway.getCurrentDeptId()).thenReturn(2001L);
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
                new AtomicLong(9900L)::incrementAndGet
            );
            when(workflowService.startCompleteTask(any(com.gxpublish.brain.common.core.domain.dto.StartProcessDTO.class))).thenReturn(true);
            when(workflowService.getInstanceIdByBusinessId(any())).thenReturn(99100L);
        }

        private void stubSubmitRouting(boolean certifiedCurrentUser) {
            when(flowConfigMapper.selectOne(any())).thenReturn(buildFlowConfig());
            when(roleMapper.selectOne(any())).thenReturn(
                buildRole(7101L, ROLE_KEY_LEVEL_1),
                buildRole(7102L, ROLE_KEY_LEVEL_2),
                buildRole(7103L, ROLE_KEY_LEVEL_3),
                buildRole(7104L, ROLE_KEY_CERTIFIED)
            );
            when(userRoleMapper.selectList(any())).thenReturn(
                List.of(buildUserRole(8101L, 7101L)),
                List.of(buildUserRole(8102L, 7102L)),
                List.of(buildUserRole(8103L, 7103L)),
                certifiedCurrentUser ? List.of(buildUserRole(currentUserId, 7104L)) : List.of()
            );
            when(userMapper.selectList(any())).thenReturn(
                List.of(buildEnabledUser(8101L)),
                List.of(buildEnabledUser(8102L)),
                List.of(buildEnabledUser(8103L)),
                certifiedCurrentUser ? List.of(buildEnabledUser(currentUserId)) : List.of()
            );
        }
    }
}
