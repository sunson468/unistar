package com.up1234567.unistar.central.api.model.cent.vo;

import com.up1234567.unistar.central.data.cent.ScheduleTask;
import com.up1234567.unistar.common.util.DateUtil;
import lombok.Data;
import org.apache.commons.lang3.time.DateFormatUtils;

@Data
public class ScheduleTaskVo {

    private String no;
    private String namespace; // 对应的空间
    private String schedule; // 所属计划

    private String preSchedule; // 前一个计划
    private String preNo; // 前一个任务
    private String preResult; // 前一个任务的返回值

    private String task; // 对应的Unistar任务
    private String group;   // 对应的组
    private String params;   // 任务参数

    private String appname; // 应用名称
    private String host;
    private int port;

    private String executeTime; // 计划执行时间
    private String result; // 任务返回值
    private String startTime; // 实际执行时间
    private String finishTime; // 执行成功时间
    private long costTime; // 执行成功时间
    //
    private String status;
    private String type; // 执行方式

    /**
     * @param o
     * @return
     */
    public static ScheduleTaskVo wrap(ScheduleTask o) {
        ScheduleTaskVo vo = new ScheduleTaskVo();
        vo.setNo(o.getNo());
        vo.setNamespace(o.getNamespace());
        vo.setSchedule(o.getSchedule());
        vo.setTask(o.getTask());
        vo.setGroup(o.getGroup());
        vo.setPreSchedule(o.getPreSchedule());
        vo.setPreNo(o.getPreNo());
        vo.setPreResult(o.getPreResult());
        vo.setParams(o.getParams());
        vo.setAppname(o.getAppname());
        vo.setHost(o.getHost());
        vo.setPort(o.getPort());
        vo.setExecuteTime(DateFormatUtils.format(o.getExecuteTime(), DateUtil.FMT_YYYY_MM_DD_HH_MM_SS));
        if (o.getStartTime() > 0) vo.setStartTime(DateFormatUtils.format(o.getStartTime(), DateUtil.FMT_YYYY_MM_DD_HH_MM_SS_SSS));
        if (o.getFinishTime() > 0) {
            vo.setFinishTime(DateFormatUtils.format(o.getFinishTime(), DateUtil.FMT_YYYY_MM_DD_HH_MM_SS_SSS));
            vo.setCostTime(o.getFinishTime() - o.getStartTime());
        }
        vo.setResult(o.getResult());
        vo.setStatus(o.getStatus().name());
        vo.setType(o.getType().name());
        return vo;
    }

}
