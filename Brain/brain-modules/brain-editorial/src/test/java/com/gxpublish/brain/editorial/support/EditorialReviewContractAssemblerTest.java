package com.gxpublish.brain.editorial.support;

import com.gxpublish.brain.editorial.domain.vo.EditorialHistoryVo;
import com.gxpublish.brain.editorial.domain.vo.EditorialReviewDetailVo;
import com.gxpublish.brain.editorial.domain.vo.EditorialReviewPageItemVo;
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
        detail.setUserId(11L);
        detail.setUserName("申请人甲");
        detail.setDeptId(22L);
        detail.setDeptName("编辑部");
        EditorialHistoryVo history = new EditorialHistoryVo();
        history.setOperateType("submit");

        EditorialReviewContractAssembler.populateDetailContract(detail, List.of(history));

        assertEquals("editorial_review_flow", detail.getApprovalContext().getFlowCode());
        assertEquals("/editorial/review/detail", detail.getApprovalContext().getFormPath());
        assertEquals("AUDIT", detail.getApprovalContext().getProcessType());
        assertEquals("waiting", detail.getApprovalContext().getStatus());
        assertEquals(20, detail.getApprovalContext().getReviewStatus());
        assertIterableEquals(List.of(history), detail.getHistoryList());
        assertEquals("申请人甲", detail.getUser().getName());
        assertEquals("编辑部", detail.getDept().getName());
    }
}
