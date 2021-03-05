package com.up1234567.unistar.springcloud.core.event;

import com.up1234567.unistar.springcloud.core.IUnistarClientListener;
import com.up1234567.unistar.common.UnistarReadyParam;

public interface IUnistarClientDispatcher {

    // =====================================================
    // 全局参数设置
    // =====================================================
    UnistarReadyParam readyParam();

    // =====================================================
    // 内部监听器
    // =====================================================

    /**
     * 添加客户端定监听器
     *
     * @param clientListener
     */
    void addClientListener(IUnistarClientListener clientListener);

    // =====================================================
    // 远程监听器
    // =====================================================

    /**
     * 添加消息监听
     *
     * @param listener
     */
    void addEventListener(Object listener);


    // =====================================================
    // 远程发布器
    // =====================================================

    /**
     * 发布信息给控制中心
     *
     * @param action 事件名称
     */
    void publish(String action);

    /**
     * 发布信息给控制中心
     *
     * @param action 事件名称
     * @param data   传递的参数
     */
    <T> void publish(String action, T data);

    /**
     * 发布信息给控制中心
     *
     * @param action 事件名称
     * @param data   传递的参数
     * @param ack    异步回调函数
     */
    <T> void publish(String action, T data, IUnistarEventDispatcherAck ack);

}
