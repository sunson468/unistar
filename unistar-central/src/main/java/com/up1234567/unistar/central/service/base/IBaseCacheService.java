package com.up1234567.unistar.central.service.base;

public interface IBaseCacheService {

    // 操作员登录Token
    // 成对保存，便于清除
    String CK_TOKEN_T_A = "base_token_account_%s";
    String CK_TOKEN_A_T = "base_account_token_%s";

    // Token有效时间，十分钟内不操作，则失效
    int TOKEN_INVALID_MINUTE = 600;

}
