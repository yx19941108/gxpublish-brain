package com.gxpublish.brain.editorial.domain.vo;

import com.gxpublish.brain.editorial.domain.EditorialLink;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;

/**
 * 审校关联链接视图对象
 *
 * @author gxpublish
 */
@Data
@AutoMapper(target = EditorialLink.class)
public class EditorialLinkVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long id;
    private Long reviewId;
    private String url;
    private String description;
    private Date createTime;

}
