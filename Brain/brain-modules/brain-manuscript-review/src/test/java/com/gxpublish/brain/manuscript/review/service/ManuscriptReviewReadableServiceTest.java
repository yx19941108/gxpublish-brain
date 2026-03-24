package com.gxpublish.brain.manuscript.review.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
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
import com.gxpublish.brain.manuscript.review.domain.entity.ManuscriptReviewExternalLinkEntity;
import com.gxpublish.brain.manuscript.review.domain.entity.ManuscriptReviewFlowConfigEntity;
import com.gxpublish.brain.manuscript.review.domain.entity.ManuscriptReviewHistoryEntity;
import com.gxpublish.brain.manuscript.review.domain.entity.ManuscriptReviewRecordEntity;
import com.gxpublish.brain.manuscript.review.domain.entity.ManuscriptReviewSystemRoleEntity;
import com.gxpublish.brain.manuscript.review.domain.entity.ManuscriptReviewSystemUserEntity;
import com.gxpublish.brain.manuscript.review.domain.entity.ManuscriptReviewSystemUserRoleEntity;
import com.gxpublish.brain.manuscript.review.domain.entity.ManuscriptReviewVideoMarkerEntity;
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
class ManuscriptReviewReadableServiceTest {

    @Test
    void shouldReturnEmptyReadableLedgerWhenDatabaseHasNoRows() {
        ReadableFixture fixture = new ReadableFixture(3001L);
        when(fixture.recordMapper.selectList(any())).thenReturn(List.of());

        TableDataInfo<ManuscriptReviewLedgerItemResponse> ledger = fixture.readableService.listLedger(new ManuscriptReviewLedgerQueryRequest());

        assertEquals(0, ledger.getTotal());
        assertTrue(ledger.getRows().isEmpty());
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
    void shouldAssembleReadableDetailWithFrozenFieldsAndCurrentResources() {
        ReadableFixture fixture = new ReadableFixture(3003L);
        when(fixture.recordMapper.selectById(9002L)).thenReturn(buildReviewRecord());
        when(fixture.attachmentMapper.selectList(any())).thenReturn(List.of(buildCurrentAttachment(), buildCurrentVideo(), buildHistoryAttachment()));
        when(fixture.externalLinkMapper.selectList(any())).thenReturn(List.of(buildCurrentLink(), buildHistoryLink()));
        when(fixture.historyMapper.selectList(any())).thenReturn(List.of(buildCreateHistory(), buildReturnHistory()));
        when(fixture.videoMarkerMapper.selectList(any())).thenReturn(List.of(buildCurrentMarker(), buildHistoryMarker()));

        ManuscriptReviewDetailResponse detail = fixture.readableService.getDetail(9002L);

        assertEquals(9002L, detail.getId());
        assertEquals("BACK", detail.getBusinessStatus());
        assertEquals("待发起人处理", detail.getCurrentNodeLabel());
        assertEquals("审核流程", detail.getProcessTypeLabel());
        assertEquals("新华社/要闻", detail.getMediaChannel());
        assertEquals("总编室", detail.getSubmitDepartment());
        assertEquals("张三、李四", detail.getAuthorName());
        assertEquals("正文内容", detail.getContentBody());
        assertEquals("2026-03-21 09:00:00", detail.getFirstSubmitTime());
        assertEquals(1, detail.getAttachmentList().size());
        assertEquals(1, detail.getExternalLinkList().size());
        assertEquals(1, detail.getVideoList().size());
        assertEquals(1, detail.getVideoMarkList().size());
        assertEquals(2, detail.getTimelineItems().size());
        assertEquals("张三新增了流程。", detail.getTimelineItems().get(0).getEventText());
        assertTrue(detail.getPermissionMatrix().isCanResubmit());
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

        assertTrue(detail.getPermissionMatrix().isCanGotoApproval());
        assertTrue(detail.getPermissionMatrix().isCanEdit());
        assertEquals("WAITING", detail.getBusinessStatus());
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
        entity.setMediaChannel("新华社/要闻");
        entity.setSubmitDepartment("总编室");
        entity.setAuthorName("张三、李四");
        entity.setRemarkText("补充说明");
        entity.setContentBody("正文内容");
        entity.setMediaChannelLabel("旧媒体/旧栏目");
        entity.setSubmitterDeptName("旧部门");
        entity.setAuthorNames("旧作者");
        entity.setContent("旧正文");
        entity.setNote("旧说明");
        entity.setFirstSubmitTime(new Date(1774054800000L));
        entity.setLatestSubmitTime(new Date(1774236153000L));
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
        entity.setEnabled("1");
        entity.setIsVideo(false);
        entity.setCreateTime(new Date(1774234800000L));
        return entity;
    }

    private static ManuscriptReviewAttachmentEntity buildCurrentVideo() {
        ManuscriptReviewAttachmentEntity entity = new ManuscriptReviewAttachmentEntity();
        entity.setId(2L);
        entity.setReviewId(9002L);
        entity.setFileName("样片.mp4");
        entity.setFileUrl("https://files.example/video.mp4");
        entity.setEnabled("1");
        entity.setIsVideo(true);
        entity.setVideoDurationSeconds(600);
        entity.setCreateTime(new Date(1774234860000L));
        return entity;
    }

    private static ManuscriptReviewAttachmentEntity buildHistoryAttachment() {
        ManuscriptReviewAttachmentEntity entity = new ManuscriptReviewAttachmentEntity();
        entity.setId(3L);
        entity.setReviewId(9002L);
        entity.setFileName("旧附件.pdf");
        entity.setFileUrl("https://files.example/old.pdf");
        entity.setEnabled("0");
        entity.setIsVideo(false);
        entity.setDisabledTime(new Date(1774235100000L));
        return entity;
    }

    private static ManuscriptReviewExternalLinkEntity buildCurrentLink() {
        ManuscriptReviewExternalLinkEntity entity = new ManuscriptReviewExternalLinkEntity();
        entity.setId(10L);
        entity.setReviewId(9002L);
        entity.setLinkTitle("素材参考");
        entity.setLinkUrl("https://example.com/ref");
        entity.setEnabled("1");
        entity.setCreateTime(new Date(1774234920000L));
        return entity;
    }

    private static ManuscriptReviewExternalLinkEntity buildHistoryLink() {
        ManuscriptReviewExternalLinkEntity entity = new ManuscriptReviewExternalLinkEntity();
        entity.setId(11L);
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
        entity.setId(20L);
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
        entity.setId(21L);
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
