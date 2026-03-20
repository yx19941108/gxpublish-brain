package com.gxpublish.brain.editorial.support;

import com.gxpublish.brain.common.core.domain.dto.RoleDTO;
import com.gxpublish.brain.common.core.domain.event.ProcessEvent;
import com.gxpublish.brain.common.core.domain.model.LoginUser;
import com.gxpublish.brain.common.core.enums.BusinessStatusEnum;
import com.gxpublish.brain.common.core.service.DeptService;
import com.gxpublish.brain.editorial.domain.EditorialHistory;
import com.gxpublish.brain.editorial.domain.EditorialReview;
import com.gxpublish.brain.editorial.domain.vo.EditorialAttachmentVo;
import com.gxpublish.brain.editorial.domain.vo.EditorialLinkVo;
import com.gxpublish.brain.editorial.enums.ReviewStatusEnum;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@Tag("dev")
@ExtendWith(MockitoExtension.class)
class EditorialHistoryFactoryTest {

    @Mock
    private DeptService deptService;

    private EditorialHistoryFactory historyFactory;

    @BeforeEach
    void setUp() {
        historyFactory = new EditorialHistoryFactory(deptService);
    }

    @Test
    void shouldBuildCreateHistorySummary() {
        EditorialReview review = new EditorialReview();
        review.setId(1L);
        review.setTitle("关于春季选题的申请");

        EditorialHistory history = historyFactory.buildCreateHistory(review, applicant("三审三校发起人"), 100L, "张三");

        assertEquals("CREATE", history.getEventType());
        assertEquals("发起人创建申请关于春季选题的申请", history.getOperateType());
        assertEquals("发起人", history.getOperatorRoleName());
    }

    @Test
    void shouldBuildApprovalHistorySummaryForApproveBackTerminateAndCancel() {
        EditorialReview review = new EditorialReview();
        review.setId(2L);
        review.setReviewStatus(ReviewStatusEnum.WAITING_FIRST.getCode());

        ProcessEvent approveEvent = processEvent(BusinessStatusEnum.WAITING.getStatus(), "first-review-node", "一级审批", "同意");
        EditorialHistory approveHistory = historyFactory.buildProcessHistory(review, approveEvent, approver("editorial_first_level_approver", "一级审批人"), 200L, "王五");
        assertEquals("王五（一级审批人）审批通过了审批", approveHistory.getOperateType());

        ProcessEvent backEvent = processEvent(BusinessStatusEnum.BACK.getStatus(), "first-review-node", "一级审批", "退回");
        EditorialHistory backHistory = historyFactory.buildProcessHistory(review, backEvent, approver("editorial_first_level_approver", "一级审批人"), 200L, "王五");
        assertEquals("王五（一级审批人）退回了审批", backHistory.getOperateType());

        ProcessEvent terminateEvent = processEvent(BusinessStatusEnum.TERMINATION.getStatus(), "first-review-node", "一级审批", "终止");
        EditorialHistory terminateHistory = historyFactory.buildProcessHistory(review, terminateEvent, approver("editorial_first_level_approver", "一级审批人"), 200L, "王五");
        assertEquals("王五（一级审批人）终止了审批", terminateHistory.getOperateType());

        ProcessEvent cancelEvent = processEvent(BusinessStatusEnum.CANCEL.getStatus(), "applicant-node", "审校申请", "撤销");
        EditorialHistory cancelHistory = historyFactory.buildProcessHistory(review, cancelEvent, applicant("发起人"), 100L, "张三");
        assertEquals("张三（发起人）撤销了审批", cancelHistory.getOperateType());
        assertEquals("APPROVAL", cancelHistory.getEventType());
        assertEquals("撤销", cancelHistory.getFieldDiff().get("comment"));
    }

