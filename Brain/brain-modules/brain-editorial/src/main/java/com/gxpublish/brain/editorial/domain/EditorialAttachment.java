package com.gxpublish.brain.editorial.domain;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;

/**
 * 审校附件对象 brain_editorial_attachment
 *
 * @author gxpublish
 */
@Data
@TableName("brain_editorial_attachment")
public class EditorialAttachment implements Serializable {

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
     * 文件名
     */
    private String fileName;

    /**
     * OSS资源ID
     */
    private String ossId;

    /**
     * 文件地址
     */
    private String fileUrl;

    /**
     * 文件大小
     */
    private Long fileSize;

    /**
     * 版本号
     */
    private Integer version;

    /**
     * 上传人ID
     */
    private Long uploaderId;

    /**
     * 上传时间
     */
    @TableField(fill = FieldFill.INSERT)
    private Date createTime;

    /**
     * 删除标志
     */
    @TableLogic
    private String delFlag;

}
