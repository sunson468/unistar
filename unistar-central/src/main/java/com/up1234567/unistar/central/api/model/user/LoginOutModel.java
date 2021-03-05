package com.up1234567.unistar.central.api.model.user;

import com.up1234567.unistar.central.api.model.BaseOutModel;
import com.up1234567.unistar.central.support.auth.EAuthRole;
import lombok.Data;

import java.util.List;

@Data
public class LoginOutModel extends BaseOutModel {

    private String account;
    private String nick;
    private String email;
    private boolean inited; //是否首次登陆
    private String token;
    private List<EAuthRole> roles;

    // 系统版本号
    private String version;

}
