package com.up1234567.unistar.central.service.connect;

import com.up1234567.unistar.central.support.util.WsSendUtil;
import org.springframework.web.socket.WebSocketSession;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class UnistarNamespaceRoom {

    // SocketId:Session
    private ConcurrentHashMap<String, WebSocketSession> connectors;

    public UnistarNamespaceRoom() {
        connectors = new ConcurrentHashMap<>();
    }

    /**
     * @param session
     */
    public void join(WebSocketSession session) {
        connectors.put(session.getId(), session);
    }

    /**
     * @param session
     */
    public void left(WebSocketSession session) {
        connectors.remove(session.getId());
    }

    /**
     * @param sessionIds
     * @param action
     * @param params
     * @return
     */
    public void send(List<String> sessionIds, String action, String params) {
        sessionIds.parallelStream().forEach(sessionId -> {
            WebSocketSession session = connectors.get(sessionId);
            if (session != null) WsSendUtil.send(session, action, params);
        });
    }

}
