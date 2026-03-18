package com.gxpublish.brain.editorial.domain.vo;

import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;
import java.util.Date;
import java.util.List;

/**
 * 审校详情对象。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@AutoMapper(target = com.gxpublish.brain.editorial.domain.EditorialReview.class)
public class EditorialReviewDetailVo extends EditorialReviewPageItemVo {

    @Serial
    private static final long serialVersionUID = 1L;

    private String content;

    private String applyCode;

    private Long currentAttachmentId;

    private String remark;

    private Date updateTime;

    private EditorialAttachmentVo attachment;

    private List<EditorialLinkVo> linkList;

    private List<EditorialHistoryVo> historyList;

    private EditorialReviewApprovalContextVo approvalContext;
}
