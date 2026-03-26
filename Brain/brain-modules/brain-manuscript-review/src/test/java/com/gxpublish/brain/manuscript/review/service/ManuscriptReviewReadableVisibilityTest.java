package com.gxpublish.brain.manuscript.review.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Date;
import java.util.List;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import com.gxpublish.brain.common.core.exception.ServiceException;
import com.gxpublish.brain.common.mybatis.core.page.TableDataInfo;
import com.gxpublish.brain.manuscript.review.controller.request.ManuscriptReviewLedgerQueryRequest;
import com.gxpublish.brain.manuscript.review.controller.response.ManuscriptReviewDetailResponse;
import com.gxpublish.brain.manuscript.review.controller.response.ManuscriptReviewLedgerItemResponse;
import com.gxpublish.brain.manuscript.review.domain.entity.ManuscriptReviewAttachmentEntity;
import com.gxpublish.brain.manuscript.review.domain.entity.ManuscriptReviewFlowConfigEntity;
import com.gxpublish.brain.manuscript.review.domain.entity.ManuscriptReviewHistoryEntity;
import com.gxpublish.brain.manuscript.review.domain.entity.ManuscriptReviewRecordEntity;
import com.gxpublish.brain.manuscript.review.domain.entity.ManuscriptReviewSystemRoleEntity;
import com.gxpublish.brain.manuscript.review.domain.entity.ManuscriptReviewSystemUserEntity;
import com.gxpublish.brain.manuscript.review.domain.entity.ManuscriptReviewSystemUserRoleEntity;
import com.gxpublish.brain.manuscript.review.gateway.ManuscriptReviewCurrentUserGateway;
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
class ManuscriptReviewReadableVisibilityTest {

    @Test
    void shouldOnlyReturnVisibleRecordsInLedger() {
        ReadableFixture fixture = new ReadableFixture(4001L);
        when(fixture.recordMapper.selectList(any())).thenReturn(List.of(
            buildRecord(9301L, 4001L, "审批中", "待一级审批"),
            buildRecord(9302L, 3002L, "审批中", "待二级审批"),
            buildRecord(9303L, 3003L, "已完成", "流程完成"),
            buildRecord(9304L, 3004L, "已取消", "流程已取消")
        ));
        when(fixture.historyMapper.selectList(any())).thenReturn(List.of(
            buildHistory(9303L, 4001L, "李四完成审批。")
        ));
        when(fixture.flowConfigMapper.selectOne(any())).thenReturn(buildFlowConfig());
        when(fixture.roleMapper.selectList(any())).thenReturn(List.of(buildRole(7102L, "role:l2")));
        when(fixture.userRoleMapper.selectList(any())).thenReturn(List.of(buildUserRole(4001L, 7102L)));
        when(fixture.userMapper.selectList(any())).thenReturn(List.of(buildEnabledUser(4001L)));

        TableDataInfo<ManuscriptReviewLedgerItemResponse> ledger = fixture.readableService.listLedger(new ManuscriptReviewLedgerQueryRequest());

        assertEquals(3, ledger.getTotal());
        assertEquals(List.of(9303L, 9302L, 9301L), ledger.getRows().stream().map(ManuscriptReviewLedgerItemResponse::getId).toList());
    }

    @Test
    void shouldRejectDetailWhenCurrentUserCannotViewRecord() {
        ReadableFixture fixture = new ReadableFixture(4999L);
        when(fixture.recordMapper.selectById(9305L)).thenReturn(buildRecord(9305L, 3005L, "审批中", "待一级审批"));
        when(fixture.attachmentMapper.selectList(any())).thenReturn(List.of());
        when(fixture.externalLinkMapper.selectList(any())).thenReturn(List.of());
        when(fixture.historyMapper.selectList(any())).thenReturn(List.of());
        when(fixture.videoMarkerMapper.selectList(any())).thenReturn(List.of());
        when(fixture.flowConfigMapper.selectOne(any())).thenReturn(buildFlowConfig());
        when(fixture.roleMapper.selectList(any())).thenReturn(List.of(buildRole(7101L, "role:l1")));
        when(fixture.userRoleMapper.selectList(any())).thenReturn(List.of(buildUserRole(4001L, 7101L)));
        when(fixture.userMapper.selectList(any())).thenReturn(List.of(buildEnabledUser(4001L)));

        ServiceException exception = assertThrows(ServiceException.class, () -> fixture.readableService.getDetail(9305L));

        assertEquals("当前用户无权查看该流程", exception.getMessage());
    }

