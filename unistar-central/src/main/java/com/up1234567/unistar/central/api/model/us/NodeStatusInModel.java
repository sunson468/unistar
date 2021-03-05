package com.up1234567.unistar.central.api.model.us;

import com.up1234567.unistar.central.api.model.BaseInModel;
import lombok.Data;

@Data
public class NodeStatusInModel extends BaseInModel {

    private String appname;
    private String nodeId;
    private int type; // 1:服务状态 2:执行器状态

}
