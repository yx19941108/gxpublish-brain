package com.gxpublish.brain.editorial.domain.vo;

import cn.idev.excel.annotation.ExcelIgnoreUnannotated;
import cn.idev.excel.annotation.ExcelProperty;
import com.gxpublish.brain.common.translation.annotation.Translation;
import com.gxpublish.brain.common.translation.constant.TransConstant;
import com.gxpublish.brain.editorial.domain.EditorialReview;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * 编辑部审校视图对象 brain_editorial_review
 *
 * @author gxpublish
 */
@Data
@ExcelIgnoreUnannotated
@AutoMapper(target = EditorialReview.class)
public class EditorialReviewVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @ExcelProperty(value = "主键ID")
    private Long id;

    /**
     * 申请标题
     */
    @ExcelProperty(value = "申请标题")
    private String title;

    /**
     * 申请内容
     */
    @ExcelProperty(value = "申请内容")
    private String content;

    /**
     * 部门ID
     */
    @ExcelProperty(value = "部门ID")
    private Long deptId;

    /**
     * 发起人ID
     */
    @ExcelProperty(value = "发起人ID")
    private Long userId;

    /**
     * 发起人姓名
     */
    @Translation(type = TransConstant.USER_ID_TO_NICKNAME, mapper = "userId")
    @ExcelProperty(value = "发起人姓名")
    private String userName;

    /**
     * 部门名称
     */
    @Translation(type = TransConstant.DEPT_ID_TO_NAME, mapper = "deptId")
    @ExcelProperty(value = "部门名称")
    private String deptName;

    /**
     * 业务状态
     */
    @ExcelProperty(value = "业务状态")
    private String status;

    /**
     * 流程类型
     */
    @ExcelProperty(value = "流程类型")
    private String processType;

    /**
     * 流程实例关联码
     */
    @ExcelProperty(value = "流程实例关联码")
    private String applyCode;

    /**
     * 当前附件ID
     */
    private Long currentAttachmentId;

    /**
     * 备注
     */
    @ExcelProperty(value = "备注")
    private String remark;

    /**
     * 创建时间
     */
    @ExcelProperty(value = "创建时间")
    private Date createTime;

    /**
     * 当前附件详情
     */
    private EditorialAttachmentVo attachment;

    /**
     * 关联链接列表
     */
    private List<EditorialLinkVo> linkList;

}
