package com.gxpublish.brain.manuscript.review.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Arrays;
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
import com.gxpublish.brain.manuscript.review.domain.command.AddManuscriptReviewResourceCommand;
import com.gxpublish.brain.manuscript.review.domain.command.AddManuscriptReviewVideoMarkCommand;
import com.gxpublish.brain.manuscript.review.domain.command.CreateManuscriptReviewCommand;
import com.gxpublish.brain.manuscript.review.domain.command.DisableManuscriptReviewResourceCommand;
import com.gxpublish.brain.manuscript.review.domain.command.DisableManuscriptReviewVideoMarkCommand;
import com.gxpublish.brain.manuscript.review.domain.command.ResubmitManuscriptReviewCommand;
import com.gxpublish.brain.manuscript.review.domain.command.UpdateManuscriptReviewCommand;
import com.gxpublish.brain.manuscript.review.domain.entity.ManuscriptReviewAttachmentEntity;
import com.gxpublish.brain.manuscript.review.domain.entity.ManuscriptReviewExternalLinkEntity;
import com.gxpublish.brain.manuscript.review.domain.entity.ManuscriptReviewFlowConfigEntity;
import com.gxpublish.brain.manuscript.review.domain.entity.ManuscriptReviewHistoryEntity;
import com.gxpublish.brain.manuscript.review.domain.entity.ManuscriptReviewRecordEntity;
import com.gxpublish.brain.manuscript.review.domain.entity.ManuscriptReviewSystemRoleEntity;
import com.gxpublish.brain.manuscript.review.domain.entity.ManuscriptReviewSysOssEntity;
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
import com.gxpublish.brain.manuscript.review.mapper.ManuscriptReviewSysOssMapper;
import com.gxpublish.brain.manuscript.review.mapper.ManuscriptReviewSystemUserMapper;
import com.gxpublish.brain.manuscript.review.mapper.ManuscriptReviewSystemUserRoleMapper;
import com.gxpublish.brain.manuscript.review.mapper.ManuscriptReviewVideoMarkerMapper;
import com.gxpublish.brain.workflow.domain.bo.FlowCancelBo;
import com.gxpublish.brain.workflow.service.IFlwInstanceService;

@Tag("dev")
class ManuscriptReviewServiceTest {

    private static final ZoneId BUSINESS_ZONE_ID = ZoneId.of("Asia/Shanghai");

    @Test
    void shouldExposePendingResourceDeletionMethodForSecondRoundIssueOne() {
        assertTrue(
            Arrays.stream(ManuscriptReviewService.class.getDeclaredMethods())
                .anyMatch(method -> "deletePendingResource".equals(method.getName())
                    && method.getParameterCount() == 1
                    && Long.class.equals(method.getParameterTypes()[0])),
            "round-2 issue 1 should be implemented through ManuscriptReviewService.deletePendingResource(Long)"
        );
    }

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
        assertEquals("manuscript_review_audit_flow", entity.getFlowCode());
        assertEquals("", entity.getFlowStatusLabel());
        assertEquals("", entity.getCurrentNodeLabel());
        assertNull(entity.getManuscriptCode());
        assertNull(entity.getFlowInstanceId());
        assertNull(entity.getFirstSubmitTime());
        assertNull(entity.getLatestSubmitTime());
        assertNull(entity.getMediaChannelLabel());
        assertNull(entity.getSubmitterDeptName());
        assertNull(entity.getAuthorNames());
        assertNull(entity.getContent());
        assertNull(entity.getNote());

        ArgumentCaptor<ManuscriptReviewHistoryEntity> historyCaptor = ArgumentCaptor.forClass(ManuscriptReviewHistoryEntity.class);
        verify(fixture.historyMapper).insert(historyCaptor.capture());
        assertEquals("CREATE", historyCaptor.getValue().getActionType());
        assertEquals("张三新增了审校流程单《审校稿件》。", historyCaptor.getValue().getActionText());
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
    void shouldRejectUpdateWhenProcessTypeChangesAfterCreation() {
        ServiceFixture fixture = new ServiceFixture(1002L, "000000", 2001L, "镜");
        ManuscriptReviewRecordEntity existing = buildRecord(9009L);
        existing.setInitiatorUserId(1002L);
        existing.setProcessType("AUDIT");
        existing.setFlowCode("manuscript_review_audit_flow");
        when(fixture.recordMapper.selectById(9009L)).thenReturn(existing);
        when(fixture.recordMapper.selectCount(any())).thenReturn(0L);

        ServiceException exception = assertThrows(ServiceException.class, () -> fixture.service.update(UpdateManuscriptReviewCommand.builder()
            .id(9009L)
            .processType(ManuscriptReviewProcessType.PROOFREAD)
            .externalManuscriptCode("EXT-009")
            .title("process type immutable")
            .mediaChannel("channel")
            .submitDepartment(existing.getSubmitDepartment())
            .authorName("author")
            .remark("remark")
            .contentBody("content")
            .build()));

        assertEquals("流程类型创建后不允许修改", exception.getMessage());
        verify(fixture.recordMapper, never()).updateById(any(ManuscriptReviewRecordEntity.class));
    }

    @Test
    void shouldKeepExistingFlowCodeWhenUpdateSavesMainForm() {
        ServiceFixture fixture = new ServiceFixture(1001L, "000000", 2001L, "张");
        ManuscriptReviewRecordEntity existing = buildRecord(9010L);
        existing.setProcessType("AUDIT");
        existing.setFlowCode("manuscript_review_custom_locked_flow");
        when(fixture.recordMapper.selectById(9010L)).thenReturn(existing);
        when(fixture.recordMapper.selectCount(any())).thenReturn(0L);

        fixture.service.update(UpdateManuscriptReviewCommand.builder()
            .id(9010L)
            .processType(ManuscriptReviewProcessType.AUDIT)
            .externalManuscriptCode("EXT-010")
            .title("keep existing flow code")
            .mediaChannel("channel")
            .submitDepartment(existing.getSubmitDepartment())
            .authorName("author")
            .remark("remark")
            .contentBody("content")
            .build());

        ArgumentCaptor<ManuscriptReviewRecordEntity> entityCaptor = ArgumentCaptor.forClass(ManuscriptReviewRecordEntity.class);
        verify(fixture.recordMapper).updateById(entityCaptor.capture());
        assertEquals("manuscript_review_custom_locked_flow", entityCaptor.getValue().getFlowCode());
    }

