package com.up1234567.unistar.central.support.ws;

import org.springframework.web.socket.WebSocketSession;

public interface IUnistarWebSocketHandler<T> {

    String handle(WebSocketSession session, T param);

}
