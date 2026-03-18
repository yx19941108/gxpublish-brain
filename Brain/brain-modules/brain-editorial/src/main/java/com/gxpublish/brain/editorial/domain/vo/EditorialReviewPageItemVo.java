package com.gxpublish.brain.editorial.domain.vo;

import cn.idev.excel.annotation.ExcelIgnoreUnannotated;
import cn.idev.excel.annotation.ExcelProperty;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.gxpublish.brain.common.translation.annotation.Translation;
import com.gxpublish.brain.common.translation.constant.TransConstant;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;

/**
 * 审校列表页对象。
 */
@Data
@ExcelIgnoreUnannotated
@AutoMapper(target = com.gxpublish.brain.editorial.domain.EditorialReview.class)
public class EditorialReviewPageItemVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @ExcelProperty(value = "主键ID")
    private Long id;

    @ExcelProperty(value = "申请标题")
    private String title;

    @ExcelProperty(value = "流转状态")
    private String status;

    @ExcelProperty(value = "业务状态")
    private Integer reviewStatus;

    @ExcelProperty(value = "流程类型")
    private String processType;

    @ExcelProperty(value = "创建时间")
    private Date createTime;

    private Boolean canEdit;

    private EditorialNamedRefVo user;

    private EditorialNamedRefVo dept;

    @JsonIgnore
    private Long userId;

    @JsonIgnore
    @Translation(type = TransConstant.USER_ID_TO_NICKNAME, mapper = "userId")
    private String userName;

    @JsonIgnore
    private Long deptId;

    @JsonIgnore
    @Translation(type = TransConstant.DEPT_ID_TO_NAME, mapper = "deptId")
    private String deptName;
}
