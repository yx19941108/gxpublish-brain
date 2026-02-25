package com.gxpublish.brain.editorial.domain.bo;

import com.gxpublish.brain.editorial.domain.EditorialLink;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;

/**
 * 审校关联链接业务对象
 *
 * @author gxpublish
 */
@Data
@AutoMapper(target = EditorialLink.class, reverseConvertGenerate = false)
public class EditorialLinkBo {

    /**
     * 主键ID (更新时用)
     */
    private Long id;

    /**
     * 链接地址
     */
    private String url;

    /**
     * 描述
     */
    private String description;

}
