package com.up1234567.unistar.central.support.util;

import com.up1234567.unistar.common.event.UnistarEventData;
import com.up1234567.unistar.common.util.JsonUtil;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;

public class WsSendUtil {

    /**
     * @param session
     * @param id
     * @param action
     * @param params
     */
    private static void sendInternal(WebSocketSession session, long id, String action, Object params) {
        try {
            UnistarEventData retDto = new UnistarEventData();
            retDto.setId(id);
            retDto.setAction(action);
            if (params != null) {
                retDto.setParams(JsonUtil.toJsonString(params));
            }
            session.sendMessage(new TextMessage(JsonUtil.toJsonString(retDto)));
        } catch (IOException ignore) {
        }
    }

    /**
     * @param session
     * @param action
     * @param params
     */
    public static void send(WebSocketSession session, String action, Object params) {
        sendInternal(session, 0L, action, params);
    }

    /**
     * @param session
     * @param eventId
     * @param action
     * @param ret
     */
    public static void reback(WebSocketSession session, long eventId, String action, Object ret) {
        if (ret == null) return;
        sendInternal(session, eventId, action, ret);
    }

}
