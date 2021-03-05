package com.up1234567.unistar.central.support.cache.redis;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(UnistarCacheRedisProperties.PREFIX)
public class UnistarCacheRedisProperties {

    public final static String PREFIX = "unistar.cache.redis";

    // standlone, sentinel, cluster
    private String password;
    private int database;

    // standlone
    private String host;
    private int port = 6379;

    // sentinel
    private String master;

    // sentinel, cluster
    private String nodes;


}
