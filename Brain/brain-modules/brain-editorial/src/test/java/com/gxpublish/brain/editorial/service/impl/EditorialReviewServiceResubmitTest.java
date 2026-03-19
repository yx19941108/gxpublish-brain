package com.gxpublish.brain.editorial.service.impl;

import com.gxpublish.brain.common.core.domain.dto.StartProcessDTO;
import com.gxpublish.brain.common.core.domain.dto.RoleDTO;
import com.gxpublish.brain.common.core.domain.event.ProcessEvent;
import com.gxpublish.brain.common.core.domain.model.LoginUser;
import com.gxpublish.brain.common.core.enums.BusinessStatusEnum;
import com.gxpublish.brain.common.core.exception.ServiceException;
import com.gxpublish.brain.common.core.service.WorkflowService;
import com.gxpublish.brain.common.core.utils.SpringUtils;
import com.gxpublish.brain.common.satoken.utils.LoginHelper;
import com.gxpublish.brain.editorial.domain.EditorialHistory;
import com.gxpublish.brain.editorial.domain.EditorialReview;
import com.gxpublish.brain.editorial.domain.bo.EditorialLinkBo;
import com.gxpublish.brain.editorial.domain.bo.EditorialReviewBo;
import com.gxpublish.brain.editorial.domain.vo.EditorialReviewTaskContextVo;
import com.gxpublish.brain.editorial.domain.vo.EditorialReviewDetailVo;
import com.gxpublish.brain.editorial.domain.vo.EditorialWorkflowRoleRefVo;
import com.gxpublish.brain.editorial.enums.ReviewStatusEnum;
import com.gxpublish.brain.editorial.mapper.EditorialAttachmentMapper;
import com.gxpublish.brain.editorial.mapper.EditorialHistoryMapper;
import com.gxpublish.brain.editorial.mapper.EditorialLinkMapper;
import com.gxpublish.brain.editorial.mapper.EditorialReviewMapper;
import com.gxpublish.brain.editorial.mapper.EditorialWorkflowRoleMapper;
import com.gxpublish.brain.editorial.service.strategy.EditorialDataScopeFactory;
import com.gxpublish.brain.editorial.support.EditorialReviewWorkflowDefinition;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.support.GenericApplicationContext;

import io.github.linpeilie.Converter;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@Tag("dev")
@ExtendWith(MockitoExtension.class)
class EditorialReviewServiceResubmitTest {

    @Mock
    private EditorialReviewMapper baseMapper;
    @Mock
    private EditorialAttachmentMapper attachmentMapper;
    @Mock
    private EditorialLinkMapper linkMapper;
    @Mock
    private EditorialHistoryMapper historyMapper;
    @Mock
    private WorkflowService workflowService;
    @Mock
    private EditorialDataScopeFactory dataScopeFactory;
    @Mock
    private EditorialWorkflowRoleMapper workflowRoleMapper;

