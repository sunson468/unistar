package com.up1234567.unistar.central.support.core.clust;

public interface IUnistarClustMsg {

    int TYPE_HEARTBEAT = 10; // Master心跳

    int TYPE_VOTE_START = 20;   // 开始投票Master
    int TYPE_VOTE_VOTE = 21;    // 投票
    int TYPE_VOTE_PUBLISH = 22; // 公布
    int TYPE_VOTE_REFUSE = 23;  // 有异议
    int TYPE_VOTE_ACCEPT = 24;  // 同意

    int TYPE_CONFIG_CHANGED = 100; // 配置更改
    int TYPE_TASKER = 101; // 任务执行
    int TYPE_SERVICE_CHANGED = 102; // 服务上线、下线、权重等变更
    int TYPE_WATCH = 103; // 路径监听
    int TYPE_LIMIT_CHANGED = 104; // 限流更新
    int TYPE_LOGGER_CHANGED = 105; // 日志开关更新
    int TYPE_LOGGER_SEARCH = 106; // 日志查询

}