    @Test
    void shouldBuildModifyHistoryWithChineseDiffAndDisplayValues() {
        EditorialReview oldReview = new EditorialReview();
        oldReview.setId(3L);
        oldReview.setReviewStatus(ReviewStatusEnum.WAITING_FIRST.getCode());
        oldReview.setTitle("旧标题");
        oldReview.setContent("旧内容");
        oldReview.setDeptId(10L);

        EditorialReview newReview = new EditorialReview();
        newReview.setId(3L);
        newReview.setReviewStatus(ReviewStatusEnum.WAITING_FIRST.getCode());
        newReview.setTitle("新标题");
        newReview.setContent("新内容");
        newReview.setDeptId(20L);

        EditorialLinkVo oldLink = new EditorialLinkVo();
        oldLink.setId(1L);
        oldLink.setDescription("旧链接");
        oldLink.setUrl("https://old.example.com");

        EditorialLinkVo newLink1 = new EditorialLinkVo();
        newLink1.setId(1L);
        newLink1.setDescription("旧链接");
        newLink1.setUrl("https://old.example.com");
        EditorialLinkVo newLink2 = new EditorialLinkVo();
        newLink2.setId(2L);
        newLink2.setDescription("新增链接A");
        newLink2.setUrl("https://new-a.example.com");
        EditorialLinkVo newLink3 = new EditorialLinkVo();
        newLink3.setId(3L);
        newLink3.setDescription("新增链接B");
        newLink3.setUrl("https://new-b.example.com");

        EditorialAttachmentVo oldAttachment = new EditorialAttachmentVo();
        oldAttachment.setId(1L);
        oldAttachment.setOssId("old-oss");
        oldAttachment.setFileName("old.pdf");

        EditorialAttachmentVo newAttachment1 = new EditorialAttachmentVo();
        newAttachment1.setId(1L);
        newAttachment1.setOssId("old-oss");
        newAttachment1.setFileName("old.pdf");
        EditorialAttachmentVo newAttachment2 = new EditorialAttachmentVo();
        newAttachment2.setId(2L);
        newAttachment2.setOssId("new-oss-a");
        newAttachment2.setFileName("new-a.pdf");
        EditorialAttachmentVo newAttachment3 = new EditorialAttachmentVo();
        newAttachment3.setId(3L);
        newAttachment3.setOssId("new-oss-b");
        newAttachment3.setFileName("new-b.pdf");

        when(deptService.selectDeptNamesByIds(List.of(10L, 20L))).thenReturn(Map.of(10L, "编辑部", 20L, "总编室"));

        EditorialHistory history = historyFactory.buildModifyHistory(
            oldReview,
            newReview,
            List.of(oldLink),
            List.of(newLink1, newLink2, newLink3),
            List.of(oldAttachment),
            List.of(newAttachment1, newAttachment2, newAttachment3),
            approver("editorial_first_level_approver", "一级审批人"),
            200L,
            "王五");

        assertEquals("MODIFY", history.getEventType());
        assertEquals("王五（一级审批人）修改了记录", history.getOperateType());
        assertEquals("一级审批人", history.getOperatorRoleName());
        assertTrue(String.valueOf(history.getFieldDiff()).contains("申请标题"));
        assertTrue(String.valueOf(history.getFieldDiff()).contains("申请内容"));
        assertTrue(String.valueOf(history.getFieldDiff()).contains("部门"));
        assertTrue(String.valueOf(history.getFieldDiff()).contains("编辑部"));
        assertTrue(String.valueOf(history.getFieldDiff()).contains("总编室"));
        assertTrue(String.valueOf(history.getFieldDiff()).contains("新增附件"));
        assertTrue(String.valueOf(history.getFieldDiff()).contains("新增关联链接"));
    }

    @Test
    void shouldBuildModifyHistoryWhenDeptIdsAreBothNull() {
        EditorialReview oldReview = new EditorialReview();
        oldReview.setId(4L);
        oldReview.setTitle("旧标题");
        oldReview.setContent("旧内容");

        EditorialReview newReview = new EditorialReview();
        newReview.setId(4L);
        newReview.setTitle("新标题");
        newReview.setContent("新内容");

        EditorialHistory history = historyFactory.buildModifyHistory(
            oldReview,
            newReview,
            List.of(),
            List.of(),
            List.of(),
            List.of(),
            applicant("发起人"),
            100L,
            "张三");

        assertEquals("MODIFY", history.getEventType());
        assertEquals("张三（发起人）修改了记录", history.getOperateType());
        assertTrue(String.valueOf(history.getFieldDiff()).contains("申请标题"));
        assertTrue(String.valueOf(history.getFieldDiff()).contains("申请内容"));
    }

    private LoginUser applicant(String roleName) {
        return roleUser("editorial_review_applicant", roleName);
    }

    private LoginUser approver(String roleKey, String roleName) {
        return roleUser(roleKey, roleName);
    }

    private LoginUser roleUser(String roleKey, String roleName) {
        LoginUser loginUser = new LoginUser();
        RoleDTO role = new RoleDTO();
        role.setRoleKey(roleKey);
        role.setRoleName(roleName);
        loginUser.setRoles(List.of(role));
        return loginUser;
    }

    private ProcessEvent processEvent(String status, String nodeCode, String nodeName, String comment) {
        ProcessEvent event = new ProcessEvent();
        event.setStatus(status);
        event.setNodeCode(nodeCode);
        event.setNodeName(nodeName);
        event.setParams(Map.of("message", comment));
        return event;
    }
}