    @Test
    void shouldWriteUpdateHistoryWithReadableDiffSummary() {
        ServiceFixture fixture = new ServiceFixture(1001L, "000000", 2001L, "张三");
        ManuscriptReviewRecordEntity existing = buildRecord(9008L);
        existing.setExternalManuscriptCode("EXT-OLD");
        existing.setTitle("旧标题");
        existing.setMediaChannel("旧栏目");
        existing.setAuthorName("旧作者");
        existing.setRemarkText("旧说明");
        existing.setContentBody("旧正文");
        when(fixture.recordMapper.selectById(9008L)).thenReturn(existing);
        when(fixture.recordMapper.selectCount(any())).thenReturn(0L);

        fixture.service.update(UpdateManuscriptReviewCommand.builder()
            .id(9008L)
            .processType(ManuscriptReviewProcessType.AUDIT)
            .externalManuscriptCode("EXT-NEW")
            .title("新标题")
            .mediaChannel("新栏目")
            .submitDepartment("总编室")
            .authorName("新作者")
            .remark("新说明")
            .contentBody("新正文")
            .build());

        ArgumentCaptor<ManuscriptReviewHistoryEntity> historyCaptor = ArgumentCaptor.forClass(ManuscriptReviewHistoryEntity.class);
        verify(fixture.historyMapper).insert(historyCaptor.capture());
        assertEquals("UPDATE", historyCaptor.getValue().getActionType());
        assertTrue(historyCaptor.getValue().getActionText().contains("标题由“旧标题”改为“新标题”"));
        assertTrue(historyCaptor.getValue().getActionText().contains("媒体/栏目由“旧栏目”改为“新栏目”"));
        assertTrue(historyCaptor.getValue().getActionText().contains("正文由“旧正文”改为“新正文”"));
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
        assertEquals(99001L, updated.getFlowInstanceId());
        assertNotNull(updated.getFirstSubmitTime());
        assertNotNull(updated.getLatestSubmitTime());
        assertEquals(updated.getFirstSubmitTime(), updated.getLatestSubmitTime());
        ArgumentCaptor<StartProcessDTO> startProcessCaptor = ArgumentCaptor.forClass(StartProcessDTO.class);
        verify(fixture.workflowService).startCompleteTask(startProcessCaptor.capture());
        StartProcessDTO startProcess = startProcessCaptor.getValue();
        assertEquals("9002", startProcess.getBusinessId());
        assertEquals("manuscript_review_audit_flow", startProcess.getFlowCode());
        assertTrue(Boolean.TRUE.equals(startProcess.getVariables().get("ignore")));
        assertEquals(Boolean.FALSE, startProcess.getVariables().get("isCertified"));
        assertEquals("role:7101", startProcess.getVariables().get("manuscriptReviewFirstLevelApprover"));
        assertEquals("role:7102", startProcess.getVariables().get("manuscriptReviewSecondLevelApprover"));
        assertEquals("role:7103", startProcess.getVariables().get("manuscriptReviewThirdLevelApprover"));
        assertEquals("SH20260321007", startProcess.getBizExt().getBusinessCode());
        assertEquals("稿件标题", startProcess.getBizExt().getBusinessTitle());
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
        assertEquals(99001L, updated.getFlowInstanceId());
        assertEquals(firstSubmitTime, updated.getFirstSubmitTime());
        assertNotNull(updated.getLatestSubmitTime());
        verify(fixture.workflowService).startCompleteTask(any(StartProcessDTO.class));
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
        ManuscriptReviewRecordEntity record = buildRecord(9005L);
        record.setInitiatorUserId(1008L);
        when(fixture.recordMapper.selectById(9005L)).thenReturn(record);
        when(fixture.sysOssMapper.selectById(8801L)).thenReturn(buildSysOss(8801L, "https://files.example/video.mp4", "video/mp4", 601));

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
        assertEquals("https://files.example/video.mp4", entity.getFileUrl());
        assertEquals(Boolean.TRUE, entity.getIsVideo());
        assertEquals(601, entity.getVideoDurationSeconds());
        assertEquals("1", entity.getEnabled());

        ArgumentCaptor<ManuscriptReviewHistoryEntity> historyCaptor = ArgumentCaptor.forClass(ManuscriptReviewHistoryEntity.class);
        verify(fixture.historyMapper).insert(historyCaptor.capture());
        assertEquals("RESOURCE_ADD", historyCaptor.getValue().getActionType());
        assertEquals("张三上传了视频《样片.mp4》。", historyCaptor.getValue().getActionText());
    }

    @Test
    void shouldRejectAddResourceWhenCurrentUserHasNoModifyPermission() {
        ServiceFixture fixture = new ServiceFixture(1015L, "000000", 2001L, "李四");
        when(fixture.recordMapper.selectById(9011L)).thenReturn(buildPendingApprovalRecord(9011L, 2001L));
        fixture.stubSubmitRouting(false);

        ServiceException exception = assertThrows(ServiceException.class, () -> fixture.service.addResource(AddManuscriptReviewResourceCommand.builder()
            .reviewId(9011L)
            .resourceType("VIDEO")
            .displayName("forbidden.mp4")
            .ossId(8802L)
            .build()));

        assertEquals("当前用户无权修改该流程", exception.getMessage());
        verify(fixture.attachmentMapper, never()).insert(any(ManuscriptReviewAttachmentEntity.class));
        verify(fixture.externalLinkMapper, never()).insert(any(ManuscriptReviewExternalLinkEntity.class));
        verify(fixture.historyMapper, never()).insert(any(ManuscriptReviewHistoryEntity.class));
    }

