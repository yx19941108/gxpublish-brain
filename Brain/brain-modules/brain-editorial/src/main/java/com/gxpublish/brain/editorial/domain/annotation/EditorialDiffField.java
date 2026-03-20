package com.gxpublish.brain.editorial.domain.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 审校修改历史字段中文名标记。
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface EditorialDiffField {

    /**
     * 字段中文展示名。
     */
    String value();
}
