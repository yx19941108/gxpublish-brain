package com.gxpublish.brain.editorial.exception;

import com.gxpublish.brain.common.core.exception.base.BaseException;

import java.io.Serial;

/**
 * 三审三校系统异常
 * @author youxiao
 */
public class EditorialException extends BaseException {

    @Serial
    private static final long serialVersionUID = 1L;

    public EditorialException(String code, Object... args) {
        super("editorial", code, args, null);
    }
}
