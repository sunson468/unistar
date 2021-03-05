package com.up1234567.unistar.central.service.cent.impl;

import com.up1234567.unistar.central.data.cent.Schedule;
import com.up1234567.unistar.central.service.cent.IScheduleInnerService;
import com.up1234567.unistar.central.service.connect.impl.UnistarConnectCacheService;
import com.up1234567.unistar.central.service.connect.impl.UnistarConnectService;
import com.up1234567.unistar.central.service.stat.impl.StatTraceService;
import com.up1234567.unistar.central.service.us.impl.AppService;
import com.up1234567.unistar.central.support.core.UnistarProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class ScheduleInnerService implements IScheduleInnerService {

    @Autowired
    private UnistarProperties unistarProperties;

    @Autowired
    private AppService appService;

    @Autowired
    private ScheduleService scheduleService;

    @Autowired
    private StatTraceService statTraceService;

    @Autowired
    private UnistarConnectCacheService unistarConnectCacheService;

    @Autowired
    private UnistarConnectService connectService;

    /**
     * 执行内部任务
     *
     * @param schedule
     */
    @Async
    public void run(Schedule schedule) {
        if (!schedule.isInner()) return;
        switch (schedule.getName()) {
            case SCHEDULE_REMOVE_TASK:
                scheduleService.removeOverTask(unistarProperties.getClean().getTask());
                break;
            case SCHEDULE_REMOVE_TRACE:
                statTraceService.removeOverStatTrace(unistarProperties.getClean().getTrace());
                break;
            case SCHEDULE_NODE_HEARTBEAT:
                syncAllManualAppNode();
                break;
        }
    }

    private void syncAllManualAppNode() {
        appService.listAllManualAppNode().parallelStream().forEach(node -> {
            // 同步节点
            if (appService.syncManualAppNode(node)) {
                // 加入到总的连接列表
                unistarConnectCacheService.connected(node);
                //
                if (node.isServerable()) {
                    connectService.serviceNodeChanged(node);
                }
            }
        });
    }

}
