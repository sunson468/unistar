package com.up1234567.unistar.central.support.core.clust.msg;

import com.up1234567.unistar.central.data.cent.ScheduleTask;
import lombok.Data;

@Data
public class TaskerMsg extends BaseMsg {
    private ScheduleTask task;
}
