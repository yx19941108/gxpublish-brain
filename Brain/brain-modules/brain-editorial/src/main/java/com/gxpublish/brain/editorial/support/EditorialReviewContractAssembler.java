package com.gxpublish.brain.editorial.support;

import cn.hutool.core.collection.CollUtil;
import com.gxpublish.brain.editorial.domain.vo.EditorialHistoryVo;
import com.gxpublish.brain.editorial.domain.vo.EditorialNamedRefVo;
import com.gxpublish.brain.editorial.domain.vo.EditorialReviewApprovalContextVo;
import com.gxpublish.brain.editorial.domain.vo.EditorialReviewDetailVo;
import com.gxpublish.brain.editorial.domain.vo.EditorialReviewPageItemVo;

import java.util.List;

/**
 * 审校 contract 组装器。
 */
public final class EditorialReviewContractAssembler {

    private EditorialReviewContractAssembler() {
    }

    public static void populatePageContract(EditorialReviewPageItemVo pageItem) {
        if (pageItem == null) {
            return;
        }
        pageItem.setUser(buildNamedRef(pageItem.getUserId(), pageItem.getUserName()));
        pageItem.setDept(buildNamedRef(pageItem.getDeptId(), pageItem.getDeptName()));
    }

    public static void populateDetailContract(EditorialReviewDetailVo detail,
                                              List<EditorialHistoryVo> historyList) {
        if (detail == null) {
            return;
        }
        populatePageContract(detail);
        detail.setHistoryList(CollUtil.isEmpty(historyList) ? List.of() : historyList);
        EditorialReviewApprovalContextVo approvalContext = new EditorialReviewApprovalContextVo();
        approvalContext.setFlowCode(EditorialReviewWorkflowDefinition.FLOW_CODE);
        approvalContext.setFormPath(EditorialReviewWorkflowDefinition.DETAIL_FORM_PATH);
        approvalContext.setApplyCode(detail.getApplyCode());
        approvalContext.setProcessType(detail.getProcessType());
        approvalContext.setStatus(detail.getStatus());
        approvalContext.setReviewStatus(detail.getReviewStatus());
        approvalContext.setCanEdit(detail.getCanEdit());
        detail.setApprovalContext(approvalContext);
    }

    private static EditorialNamedRefVo buildNamedRef(Long id, String name) {
        if (id == null && name == null) {
            return null;
        }
        EditorialNamedRefVo refVo = new EditorialNamedRefVo();
        refVo.setId(id);
        refVo.setName(name);
        return refVo;
    }
}
