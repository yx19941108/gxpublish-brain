package com.gxpublish.brain.manuscript.review.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
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
import com.gxpublish.brain.manuscript.review.domain.command.AddManuscriptReviewResourceCommand;
import com.gxpublish.brain.manuscript.review.domain.command.AddManuscriptReviewVideoMarkCommand;
import com.gxpublish.brain.manuscript.review.domain.command.CreateManuscriptReviewCommand;
import com.gxpublish.brain.manuscript.review.domain.command.DisableManuscriptReviewResourceCommand;
import com.gxpublish.brain.manuscript.review.domain.command.ResubmitManuscriptReviewCommand;
import com.gxpublish.brain.manuscript.review.domain.command.UpdateManuscriptReviewCommand;
import com.gxpublish.brain.manuscript.review.domain.entity.ManuscriptReviewAttachmentEntity;
import com.gxpublish.brain.manuscript.review.domain.entity.ManuscriptReviewFlowConfigEntity;
import com.gxpublish.brain.manuscript.review.domain.entity.ManuscriptReviewRecordEntity;
import com.gxpublish.brain.manuscript.review.domain.entity.ManuscriptReviewSystemRoleEntity;
import com.gxpublish.brain.manuscript.review.domain.entity.ManuscriptReviewSystemUserEntity;
import com.gxpublish.brain.manuscript.review.domain.entity.ManuscriptReviewSystemUserRoleEntity;
import com.gxpublish.brain.manuscript.review.domain.entity.ManuscriptReviewVideoMarkerEntity;
import com.gxpublish.brain.manuscript.review.domain.enums.ManuscriptReviewProcessType;
import com.gxpublish.brain.manuscript.review.gateway.ManuscriptReviewCurrentUserGateway;
import com.gxpublish.brain.manuscript.review.gateway.ManuscriptReviewSerialGateway;
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
    void shouldPersistCreateRecordWithFrozenWriteFieldsOnly() {
        ServiceFixture fixture = new ServiceFixture(1001L, "000000", 2001L, "张三");
        when(fixture.recordMapper.selectCount(any())).thenReturn(0L);

        Long reviewId = fixture.service.create(CreateManuscriptReviewCommand.builder()
            .processType(ManuscriptReviewProcessType.AUDIT)
            .externalManuscriptCode("EXT-001")
            .title("审校稿件")
            .mediaChannel("新华社/要闻")
            .submitDepartment("总编室")
            .authorName("张三、李四")
            .remark("需重点核验")
            .contentBody("正文内容")
            .build());

        assertEquals(9001L, reviewId);

        ArgumentCaptor<ManuscriptReviewRecordEntity> captor = ArgumentCaptor.forClass(ManuscriptReviewRecordEntity.class);
        verify(fixture.recordMapper).insert(captor.capture());
        ManuscriptReviewRecordEntity entity = captor.getValue();
        assertEquals(9001L, entity.getId());
        assertEquals("000000", entity.getTenantId());
        assertEquals("AUDIT", entity.getProcessType());
        assertEquals("EXT-001", entity.getExternalManuscriptCode());
        assertEquals("审校稿件", entity.getTitle());
        assertEquals("新华社/要闻", entity.getMediaChannel());
        assertEquals("总编室", entity.getSubmitDepartment());
        assertEquals("张三、李四", entity.getAuthorName());
        assertEquals("需重点核验", entity.getRemarkText());
        assertEquals("正文内容", entity.getContentBody());
        assertEquals(1001L, entity.getInitiatorUserId());
        assertEquals("张三", entity.getInitiatorName());
        assertEquals(2001L, entity.getCreateDept());
        assertEquals(1001L, entity.getCreateBy());
        assertEquals(1001L, entity.getUpdateBy());
        assertNull(entity.getManuscriptCode());
        assertNull(entity.getFirstSubmitTime());
        assertNull(entity.getLatestSubmitTime());
        assertNull(entity.getMediaChannelLabel());
        assertNull(entity.getSubmitterDeptName());
        assertNull(entity.getAuthorNames());
        assertNull(entity.getContent());
        assertNull(entity.getNote());
    }

    @Test
    void shouldRejectUpdateWhenSubmitDepartmentChanges() {
        ServiceFixture fixture = new ServiceFixture(1002L, "000000", 2001L, "李四");
        ManuscriptReviewRecordEntity existing = buildRecord(9001L);
        existing.setSubmitDepartment("总编室");
        when(fixture.recordMapper.selectById(9001L)).thenReturn(existing);
        when(fixture.recordMapper.selectCount(any())).thenReturn(0L);

        ServiceException exception = assertThrows(ServiceException.class, () -> fixture.service.update(UpdateManuscriptReviewCommand.builder()
            .id(9001L)
            .processType(ManuscriptReviewProcessType.AUDIT)
            .externalManuscriptCode("EXT-001")
            .title("审校稿件")
            .mediaChannel("新华社/要闻")
            .submitDepartment("采访中心")
            .authorName("张三")
            .remark("备注")
            .contentBody("正文")
            .build()));

        assertEquals("报送部门不允许修改", exception.getMessage());
        verify(fixture.recordMapper, never()).updateById(any(ManuscriptReviewRecordEntity.class));
    }

    @Test
    void shouldRejectCreateWhenExternalCodeDuplicated() {
        ServiceFixture fixture = new ServiceFixture(1003L, "000000", 2001L, "王五");
        when(fixture.recordMapper.selectCount(any())).thenReturn(1L);

        ServiceException exception = assertThrows(ServiceException.class, () -> fixture.service.create(CreateManuscriptReviewCommand.builder()
            .processType(ManuscriptReviewProcessType.AUDIT)
            .externalManuscriptCode("EXT-001")
            .title("重复编号稿件")
            .mediaChannel("新华社/要闻")
            .submitDepartment("总编室")
            .authorName("张三")
            .remark("备注")
            .contentBody("正文")
            .build()));

        assertEquals("外部稿件编号已存在", exception.getMessage());
    }

    @Test
    void shouldRejectCreateWhenAuthorDelimiterInvalid() {
        ServiceFixture fixture = new ServiceFixture(1004L, "000000", 2001L, "赵六");

        ServiceException exception = assertThrows(ServiceException.class, () -> fixture.service.create(CreateManuscriptReviewCommand.builder()
            .processType(ManuscriptReviewProcessType.AUDIT)
            .title("分隔符错误稿件")
            .mediaChannel("新华社/要闻")
            .submitDepartment("总编室")
            .authorName("张三,李四")
            .remark("备注")
            .contentBody("正文")
            .build()));

        assertEquals("作者仅允许使用中文顿号分隔", exception.getMessage());
    }

    @Test
    void shouldGenerateCodeAndWriteFirstAndLatestSubmitTimeWhenSubmitStarts() {
        ServiceFixture fixture = new ServiceFixture(1005L, "000000", 2001L, "张三");
        ManuscriptReviewRecordEntity existing = buildRecord(9002L);
        existing.setProcessType("AUDIT");
        when(fixture.recordMapper.selectById(9002L)).thenReturn(existing);
        when(fixture.attachmentMapper.selectList(any())).thenReturn(List.of(buildVideoAttachment(7001L, true, 600)));
        when(fixture.externalLinkMapper.selectList(any())).thenReturn(List.of());
        when(fixture.serialGateway.nextSerial(ManuscriptReviewProcessType.AUDIT, "20260321")).thenReturn(7);
        fixture.stubSubmitRouting(false);

        String manuscriptCode = fixture.service.submitAndFlowStart(9002L);

        assertEquals("SH20260321007", manuscriptCode);
        ArgumentCaptor<ManuscriptReviewRecordEntity> captor = ArgumentCaptor.forClass(ManuscriptReviewRecordEntity.class);
        verify(fixture.recordMapper).updateById(captor.capture());
        ManuscriptReviewRecordEntity updated = captor.getValue();
        assertEquals(9002L, updated.getId());
        assertEquals("SH20260321007", updated.getManuscriptCode());
        assertNotNull(updated.getFirstSubmitTime());
        assertNotNull(updated.getLatestSubmitTime());
        assertEquals(updated.getFirstSubmitTime(), updated.getLatestSubmitTime());
    }

    @Test
    void shouldRefreshOnlyLatestSubmitTimeWhenResubmit() {
        ServiceFixture fixture = new ServiceFixture(1006L, "000000", 2001L, "张三");
        ManuscriptReviewRecordEntity existing = buildRecord(9003L);
        existing.setProcessType("AUDIT");
        existing.setManuscriptCode("SH20260321008");
        existing.setInitiatorUserId(1006L);
        existing.setFlowStatusLabel("已退回");
        existing.setCurrentNodeLabel("待发起人处理");
        Date firstSubmitTime = new Date(1774060800000L);
        existing.setFirstSubmitTime(firstSubmitTime);
        existing.setLatestSubmitTime(new Date(1774064400000L));
        when(fixture.recordMapper.selectById(9003L)).thenReturn(existing);
        when(fixture.attachmentMapper.selectList(any())).thenReturn(List.of(buildAttachment(7002L)));
        when(fixture.externalLinkMapper.selectList(any())).thenReturn(List.of());
        fixture.stubSubmitRouting(false);

        fixture.service.resubmit(ResubmitManuscriptReviewCommand.builder().reviewId(9003L).build());

        ArgumentCaptor<ManuscriptReviewRecordEntity> captor = ArgumentCaptor.forClass(ManuscriptReviewRecordEntity.class);
        verify(fixture.recordMapper).updateById(captor.capture());
        ManuscriptReviewRecordEntity updated = captor.getValue();
        assertEquals("SH20260321008", updated.getManuscriptCode());
        assertEquals(firstSubmitTime, updated.getFirstSubmitTime());
        assertNotNull(updated.getLatestSubmitTime());
    }

    @Test
    void shouldRejectSubmitWhenNoEffectiveResourceExists() {
        ServiceFixture fixture = new ServiceFixture(1007L, "000000", 2001L, "张三");
        when(fixture.recordMapper.selectById(9004L)).thenReturn(buildRecord(9004L));
        when(fixture.attachmentMapper.selectList(any())).thenReturn(List.of());
        when(fixture.externalLinkMapper.selectList(any())).thenReturn(List.of());

        ServiceException exception = assertThrows(ServiceException.class, () -> fixture.service.submitAndFlowStart(9004L));

        assertEquals("稿件至少需要一种有效资源", exception.getMessage());
        verify(fixture.recordMapper, never()).updateById(any(ManuscriptReviewRecordEntity.class));
    }

    @Test
    void shouldAddVideoResourceWithoutReusingExternalLinkSemantics() {
        ServiceFixture fixture = new ServiceFixture(1008L, "000000", 2001L, "张三");
        when(fixture.recordMapper.selectById(9005L)).thenReturn(buildRecord(9005L));

        Long resourceId = fixture.service.addResource(AddManuscriptReviewResourceCommand.builder()
            .reviewId(9005L)
            .resourceType("VIDEO")
            .displayName("样片.mp4")
            .ossId(8801L)
            .build());

        assertEquals(9001L, resourceId);
        ArgumentCaptor<ManuscriptReviewAttachmentEntity> captor = ArgumentCaptor.forClass(ManuscriptReviewAttachmentEntity.class);
        verify(fixture.attachmentMapper).insert(captor.capture());
        ManuscriptReviewAttachmentEntity entity = captor.getValue();
        assertEquals(9001L, entity.getId());
        assertEquals(9005L, entity.getReviewId());
        assertEquals(8801L, entity.getOssId());
        assertEquals("样片.mp4", entity.getFileName());
        assertEquals(Boolean.TRUE, entity.getIsVideo());
        assertEquals("1", entity.getEnabled());
    }

    @Test
    void shouldDisableResourceByFlagInsteadOfDeletingRow() {
        ServiceFixture fixture = new ServiceFixture(1009L, "000000", 2001L, "张三");
        ManuscriptReviewAttachmentEntity attachment = buildAttachment(7003L);
        when(fixture.attachmentMapper.selectById(7003L)).thenReturn(attachment);
        when(fixture.externalLinkMapper.selectById(7003L)).thenReturn(null);

        fixture.service.disableResource(DisableManuscriptReviewResourceCommand.builder()
            .resourceId(7003L)
            .disabledReason("替换新版本附件")
            .build());

        ArgumentCaptor<ManuscriptReviewAttachmentEntity> captor = ArgumentCaptor.forClass(ManuscriptReviewAttachmentEntity.class);
        verify(fixture.attachmentMapper).updateById(captor.capture());
        ManuscriptReviewAttachmentEntity updated = captor.getValue();
        assertEquals("0", updated.getEnabled());
        assertEquals(1009L, updated.getDisabledBy());
        assertNotNull(updated.getDisabledTime());
        assertEquals("替换新版本附件", updated.getRemark());
    }

    @Test
    void shouldNormalizeVideoMarkTimeAndPersistSeconds() {
        ServiceFixture fixture = new ServiceFixture(1010L, "000000", 2001L, "张三");
        when(fixture.recordMapper.selectById(9006L)).thenReturn(buildRecord(9006L));
        when(fixture.attachmentMapper.selectById(7004L)).thenReturn(buildVideoAttachment(7004L, true, 600));

        Long markerId = fixture.service.addVideoMark(AddManuscriptReviewVideoMarkCommand.builder()
            .reviewId(9006L)
            .resourceId(7004L)
            .startTimeText("5")
            .endTimeText("01:10")
            .markContent("第一处问题")
            .build());

        assertEquals(9001L, markerId);
        ArgumentCaptor<ManuscriptReviewVideoMarkerEntity> captor = ArgumentCaptor.forClass(ManuscriptReviewVideoMarkerEntity.class);
        verify(fixture.videoMarkerMapper).insert(captor.capture());
        ManuscriptReviewVideoMarkerEntity entity = captor.getValue();
        assertEquals("00:00:05", entity.getStartTime());
        assertEquals("00:01:10", entity.getEndTime());
        assertEquals(5, entity.getStartSeconds());
        assertEquals(70, entity.getEndSeconds());
        assertEquals("第一处问题", entity.getMarkerNote());
    }

    @Test
    void shouldRejectVideoMarkWhenDurationMissing() {
        ServiceFixture fixture = new ServiceFixture(1011L, "000000", 2001L, "张三");
        when(fixture.recordMapper.selectById(9007L)).thenReturn(buildRecord(9007L));
        when(fixture.attachmentMapper.selectById(7005L)).thenReturn(buildVideoAttachment(7005L, true, null));

        ServiceException exception = assertThrows(ServiceException.class, () -> fixture.service.addVideoMark(AddManuscriptReviewVideoMarkCommand.builder()
            .reviewId(9007L)
            .resourceId(7005L)
            .startTimeText("00:00:05")
            .endTimeText("00:00:12")
            .markContent("缺少时长")
            .build()));

        assertEquals("视频总时长未识别", exception.getMessage());
    }

    private static ManuscriptReviewRecordEntity buildRecord(Long reviewId) {
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
        entity.setReviewId(9006L);
        entity.setFileName("附件.pdf");
        entity.setEnabled("1");
        entity.setIsVideo(false);
        return entity;
    }

    private static ManuscriptReviewAttachmentEntity buildVideoAttachment(Long resourceId, boolean enabled, Integer durationSeconds) {
        ManuscriptReviewAttachmentEntity entity = new ManuscriptReviewAttachmentEntity();
        entity.setId(resourceId);
        entity.setReviewId(9006L);
        entity.setFileName("样片.mp4");
        entity.setEnabled(enabled ? "1" : "0");
        entity.setIsVideo(true);
        entity.setVideoDurationSeconds(durationSeconds);
        return entity;
    }

    private static Clock fixedBusinessClock() {
        return Clock.fixed(
            LocalDate.of(2026, 3, 21).atStartOfDay(BUSINESS_ZONE_ID).toInstant(),
            BUSINESS_ZONE_ID
        );
    }

    private static final class ServiceFixture {

        private final ManuscriptReviewRecordMapper recordMapper = mock(ManuscriptReviewRecordMapper.class);
        private final ManuscriptReviewAttachmentMapper attachmentMapper = mock(ManuscriptReviewAttachmentMapper.class);
        private final ManuscriptReviewExternalLinkMapper externalLinkMapper = mock(ManuscriptReviewExternalLinkMapper.class);
        private final ManuscriptReviewVideoMarkerMapper videoMarkerMapper = mock(ManuscriptReviewVideoMarkerMapper.class);
        private final ManuscriptReviewHistoryMapper historyMapper = mock(ManuscriptReviewHistoryMapper.class);
        private final ManuscriptReviewFlowConfigMapper flowConfigMapper = mock(ManuscriptReviewFlowConfigMapper.class);
        private final ManuscriptReviewSystemRoleMapper roleMapper = mock(ManuscriptReviewSystemRoleMapper.class);
        private final ManuscriptReviewSystemUserRoleMapper userRoleMapper = mock(ManuscriptReviewSystemUserRoleMapper.class);
        private final ManuscriptReviewSystemUserMapper userMapper = mock(ManuscriptReviewSystemUserMapper.class);
        private final ManuscriptReviewSerialGateway serialGateway = mock(ManuscriptReviewSerialGateway.class);
        private final ManuscriptReviewCurrentUserGateway currentUserGateway = mock(ManuscriptReviewCurrentUserGateway.class);
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

            ManuscriptReviewSystemRoleEntity levelOneRole = new ManuscriptReviewSystemRoleEntity();
            levelOneRole.setRoleId(7101L);
            levelOneRole.setTenantId("000000");
            levelOneRole.setRoleKey("manuscript_review_level_1_approver");
            levelOneRole.setStatus("0");
            levelOneRole.setDelFlag("0");
            ManuscriptReviewSystemRoleEntity levelTwoRole = new ManuscriptReviewSystemRoleEntity();
            levelTwoRole.setRoleId(7102L);
            levelTwoRole.setTenantId("000000");
            levelTwoRole.setRoleKey("manuscript_review_level_2_approver");
            levelTwoRole.setStatus("0");
            levelTwoRole.setDelFlag("0");
            ManuscriptReviewSystemRoleEntity levelThreeRole = new ManuscriptReviewSystemRoleEntity();
            levelThreeRole.setRoleId(7103L);
            levelThreeRole.setTenantId("000000");
            levelThreeRole.setRoleKey("manuscript_review_level_3_approver");
            levelThreeRole.setStatus("0");
            levelThreeRole.setDelFlag("0");
            ManuscriptReviewSystemRoleEntity certifiedRole = new ManuscriptReviewSystemRoleEntity();
            certifiedRole.setRoleId(7104L);
            certifiedRole.setTenantId("000000");
            certifiedRole.setRoleKey("manuscript_review_certified_initiator");
            certifiedRole.setStatus("0");
            certifiedRole.setDelFlag("0");
            when(roleMapper.selectOne(any())).thenReturn(levelOneRole, levelTwoRole, levelThreeRole, certifiedRole);

            ManuscriptReviewSystemUserRoleEntity levelOneUserRole = new ManuscriptReviewSystemUserRoleEntity();
            levelOneUserRole.setUserId(8101L);
            levelOneUserRole.setRoleId(7101L);
            ManuscriptReviewSystemUserRoleEntity levelTwoUserRole = new ManuscriptReviewSystemUserRoleEntity();
            levelTwoUserRole.setUserId(8102L);
            levelTwoUserRole.setRoleId(7102L);
            ManuscriptReviewSystemUserRoleEntity levelThreeUserRole = new ManuscriptReviewSystemUserRoleEntity();
            levelThreeUserRole.setUserId(8103L);
            levelThreeUserRole.setRoleId(7103L);
            ManuscriptReviewSystemUserRoleEntity certifiedUserRole = new ManuscriptReviewSystemUserRoleEntity();
            certifiedUserRole.setUserId(currentUserGateway.getCurrentUserId());
            certifiedUserRole.setRoleId(7104L);
            when(userRoleMapper.selectList(any())).thenReturn(
                List.of(levelOneUserRole),
                List.of(levelTwoUserRole),
                List.of(levelThreeUserRole),
                certifiedCurrentUser ? List.of(certifiedUserRole) : List.of()
            );

            ManuscriptReviewSystemUserEntity levelOneUser = new ManuscriptReviewSystemUserEntity();
            levelOneUser.setUserId(8101L);
            levelOneUser.setTenantId("000000");
            levelOneUser.setStatus("0");
            levelOneUser.setDelFlag("0");
            ManuscriptReviewSystemUserEntity levelTwoUser = new ManuscriptReviewSystemUserEntity();
            levelTwoUser.setUserId(8102L);
            levelTwoUser.setTenantId("000000");
            levelTwoUser.setStatus("0");
            levelTwoUser.setDelFlag("0");
            ManuscriptReviewSystemUserEntity levelThreeUser = new ManuscriptReviewSystemUserEntity();
            levelThreeUser.setUserId(8103L);
            levelThreeUser.setTenantId("000000");
            levelThreeUser.setStatus("0");
            levelThreeUser.setDelFlag("0");
            ManuscriptReviewSystemUserEntity certifiedUser = new ManuscriptReviewSystemUserEntity();
            certifiedUser.setUserId(currentUserGateway.getCurrentUserId());
            certifiedUser.setTenantId("000000");
            certifiedUser.setStatus("0");
            certifiedUser.setDelFlag("0");
            when(userMapper.selectList(any())).thenReturn(
                List.of(levelOneUser),
                List.of(levelTwoUser),
                List.of(levelThreeUser),
                certifiedCurrentUser ? List.of(certifiedUser) : List.of()
            );
        }
    }
}
