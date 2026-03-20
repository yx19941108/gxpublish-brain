package com.gxpublish.brain.editorial.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.gxpublish.brain.common.mybatis.core.page.PageQuery;
import com.gxpublish.brain.common.mybatis.core.page.TableDataInfo;
import com.gxpublish.brain.common.core.domain.dto.StartProcessDTO;
import com.gxpublish.brain.common.core.domain.dto.RoleDTO;
import com.gxpublish.brain.common.core.domain.event.ProcessEvent;
import com.gxpublish.brain.common.core.domain.model.LoginUser;
import com.gxpublish.brain.common.core.enums.BusinessStatusEnum;
import com.gxpublish.brain.common.core.exception.ServiceException;
import com.gxpublish.brain.common.core.service.DeptService;
import com.gxpublish.brain.common.core.service.WorkflowService;
import com.gxpublish.brain.common.core.utils.SpringUtils;
import com.gxpublish.brain.common.satoken.utils.LoginHelper;
import com.gxpublish.brain.editorial.domain.EditorialHistory;
import com.gxpublish.brain.editorial.domain.EditorialLink;
import com.gxpublish.brain.editorial.domain.EditorialReview;
import com.gxpublish.brain.editorial.domain.bo.EditorialAttachmentBo;
import com.gxpublish.brain.editorial.domain.bo.EditorialLinkBo;
import com.gxpublish.brain.editorial.domain.bo.EditorialReviewBo;
import com.gxpublish.brain.editorial.domain.param.EditorialScopeParam;
import com.gxpublish.brain.editorial.domain.vo.EditorialAttachmentVo;
import com.gxpublish.brain.editorial.domain.vo.EditorialHistoryVo;
import com.gxpublish.brain.editorial.domain.vo.EditorialLinkVo;
import com.gxpublish.brain.editorial.domain.vo.EditorialReviewPageItemVo;
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
import com.gxpublish.brain.editorial.support.EditorialHistoryFactory;
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
import static org.mockito.Mockito.never;
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
    @Mock
    private DeptService deptService;

    private EditorialReviewServiceImpl service;

    @BeforeEach
    void setUp() {
        EditorialHistoryFactory historyFactory = new EditorialHistoryFactory(deptService);
        service = new EditorialReviewServiceImpl(
            baseMapper,
            attachmentMapper,
            linkMapper,
            historyMapper,
            workflowService,
            dataScopeFactory,
            workflowRoleMapper,
            historyFactory
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
        assertEquals("MODIFY", historyCaptor.getValue().getEventType());
        assertEquals("wangwu（一级审批人）修改了记录", historyCaptor.getValue().getOperateType());
        assertTrue(String.valueOf(historyCaptor.getValue().getFieldDiff()).contains("申请标题"));
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
            loginHelper.when(LoginHelper::getUsername).thenReturn("王五");
            LoginUser loginUser = new LoginUser();
            RoleDTO approverRole = new RoleDTO();
            approverRole.setRoleKey("editorial_first_level_approver");
            approverRole.setRoleName("一级审批人");
            loginUser.setRoles(List.of(approverRole));
            loginHelper.when(LoginHelper::getLoginUser).thenReturn(loginUser);
            service.processHandler(processEvent);
        }

        ArgumentCaptor<EditorialHistory> historyCaptor = ArgumentCaptor.forClass(EditorialHistory.class);
        verify(historyMapper).insert(historyCaptor.capture());
        assertEquals("王五（一级审批人）审批通过了审批", historyCaptor.getValue().getOperateType());
        assertEquals("APPROVAL", historyCaptor.getValue().getEventType());
        assertEquals("一级审批人", historyCaptor.getValue().getOperatorRoleName());
        assertEquals("同意", historyCaptor.getValue().getFieldDiff().get("comment"));
    }

    @Test
    void shouldAppendNewLinksInsteadOfDeletingExistingLinksOnUpdate() throws Exception {
        Long reviewId = 520L;
        Method method = EditorialReviewServiceImpl.class.getMethod("updateByBo", EditorialReviewBo.class);

        EditorialReview existing = new EditorialReview();
        existing.setId(reviewId);
        existing.setStatus(BusinessStatusEnum.DRAFT.getStatus());
        existing.setReviewStatus(ReviewStatusEnum.DRAFT.getCode());
        existing.setCreateBy(1000L);
        existing.setUserId(1000L);
        existing.setTitle("旧标题");
        existing.setContent("旧内容");
        existing.setDeptId(2000L);
        existing.setProcessType("AUDIT");

        EditorialReview refreshed = new EditorialReview();
        refreshed.setId(reviewId);
        refreshed.setStatus(BusinessStatusEnum.DRAFT.getStatus());
        refreshed.setReviewStatus(ReviewStatusEnum.DRAFT.getCode());
        refreshed.setCreateBy(1000L);
        refreshed.setUserId(1000L);
        refreshed.setTitle("旧标题");
        refreshed.setContent("旧内容");
        refreshed.setDeptId(2000L);
        refreshed.setProcessType("AUDIT");

        EditorialReviewDetailVo detail = new EditorialReviewDetailVo();
        detail.setId(reviewId);
        detail.setStatus(BusinessStatusEnum.DRAFT.getStatus());
        detail.setReviewStatus(ReviewStatusEnum.DRAFT.getCode());
        detail.setUserId(1000L);

        EditorialLinkVo oldLink = new EditorialLinkVo();
        oldLink.setId(1L);
        oldLink.setReviewId(reviewId);
        oldLink.setUrl("https://old.example.com");
        oldLink.setDescription("旧链接");

        EditorialLinkVo newLink = new EditorialLinkVo();
        newLink.setId(2L);
        newLink.setReviewId(reviewId);
        newLink.setUrl("https://new.example.com");
        newLink.setDescription("新链接");

        when(baseMapper.selectById(reviewId)).thenReturn(existing, existing, refreshed);
        when(baseMapper.selectVoById(reviewId)).thenReturn(detail);
        when(baseMapper.updateById(any(EditorialReview.class))).thenReturn(1);
        when(linkMapper.selectVoList(any())).thenReturn(List.of(oldLink), List.of(oldLink), List.of(oldLink, newLink), List.of(oldLink, newLink));
        when(attachmentMapper.selectVoList(any())).thenReturn(List.of(), List.of(), List.of());
        when(historyMapper.selectVoList(any())).thenReturn(List.of());

        Converter converter = mock(Converter.class);
        org.mockito.Mockito.lenient().when(converter.convert(any(EditorialReviewBo.class), eq(EditorialReview.class))).thenAnswer(invocation -> {
            EditorialReviewBo source = invocation.getArgument(0);
            EditorialReview mapped = new EditorialReview();
            mapped.setId(source.getId());
            mapped.setTitle(source.getTitle());
            mapped.setContent(source.getContent());
            mapped.setDeptId(source.getDeptId());
            mapped.setProcessType(source.getProcessType());
            return mapped;
        });

        EditorialReviewBo bo = new EditorialReviewBo();
        bo.setId(reviewId);
        bo.setTitle("旧标题");
        bo.setContent("旧内容");
        bo.setDeptId(2000L);
        bo.setProcessType("AUDIT");
        EditorialLinkBo linkBo = new EditorialLinkBo();
        linkBo.setUrl("https://new.example.com");
        linkBo.setDescription("新链接");
        bo.setLinkList(List.of(linkBo));

        GenericApplicationContext context = new GenericApplicationContext();
        context.registerBean(Converter.class, () -> converter);
        context.registerBean(SpringUtils.class);
        context.refresh();
        try (MockedStatic<LoginHelper> loginHelper = org.mockito.Mockito.mockStatic(LoginHelper.class)) {
            LoginUser applicant = new LoginUser();
            applicant.setUserId(1000L);
            RoleDTO role = new RoleDTO();
            role.setRoleKey("editorial_review_applicant");
            role.setRoleName("发起人");
            applicant.setRoles(List.of(role));
            loginHelper.when(LoginHelper::getUserId).thenReturn(1000L);
            loginHelper.when(LoginHelper::isSuperAdmin).thenReturn(false);
            loginHelper.when(LoginHelper::getUsername).thenReturn("张三");
            loginHelper.when(LoginHelper::getLoginUser).thenReturn(applicant);

            EditorialReviewDetailVo result = assertInstanceOf(EditorialReviewDetailVo.class, method.invoke(service, bo));
            assertEquals(2, result.getLinkList().size());
        } finally {
            context.close();
        }

        verify(linkMapper, never()).delete(any());
        ArgumentCaptor<EditorialLink> linkCaptor = ArgumentCaptor.forClass(EditorialLink.class);
        verify(linkMapper).insert(linkCaptor.capture());
        assertEquals(reviewId, linkCaptor.getValue().getReviewId());
        assertEquals("https://new.example.com", linkCaptor.getValue().getUrl());

        ArgumentCaptor<EditorialHistory> historyCaptor = ArgumentCaptor.forClass(EditorialHistory.class);
        verify(historyMapper).insert(historyCaptor.capture());
        assertEquals("MODIFY", historyCaptor.getValue().getEventType());
        assertEquals("张三（发起人）修改了记录", historyCaptor.getValue().getOperateType());
        assertTrue(String.valueOf(historyCaptor.getValue().getFieldDiff()).contains("新增关联链接"));
    }

    @Test
    void shouldKeepExistingAttachmentsWhenUpdateOnlyCarriesNewAttachment() throws Exception {
        Long reviewId = 530L;
        Method method = EditorialReviewServiceImpl.class.getMethod("updateByBo", EditorialReviewBo.class);

        EditorialReview existing = new EditorialReview();
        existing.setId(reviewId);
        existing.setStatus(BusinessStatusEnum.BACK.getStatus());
        existing.setReviewStatus(ReviewStatusEnum.BACK.getCode());
        existing.setCreateBy(1000L);
        existing.setUserId(1000L);
        existing.setTitle("旧标题");
        existing.setContent("旧内容");
        existing.setDeptId(2000L);
        existing.setProcessType("AUDIT");
        existing.setCurrentAttachmentId(501L);

        EditorialReview attachmentCarrier = new EditorialReview();
        attachmentCarrier.setId(reviewId);
        attachmentCarrier.setCurrentAttachmentId(501L);

        EditorialReview refreshed = new EditorialReview();
        refreshed.setId(reviewId);
        refreshed.setStatus(BusinessStatusEnum.BACK.getStatus());
        refreshed.setReviewStatus(ReviewStatusEnum.BACK.getCode());
        refreshed.setCreateBy(1000L);
        refreshed.setUserId(1000L);
        refreshed.setTitle("旧标题");
        refreshed.setContent("旧内容");
        refreshed.setDeptId(2000L);
        refreshed.setProcessType("AUDIT");

        EditorialReviewDetailVo detail = new EditorialReviewDetailVo();
        detail.setId(reviewId);
        detail.setStatus(BusinessStatusEnum.BACK.getStatus());
        detail.setReviewStatus(ReviewStatusEnum.BACK.getCode());
        detail.setUserId(1000L);
        detail.setCurrentAttachmentId(502L);

        EditorialAttachmentVo oldAttachment = new EditorialAttachmentVo();
        oldAttachment.setId(501L);
        oldAttachment.setReviewId(reviewId);
        oldAttachment.setOssId("oss-old");
        oldAttachment.setFileName("old.pdf");
        oldAttachment.setVersion(1);

        EditorialAttachmentVo newAttachment = new EditorialAttachmentVo();
        newAttachment.setId(502L);
        newAttachment.setReviewId(reviewId);
        newAttachment.setOssId("oss-new");
        newAttachment.setFileName("new.pdf");
        newAttachment.setVersion(2);

        when(baseMapper.selectById(reviewId)).thenReturn(existing, attachmentCarrier, refreshed);
        when(baseMapper.selectVoById(reviewId)).thenReturn(detail);
        when(baseMapper.updateById(any(EditorialReview.class))).thenReturn(1);
        when(linkMapper.selectVoList(any())).thenReturn(List.of(), List.of(), List.of());
        when(attachmentMapper.selectVoList(any())).thenReturn(
            List.of(oldAttachment),
            List.of(oldAttachment),
            List.of(oldAttachment, newAttachment),
            List.of(oldAttachment, newAttachment));
        when(attachmentMapper.insert(any(com.gxpublish.brain.editorial.domain.EditorialAttachment.class))).thenAnswer(invocation -> {
            com.gxpublish.brain.editorial.domain.EditorialAttachment attachment = invocation.getArgument(0);
            attachment.setId(502L);
            return 1;
        });
        when(historyMapper.selectVoList(any())).thenReturn(List.of());

        Converter converter = mock(Converter.class);
        org.mockito.Mockito.lenient().when(converter.convert(any(EditorialReviewBo.class), eq(EditorialReview.class))).thenAnswer(invocation -> {
            EditorialReviewBo source = invocation.getArgument(0);
            EditorialReview mapped = new EditorialReview();
            mapped.setId(source.getId());
            mapped.setTitle(source.getTitle());
            mapped.setContent(source.getContent());
            mapped.setDeptId(source.getDeptId());
            mapped.setProcessType(source.getProcessType());
            return mapped;
        });

        EditorialReviewBo bo = new EditorialReviewBo();
        bo.setId(reviewId);
        bo.setTitle("旧标题");
        bo.setContent("旧内容");
        bo.setDeptId(2000L);
        bo.setProcessType("AUDIT");
        EditorialAttachmentBo attachmentBo = new EditorialAttachmentBo();
        attachmentBo.setOssId("oss-new");
        attachmentBo.setFileName("new.pdf");
        attachmentBo.setFileUrl("https://oss.example.com/new.pdf");
        attachmentBo.setFileSize(2048L);
        bo.setAttachmentList(List.of(attachmentBo));

        GenericApplicationContext context = new GenericApplicationContext();
        context.registerBean(Converter.class, () -> converter);
        context.registerBean(SpringUtils.class);
        context.refresh();
        try (MockedStatic<LoginHelper> loginHelper = org.mockito.Mockito.mockStatic(LoginHelper.class)) {
            LoginUser applicant = new LoginUser();
            applicant.setUserId(1000L);
            RoleDTO role = new RoleDTO();
            role.setRoleKey("editorial_review_applicant");
            role.setRoleName("发起人");
            applicant.setRoles(List.of(role));
            loginHelper.when(LoginHelper::getUserId).thenReturn(1000L);
            loginHelper.when(LoginHelper::isSuperAdmin).thenReturn(false);
            loginHelper.when(LoginHelper::getUsername).thenReturn("张三");
            loginHelper.when(LoginHelper::getLoginUser).thenReturn(applicant);

            EditorialReviewDetailVo result = assertInstanceOf(EditorialReviewDetailVo.class, method.invoke(service, bo));
            assertEquals(2, result.getAttachmentList().size());
            assertEquals(502L, result.getAttachment().getId());
        } finally {
            context.close();
        }

        ArgumentCaptor<EditorialHistory> historyCaptor = ArgumentCaptor.forClass(EditorialHistory.class);
        verify(historyMapper).insert(historyCaptor.capture());
        assertTrue(String.valueOf(historyCaptor.getValue().getFieldDiff()).contains("新增附件"));
    }

    @Test
    void shouldReturnDistinctHistoryItemsAndFullCollectionsInDetail() {
        Long reviewId = 540L;
        EditorialReviewDetailVo detail = new EditorialReviewDetailVo();
        detail.setId(reviewId);
        detail.setStatus(BusinessStatusEnum.WAITING.getStatus());
        detail.setReviewStatus(ReviewStatusEnum.WAITING_FIRST.getCode());
        detail.setProcessType("AUDIT");
        detail.setUserId(1000L);
        detail.setDeptId(2000L);
        detail.setDeptName("编辑部");
        detail.setCreateTime(new java.util.Date());
        detail.setUpdateTime(new java.util.Date());
        detail.setCurrentAttachmentId(502L);

        EditorialAttachmentVo firstAttachment = new EditorialAttachmentVo();
        firstAttachment.setId(501L);
        firstAttachment.setReviewId(reviewId);
        firstAttachment.setOssId("oss-501");
        firstAttachment.setFileName("old.pdf");
        firstAttachment.setVersion(1);

        EditorialAttachmentVo secondAttachment = new EditorialAttachmentVo();
        secondAttachment.setId(502L);
        secondAttachment.setReviewId(reviewId);
        secondAttachment.setOssId("oss-502");
        secondAttachment.setFileName("new.pdf");
        secondAttachment.setVersion(2);

        EditorialLinkVo firstLink = new EditorialLinkVo();
        firstLink.setId(1L);
        firstLink.setReviewId(reviewId);
        firstLink.setUrl("https://old.example.com");
        firstLink.setDescription("旧链接");

        EditorialLinkVo secondLink = new EditorialLinkVo();
        secondLink.setId(2L);
        secondLink.setReviewId(reviewId);
        secondLink.setUrl("https://new.example.com");
        secondLink.setDescription("新链接");

        EditorialHistoryVo modifyHistory = new EditorialHistoryVo();
        modifyHistory.setId(1L);
        modifyHistory.setReviewId(reviewId);
        modifyHistory.setEventType("MODIFY");
        modifyHistory.setOperateType("张三（发起人）修改了记录");
        modifyHistory.setFieldDiff(Map.of("items", List.of(Map.of("label", "申请标题"))));

        EditorialHistoryVo approvalHistory = new EditorialHistoryVo();
        approvalHistory.setId(2L);
        approvalHistory.setReviewId(reviewId);
        approvalHistory.setEventType("APPROVAL");
        approvalHistory.setOperateType("李四（一级审批人）审批通过了审批");
        approvalHistory.setFieldDiff(Map.of("comment", "同意"));

        when(baseMapper.selectVoById(reviewId)).thenReturn(detail);
        when(attachmentMapper.selectVoList(any())).thenReturn(List.of(firstAttachment, secondAttachment));
        when(linkMapper.selectVoList(any())).thenReturn(List.of(firstLink, secondLink));
        when(historyMapper.selectVoList(any())).thenReturn(List.of(modifyHistory, approvalHistory));

        LoginUser loginUser = new LoginUser();
        loginUser.setUserId(1000L);
        try (MockedStatic<LoginHelper> loginHelper = org.mockito.Mockito.mockStatic(LoginHelper.class)) {
            loginHelper.when(LoginHelper::getLoginUser).thenReturn(loginUser);
            loginHelper.when(LoginHelper::getUserId).thenReturn(1000L);
            loginHelper.when(LoginHelper::getUserIdStr).thenReturn("1000");
            loginHelper.when(LoginHelper::isSuperAdmin).thenReturn(false);

            EditorialReviewDetailVo result = service.queryById(reviewId);

            assertEquals(2, result.getAttachmentList().size());
            assertEquals(2, result.getLinkList().size());
            assertEquals(2, result.getHistoryList().size());
            assertEquals("MODIFY", result.getHistoryList().get(0).getEventType());
            assertEquals("APPROVAL", result.getHistoryList().get(1).getEventType());
            assertEquals("编辑部", result.getDept().getName());
            assertEquals(502L, result.getAttachment().getId());
        }
    }

    @Test
    void shouldPopulateWaitingApproverPageRowsWithApprovalContext() {
        EditorialReviewPageItemVo row = new EditorialReviewPageItemVo();
        row.setId(510L);
        row.setTitle("待一审批稿");
        row.setStatus(BusinessStatusEnum.WAITING.getStatus());
        row.setReviewStatus(ReviewStatusEnum.WAITING_FIRST.getCode());
        row.setProcessType("AUDIT");
        row.setUserId(1000L);
        row.setUserName("申请人甲");
        row.setDeptId(2000L);
        row.setDeptName("编辑部");

        Page<EditorialReviewPageItemVo> page = new Page<>();
        page.setRecords(List.of(row));
        page.setTotal(1L);

        EditorialReviewTaskContextVo taskContext = new EditorialReviewTaskContextVo();
        taskContext.setReviewId(510L);
        taskContext.setTaskId(9001L);
        taskContext.setInstanceId(9002L);
        taskContext.setNodeCode("first-review-node");
        taskContext.setNodeName("一级审批");

        when(dataScopeFactory.buildScopeParams()).thenReturn(new EditorialScopeParam());
        when(baseMapper.customSelectPage(any(Page.class), any(EditorialReviewBo.class), any(Map.class))).thenReturn(page);
        when(baseMapper.selectCurrentTaskContexts(eq(List.of("510")), eq("2000"))).thenReturn(List.of(taskContext));

        LoginUser loginUser = new LoginUser();
        loginUser.setUserId(2000L);
        RoleDTO approverRole = new RoleDTO();
        approverRole.setRoleKey("editorial_first_level_approver");
        loginUser.setRoles(List.of(approverRole));

        try (MockedStatic<LoginHelper> loginHelper = org.mockito.Mockito.mockStatic(LoginHelper.class)) {
            loginHelper.when(LoginHelper::getLoginUser).thenReturn(loginUser);
            loginHelper.when(LoginHelper::getUserId).thenReturn(2000L);
            loginHelper.when(LoginHelper::getUserIdStr).thenReturn("2000");
            loginHelper.when(LoginHelper::isSuperAdmin).thenReturn(false);

            TableDataInfo<EditorialReviewPageItemVo> result = service.queryPageList(new EditorialReviewBo(), new PageQuery(10, 1));

            assertEquals(1L, result.getTotal());
            EditorialReviewPageItemVo item = result.getRows().get(0);
            assertEquals(Boolean.TRUE, item.getCanEdit());
            assertEquals(Boolean.TRUE, item.getCanApprove());
            assertEquals(9001L, item.getTaskId());
            assertEquals(9002L, item.getInstanceId());
            assertEquals("申请人甲", item.getUser().getName());
            assertEquals("编辑部", item.getDept().getName());
        }
    }

    private EditorialWorkflowRoleRefVo roleRef(Long roleId, String roleKey) {
        EditorialWorkflowRoleRefVo refVo = new EditorialWorkflowRoleRefVo();
        refVo.setRoleId(roleId);
        refVo.setRoleKey(roleKey);
        return refVo;
    }
}
