package com.up1234567.unistar.central.data.cent;

import com.up1234567.unistar.common.task.UnistarTaskData;
import com.up1234567.unistar.common.util.DateUtil;
import lombok.Data;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Document(collection = "cent_schedule_task")
@CompoundIndexes({
        @CompoundIndex(name = "idx_s_et", def = "{'status':1,'executeTime':1}"),
        @CompoundIndex(name = "idx_ns_st", def = "{'namespace':1,'startTime':-1}"),
})
public class ScheduleTask {

    @Indexed(unique = true)
    private String no;          // 任务编号

    private String namespace; // 对应的空间
    private String schedule; // 所属计划

    private String preSchedule; // 前置计划

    @Indexed
    private String preNo; // 前一个任务编号
    private String preResult; // 前一个任务的返回值

    private String task; // 对应的Unistar任务
    private String group;   // 对应的组
    private String params;   // 任务参数

    private String appname; // 应用名称
    private String host;
    private int port;

    // 执行时间
    private long executeTime;
    private String result; // 任务返回值

    private long startTime; // 实际执行时间
    private long finishTime; // 执行成功时间

    // 记录为系统自动还是后台手动
    private EType type;
    // 任务的状态
    private UnistarTaskData.EStatus status;
    private long createTime;

    //
    public enum EType {
        AUTO,       // 系统自动
        MANUALLY,  // 手动执行
        REMEDY,  // 异常补救
        ;
    }

    public boolean canRetry() {
        return UnistarTaskData.EStatus.FAILED.equals(status) || UnistarTaskData.EStatus.OVERTIME.equals(status);
    }

    /**
     * 校验是否出现超时异常，具体正常处理时间超过一分钟，处理中的
     *
     * @return
     */
    public boolean checkOvertime() {
        if (UnistarTaskData.EStatus.EXECUTING.equals(status)) {
            long now = DateUtil.now();
            return now - startTime > DateUtil.MINUTE;
        }
        return false;
    }

}
