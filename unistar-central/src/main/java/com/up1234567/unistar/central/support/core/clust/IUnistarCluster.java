package com.up1234567.unistar.central.support.core.clust;

import com.up1234567.unistar.central.support.core.UnistarNode;

public interface IUnistarCluster {

    String CENTRAL_CHANNEL = "unistar_central_channel";

    /**
     * @param node unistar中心
     * @param type
     * @param body
     */
    void send(UnistarNode node, int type, Object body);

    /**
     * @param type
     * @param body
     */
    void multicast(int type, Object body);

    /**
     * 订阅消息
     *
     * @param message
     */
    void handleMessage(String message);

    // ======================================================

    /**
     * 增加内部处理器，用于分解处理具体的Clust消息
     *
     * @param handler
     * @param types
     */
    void addListener(IUnistarClustListener handler, Integer... types);

}