    @Test
    void shouldWriteExternalLinkHistoryWhenAddingResource() {
        ServiceFixture fixture = new ServiceFixture(1012L, "000000", 2001L, "张三");
        ManuscriptReviewRecordEntity record = buildRecord(9010L);
        record.setInitiatorUserId(1012L);
        when(fixture.recordMapper.selectById(9010L)).thenReturn(record);
        when(fixture.externalLinkMapper.selectCount(any())).thenReturn(0L);

        fixture.service.addResource(AddManuscriptReviewResourceCommand.builder()
            .reviewId(9010L)
            .resourceType("EXTERNAL_LINK")
            .displayName("素材参考")
            .externalUrl("https://example.com/ref")
            .build());

        ArgumentCaptor<ManuscriptReviewHistoryEntity> historyCaptor = ArgumentCaptor.forClass(ManuscriptReviewHistoryEntity.class);
        verify(fixture.historyMapper).insert(historyCaptor.capture());
        assertEquals("RESOURCE_ADD", historyCaptor.getValue().getActionType());
        assertEquals("张三新增了外链《素材参考》。", historyCaptor.getValue().getActionText());
    }

    @Test
    void shouldDisableResourceByFlagInsteadOfDeletingRow() {
        ServiceFixture fixture = new ServiceFixture(1009L, "000000", 2001L, "张三");
        ManuscriptReviewAttachmentEntity attachment = buildAttachment(7003L);
        when(fixture.attachmentMapper.selectById(7003L)).thenReturn(attachment);
        when(fixture.externalLinkMapper.selectById(7003L)).thenReturn(null);
        ManuscriptReviewRecordEntity record = buildRecord(attachment.getReviewId());
        record.setInitiatorUserId(1009L);
        when(fixture.recordMapper.selectById(attachment.getReviewId())).thenReturn(record);

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

        ArgumentCaptor<ManuscriptReviewHistoryEntity> historyCaptor = ArgumentCaptor.forClass(ManuscriptReviewHistoryEntity.class);
        verify(fixture.historyMapper).insert(historyCaptor.capture());
        assertEquals("RESOURCE_DISABLE", historyCaptor.getValue().getActionType());
        assertEquals("张三停用了附件《附件.pdf》。", historyCaptor.getValue().getActionText());
    }

    @Test
    void shouldRejectDisableResourceWhenCurrentUserHasNoModifyPermission() {
        ServiceFixture fixture = new ServiceFixture(1016L, "000000", 2001L, "王五");
        ManuscriptReviewAttachmentEntity attachment = buildAttachment(7006L);
        attachment.setReviewId(9012L);
        when(fixture.attachmentMapper.selectById(7006L)).thenReturn(attachment);
        when(fixture.externalLinkMapper.selectById(7006L)).thenReturn(null);
        when(fixture.recordMapper.selectById(9012L)).thenReturn(buildPendingApprovalRecord(9012L, 2002L));
        fixture.stubSubmitRouting(false);

        ServiceException exception = assertThrows(ServiceException.class, () -> fixture.service.disableResource(DisableManuscriptReviewResourceCommand.builder()
            .resourceId(7006L)
            .disabledReason("no permission")
            .build()));

        assertEquals("当前用户无权修改该流程", exception.getMessage());
        verify(fixture.attachmentMapper, never()).updateById(any(ManuscriptReviewAttachmentEntity.class));
        verify(fixture.externalLinkMapper, never()).updateById(any(ManuscriptReviewExternalLinkEntity.class));
        verify(fixture.historyMapper, never()).insert(any(ManuscriptReviewHistoryEntity.class));
    }

    @Test
    void shouldWriteExternalLinkDisableHistory() {
        ServiceFixture fixture = new ServiceFixture(1013L, "000000", 2001L, "张三");
        ManuscriptReviewExternalLinkEntity externalLink = buildExternalLink(7101L);
        when(fixture.attachmentMapper.selectById(7101L)).thenReturn(null);
        when(fixture.externalLinkMapper.selectById(7101L)).thenReturn(externalLink);
        ManuscriptReviewRecordEntity record = buildRecord(externalLink.getReviewId());
        record.setInitiatorUserId(1013L);
        when(fixture.recordMapper.selectById(externalLink.getReviewId())).thenReturn(record);

        fixture.service.disableResource(DisableManuscriptReviewResourceCommand.builder()
            .resourceId(7101L)
            .disabledReason("来源失效")
            .build());

        ArgumentCaptor<ManuscriptReviewHistoryEntity> historyCaptor = ArgumentCaptor.forClass(ManuscriptReviewHistoryEntity.class);
        verify(fixture.historyMapper).insert(historyCaptor.capture());
        assertEquals("RESOURCE_DISABLE", historyCaptor.getValue().getActionType());
        assertEquals("张三停用了外链《素材参考》。", historyCaptor.getValue().getActionText());
    }

    @Test
    void shouldNormalizeVideoMarkTimeAndPersistSeconds() {
        ServiceFixture fixture = new ServiceFixture(1010L, "000000", 2001L, "张三");
        ManuscriptReviewRecordEntity record = buildRecord(9006L);
        record.setInitiatorUserId(1010L);
        when(fixture.recordMapper.selectById(9006L)).thenReturn(record);
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

        ArgumentCaptor<ManuscriptReviewHistoryEntity> historyCaptor = ArgumentCaptor.forClass(ManuscriptReviewHistoryEntity.class);
        verify(fixture.historyMapper).insert(historyCaptor.capture());
        assertEquals("VIDEO_MARK_ADD", historyCaptor.getValue().getActionType());
        assertEquals("张三新增了视频标注《第一处问题》（00:00:05 - 00:01:10）。", historyCaptor.getValue().getActionText());
    }

