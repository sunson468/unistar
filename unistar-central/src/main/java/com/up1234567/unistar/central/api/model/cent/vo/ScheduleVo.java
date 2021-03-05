package com.up1234567.unistar.central.api.model.cent.vo;

import com.up1234567.unistar.central.data.cent.Schedule;
import com.up1234567.unistar.common.util.DateUtil;
import com.up1234567.unistar.common.util.StringUtil;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateFormatUtils;

import java.util.List;

@Data
public class ScheduleVo {

    private String name; // 计划名称
    private String namespace; // 对应的空间
    private boolean post;
    private String cron;
    private String remark; // 计划备注说明

    private String group;    // 不填写则代表任意组服务
    private String task;   // 对应的任务
    private String params;   // 调用的参数 JSON格式

    private int parallel; // 任务并行执行

    // 执行后处理，为避免混乱，并行任务不支持后续处理
    private List<String> postSchedules;

    private String nextTime; // 下一次计划执行时间
    private String startTime; // 生效时间
    private String endTime;   // 失效时间
    private String status;

    /**
     * @param o
     * @return
     */
    public static ScheduleVo wrap(Schedule o) {
        ScheduleVo vo = new ScheduleVo();
        vo.setName(o.getName());
        vo.setNamespace(o.getNamespace());
        vo.setPost(o.isPost());
        vo.setCron(o.getCron());
        vo.setRemark(o.getRemark());
        vo.setGroup(o.getGroup());
        vo.setTask(o.getTask());
        vo.setParams(o.getParams());
        vo.setParallel(o.getParallel());
        if (StringUtils.isNotEmpty(o.getPostSchedules())) {
            vo.setPostSchedules(StringUtil.fromCommaString(o.getPostSchedules()));
        }
        if (o.getNextTime() > 0) {
            vo.setNextTime(DateFormatUtils.format(o.getNextTime(), DateUtil.FMT_YYYY_MM_DD_HH_MM_SS));
        }
        if (o.getStartTime() > 0) {
            vo.setStartTime(DateFormatUtils.format(o.getStartTime(), DateUtil.FMT_YYYY_MM_DD));
        }
        if (o.getEndTime() != Long.MAX_VALUE) {
            vo.setEndTime(DateFormatUtils.format(o.getEndTime(), DateUtil.FMT_YYYY_MM_DD));
        }
        vo.setStatus(o.getStatus().name());
        return vo;
    }

}
