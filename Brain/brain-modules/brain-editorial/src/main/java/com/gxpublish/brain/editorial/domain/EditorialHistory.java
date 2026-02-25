package com.gxpublish.brain.editorial.domain;

import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;
import java.util.Map;

/**
 * 审校历史记录对象 brain_editorial_history
 *
 * @author gxpublish
 */
@Data
@TableName("brain_editorial_history")
public class EditorialHistory implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @TableId(value = "id")
    private Long id;

    /**
     * 关联申请ID
     */
    private Long reviewId;

    /**
     * 操作人ID
     */
    private Long operatorId;

    /**
     * 操作人姓名
     */
    private String operatorName;

    /**
     * 操作时间
     */
    @TableField(fill = FieldFill.INSERT)
    private Date operateTime;

    /**
     * 操作类型: SUBMIT, APPROVE, REJECT, MODIFY
     */
    private String operateType;

    /**
     * 字段差异JSON
     */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private Map<String, Object> fieldDiff;

}
