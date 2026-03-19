package com.gxpublish.brain.editorial.domain.bo;

import com.gxpublish.brain.editorial.domain.EditorialAttachment;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;

/**
 * 审校附件业务对象。
 */
@Data
@AutoMapper(target = EditorialAttachment.class, reverseConvertGenerate = false)
public class EditorialAttachmentBo {

    /**
     * 主键ID（已存在附件时用于兼容回传）。
     */
    private Long id;

    /**
     * OSS资源ID。
     */
    private String ossId;

    /**
     * 文件名。
     */
    private String fileName;

    /**
     * 文件地址。
     */
    private String fileUrl;

    /**
     * 文件大小。
     */
    private Long fileSize;
}
