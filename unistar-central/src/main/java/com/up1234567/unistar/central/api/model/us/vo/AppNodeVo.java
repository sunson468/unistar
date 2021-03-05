package com.up1234567.unistar.central.api.model.us.vo;

import com.up1234567.unistar.central.data.us.AppNode;
import lombok.Data;

@Data
public class AppNodeVo {

    private String appname;
    private String nodeId;

    private String host;    // 应用地址
    private int port;       // 应用端口
    private String group;   // 分组标记
    private String profiles;

    private boolean serverable; // 可提供服务
    private int weight;
    private String serviceStatus;     // 服务状态

    private boolean discoverable; // 可发现服务
    private String discovers; // 发现的服务列表
    private String discoverStatus; // 可发现服务状态

    private boolean taskable; // 可执行任务
    private String tasks;
    private String taskStatus;     // 任务执行器状态

    private boolean online; // 是否在线
    private boolean manual; // 是否手动添加
    private String manualAnurl; // 活跃检测地址

    public static AppNodeVo wrap(AppNode o) {
        AppNodeVo vo = new AppNodeVo();
        vo.setAppname(o.getAppname());
        vo.setNodeId(o.getNodeId());
        vo.setHost(o.getHost());
        vo.setPort(o.getPort());
        vo.setGroup(o.getGroup());
        //
        vo.setServerable(o.isServerable());
        vo.setWeight(o.getWeight());
        vo.setServiceStatus(o.getServiceStatus().name());
        //
        vo.setDiscoverable(o.isDiscoverable());
        vo.setDiscovers(o.getDiscovers());
        vo.setDiscoverStatus(o.getDiscoverStatus() != null ? o.getDiscoverStatus().name() : AppNode.EStatus.OFF.name());
        //
        vo.setTaskable(o.isTaskable());
        vo.setTasks(o.getTasks());
        vo.setTaskStatus(o.getTaskStatus().name());
        //
        vo.setManual(o.isManual());
        vo.setManualAnurl(o.getManualAnurl());
        //
        return vo;
    }

}
