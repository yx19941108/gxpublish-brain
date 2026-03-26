package com.gxpublish.brain.manuscript.review.domain.command;

import java.util.List;

import com.gxpublish.brain.manuscript.review.domain.command.SubmitAndStartManuscriptReviewCommand.SubmitAttachmentResourceCommand;
import com.gxpublish.brain.manuscript.review.domain.command.SubmitAndStartManuscriptReviewCommand.SubmitExternalLinkCommand;
import com.gxpublish.brain.manuscript.review.domain.enums.ManuscriptReviewProcessType;

import lombok.Builder;
import lombok.Data;

/**
 * 稿件审校修改保存命令。
 *
 * <p>v6.26 追加改动：修改保存与新增提交一样，支持一次性携带主表字段、
 * 追加附件/视频参数和追加外链参数，但不触发 BPM 状态推进。</p>
 *
 * @author Codex
 * @since 2026-03-26
 */
@Data
@Builder
public class UpdateManuscriptReviewCommand {

    /**
     * 流程单主键。
     */
    private Long id;

    /**
     * 流程类型。
     */
    private ManuscriptReviewProcessType processType;

    /**
     * 外部稿件编号。
     */
    private String externalManuscriptCode;

    /**
     * 标题。
     */
    private String title;

    /**
     * 媒体栏目。
     */
    private String mediaChannel;

    /**
     * 报送部门。
     */
    private String submitDepartment;

    /**
     * 作者。
     */
    private String authorName;

    /**
     * 备注说明。
     */
    private String remark;

    /**
     * 正文内容。
     */
    private String contentBody;

    /**
     * 本次保存新追加的附件/视频参数。
     */
    private List<SubmitAttachmentResourceCommand> attachmentResources;

    /**
     * 本次保存新追加的外链参数。
     */
    private List<SubmitExternalLinkCommand> externalLinks;
}
