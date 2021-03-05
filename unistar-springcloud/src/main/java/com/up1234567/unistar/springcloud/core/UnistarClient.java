package com.up1234567.unistar.springcloud.core;

import okhttp3.*;

import java.io.Closeable;

/**
 * 连接器，维持节点和控制中心的连接和通讯
 */
public class UnistarClient extends WebSocketListener implements Closeable {

    private final static int EXIT_CODE = 1234;
    //
    private final UnistarClientManager unistarClientManager;
    //
    private WebSocket session = null; // 客户端连接

    public UnistarClient(UnistarClientManager unistarClientManager, String wsUrl) {
        this.unistarClientManager = unistarClientManager;
        // 启动连接
        OkHttpClient client = new OkHttpClient.Builder().build();
        Request request = new Request.Builder().url(wsUrl).build();
        client.newWebSocket(request, this);
    }

    /**
     * 是否打开
     *
     * @return
     */
    public boolean isOpen() {
        return session != null;
    }

    @Override
    public void onOpen(WebSocket webSocket, Response response) {
        session = webSocket;
    }

    @Override
    public void onMessage(WebSocket webSocket, String text) {
        unistarClientManager.onMessage(text);
    }

    @Override
    public void onClosing(WebSocket webSocket, int code, String reason) {
        onClosed(webSocket, 0, null);
    }

    @Override
    public void onClosed(WebSocket webSocket, int code, String reason) {
        if (EXIT_CODE == code) return;
        if (!isOpen()) return;
        this.close();
        unistarClientManager.onClosed();
    }

    @Override
    public void onFailure(WebSocket webSocket, Throwable t, Response response) {
        onClosed(webSocket, 0, null);
    }

    /**
     * @param message
     */
    public void sendMessage(String message) {
        if (!isOpen()) return;
        session.send(message);
    }

    @Override
    public void close() {
        if (session != null) {
            try {
                session.close(EXIT_CODE, null);
                session = null;
            } catch (Exception ignore) {
            }
        }
    }
}