    @Test
    void shouldReturnTerminalPermissionReasonForRejectedFlow() {
        ReadableFixture fixture = new ReadableFixture(4001L);
        ManuscriptReviewRecordEntity record = buildRecord(9306L, 4001L, "已驳回", "流程已驳回");
        when(fixture.recordMapper.selectById(9306L)).thenReturn(record);
        when(fixture.attachmentMapper.selectList(any())).thenReturn(List.of(buildAttachment(8401L)));
        when(fixture.externalLinkMapper.selectList(any())).thenReturn(List.of());
        when(fixture.historyMapper.selectList(any())).thenReturn(List.of(buildHistory(9306L, 4001L, "三级审批驳回了流程。")));
        when(fixture.videoMarkerMapper.selectList(any())).thenReturn(List.of());

        ManuscriptReviewDetailResponse detail = fixture.readableService.getDetail(9306L);

        assertTrue(detail.getPermissionMatrix().isCanView());
        assertFalse(detail.getPermissionMatrix().isCanEdit());
        assertFalse(detail.getPermissionMatrix().isCanResubmit());
        assertFalse(detail.getPermissionMatrix().isCanCancel());
        assertFalse(detail.getPermissionMatrix().isCanGotoApproval());
        assertEquals("流程已驳回，不可继续操作", detail.getPermissionMatrix().getButtonReason());
    }

    @Test
    void shouldTreatRuntimeWorkflowNodeNameAsCurrentApproverNode() {
        ReadableFixture fixture = new ReadableFixture(4001L);
        ManuscriptReviewRecordEntity record = buildRecord(9307L, 3007L, "审批中", "一级审批");
        when(fixture.recordMapper.selectById(9307L)).thenReturn(record);
        when(fixture.attachmentMapper.selectList(any())).thenReturn(List.of(buildAttachment(8402L)));
        when(fixture.externalLinkMapper.selectList(any())).thenReturn(List.of());
        when(fixture.historyMapper.selectList(any())).thenReturn(List.of());
        when(fixture.videoMarkerMapper.selectList(any())).thenReturn(List.of());
        when(fixture.flowConfigMapper.selectOne(any())).thenReturn(buildFlowConfig());
        when(fixture.roleMapper.selectList(any())).thenReturn(List.of(buildRole(7101L, "role:l1")));
        when(fixture.userRoleMapper.selectList(any())).thenReturn(List.of(buildUserRole(4001L, 7101L)));
        when(fixture.userMapper.selectList(any())).thenReturn(List.of(buildEnabledUser(4001L)));

        ManuscriptReviewDetailResponse detail = fixture.readableService.getDetail(9307L);

        assertEquals("待一级审批", detail.getCurrentNodeLabel());
        assertEquals("LEVEL_1", detail.getCurrentNodeCode());
        assertTrue(detail.getPermissionMatrix().isCanView());
        assertTrue(detail.getPermissionMatrix().isCanEdit());
        assertTrue(detail.getPermissionMatrix().isCanGotoApproval());
    }

