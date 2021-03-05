package com.up1234567.unistar.central.config;

import com.up1234567.unistar.central.support.ws.UnistarHandshakeInterceptor;
import com.up1234567.unistar.central.support.ws.UnistarWebSocketDispatcher;
import org.eclipse.jetty.websocket.api.WebSocketPolicy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;
import org.springframework.web.socket.server.jetty.JettyRequestUpgradeStrategy;
import org.springframework.web.socket.server.support.DefaultHandshakeHandler;

@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

    @Autowired
    private UnistarWebSocketDispatcher unistarWebSocketDispatcher;

    @Autowired
    private UnistarHandshakeInterceptor unistarHandshakeInterceptor;

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(unistarWebSocketDispatcher, "/ws")
                .setAllowedOrigins(CorsConfiguration.ALL)
                .addInterceptors(unistarHandshakeInterceptor)
                .setHandshakeHandler(handshakeHandler());
    }

    @Bean
    public DefaultHandshakeHandler handshakeHandler() {
        WebSocketPolicy policy = WebSocketPolicy.newServerPolicy();
        policy.setInputBufferSize(8192);
        policy.setIdleTimeout(600000);
        return new DefaultHandshakeHandler(new JettyRequestUpgradeStrategy(policy));
    }

}