package com.gxpublish.brain.editorial.domain;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;

/**
 * 审校关联链接对象 brain_editorial_link
 *
 * @author gxpublish
 */
@Data
@TableName("brain_editorial_link")
public class EditorialLink implements Serializable {

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
     * 链接地址
     */
    private String url;

    /**
     * 描述
     */
    private String description;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private Date createTime;

}
