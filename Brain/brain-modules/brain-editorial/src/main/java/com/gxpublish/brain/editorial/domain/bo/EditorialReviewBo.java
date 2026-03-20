package com.gxpublish.brain.editorial.domain.bo;

import com.gxpublish.brain.common.core.validate.AddGroup;
import com.gxpublish.brain.common.core.validate.EditGroup;
import com.gxpublish.brain.common.mybatis.core.domain.BaseEntity;
import com.gxpublish.brain.editorial.domain.EditorialReview;
import com.gxpublish.brain.editorial.domain.annotation.EditorialDiffField;
import io.github.linpeilie.annotations.AutoMapper;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * 编辑部审校业务对象 brain_editorial_review
 *
 * @author gxpublish
 */
@Data
@EqualsAndHashCode(callSuper = true)
@AutoMapper(target = EditorialReview.class, reverseConvertGenerate = false)
public class EditorialReviewBo extends BaseEntity {

    /**
     * 主键ID
     */
    @NotNull(message = "主键不能为空", groups = { EditGroup.class })
    private Long id;

    /**
     * 申请标题
     */
    @EditorialDiffField("申请标题")
    @NotBlank(message = "申请标题不能为空", groups = { AddGroup.class, EditGroup.class })
    private String title;

    /**
     * 申请内容
     */
    @EditorialDiffField("申请内容")
    @NotBlank(message = "申请内容不能为空", groups = { AddGroup.class, EditGroup.class })
    private String content;

    /**
     * 部门ID
     */
    @EditorialDiffField("部门")
    private Long deptId;

    /**
     * 用户ID (申请人)
     */
    private Long userId;

    /**
     * 业务状态
     */
    private String status;

    /**
     * 三审三校专属精细业务状态 (如 10待一审, 20待二审)
     */
    private Integer reviewStatus;

    /**
     * 流程类型
     */
    @NotBlank(message = "流程类型不能为空", groups = { AddGroup.class, EditGroup.class })
    private String processType;

    /**
     * 流程实例关联码
     */
    private String applyCode;

    /**
     * 流程编码
     */
    private String flowCode;

    /**
     * 备注
     */
    private String remark;

    /**
     * 附件OSS ID (如果有新上传附件)
     */
    private String attachmentOssId;

    /**
     * 附件名称 (如果有新上传附件)
     */
    private String attachmentFileName;

    /**
     * 附件地址 (如果有新上传附件)
     */
    private String attachmentFileUrl;

    /**
     * 附件大小 (如果有新上传附件)
     */
    private Long attachmentFileSize;

    /**
     * 关联链接列表
     */
    private List<EditorialLinkBo> linkList;

    /**
     * 多附件主合同，兼容旧单附件字段。
     */
    private List<EditorialAttachmentBo> attachmentList;

}
