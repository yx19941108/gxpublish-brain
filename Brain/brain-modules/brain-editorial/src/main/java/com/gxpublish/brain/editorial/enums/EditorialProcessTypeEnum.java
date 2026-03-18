package com.gxpublish.brain.editorial.enums;

import com.gxpublish.brain.common.core.exception.ServiceException;
import com.gxpublish.brain.common.core.utils.StringUtils;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 审校流程类型枚举。
 */
@Getter
@AllArgsConstructor
public enum EditorialProcessTypeEnum {

    AUDIT("AUDIT"),

    PROOFREAD("PROOFREAD");

    private final String code;

    public static String normalize(String processType) {
        String normalized = StringUtils.upperCase(StringUtils.trim(processType));
        for (EditorialProcessTypeEnum value : values()) {
            if (value.code.equals(normalized)) {
                return value.code;
            }
        }
        throw new ServiceException("流程类型仅支持 AUDIT 或 PROOFREAD");
    }
}
