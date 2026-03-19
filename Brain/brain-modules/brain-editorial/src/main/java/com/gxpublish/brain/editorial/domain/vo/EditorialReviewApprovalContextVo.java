package com.gxpublish.brain.editorial.domain.vo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 审批壳上下文。
 */
@Data
public class EditorialReviewApprovalContextVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private String flowCode;

    private String formPath;

    private String applyCode;

    private String processType;

    private String status;

    private Integer reviewStatus;

    private Boolean canEdit;

    private Boolean canApprove;

    private Long taskId;

    private Long instanceId;
}
