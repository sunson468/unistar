package com.up1234567.unistar.central.support.data;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(UnistarDataProperties.PREFIX)
public class UnistarDataProperties {

    public final static String PREFIX = "unistar.data";
    public final static String TYPE = PREFIX + ".type";

    private String type = "mongodb"; // mongodb | mysql
    private String uri;
    private String username;
    private String password;

}
