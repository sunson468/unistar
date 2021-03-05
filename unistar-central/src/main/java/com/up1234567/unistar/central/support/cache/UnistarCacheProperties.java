package com.up1234567.unistar.central.support.cache;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(UnistarCacheProperties.PREFIX)
public class UnistarCacheProperties {

    public final static String PREFIX = "unistar.cache";
    public final static String TYPE = PREFIX + ".type";

    // memory | redis
    private String type;


}
