package com.up1234567.unistar.common.discover;

import lombok.Data;

@Data
public class UnistarServiceNode {

    private String nodeId;
    // ==================================
    private String namespace;   // 空间
    private String service;     // 服务名称，等同于应用名
    //
    private String host;
    private int port;
    private int weight;
    private boolean transport;
    // ================================
    private boolean available;

}
