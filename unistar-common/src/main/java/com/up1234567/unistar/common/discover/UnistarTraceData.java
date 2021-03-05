package com.up1234567.unistar.common.discover;

import lombok.Data;

@Data
public class UnistarTraceData {
    private String traceId; // 当前跟踪ID
    private int index; // 跟踪索引
    private String path;    // 当前path
    private long startTime; // 请求开始时间
    private long endTime;   // 请求结束时间
    private boolean success; // 请求成功与否标记

    private String target;
    private String error;
}
