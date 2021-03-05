package com.up1234567.unistar.central.support.ws;

import org.springframework.web.socket.WebSocketSession;

public interface IUnistarConnectorManager {

    /**
     * 清理
     */
    void clean();

    /**
     * 节点接入
     *
     * @param session
     */
    void connect(WebSocketSession session);

    /**
     * 节点断开
     *
     * @param session
     */
    void disconnect(WebSocketSession session);

}
