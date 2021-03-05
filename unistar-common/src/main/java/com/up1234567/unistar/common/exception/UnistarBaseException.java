package com.up1234567.unistar.common.exception;

import com.up1234567.unistar.common.util.StringUtil;

public class UnistarBaseException extends RuntimeException {

    public UnistarBaseException(String msg, Object... params) {
        super(StringUtil.relace(msg, params));
    }

}
