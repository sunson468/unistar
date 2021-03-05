package com.up1234567.unistar.springcloud.core;

import com.up1234567.unistar.common.heartbeat.UnistarHeartbeatData;

public interface IUnistarClientListener {

    /**
     * 心跳数据设置，一分钟一次心跳
     *
     * @param heartbeatData
     */
    default void heartbeat(UnistarHeartbeatData heartbeatData) {
    }

    /**
     * 客户端重连成功事件
     */
    default void reconnected() {
    }
}