    private EditorialReviewServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new EditorialReviewServiceImpl(
            baseMapper,
            attachmentMapper,
            linkMapper,
            historyMapper,
            workflowService,
            dataScopeFactory,
            workflowRoleMapper
        );
    }

    @Test
    void shouldResubmitBackReviewUsingExistingWorkflowRestart() throws Exception {
        Method method = EditorialReviewServiceImpl.class.getMethod("resubmitAndFlowStart", EditorialReviewBo.class);
        Long reviewId = 100L;

        EditorialReview existing = new EditorialReview();
        existing.setId(reviewId);
        existing.setStatus(BusinessStatusEnum.BACK.getStatus());
        existing.setReviewStatus(ReviewStatusEnum.BACK.getCode());
        existing.setCreateBy(900L);

        EditorialReviewDetailVo detail = new EditorialReviewDetailVo();
        detail.setId(reviewId);
        detail.setApplyCode("APPLY-100");
        detail.setStatus(BusinessStatusEnum.WAITING.getStatus());
        detail.setReviewStatus(ReviewStatusEnum.WAITING_FIRST.getCode());
        detail.setProcessType("AUDIT");

        when(baseMapper.selectById(reviewId)).thenReturn(existing);
        when(baseMapper.selectVoById(reviewId)).thenReturn(detail);
        when(baseMapper.updateById(any(EditorialReview.class))).thenReturn(1);
        when(linkMapper.selectVoList(any())).thenReturn(List.of());
        when(historyMapper.selectVoList(any())).thenReturn(List.of());
        when(workflowService.startCompleteTask(any(StartProcessDTO.class))).thenReturn(true);
        when(workflowRoleMapper.selectRoleRefs(anyList())).thenReturn(List.of(
            roleRef(11L, "editorial_first_level_approver"),
            roleRef(22L, "editorial_second_level_approver"),
            roleRef(33L, "editorial_third_level_approver")
        ));
        EditorialReviewBo bo = new EditorialReviewBo();
        bo.setId(reviewId);
        bo.setTitle("退回后重提");
        bo.setContent("更新后的内容");
        bo.setProcessType(" audit ");
        EditorialLinkBo linkBo = new EditorialLinkBo();
        linkBo.setUrl("https://example.com/proof");
        linkBo.setDescription("回提佐证");
        bo.setLinkList(List.of(linkBo));

        EditorialReviewDetailVo result;
        try (MockedStatic<LoginHelper> loginHelper = org.mockito.Mockito.mockStatic(LoginHelper.class)) {
            loginHelper.when(LoginHelper::getUserId).thenReturn(900L);
            loginHelper.when(LoginHelper::isSuperAdmin).thenReturn(false);
            loginHelper.when(LoginHelper::getLoginUser).thenReturn(null);
            result = assertInstanceOf(EditorialReviewDetailVo.class, method.invoke(service, bo));
        }

        assertEquals("AUDIT", result.getProcessType());
        ArgumentCaptor<StartProcessDTO> startProcessCaptor = ArgumentCaptor.forClass(StartProcessDTO.class);
        verify(workflowService).startCompleteTask(startProcessCaptor.capture());
        StartProcessDTO startProcess = startProcessCaptor.getValue();
        assertEquals(String.valueOf(reviewId), startProcess.getBusinessId());
        assertEquals(EditorialReviewWorkflowDefinition.FLOW_CODE, startProcess.getFlowCode());
        assertEquals(Boolean.TRUE, startProcess.getVariables().get("ignore"));
        assertEquals(Boolean.FALSE, startProcess.getVariables().get("isCertified"));
        assertEquals("role:11",
            startProcess.getVariables().get(EditorialReviewWorkflowDefinition.FIRST_APPROVER_PERMISSION_VAR));
        assertEquals("role:22",
            startProcess.getVariables().get(EditorialReviewWorkflowDefinition.SECOND_APPROVER_PERMISSION_VAR));
        assertEquals("role:33",
            startProcess.getVariables().get(EditorialReviewWorkflowDefinition.FINAL_APPROVER_PERMISSION_VAR));

        ArgumentCaptor<EditorialReview> updateCaptor = ArgumentCaptor.forClass(EditorialReview.class);
        verify(baseMapper, atLeastOnce()).updateById(updateCaptor.capture());
        assertEquals("AUDIT", updateCaptor.getAllValues().get(0).getProcessType());
    }

    @Test
    void shouldRejectResubmitOutsideBackStatus() throws Exception {
        Method method = EditorialReviewServiceImpl.class.getMethod("resubmitAndFlowStart", EditorialReviewBo.class);
        Long reviewId = 200L;

        EditorialReview existing = new EditorialReview();
        existing.setId(reviewId);
        existing.setStatus(BusinessStatusEnum.WAITING.getStatus());
        existing.setReviewStatus(ReviewStatusEnum.WAITING_FIRST.getCode());

        when(baseMapper.selectById(reviewId)).thenReturn(existing);

        EditorialReviewBo bo = new EditorialReviewBo();
        bo.setId(reviewId);
        bo.setTitle("审批中稿件");
        bo.setContent("不能重提");
        bo.setProcessType("AUDIT");

        InvocationTargetException ex = assertThrows(InvocationTargetException.class, () -> method.invoke(service, bo));

        assertInstanceOf(ServiceException.class, ex.getCause());
        assertEquals("只有退回状态可重新提交", ex.getCause().getMessage());
        verifyNoInteractions(workflowService);
    }

    @Test
    void shouldPromoteCertifiedSubmitDirectlyToSecondWaitingStatus() {
        Long reviewId = 300L;
        EditorialReview review = new EditorialReview();
        review.setId(reviewId);
        review.setStatus(BusinessStatusEnum.DRAFT.getStatus());
        review.setReviewStatus(ReviewStatusEnum.DRAFT.getCode());

        when(baseMapper.selectById(reviewId)).thenReturn(review);
        when(baseMapper.updateById(any(EditorialReview.class))).thenReturn(1);

        ProcessEvent processEvent = new ProcessEvent();
        processEvent.setFlowCode(EditorialReviewWorkflowDefinition.FLOW_CODE);
        processEvent.setBusinessId(String.valueOf(reviewId));
        processEvent.setSubmit(Boolean.TRUE);
        processEvent.setStatus(BusinessStatusEnum.WAITING.getStatus());
        processEvent.setParams(Map.of("isCertified", true, "businessCode", "APPLY-300"));

        service.processHandler(processEvent);

        ArgumentCaptor<EditorialReview> updateCaptor = ArgumentCaptor.forClass(EditorialReview.class);
        verify(baseMapper).updateById(updateCaptor.capture());
        EditorialReview updated = updateCaptor.getValue();
        assertEquals(BusinessStatusEnum.WAITING.getStatus(), updated.getStatus());
        assertEquals(ReviewStatusEnum.WAITING_SECOND.getCode(), updated.getReviewStatus());
        assertEquals("APPLY-300", updated.getApplyCode());
    }

    @Test
    void shouldMarkBackDetailEditableForApplicantInQueryDetailContract() {
        Long reviewId = 400L;
        EditorialReviewDetailVo detail = new EditorialReviewDetailVo();
        detail.setId(reviewId);
        detail.setStatus(BusinessStatusEnum.BACK.getStatus());
        detail.setReviewStatus(ReviewStatusEnum.BACK.getCode());
        detail.setProcessType("AUDIT");
        detail.setUserId(900L);

        when(baseMapper.selectVoById(reviewId)).thenReturn(detail);
        when(linkMapper.selectVoList(any())).thenReturn(List.of());
        when(historyMapper.selectVoList(any())).thenReturn(List.of());

        LoginUser loginUser = new LoginUser();
        loginUser.setUserId(900L);
        RoleDTO applicantRole = new RoleDTO();
        applicantRole.setRoleKey("editorial_review_applicant");
        loginUser.setRoles(List.of(applicantRole));

        try (MockedStatic<LoginHelper> loginHelper = org.mockito.Mockito.mockStatic(LoginHelper.class)) {
            loginHelper.when(LoginHelper::getLoginUser).thenReturn(loginUser);
            loginHelper.when(LoginHelper::isSuperAdmin).thenReturn(false);

            EditorialReviewDetailVo result = service.queryById(reviewId);

            assertEquals(Boolean.TRUE, result.getCanEdit());
            assertEquals(Boolean.TRUE, result.getApprovalContext().getCanEdit());
        }
    }

    @Test
    void shouldExposeApprovalContextForCurrentApproverInQueryDetail() {
        Long reviewId = 410L;
        EditorialReviewDetailVo detail = new EditorialReviewDetailVo();
        detail.setId(reviewId);
        detail.setStatus(BusinessStatusEnum.WAITING.getStatus());
        detail.setReviewStatus(ReviewStatusEnum.WAITING_SECOND.getCode());
        detail.setProcessType("AUDIT");

        EditorialReviewTaskContextVo taskContext = new EditorialReviewTaskContextVo();
        taskContext.setTaskId(9001L);
        taskContext.setInstanceId(9002L);

        when(baseMapper.selectVoById(reviewId)).thenReturn(detail);
        when(baseMapper.selectCurrentTaskContext(String.valueOf(reviewId), "2000")).thenReturn(taskContext);
        when(linkMapper.selectVoList(any())).thenReturn(List.of());
        when(historyMapper.selectVoList(any())).thenReturn(List.of());

        LoginUser loginUser = new LoginUser();
        RoleDTO approverRole = new RoleDTO();
        approverRole.setRoleKey("editorial_second_level_approver");
        loginUser.setRoles(List.of(approverRole));
        loginUser.setUserId(2000L);

        try (MockedStatic<LoginHelper> loginHelper = org.mockito.Mockito.mockStatic(LoginHelper.class)) {
            loginHelper.when(LoginHelper::getLoginUser).thenReturn(loginUser);
            loginHelper.when(LoginHelper::getUserId).thenReturn(2000L);
            loginHelper.when(LoginHelper::getUserIdStr).thenReturn("2000");
            loginHelper.when(LoginHelper::isSuperAdmin).thenReturn(false);

            EditorialReviewDetailVo result = service.queryById(reviewId);

            assertEquals(Boolean.TRUE, result.getCanEdit());
            assertEquals(Boolean.TRUE, result.getApprovalContext().getCanApprove());
            assertEquals(9001L, result.getApprovalContext().getTaskId());
            assertEquals(9002L, result.getApprovalContext().getInstanceId());
        }
    }

    @Test
    void shouldAllowCurrentApproverToModifyWaitingReviewAndRecordHistory() throws Exception {
        Long reviewId = 420L;
        Method method = EditorialReviewServiceImpl.class.getMethod("updateByBo", EditorialReviewBo.class);

        EditorialReview existing = new EditorialReview();
        existing.setId(reviewId);
        existing.setStatus(BusinessStatusEnum.WAITING.getStatus());
        existing.setReviewStatus(ReviewStatusEnum.WAITING_FIRST.getCode());
        existing.setCreateBy(1000L);
        existing.setUserId(1000L);

        EditorialReview refreshed = new EditorialReview();
        refreshed.setId(reviewId);
        refreshed.setStatus(BusinessStatusEnum.WAITING.getStatus());
        refreshed.setReviewStatus(ReviewStatusEnum.WAITING_FIRST.getCode());
        refreshed.setCreateBy(1000L);
        refreshed.setUserId(1000L);
        refreshed.setTitle("审批人修改后的标题");
        refreshed.setContent("审批人修改后的内容");
        refreshed.setProcessType("AUDIT");

        EditorialReviewDetailVo detail = new EditorialReviewDetailVo();
        detail.setId(reviewId);
        detail.setStatus(BusinessStatusEnum.WAITING.getStatus());
        detail.setReviewStatus(ReviewStatusEnum.WAITING_FIRST.getCode());
        detail.setProcessType("AUDIT");

        when(baseMapper.selectById(reviewId)).thenReturn(existing, existing, refreshed);
        when(baseMapper.selectVoById(reviewId)).thenReturn(detail);
        when(baseMapper.updateById(any(EditorialReview.class))).thenReturn(1);
        EditorialReviewTaskContextVo taskContext = new EditorialReviewTaskContextVo();
        taskContext.setTaskId(7001L);
        taskContext.setInstanceId(7002L);
        when(baseMapper.selectCurrentTaskContext(String.valueOf(reviewId), "2000")).thenReturn(taskContext);
        when(linkMapper.selectVoList(any())).thenReturn(List.of());
        when(historyMapper.selectVoList(any())).thenReturn(List.of());

        Converter converter = mock(Converter.class);
        org.mockito.Mockito.lenient().when(converter.convert(any(EditorialReviewBo.class), eq(EditorialReview.class))).thenAnswer(invocation -> {
            EditorialReviewBo source = invocation.getArgument(0);
            EditorialReview mapped = new EditorialReview();
            mapped.setId(source.getId());
            mapped.setTitle(source.getTitle());
            mapped.setContent(source.getContent());
            mapped.setRemark(source.getRemark());
            mapped.setProcessType(source.getProcessType());
            return mapped;
        });

        EditorialReviewBo bo = new EditorialReviewBo();
        bo.setId(reviewId);
        bo.setTitle("审批人修改后的标题");
        bo.setContent("审批人修改后的内容");
        bo.setProcessType("AUDIT");
        bo.setLinkList(List.of());

        GenericApplicationContext context = new GenericApplicationContext();
        context.registerBean(Converter.class, () -> converter);
        context.registerBean(SpringUtils.class);
        context.refresh();
        try (MockedStatic<LoginHelper> loginHelper = org.mockito.Mockito.mockStatic(LoginHelper.class)) {
            LoginUser approverUser = new LoginUser();
            RoleDTO approverRole = new RoleDTO();
            approverRole.setRoleKey("editorial_first_level_approver");
            approverUser.setRoles(List.of(approverRole));
            loginHelper.when(LoginHelper::getUserId).thenReturn(2000L);
            loginHelper.when(LoginHelper::getUserIdStr).thenReturn("2000");
            loginHelper.when(LoginHelper::isSuperAdmin).thenReturn(false);
            loginHelper.when(LoginHelper::getUsername).thenReturn("wangwu");
            loginHelper.when(LoginHelper::getLoginUser).thenReturn(approverUser);

            EditorialReviewDetailVo result = assertInstanceOf(EditorialReviewDetailVo.class, method.invoke(service, bo));
            assertEquals(reviewId, result.getId());
        } finally {
            context.close();
        }

        ArgumentCaptor<EditorialHistory> historyCaptor = ArgumentCaptor.forClass(EditorialHistory.class);
        verify(historyMapper).insert(historyCaptor.capture());
        assertEquals("MODIFY", historyCaptor.getValue().getOperateType());
        assertTrue(historyCaptor.getValue().getFieldDiff().containsKey("title"));
    }

    @Test
    void shouldRejectSubmitWhenNeitherAttachmentNorLinkProvided() {
        EditorialReviewBo bo = new EditorialReviewBo();
        bo.setTitle("空附件空链接");
        bo.setContent("没有任何提交材料");
        bo.setProcessType("AUDIT");
        bo.setLinkList(List.of());

        ServiceException ex = assertThrows(ServiceException.class, () -> service.submitAndFlowStart(bo));
        assertEquals("提交审批时，附件和关联链接至少需要填写一项", ex.getMessage());
        verifyNoInteractions(workflowService);
    }

    @Test
    void shouldExposeAttachmentListAndKeepLegacyAttachmentCompatibility() {
        Long reviewId = 430L;
        EditorialReviewDetailVo detail = new EditorialReviewDetailVo();
        detail.setId(reviewId);
        detail.setUserId(900L);
        detail.setStatus(BusinessStatusEnum.BACK.getStatus());
        detail.setReviewStatus(ReviewStatusEnum.BACK.getCode());
        detail.setCurrentAttachmentId(502L);

        com.gxpublish.brain.editorial.domain.vo.EditorialAttachmentVo firstAttachment = new com.gxpublish.brain.editorial.domain.vo.EditorialAttachmentVo();
        firstAttachment.setId(501L);
        firstAttachment.setOssId("oss-501");
        firstAttachment.setFileName("old.pdf");
        firstAttachment.setVersion(1);

        com.gxpublish.brain.editorial.domain.vo.EditorialAttachmentVo secondAttachment = new com.gxpublish.brain.editorial.domain.vo.EditorialAttachmentVo();
        secondAttachment.setId(502L);
        secondAttachment.setOssId("oss-502");
        secondAttachment.setFileName("new.pdf");
        secondAttachment.setVersion(2);

        when(baseMapper.selectVoById(reviewId)).thenReturn(detail);
        when(attachmentMapper.selectVoList(any())).thenReturn(List.of(firstAttachment, secondAttachment));
        when(linkMapper.selectVoList(any())).thenReturn(List.of());
        when(historyMapper.selectVoList(any())).thenReturn(List.of());

        LoginUser loginUser = new LoginUser();
        loginUser.setUserId(900L);
        try (MockedStatic<LoginHelper> loginHelper = org.mockito.Mockito.mockStatic(LoginHelper.class)) {
            loginHelper.when(LoginHelper::getLoginUser).thenReturn(loginUser);
            loginHelper.when(LoginHelper::isSuperAdmin).thenReturn(false);

            EditorialReviewDetailVo result = service.queryById(reviewId);

            assertEquals(2, result.getAttachmentList().size());
            assertEquals(502L, result.getAttachment().getId());
            assertEquals("new.pdf", result.getAttachment().getFileName());
        }
    }

    @Test
    void shouldWriteHumanReadableOperationTypeForApprovalNode() {
        Long reviewId = 440L;
        EditorialReview review = new EditorialReview();
        review.setId(reviewId);
        review.setStatus(BusinessStatusEnum.WAITING.getStatus());
        review.setReviewStatus(ReviewStatusEnum.WAITING_FIRST.getCode());
        review.setApplyCode("APPLY-440");

        when(baseMapper.selectById(reviewId)).thenReturn(review);
        when(baseMapper.updateById(any(EditorialReview.class))).thenReturn(1);

        ProcessEvent processEvent = new ProcessEvent();
        processEvent.setFlowCode(EditorialReviewWorkflowDefinition.FLOW_CODE);
        processEvent.setBusinessId(String.valueOf(reviewId));
        processEvent.setStatus(BusinessStatusEnum.WAITING.getStatus());
        processEvent.setNodeCode("first-review-node");
        processEvent.setNodeName("一级审批");
        processEvent.setParams(Map.of("message", "同意", "handler", "wangwu"));
        processEvent.setSubmit(Boolean.FALSE);

        try (MockedStatic<LoginHelper> loginHelper = org.mockito.Mockito.mockStatic(LoginHelper.class)) {
            loginHelper.when(LoginHelper::getUserId).thenReturn(2000L);
            loginHelper.when(LoginHelper::getUsername).thenReturn("wangwu");
            service.processHandler(processEvent);
        }

        ArgumentCaptor<EditorialHistory> historyCaptor = ArgumentCaptor.forClass(EditorialHistory.class);
        verify(historyMapper).insert(historyCaptor.capture());
        assertEquals("一级审批审批通过", historyCaptor.getValue().getOperateType());
        assertTrue(historyCaptor.getValue().getFieldDiff().containsKey("node"));
        assertTrue(historyCaptor.getValue().getFieldDiff().containsKey("message"));
    }

    private EditorialWorkflowRoleRefVo roleRef(Long roleId, String roleKey) {
        EditorialWorkflowRoleRefVo refVo = new EditorialWorkflowRoleRefVo();
        refVo.setRoleId(roleId);
        refVo.setRoleKey(roleKey);
        return refVo;
    }
}
