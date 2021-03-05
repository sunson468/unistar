package com.up1234567.unistar.central.api.model.us.vo;

import com.up1234567.unistar.central.data.us.Config;
import lombok.Data;

@Data
public class ConfigVo {

    private String name;
    private String profile;
    private String properties;

    public static ConfigVo wrap(Config o) {
        ConfigVo vo = new ConfigVo();
        vo.setName(o.getName());
        vo.setProfile(o.getProfile());
        vo.setProperties(o.getProperties());
        return vo;
    }
}
