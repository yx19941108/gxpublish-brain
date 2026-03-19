package com.gxpublish.brain.editorial.support;

import com.gxpublish.brain.editorial.domain.vo.EditorialHistoryVo;
import com.gxpublish.brain.editorial.domain.vo.EditorialReviewDetailVo;
import com.gxpublish.brain.editorial.domain.vo.EditorialReviewPageItemVo;
import com.gxpublish.brain.editorial.domain.vo.EditorialReviewTaskContextVo;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;

@Tag("dev")
class EditorialReviewContractAssemblerTest {

    @Test
    void shouldPopulatePageContractParticipants() {
        EditorialReviewPageItemVo pageItem = new EditorialReviewPageItemVo();
        pageItem.setUserId(11L);
        pageItem.setUserName("申请人甲");
        pageItem.setDeptId(22L);
        pageItem.setDeptName("编辑部");

        EditorialReviewContractAssembler.populatePageContract(pageItem);

        assertEquals(11L, pageItem.getUser().getId());
        assertEquals("申请人甲", pageItem.getUser().getName());
        assertEquals(22L, pageItem.getDept().getId());
        assertEquals("编辑部", pageItem.getDept().getName());
    }

    @Test
    void shouldPopulateDetailApprovalContextAndHistory() {
        EditorialReviewDetailVo detail = new EditorialReviewDetailVo();
        detail.setApplyCode("APPLY-001");
        detail.setProcessType("AUDIT");
        detail.setStatus("waiting");
        detail.setReviewStatus(20);
        detail.setCanEdit(Boolean.TRUE);
        detail.setUserId(11L);
        detail.setUserName("申请人甲");
        detail.setDeptId(22L);
        detail.setDeptName("编辑部");
        EditorialHistoryVo history = new EditorialHistoryVo();
        history.setOperateType("submit");
        EditorialReviewTaskContextVo taskContext = new EditorialReviewTaskContextVo();
        taskContext.setTaskId(123L);
        taskContext.setInstanceId(456L);

        EditorialReviewContractAssembler.populateDetailContract(detail, List.of(history), taskContext, true);

        assertEquals("editorial_review_flow", detail.getApprovalContext().getFlowCode());
        assertEquals("/editorial/review/detail", detail.getApprovalContext().getFormPath());
        assertEquals("AUDIT", detail.getApprovalContext().getProcessType());
        assertEquals("waiting", detail.getApprovalContext().getStatus());
        assertEquals(20, detail.getApprovalContext().getReviewStatus());
        assertEquals(123L, detail.getApprovalContext().getTaskId());
        assertEquals(456L, detail.getApprovalContext().getInstanceId());
        assertEquals(Boolean.TRUE, detail.getApprovalContext().getCanApprove());
        assertEquals(Boolean.TRUE, detail.getApprovalContext().getCanEdit());
        assertIterableEquals(List.of(history), detail.getHistoryList());
        assertEquals("申请人甲", detail.getUser().getName());
        assertEquals("编辑部", detail.getDept().getName());
    }
}
