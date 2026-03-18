package com.gxpublish.brain.editorial.domain.vo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 审校流程角色引用。
 */
@Data
public class EditorialWorkflowRoleRefVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long roleId;

    private String roleKey;
}
