package com.up1234567.unistar.springcloud.discover;

import lombok.Data;

@Data
public class UnistarDiscoverProperties {

    private FeignOption feign = new FeignOption();

    @Data
    public static class FeignOption {
        private int connectTimeout = 3_000; // 连接最多3秒
        private int readTimeout = 15_000;   // 读取数据最多15秒
        private int writeTimeout = 15_000; // 发送数据最多15秒
    }

}
