package com.up1234567.unistar.springcloud.registry;

import lombok.Data;

@Data
public class UnistarRegistryProperties {

    /**
     * 是否开启服务
     */
    private boolean enabled = true;

    /**
     * 服务是否可用
     */
    private boolean available = true;

    /**
     * 服务负载比重
     */
    private int weight = 1;

}
