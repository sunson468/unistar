package com.up1234567.unistar.central.ws;

import com.up1234567.unistar.central.service.cent.impl.ScheduleService;
import com.up1234567.unistar.central.support.ws.IUnistarWebSocketHandler;
import com.up1234567.unistar.central.data.cent.Schedule;
import com.up1234567.unistar.central.data.cent.ScheduleTask;
import com.up1234567.unistar.central.data.us.AppNode;
import com.up1234567.unistar.central.service.us.impl.AppService;
import com.up1234567.unistar.central.support.ws.AUnistarWebSocketHandler;
import com.up1234567.unistar.common.IUnistarConst;
import com.up1234567.unistar.common.UnistarParam;
import com.up1234567.unistar.common.event.IUnistarEventConst;
import com.up1234567.unistar.common.exception.UnistarRegistraionException;
import com.up1234567.unistar.common.task.UnistarTaskData;
import com.up1234567.unistar.common.util.DateUtil;
import com.up1234567.unistar.common.util.StringUtil;
import lombok.CustomLog;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.socket.WebSocketSession;

import java.util.List;

@CustomLog(topic = IUnistarConst.DEFAULT_LOGGER_UNISTAR)
@AUnistarWebSocketHandler(IUnistarEventConst.HANDLE_TASK_STATUS)
public class TaskStatusHandler implements IUnistarWebSocketHandler<UnistarTaskData> {

    @Autowired
    private AppService appService;

    @Autowired
    private ScheduleService scheduleService;

    @Override
    public String handle(WebSocketSession session, UnistarTaskData param) {
        UnistarParam attrs = (UnistarParam) session.getAttributes().get(UnistarParam.UNISTAR_PARAM);
        AppNode node = appService.findAppNode(attrs.getNamespace(), attrs.getName(), attrs.getHost(), attrs.getPort());
        if (node == null) throw new UnistarRegistraionException("获取配置的节点不存在");
        long now = DateUtil.now();
        // 处理任务结果
        ScheduleTask task = scheduleService.findTask(param.getNo());
        task.setResult(param.getResult());
        task.setStatus(param.getStatus());
        task.setFinishTime(now);
        scheduleService.taskFinished(task);
        // 后续任务
        Schedule currentSchedule = scheduleService.findScheduleFromCache(task.getNamespace(), task.getSchedule());
        // 处理后续任务
        List<String> postSchedules = StringUtil.fromCommaString(currentSchedule.getPostSchedules());
        if (CollectionUtils.isNotEmpty(postSchedules)) {
            postSchedules.parallelStream().forEach(tn -> {
                Schedule newSchedule = scheduleService.findScheduleFromCache(task.getNamespace(), tn);
                if (newSchedule != null) scheduleService.createScheduleTask(newSchedule, now, task.getSchedule(), task.getNo(), task.getResult());
            });
        }
        return null;
    }

}
