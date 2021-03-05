package com.up1234567.unistar.common.event;

import lombok.Data;

@Data
public class UnistarEventData {

    private long id; // 用于标识ACK

    private String action;      // 事件名称
    private String params;      // 请求参数

}
