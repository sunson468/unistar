package com.up1234567.unistar.central.support.auth;

import lombok.Data;
import org.apache.commons.collections4.IterableUtils;

import java.util.List;

@Data
public class AuthToken {

    public final static String HEADER_TOKEN = "unistar_api_token";
    public final static String REQ_AUTH = "_REQ_AUTHTOKEN";
    public final static String PATH_PREFIX = "/api/";

    private String account;
    private String nick;

    private List<EAuthRole> roles;

    private String ip; // 登录的IP
    private String agent; // 登录的终端信息

    /**
     * 是否为超级管理员
     *
     * @return
     */
    public boolean isSuper() {
        return IterableUtils.contains(roles, EAuthRole.SUPER);
    }

}
