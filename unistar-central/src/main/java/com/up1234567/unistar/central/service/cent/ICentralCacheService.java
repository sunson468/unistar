package com.up1234567.unistar.central.service.cent;

public interface ICentralCacheService {

    // 投票中
    String CK_VOTING = "cent_voting";
    // 定时器执行中
    String CK_TIMERING = "cent_timering";

    // 节点临时配置更改
    String CK_CONFIG_CHANGED = "cent_ns_%s_node_%s";

    // 计划 ns + name
    String CK_SCHEDULE = "cent_ns_%s_schedule_%s";
    // 计划任务 no
    String CK_SCHEDULE_TASK = "cent_schedule_task_%s";


}
