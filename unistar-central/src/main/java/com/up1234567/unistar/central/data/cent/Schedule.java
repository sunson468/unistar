package com.up1234567.unistar.central.data.cent;

import com.up1234567.unistar.central.support.util.CronUtil;
import com.up1234567.unistar.common.util.DateUtil;
import lombok.Data;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Document(collection = "cent_schedule")
@CompoundIndexes({
        @CompoundIndex(name = "uidx_ns_n", def = "{'namespace':1,'name':1}", unique = true),
        @CompoundIndex(name = "idx_p_s_nt", def = "{'post':1,'status':1,'nextTime':1}"),
})
public class Schedule {

    private String name; // 计划名称

    @Indexed
    private String namespace; // 对应的空间

    // 是否为后续计划
    private boolean post;
    // 计划的cron表达式，后续计划不支持设置Cron表达式
    private String cron;

    private String remark; // 计划备注说明

    // ========================================
    // 计划执行细节
    private String group;    // 不填写则代表任意组服务
    private String task;   // 对应的任务
    private String params;   // 调用的参数 JSON格式

    private int parallel; // 任务并行执行

    // 执行后处理，为避免混乱，并行任务不支持后续处理
    private String postSchedules;

    private long nextTime; // 下一次计划执行时间

    private long startTime; // 生效时间
    private long endTime;   // 失效时间

    private boolean inner; // 内置任务，不可编辑调整
    private EStatus status;
    private long createTime;

    /**
     * @return
     */
    public boolean isValid() {
        if (EStatus.OFF.equals(status)) return false;
        long now = DateUtil.now();
        return startTime <= now && (endTime == 0 || now < endTime);
    }

    // 重新设置
    public void resetNextTime() {
        nextTime = isValid() ? CronUtil.nextRunTime(cron, DateUtil.now()) : 0;
    }

    // 将要执行
    public boolean isGoingRun() {
        return isValid() && nextTime < DateUtil.now();
    }

    public enum EStatus {
        ON,     // 计划开启
        OFF,    // 计划关闭
        ;
    }

}