    @Test
    void shouldRejectAddVideoMarkWhenCurrentUserHasNoModifyPermission() {
        ServiceFixture fixture = new ServiceFixture(1017L, "000000", 2001L, "赵六");
        when(fixture.recordMapper.selectById(9013L)).thenReturn(buildPendingApprovalRecord(9013L, 2003L));
        when(fixture.attachmentMapper.selectById(7007L)).thenReturn(buildVideoAttachment(7007L, true, 600));
        fixture.stubSubmitRouting(false);

        ServiceException exception = assertThrows(ServiceException.class, () -> fixture.service.addVideoMark(AddManuscriptReviewVideoMarkCommand.builder()
            .reviewId(9013L)
            .resourceId(7007L)
            .startTimeText("00:00:05")
            .endTimeText("00:00:10")
            .markContent("forbidden")
            .build()));

        assertEquals("当前用户无权修改该流程", exception.getMessage());
        verify(fixture.videoMarkerMapper, never()).insert(any(ManuscriptReviewVideoMarkerEntity.class));
        verify(fixture.historyMapper, never()).insert(any(ManuscriptReviewHistoryEntity.class));
    }

    @Test
    void shouldRejectVideoMarkWhenDurationMissing() {
        ServiceFixture fixture = new ServiceFixture(1011L, "000000", 2001L, "张三");
        ManuscriptReviewRecordEntity record = buildRecord(9007L);
        record.setInitiatorUserId(1011L);
        when(fixture.recordMapper.selectById(9007L)).thenReturn(record);
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

    @Test
    void shouldDisableVideoMarkAndWriteHistory() {
        ServiceFixture fixture = new ServiceFixture(1014L, "000000", 2001L, "张三");
        ManuscriptReviewVideoMarkerEntity marker = buildVideoMarker(7201L);
        when(fixture.videoMarkerMapper.selectById(7201L)).thenReturn(marker);
        ManuscriptReviewRecordEntity record = buildRecord(marker.getReviewId());
        record.setInitiatorUserId(1014L);
        when(fixture.recordMapper.selectById(marker.getReviewId())).thenReturn(record);

        fixture.service.disableVideoMark(DisableManuscriptReviewVideoMarkCommand.builder()
            .markId(7201L)
            .disabledReason("标注已废弃")
            .build());

        ArgumentCaptor<ManuscriptReviewVideoMarkerEntity> markerCaptor = ArgumentCaptor.forClass(ManuscriptReviewVideoMarkerEntity.class);
        verify(fixture.videoMarkerMapper).updateById(markerCaptor.capture());
        assertEquals("0", markerCaptor.getValue().getEnabled());
        assertEquals(1014L, markerCaptor.getValue().getDisabledBy());
        assertNotNull(markerCaptor.getValue().getDisabledTime());
        assertEquals("标注已废弃", markerCaptor.getValue().getRemark());

        ArgumentCaptor<ManuscriptReviewHistoryEntity> historyCaptor = ArgumentCaptor.forClass(ManuscriptReviewHistoryEntity.class);
        verify(fixture.historyMapper).insert(historyCaptor.capture());
        assertEquals("VIDEO_MARK_DISABLE", historyCaptor.getValue().getActionType());
        assertEquals("张三停用了视频标注《第一处问题》（00:00:05 - 00:01:10）。", historyCaptor.getValue().getActionText());
    }

    @Test
    void shouldRejectDisableVideoMarkWhenCurrentUserHasNoModifyPermission() {
        ServiceFixture fixture = new ServiceFixture(1018L, "000000", 2001L, "孙七");
        ManuscriptReviewVideoMarkerEntity marker = buildVideoMarker(7202L);
        marker.setReviewId(9014L);
        when(fixture.videoMarkerMapper.selectById(7202L)).thenReturn(marker);
        when(fixture.recordMapper.selectById(9014L)).thenReturn(buildPendingApprovalRecord(9014L, 2004L));
        fixture.stubSubmitRouting(false);

        ServiceException exception = assertThrows(ServiceException.class, () -> fixture.service.disableVideoMark(DisableManuscriptReviewVideoMarkCommand.builder()
            .markId(7202L)
            .disabledReason("no permission")
            .build()));

        assertEquals("当前用户无权修改该流程", exception.getMessage());
        verify(fixture.videoMarkerMapper, never()).updateById(any(ManuscriptReviewVideoMarkerEntity.class));
        verify(fixture.historyMapper, never()).insert(any(ManuscriptReviewHistoryEntity.class));
    }

    @Test
    void shouldRejectCreateWhenTitleExceedsLengthLimit() {
        ServiceFixture fixture = new ServiceFixture(1019L, "000000", 2001L, "张三");

        ServiceException exception = assertThrows(ServiceException.class, () -> fixture.service.create(CreateManuscriptReviewCommand.builder()
            .processType(ManuscriptReviewProcessType.AUDIT)
            .title("题".repeat(201))
            .mediaChannel("新华社/要闻")
            .submitDepartment("总编室")
            .authorName("张三")
            .remark("说明")
            .contentBody("正文内容")
            .build()));

        assertEquals("标题长度不能超过200个字符", exception.getMessage());
        verify(fixture.recordMapper, never()).insert(any(ManuscriptReviewRecordEntity.class));
    }

    @Test
    void shouldRejectCreateWhenRemarkExceedsLengthLimit() {
        ServiceFixture fixture = new ServiceFixture(1020L, "000000", 2001L, "张三");

        ServiceException exception = assertThrows(ServiceException.class, () -> fixture.service.create(CreateManuscriptReviewCommand.builder()
            .processType(ManuscriptReviewProcessType.AUDIT)
            .title("稿件标题")
            .mediaChannel("新华社/要闻")
            .submitDepartment("总编室")
            .authorName("张三")
            .remark("说".repeat(1001))
            .contentBody("正文内容")
            .build()));

        assertEquals("说明长度不能超过1000个字符", exception.getMessage());
        verify(fixture.recordMapper, never()).insert(any(ManuscriptReviewRecordEntity.class));
    }

    @Test
    void shouldRejectCreateWhenContentBodyExceedsLengthLimit() {
        ServiceFixture fixture = new ServiceFixture(1021L, "000000", 2001L, "张三");

        ServiceException exception = assertThrows(ServiceException.class, () -> fixture.service.create(CreateManuscriptReviewCommand.builder()
            .processType(ManuscriptReviewProcessType.AUDIT)
            .title("稿件标题")
            .mediaChannel("新华社/要闻")
            .submitDepartment("总编室")
            .authorName("张三")
            .remark("说明")
            .contentBody("正".repeat(20001))
            .build()));

        assertEquals("正文长度不能超过20000个字符", exception.getMessage());
        verify(fixture.recordMapper, never()).insert(any(ManuscriptReviewRecordEntity.class));
    }

    @Test
    void shouldRejectAddExternalLinkWhenTitleExceedsLengthLimit() {
        ServiceFixture fixture = new ServiceFixture(1022L, "000000", 2001L, "张三");
        ManuscriptReviewRecordEntity record = buildRecord(9015L);
        record.setInitiatorUserId(1022L);
        when(fixture.recordMapper.selectById(9015L)).thenReturn(record);

        ServiceException exception = assertThrows(ServiceException.class, () -> fixture.service.addResource(AddManuscriptReviewResourceCommand.builder()
            .reviewId(9015L)
            .resourceType("EXTERNAL_LINK")
            .displayName("标".repeat(201))
            .externalUrl("https://example.com/ref")
            .build()));

        assertEquals("外链标题长度不能超过200个字符", exception.getMessage());
        verify(fixture.externalLinkMapper, never()).insert(any(ManuscriptReviewExternalLinkEntity.class));
    }

    @Test
    void shouldRejectAddExternalLinkWhenUrlExceedsLengthLimit() {
        ServiceFixture fixture = new ServiceFixture(1023L, "000000", 2001L, "张三");
        ManuscriptReviewRecordEntity record = buildRecord(9016L);
        record.setInitiatorUserId(1023L);
        when(fixture.recordMapper.selectById(9016L)).thenReturn(record);

        ServiceException exception = assertThrows(ServiceException.class, () -> fixture.service.addResource(AddManuscriptReviewResourceCommand.builder()
            .reviewId(9016L)
            .resourceType("EXTERNAL_LINK")
            .displayName("素材参考")
            .externalUrl("https://" + "a".repeat(493))
            .build()));

        assertEquals("外链地址长度不能超过500个字符", exception.getMessage());
        verify(fixture.externalLinkMapper, never()).insert(any(ManuscriptReviewExternalLinkEntity.class));
    }

    @Test
    void shouldRejectAddExternalLinkWhenProtocolInvalid() {
        ServiceFixture fixture = new ServiceFixture(1024L, "000000", 2001L, "张三");
        ManuscriptReviewRecordEntity record = buildRecord(9017L);
        record.setInitiatorUserId(1024L);
        when(fixture.recordMapper.selectById(9017L)).thenReturn(record);

        ServiceException exception = assertThrows(ServiceException.class, () -> fixture.service.addResource(AddManuscriptReviewResourceCommand.builder()
            .reviewId(9017L)
            .resourceType("EXTERNAL_LINK")
            .displayName("素材参考")
            .externalUrl("ftp://example.com/ref")
            .build()));

        assertEquals("外链只允许http/https协议", exception.getMessage());
        verify(fixture.externalLinkMapper, never()).insert(any(ManuscriptReviewExternalLinkEntity.class));
    }

    @Test
    void shouldRejectAddExternalLinkWhenUrlDuplicatedWithinSameReview() {
        ServiceFixture fixture = new ServiceFixture(1025L, "000000", 2001L, "张三");
        ManuscriptReviewRecordEntity record = buildRecord(9018L);
        record.setInitiatorUserId(1025L);
        when(fixture.recordMapper.selectById(9018L)).thenReturn(record);
        when(fixture.externalLinkMapper.selectCount(any())).thenReturn(1L);

        ServiceException exception = assertThrows(ServiceException.class, () -> fixture.service.addResource(AddManuscriptReviewResourceCommand.builder()
            .reviewId(9018L)
            .resourceType("EXTERNAL_LINK")
            .displayName("素材参考")
            .externalUrl("https://example.com/ref")
            .build()));

        assertEquals("同一流程内URL不允许重复", exception.getMessage());
        verify(fixture.externalLinkMapper, never()).insert(any(ManuscriptReviewExternalLinkEntity.class));
    }

    @Test
    void shouldRejectVideoMarkWhenStartTimeFormatInvalid() {
        ServiceFixture fixture = new ServiceFixture(1026L, "000000", 2001L, "张三");
        ManuscriptReviewRecordEntity record = buildRecord(9019L);
        record.setInitiatorUserId(1026L);
        when(fixture.recordMapper.selectById(9019L)).thenReturn(record);
        when(fixture.attachmentMapper.selectById(7010L)).thenReturn(buildVideoAttachment(7010L, true, 600));

        ServiceException exception = assertThrows(ServiceException.class, () -> fixture.service.addVideoMark(AddManuscriptReviewVideoMarkCommand.builder()
            .reviewId(9019L)
            .resourceId(7010L)
            .startTimeText("3:7")
            .endTimeText("00:00:10")
            .markContent("非法时间")
            .build()));

        assertEquals("标注开始时间格式不合法", exception.getMessage());
        verify(fixture.videoMarkerMapper, never()).insert(any(ManuscriptReviewVideoMarkerEntity.class));
    }

    @Test
    void shouldRejectVideoMarkWhenEndEarlierThanStart() {
        ServiceFixture fixture = new ServiceFixture(1027L, "000000", 2001L, "张三");
        ManuscriptReviewRecordEntity record = buildRecord(9020L);
        record.setInitiatorUserId(1027L);
        when(fixture.recordMapper.selectById(9020L)).thenReturn(record);
        when(fixture.attachmentMapper.selectById(7011L)).thenReturn(buildVideoAttachment(7011L, true, 600));

        ServiceException exception = assertThrows(ServiceException.class, () -> fixture.service.addVideoMark(AddManuscriptReviewVideoMarkCommand.builder()
            .reviewId(9020L)
            .resourceId(7011L)
            .startTimeText("00:00:10")
            .endTimeText("00:00:05")
            .markContent("结束早于开始")
            .build()));

        assertEquals("标注结束时间不能早于开始时间", exception.getMessage());
        verify(fixture.videoMarkerMapper, never()).insert(any(ManuscriptReviewVideoMarkerEntity.class));
    }

    @Test
    void shouldRejectVideoMarkWhenTimeExceedsDuration() {
        ServiceFixture fixture = new ServiceFixture(1028L, "000000", 2001L, "张三");
        ManuscriptReviewRecordEntity record = buildRecord(9021L);
        record.setInitiatorUserId(1028L);
        when(fixture.recordMapper.selectById(9021L)).thenReturn(record);
        when(fixture.attachmentMapper.selectById(7012L)).thenReturn(buildVideoAttachment(7012L, true, 600));

        ServiceException exception = assertThrows(ServiceException.class, () -> fixture.service.addVideoMark(AddManuscriptReviewVideoMarkCommand.builder()
            .reviewId(9021L)
            .resourceId(7012L)
            .startTimeText("00:10:01")
            .endTimeText(null)
            .markContent("超出总时长")
            .build()));

        assertEquals("标注时间不能超出视频总时长", exception.getMessage());
        verify(fixture.videoMarkerMapper, never()).insert(any(ManuscriptReviewVideoMarkerEntity.class));
    }

    @Test
    void shouldCancelWaitingReviewAndWriteHistory() {
        ServiceFixture fixture = new ServiceFixture(1029L, "000000", 2001L, "张三");
        ManuscriptReviewRecordEntity record = buildRecord(9022L);
        record.setInitiatorUserId(1029L);
        record.setFlowStatusLabel("审批中");
        record.setCurrentNodeLabel("待一级审批");
        when(fixture.recordMapper.selectById(9022L)).thenReturn(record);

        fixture.service.cancelProcessApply(9022L, "发起人主动撤销");

        ArgumentCaptor<ManuscriptReviewRecordEntity> recordCaptor = ArgumentCaptor.forClass(ManuscriptReviewRecordEntity.class);
        verify(fixture.recordMapper).updateById(recordCaptor.capture());
        ManuscriptReviewRecordEntity updated = recordCaptor.getValue();
        assertEquals("已取消", updated.getFlowStatusLabel());
        assertEquals("流程已取消", updated.getCurrentNodeLabel());
        assertEquals("发起人主动撤销", updated.getRemark());
        ArgumentCaptor<FlowCancelBo> cancelCaptor = ArgumentCaptor.forClass(FlowCancelBo.class);
        verify(fixture.flwInstanceService).cancelProcessApply(cancelCaptor.capture());
        assertEquals("9022", cancelCaptor.getValue().getBusinessId());
        assertEquals("发起人主动撤销", cancelCaptor.getValue().getMessage());

        ArgumentCaptor<ManuscriptReviewHistoryEntity> historyCaptor = ArgumentCaptor.forClass(ManuscriptReviewHistoryEntity.class);
        verify(fixture.historyMapper).insert(historyCaptor.capture());
        assertEquals("CANCEL", historyCaptor.getValue().getActionType());
        assertEquals("张三撤销了审校流程单。", historyCaptor.getValue().getActionText());
    }

    @Test
    void shouldCancelReturnedReviewAndWriteHistory() {
        ServiceFixture fixture = new ServiceFixture(1030L, "000000", 2001L, "张三");
        ManuscriptReviewRecordEntity record = buildRecord(9024L);
        record.setInitiatorUserId(1030L);
        record.setFlowStatusLabel("已退回");
        record.setCurrentNodeLabel("待发起人处理");
        when(fixture.recordMapper.selectById(9024L)).thenReturn(record);

        fixture.service.cancelProcessApply(9024L, "退回后主动撤销");

        ArgumentCaptor<ManuscriptReviewRecordEntity> recordCaptor = ArgumentCaptor.forClass(ManuscriptReviewRecordEntity.class);
        verify(fixture.recordMapper).updateById(recordCaptor.capture());
        ManuscriptReviewRecordEntity updated = recordCaptor.getValue();
        assertEquals("已取消", updated.getFlowStatusLabel());
        assertEquals("流程已取消", updated.getCurrentNodeLabel());
        assertEquals("退回后主动撤销", updated.getRemark());

        ArgumentCaptor<FlowCancelBo> cancelCaptor = ArgumentCaptor.forClass(FlowCancelBo.class);
        verify(fixture.flwInstanceService).cancelProcessApply(cancelCaptor.capture());
        assertEquals("9024", cancelCaptor.getValue().getBusinessId());
        assertEquals("退回后主动撤销", cancelCaptor.getValue().getMessage());

        ArgumentCaptor<ManuscriptReviewHistoryEntity> historyCaptor = ArgumentCaptor.forClass(ManuscriptReviewHistoryEntity.class);
        verify(fixture.historyMapper).insert(historyCaptor.capture());
        assertEquals("CANCEL", historyCaptor.getValue().getActionType());
        assertEquals("张三撤销了审校流程单。", historyCaptor.getValue().getActionText());
    }

    @Test
    void shouldRejectCancelWhenFlowIsNotWaiting() {
        ServiceFixture fixture = new ServiceFixture(1030L, "000000", 2001L, "张三");
        ManuscriptReviewRecordEntity record = buildRecord(9023L);
        record.setInitiatorUserId(1030L);
        record.setFlowStatusLabel("已取消");
        record.setCurrentNodeLabel("流程已取消");
        when(fixture.recordMapper.selectById(9023L)).thenReturn(record);

        ServiceException exception = assertThrows(ServiceException.class, () -> fixture.service.cancelProcessApply(9023L, "重复撤销"));

        assertEquals("仅审批中或已退回的流程可撤销", exception.getMessage());
        verify(fixture.recordMapper, never()).updateById(any(ManuscriptReviewRecordEntity.class));
        verify(fixture.historyMapper, never()).insert(any(ManuscriptReviewHistoryEntity.class));
    }

    @Test
    void shouldRejectUpdateWhenCurrentUserHasNoModifyPermission() {
        ServiceFixture fixture = new ServiceFixture(1031L, "000000", 2001L, "张三");
        when(fixture.recordMapper.selectById(9024L)).thenReturn(buildPendingApprovalRecord(9024L, 2008L));

        ServiceException exception = assertThrows(ServiceException.class, () -> fixture.service.update(UpdateManuscriptReviewCommand.builder()
            .id(9024L)
            .processType(ManuscriptReviewProcessType.AUDIT)
            .externalManuscriptCode("EXT-9024")
            .title("越权修改稿件")
            .mediaChannel("新华社/要闻")
            .submitDepartment("总编室")
            .authorName("张三")
            .remark("越权修改")
            .contentBody("正文内容")
            .build()));

        assertEquals("当前用户无权修改该流程", exception.getMessage());
        verify(fixture.recordMapper, never()).updateById(any(ManuscriptReviewRecordEntity.class));
    }

    @Test
    void shouldRejectResubmitWhenFlowIsNotReturned() {
        ServiceFixture fixture = new ServiceFixture(1032L, "000000", 2001L, "张三");
        ManuscriptReviewRecordEntity record = buildRecord(9025L);
        record.setInitiatorUserId(1032L);
        record.setFlowStatusLabel("审批中");
        record.setCurrentNodeLabel("待一级审批");
        record.setManuscriptCode("SH20260321025");
        when(fixture.recordMapper.selectById(9025L)).thenReturn(record);

        ServiceException exception = assertThrows(ServiceException.class, () -> fixture.service.resubmit(
            ResubmitManuscriptReviewCommand.builder().reviewId(9025L).build()));

        assertEquals("仅退回给发起人的流程可再次提交", exception.getMessage());
        verify(fixture.recordMapper, never()).updateById(any(ManuscriptReviewRecordEntity.class));
    }

    @Test
    void shouldSyncCanceledRuntimeStateWhenProcessEventReportsCancel() {
        ServiceFixture fixture = new ServiceFixture(1033L, "000000", 2001L, "张三");
        ManuscriptReviewRecordEntity record = buildPendingApprovalRecord(9026L, 1033L);
        when(fixture.recordMapper.selectById(9026L)).thenReturn(record);

        ProcessEvent processEvent = new ProcessEvent();
        processEvent.setTenantId("000000");
        processEvent.setBusinessId("9026");
        processEvent.setInstanceId(99026L);
        processEvent.setStatus("cancel");

        fixture.service.processHandler(processEvent);

        ArgumentCaptor<ManuscriptReviewRecordEntity> recordCaptor = ArgumentCaptor.forClass(ManuscriptReviewRecordEntity.class);
        verify(fixture.recordMapper).updateById(recordCaptor.capture());
        ManuscriptReviewRecordEntity updated = recordCaptor.getValue();
        assertEquals(99026L, updated.getFlowInstanceId());
        assertEquals("已取消", updated.getFlowStatusLabel());
        assertEquals("流程已取消", updated.getCurrentNodeLabel());
        verify(fixture.historyMapper, never()).insert(any(ManuscriptReviewHistoryEntity.class));
    }

    @Test
    void shouldSyncRejectedRuntimeStateAndHistoryWhenProcessEventReportsTermination() {
        ServiceFixture fixture = new ServiceFixture(1034L, "000000", 2001L, "张三");
        ManuscriptReviewRecordEntity record = buildPendingApprovalRecord(9027L, 1034L);
        when(fixture.recordMapper.selectById(9027L)).thenReturn(record);

        ProcessEvent processEvent = new ProcessEvent();
        processEvent.setTenantId("000000");
        processEvent.setBusinessId("9027");
        processEvent.setInstanceId(99027L);
        processEvent.setStatus("termination");
        processEvent.setParams(java.util.Map.of("message", "不同意"));

        fixture.service.processHandler(processEvent);

        ArgumentCaptor<ManuscriptReviewRecordEntity> recordCaptor = ArgumentCaptor.forClass(ManuscriptReviewRecordEntity.class);
        verify(fixture.recordMapper).updateById(recordCaptor.capture());
        ManuscriptReviewRecordEntity updated = recordCaptor.getValue();
        assertEquals(99027L, updated.getFlowInstanceId());
        assertEquals("已驳回", updated.getFlowStatusLabel());
        assertEquals("流程已驳回", updated.getCurrentNodeLabel());

        ArgumentCaptor<ManuscriptReviewHistoryEntity> historyCaptor = ArgumentCaptor.forClass(ManuscriptReviewHistoryEntity.class);
        verify(fixture.historyMapper).insert(historyCaptor.capture());
        assertEquals("REJECT", historyCaptor.getValue().getActionType());
        assertTrue(historyCaptor.getValue().getActionText().contains("三级审批驳回"));
        assertTrue(historyCaptor.getValue().getActionText().contains("不同意"));
    }

    @Test
    void shouldSyncCurrentNodeWhenProcessTaskEventCreatesWaitingTask() {
        ServiceFixture fixture = new ServiceFixture(1035L, "000000", 2001L, "张三");
        ManuscriptReviewRecordEntity record = buildRecord(9028L);
        when(fixture.recordMapper.selectById(9028L)).thenReturn(record);

        ProcessTaskEvent processTaskEvent = new ProcessTaskEvent();
        processTaskEvent.setBusinessId("9028");
        processTaskEvent.setInstanceId(99028L);
        processTaskEvent.setStatus("waiting");
        processTaskEvent.setNodeName("待二级审批");

        fixture.service.processTaskHandler(processTaskEvent);

        ArgumentCaptor<ManuscriptReviewRecordEntity> recordCaptor = ArgumentCaptor.forClass(ManuscriptReviewRecordEntity.class);
        verify(fixture.recordMapper).updateById(recordCaptor.capture());
        ManuscriptReviewRecordEntity updated = recordCaptor.getValue();
        assertEquals(99028L, updated.getFlowInstanceId());
        assertEquals("审批中", updated.getFlowStatusLabel());
        assertEquals("待二级审批", updated.getCurrentNodeLabel());
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

    private static ManuscriptReviewRecordEntity buildPendingApprovalRecord(Long reviewId, Long initiatorUserId) {
        ManuscriptReviewRecordEntity entity = buildRecord(reviewId);
        entity.setInitiatorUserId(initiatorUserId);
        entity.setInitiatorName("其他人");
        entity.setFlowStatusLabel("审批中");
        entity.setCurrentNodeLabel("待一级审批");
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

    private static ManuscriptReviewExternalLinkEntity buildExternalLink(Long resourceId) {
        ManuscriptReviewExternalLinkEntity entity = new ManuscriptReviewExternalLinkEntity();
        entity.setId(resourceId);
        entity.setReviewId(9010L);
        entity.setLinkTitle("素材参考");
        entity.setLinkUrl("https://example.com/ref");
        entity.setEnabled("1");
        return entity;
    }

    private static ManuscriptReviewVideoMarkerEntity buildVideoMarker(Long markerId) {
        ManuscriptReviewVideoMarkerEntity entity = new ManuscriptReviewVideoMarkerEntity();
        entity.setId(markerId);
        entity.setReviewId(9006L);
        entity.setVideoAttachmentId(7004L);
        entity.setStartTime("00:00:05");
        entity.setEndTime("00:01:10");
        entity.setMarkerNote("第一处问题");
        entity.setEnabled("1");
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
            when(workflowService.startCompleteTask(any(StartProcessDTO.class))).thenReturn(true);
            when(workflowService.getInstanceIdByBusinessId(any())).thenReturn(99001L);
        }
    }
    @Test
    void shouldWriteApprovalHistoryWhenProcessTaskEventMovesToSecondLevel() {
        ServiceFixture fixture = new ServiceFixture(1038L, "000000", 2001L, "张三");
        ManuscriptReviewRecordEntity record = buildPendingApprovalRecord(9031L, 1038L);
        when(fixture.recordMapper.selectById(9031L)).thenReturn(record);

        ProcessTaskEvent processTaskEvent = new ProcessTaskEvent();
        processTaskEvent.setTenantId("000000");
        processTaskEvent.setBusinessId("9031");
        processTaskEvent.setInstanceId(99031L);
        processTaskEvent.setStatus("waiting");
        processTaskEvent.setNodeCode("second-review-node");
        processTaskEvent.setNodeName("二级审批");
        processTaskEvent.setParams(java.util.Map.of("message", "一级通过"));

        fixture.service.processTaskHandler(processTaskEvent);

        ArgumentCaptor<ManuscriptReviewRecordEntity> recordCaptor = ArgumentCaptor.forClass(ManuscriptReviewRecordEntity.class);
        verify(fixture.recordMapper).updateById(recordCaptor.capture());
        assertEquals("LEVEL_2", recordCaptor.getValue().getCurrentNodeStatus());
        assertEquals("待二级审批", recordCaptor.getValue().getCurrentNodeLabel());

        ArgumentCaptor<ManuscriptReviewHistoryEntity> historyCaptor = ArgumentCaptor.forClass(ManuscriptReviewHistoryEntity.class);
        verify(fixture.historyMapper).insert(historyCaptor.capture());
        assertEquals("APPROVE", historyCaptor.getValue().getActionType());
        assertTrue(historyCaptor.getValue().getActionText().contains("一级审批审批通过"));
        assertTrue(historyCaptor.getValue().getActionText().contains("一级通过"));
    }

    @Test
    void shouldNotWriteApprovalHistoryWhenSubmitCreatesSecondLevelTask() {
        ServiceFixture fixture = new ServiceFixture(1040L, "000000", 2001L, "寮犱笁");
        ManuscriptReviewRecordEntity record = buildPendingApprovalRecord(9033L, 1040L);
        when(fixture.recordMapper.selectById(9033L)).thenReturn(record);

        ProcessTaskEvent processTaskEvent = new ProcessTaskEvent();
        processTaskEvent.setTenantId("000000");
        processTaskEvent.setBusinessId("9033");
        processTaskEvent.setInstanceId(99033L);
        processTaskEvent.setStatus("waiting");
        processTaskEvent.setNodeCode("second-review-node");
        processTaskEvent.setNodeName("浜岀骇瀹℃壒");
        processTaskEvent.setParams(java.util.Map.of("submit", true, "handler", "64005"));

        fixture.service.processTaskHandler(processTaskEvent);

        verify(fixture.historyMapper, never()).insert(any(ManuscriptReviewHistoryEntity.class));
    }

    @Test
    void shouldWriteApprovalHistoryWhenProcessTaskEventMovesToFinalLevel() {
        ServiceFixture fixture = new ServiceFixture(1039L, "000000", 2001L, "张三");
        ManuscriptReviewRecordEntity record = buildPendingApprovalRecord(9032L, 1039L);
        when(fixture.recordMapper.selectById(9032L)).thenReturn(record);

        ProcessTaskEvent processTaskEvent = new ProcessTaskEvent();
        processTaskEvent.setTenantId("000000");
        processTaskEvent.setBusinessId("9032");
        processTaskEvent.setInstanceId(99032L);
        processTaskEvent.setStatus("waiting");
        processTaskEvent.setNodeCode("final-review-node");
        processTaskEvent.setNodeName("三级审批");
        processTaskEvent.setParams(java.util.Map.of("message", "二级通过"));

        fixture.service.processTaskHandler(processTaskEvent);

        ArgumentCaptor<ManuscriptReviewHistoryEntity> historyCaptor = ArgumentCaptor.forClass(ManuscriptReviewHistoryEntity.class);
        verify(fixture.historyMapper).insert(historyCaptor.capture());
        assertEquals("APPROVE", historyCaptor.getValue().getActionType());
        assertTrue(historyCaptor.getValue().getActionText().contains("二级审批审批通过"));
        assertTrue(historyCaptor.getValue().getActionText().contains("二级通过"));
    }
}