    @Test
    void shouldKeepCreateAheadOfSkipLevelOneWhenTimelineTimestampsTie() {
        ReadableFixture fixture = new ReadableFixture(3008L);
        ManuscriptReviewRecordEntity record = buildRecord(9308L, 3008L, "审批中", "待二级审批");
        when(fixture.recordMapper.selectById(9308L)).thenReturn(record);
        when(fixture.attachmentMapper.selectList(any())).thenReturn(List.of(buildAttachment(8403L)));
        when(fixture.externalLinkMapper.selectList(any())).thenReturn(List.of());
        Date sameTime = new Date(1774236000000L);
        when(fixture.historyMapper.selectList(any())).thenReturn(List.of(
            buildHistory(9308L, null, "SKIP_LEVEL_1", "系统判定发起人具备持证资格，自动跳过一级审批。", sameTime),
            buildHistory(9308L, 3008L, "CREATE", "张三新增了流程。", sameTime)
        ));
        when(fixture.videoMarkerMapper.selectList(any())).thenReturn(List.of());

        ManuscriptReviewDetailResponse detail = fixture.readableService.getDetail(9308L);

        assertEquals(List.of("张三新增了流程。", "系统判定发起人具备持证资格，自动跳过一级审批。"),
            detail.getTimelineItems().stream().map(ManuscriptReviewDetailResponse.TimelineItemVO::getEventText).toList());
    }

    private static ManuscriptReviewRecordEntity buildRecord(Long id, Long initiatorUserId, String flowStatusLabel, String currentNodeLabel) {
        ManuscriptReviewRecordEntity entity = new ManuscriptReviewRecordEntity();
        entity.setId(id);
        entity.setTenantId("000000");
        entity.setProcessType("AUDIT");
        entity.setManuscriptCode("SH20260321001");
        entity.setTitle("稿件标题");
        entity.setMediaChannel("新华社/要闻");
        entity.setSubmitDepartment("总编室");
        entity.setAuthorName("张三");
        entity.setRemarkText("补充说明");
        entity.setContentBody("正文内容");
        entity.setInitiatorUserId(initiatorUserId);
        entity.setInitiatorName("张三");
        entity.setFlowStatusLabel(flowStatusLabel);
        entity.setCurrentNodeLabel(currentNodeLabel);
        entity.setFirstSubmitTime(new Date(1774054800000L));
        entity.setLatestSubmitTime(new Date(1774236153000L));
        entity.setUpdateTime(new Date(1774236153000L));
        return entity;
    }

    private static ManuscriptReviewHistoryEntity buildHistory(Long reviewId, Long actorUserId, String actionText) {
        return buildHistory(reviewId, actorUserId, "WORKFLOW", actionText, new Date(1774236000000L));
    }

    private static ManuscriptReviewHistoryEntity buildHistory(Long reviewId, Long actorUserId, String actionType, String actionText, Date createTime) {
        ManuscriptReviewHistoryEntity entity = new ManuscriptReviewHistoryEntity();
        entity.setReviewId(reviewId);
        entity.setActionType(actionType);
        entity.setActionText(actionText);
        entity.setActorUserId(actorUserId);
        entity.setActorName("李四");
        entity.setCreateTime(createTime);
        return entity;
    }

    private static ManuscriptReviewAttachmentEntity buildAttachment(Long id) {
        ManuscriptReviewAttachmentEntity entity = new ManuscriptReviewAttachmentEntity();
        entity.setId(id);
        entity.setReviewId(9306L);
        entity.setFileName("送审单.pdf");
        entity.setFileUrl("https://files.example/a.pdf");
        entity.setEnabled("1");
        entity.setIsVideo(false);
        entity.setCreateTime(new Date(1774234800000L));
        return entity;
    }

    private static ManuscriptReviewFlowConfigEntity buildFlowConfig() {
        ManuscriptReviewFlowConfigEntity entity = new ManuscriptReviewFlowConfigEntity();
        entity.setTenantId("000000");
        entity.setProcessType("AUDIT");
        entity.setLevelOneRoleKey("role:l1");
        entity.setLevelTwoRoleKey("role:l2");
        entity.setLevelThreeRoleKey("role:l3");
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
