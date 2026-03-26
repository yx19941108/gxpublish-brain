package com.gxpublish.brain.manuscript.review.domain.command;

import java.util.List;

import com.gxpublish.brain.manuscript.review.domain.enums.ManuscriptReviewProcessType;

import lombok.Builder;
import lombok.Data;

/**
 * 新增并提交一体化命令对象。
 *
 * <p>v6.26 追加改动：用于承载新增页点击提交时的完整载荷，
 * 包含主表字段、附件/视频参数和外链参数。</p>
 *
 * @author Codex
 * @since 2026-03-26
 */
@Data
@Builder
public class SubmitAndStartManuscriptReviewCommand {

    /**
     * 兼容旧草稿提交时的主键。
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
     * 附件/视频参数列表。
     */
    private List<SubmitAttachmentResourceCommand> attachmentResources;

    /**
     * 外链参数列表。
     */
    private List<SubmitExternalLinkCommand> externalLinks;

    /**
     * 提交场景下的附件/视频资源命令。
     */
    @Data
    @Builder
    public static class SubmitAttachmentResourceCommand {

        /**
         * 资源类型，仅允许附件或视频。
         */
        private String resourceType;

        /**
         * 显示名称。
         */
        private String displayName;

        /**
         * OSS 主键。
         */
        private Long ossId;
    }

    /**
     * 提交场景下的外链资源命令。
     */
    @Data
    @Builder
    public static class SubmitExternalLinkCommand {

        /**
         * 显示名称。
         */
        private String displayName;

        /**
         * 外链地址。
         */
        private String externalUrl;
    }
}
