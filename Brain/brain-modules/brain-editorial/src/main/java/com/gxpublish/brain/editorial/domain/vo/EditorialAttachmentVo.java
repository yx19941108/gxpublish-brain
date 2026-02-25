package com.gxpublish.brain.editorial.domain.vo;

import com.gxpublish.brain.common.translation.annotation.Translation;
import com.gxpublish.brain.common.translation.constant.TransConstant;
import com.gxpublish.brain.editorial.domain.EditorialAttachment;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;

/**
 * 审校附件视图对象
 *
 * @author gxpublish
 */
@Data
@AutoMapper(target = EditorialAttachment.class)
public class EditorialAttachmentVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long id;
    private Long reviewId;
    private String fileName;
    private String ossId;
    @Translation(type = TransConstant.OSS_ID_TO_URL, mapper = "ossId")
    private String fileUrl;
    private Long fileSize;
    private Integer version;
    private Long uploaderId;
    private String uploaderName; // 需要关联查询
    private Date createTime;

}
