package com.up1234567.unistar.central.support.ws;

import com.up1234567.unistar.common.IUnistarConst;
import com.up1234567.unistar.common.UnistarParam;
import lombok.CustomLog;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.util.Map;

@CustomLog(topic = IUnistarConst.DEFAULT_LOGGER_UNISTAR)
@Component
public class UnistarHandshakeInterceptor implements HandshakeInterceptor {

    @Autowired
    private IUnistarConnectorAuthor unistarConnectorAuthor;

    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler, Map<String, Object> attributes) throws Exception {
        String query = ((ServletServerHttpRequest) request).getServletRequest().getQueryString();
        UnistarParam param = UnistarParam.fromQuery(query);
        if (unistarConnectorAuthor.checkApp(param)) {
            attributes.put(UnistarParam.UNISTAR_PARAM, param);
            return true;
        } else {
            return false;
        }
    }

    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler, Exception exception) {
        return;
    }

}
