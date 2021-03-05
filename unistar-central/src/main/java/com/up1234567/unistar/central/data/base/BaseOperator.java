package com.up1234567.unistar.central.data.base;

import com.up1234567.unistar.central.service.base.IOperatorService;
import com.up1234567.unistar.central.support.auth.EAuthRole;
import com.up1234567.unistar.common.util.SecurityUtil;
import com.up1234567.unistar.common.util.StringUtil;
import lombok.Data;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Data
@Document(collection = "base_operator")
public class BaseOperator {

    private final static String MIX_KEY = "uIst-c0nso1e@xc!";

    @Indexed(unique = true)
    private String account; // 除管理员外，均为邮箱地址，用于后续通知用

    private String nick;
    private String password;

    private boolean inited; //是否首次登陆

    private String roles; // 角色
    private String namespaces; //  可以管理的空间

    private String lastLoginIp; // 最后登录IP
    private long lastLoginTime; // 最后登录时间

    private EStatus status;
    private long createTime;

    public boolean isSuper() {
        return IOperatorService.SUPER_ADMIN.equals(account);
    }

    public boolean checkPass(String oripass) {
        return password.equals(encrypt(oripass));
    }

    public static String encrypt(String oripass) {
        return SecurityUtil.md5(oripass + MIX_KEY);
    }

    public enum EStatus {
        OK, BANNED
    }

    /**
     * 转化为List
     *
     * @return
     */
    public List<EAuthRole> asRoles() {
        if (StringUtils.isEmpty(roles)) return new ArrayList<>();
        return Arrays.stream(roles.split(StringUtil.COMMA)).map(EAuthRole::valueOf).collect(Collectors.toList());
    }

    /**
     * 添加为什么角色
     *
     * @param role
     */
    public void addRole(EAuthRole... role) {
        List<EAuthRole> roles = asRoles();
        roles.addAll(Arrays.asList(role));
        toRoles(roles);
    }

    /**
     * @param roles
     */
    private void toRoles(List<EAuthRole> roles) {
        if (CollectionUtils.isEmpty(roles)) {
            this.roles = StringUtil.EMPTY;
        } else {
            this.roles = roles.stream()
                    .filter(Objects::nonNull).distinct()
                    .map(Enum::name)
                    .reduce((s, rm) -> s += rm + StringUtil.COMMA)
                    .get();
        }
    }

}
