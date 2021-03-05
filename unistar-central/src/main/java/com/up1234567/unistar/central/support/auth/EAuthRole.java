package com.up1234567.unistar.central.support.auth;

public enum EAuthRole {

    ALL, // 当请求的方法需要登录，但任意角色均可操作时设置
    SUPER, APPER, SCHEDULER

}
