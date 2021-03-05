package com.up1234567.unistar.central.api.model.user.vo;

import com.up1234567.unistar.central.data.base.BaseOperator;
import lombok.Data;

@Data
public class OperatorVo {

    private String account; // 除管理员外，均为邮箱地址，用于后续通知用
    private String nick;

    private boolean inited; //是否首次登陆

    private String roles; // 角色
    private String namespaces; //  可以管理的空间
    private String apps;

    private String lastLoginIp; // 最后登录IP
    private long lastLoginTime; // 最后登录时间

    private String status;

    public static OperatorVo wrap(BaseOperator o) {
        OperatorVo vo = new OperatorVo();
        vo.setAccount(o.getAccount());
        vo.setNick(o.getNick());
        vo.setInited(o.isInited());
        vo.setRoles(o.getRoles());
        vo.setNamespaces(o.getNamespaces());
        vo.setLastLoginIp(o.getLastLoginIp());
        vo.setLastLoginTime(o.getLastLoginTime());
        vo.setStatus(o.getStatus().name());
        return vo;
    }
}
