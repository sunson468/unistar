package com.up1234567.unistar.central.api.model;

import lombok.Data;

@Data
public class BaseOutModel {

    public final static int RET_OK = 1;
    public final static int RET_LOGIN_INVALID = -1;

    private int retCode;
    private String retMsg;

    private Object data; // 通用返回

}
