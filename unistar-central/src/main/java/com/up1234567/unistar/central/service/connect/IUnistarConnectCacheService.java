package com.up1234567.unistar.central.service.connect;

public interface IUnistarConnectCacheService {

    // 节点缓存有效期90秒，需要通过心跳来刷新有效期
    int NODE_EXPIRE = 90;

    //============================================================
    // 所有节点
    // 节点 ns + hashkey
    String CK_NS_NODE = "us_ns_%s_node_%s";
    // 节点 ns + socketId
    String CK_NS_ALL = "us_ns_%s";
    // 节点 Central
    String CK_CENTRAL = "us_central_%s";

    //============================================================
    // 对外服务的节点
    // 服务节点列表 ns + service
    // NOTE: 所有的应用都作为服务
    String CK_SVR_NS_SERVICE = "us_svr_ns_%s_service_%s";
    // 服务对应的发现者节点 ns + service
    String CK_SVR_NS_DISCOVER = "us_svr_ns_%s_discover_%s";

    //============================================================
    // 执行任务的节点 ns + taskname
    String CK_NS_TASKER = "us_ns_%s_tasker_%s";

    //============================================================

}
