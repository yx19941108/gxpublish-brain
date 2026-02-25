package com.gxpublish.brain.editorial.domain;

import com.baomidou.mybatisplus.annotation.*;
import com.gxpublish.brain.common.mybatis.core.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;

/**
 * 编辑部审校业务对象 brain_editorial_review
 *
 * @author gxpublish
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("brain_editorial_review")
public class EditorialReview extends BaseEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @TableId(value = "id")
    private Long id;

    /**
     * 申请标题
     */
    private String title;

    /**
     * 申请内容
     */
    private String content;

    /**
     * 部门ID
     */
    private Long deptId;

    /**
     * 发起人ID
     */
    private Long userId;

    /**
     * 业务状态: DRAFT, WAITING, APPROVED, REJECTED
     */
    private String status;

    /**
     * 流程实例关联码
     */
    private String applyCode;

    /**
     * 流程类型
     */
    private String processType;

    /**
     * 当前附件ID
     */
    private Long currentAttachmentId;

    /**
     * 删除标志（0代表存在 2代表删除）
     */
    @TableLogic
    private String delFlag;

    /**
     * 备注
     */
    private String remark;

}
