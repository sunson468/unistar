package com.up1234567.unistar.common;

public interface IUnistarConst {

    String DEFAULT_NS = "DEFAULT_NS";
    String DEFAULT_GROUP = "DEFAULT_GROUP";
    String DEFAULT_LOGGER = "default";
    String DEFAULT_LOGGER_UNISTAR = "com.xcitech.unistar";

    // Header里传递发起服务名
    String HTTP_SERVICE = "UNISTAR_SERVICE";
    // Header里传递发起服务所属组别
    String HTTP_SERVICE_GROUP = "UNISTAR_SERVICE_GROUP";
    // Header里传递追踪ID
    String HTTP_TRACE_ID = "UNISTAR_HTTP_TRACE_ID";
    // Header里传递是否正在监听，监听的请求需要发送监听数据至Unistar中心
    String HTTP_TRACE_WATCHING = "UNISTAR_HTTP_TRACE_WATCHING";

}
