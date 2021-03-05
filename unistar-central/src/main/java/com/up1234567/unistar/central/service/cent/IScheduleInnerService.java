package com.up1234567.unistar.central.service.cent;

public interface IScheduleInnerService {

    String SCHEDULE_NS = "_schedule";

    String SCHEDULE_REMOVE_TASK = "_remove_task"; // 移除久远的执行完成任务
    String SCHEDULE_REMOVE_TRACE = "_remove_trace"; // 移除久远的统计数据

    String SCHEDULE_NODE_HEARTBEAT = "_node_heartbeat";

}
