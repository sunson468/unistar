package com.up1234567.unistar.central.api.model.cent;

import com.up1234567.unistar.central.api.model.BaseOutModel;
import com.up1234567.unistar.central.data.base.BaseGroup;
import com.up1234567.unistar.central.data.base.BaseTask;
import lombok.Data;

import java.util.List;

@Data
public class CentConditionModel extends BaseOutModel {

    private List<BaseGroup> groups;
    private List<BaseTask> tasks;

}
