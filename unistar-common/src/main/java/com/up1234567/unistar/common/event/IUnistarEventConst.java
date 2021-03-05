package com.up1234567.unistar.common.event;

public interface IUnistarEventConst {

    String EVENT_PREFIX = "unistar_";

    // ====================================================
    // 以下为中央服务器监听事件
    // ====================================================
    // 获取配置
    String HANDLE_CONFIG = EVENT_PREFIX + "config";
    // 启动完毕
    String HANDLE_READY = EVENT_PREFIX + "ready";
    // 发现服务
    String HANDLE_DISCOVER = EVENT_PREFIX + "discover";
    // 服务重连
    String HANDLE_RECONNECT = EVENT_PREFIX + "reconnect";
    // 任务结果处理
    String HANDLE_TASK_STATUS = EVENT_PREFIX + "task_status";
    // 同步状态，提交统计数据
    String HANDLE_HEARTBEAT = EVENT_PREFIX + "heartbeat";
    // 同步监听
    String HANDLE_TRACE_WATCH = EVENT_PREFIX + "trace_watch";

    // ====================================================
    // 以下为客户端节点内置监听事件
    // ====================================================
    // 配置发生变化，仅针对已有的属性刷新，且痐
    String EVENT_CONFIG_CHANGED = EVENT_PREFIX + "config_changed";
    // 服务实例发生变化，上线，下线
    String EVENT_DISCOVER_INSTANCE_CHANGED = EVENT_PREFIX + "discover_instance_changed";
    // 任务调度
    String EVENT_TASK = EVENT_PREFIX + "task";
    // 添加监听，监听是一次性的
    String EVENT_TRACE_WATCH = EVENT_PREFIX + "trace_watch";
    // 限流规则发生变化
    String EVENT_LIMIT_CHANGED = EVENT_PREFIX + "limit_changed";
    // 日志开关发生变化
    String EVENT_LOGGER_CHANGED = EVENT_PREFIX + "logger_changed";

}
