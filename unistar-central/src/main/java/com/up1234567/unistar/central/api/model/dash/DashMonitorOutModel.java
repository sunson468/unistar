package com.up1234567.unistar.central.api.model.dash;

import com.up1234567.unistar.central.api.model.BaseOutModel;
import lombok.Data;

@Data
public class DashMonitorOutModel extends BaseOutModel {

    private long apps; // 应用数
    private long nodes; // 应用节点数

    private long onlines; // 在线节点数

    private long count; // 累计请求量
    private long errors; // 累计错误

}
