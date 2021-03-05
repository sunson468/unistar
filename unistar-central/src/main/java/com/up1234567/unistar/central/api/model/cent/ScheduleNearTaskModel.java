package com.up1234567.unistar.central.api.model.cent;

import com.up1234567.unistar.central.api.model.BaseOutModel;
import com.up1234567.unistar.central.api.model.cent.vo.ScheduleTaskVo;
import lombok.Data;

import java.util.List;

@Data
public class ScheduleNearTaskModel extends BaseOutModel {

    private ScheduleTaskVo preTask;
    private List<ScheduleTaskVo> afterTasks;

}
