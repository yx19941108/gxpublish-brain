package com.gxpublish.brain.editorial.domain.vo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 通用的名称引用对象。
 */
@Data
public class EditorialNamedRefVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long id;

    private String name;
}
