package com.up1234567.unistar.central.api.model.us.vo;

import com.up1234567.unistar.central.data.us.App;
import lombok.Data;

@Data
public class AppVo {

    private String namespace;
    private String name;
    private String remark;

    private boolean serverable; // 可提供服务
    private boolean discoverable; // 可发现服务
    private boolean taskable; // 可执行任务

    public static AppVo wrap(App o) {
        AppVo vo = new AppVo();
        vo.setNamespace(o.getNamespace());
        vo.setName(o.getName());
        vo.setRemark(o.getRemark());
        vo.setServerable(o.isServerable());
        vo.setDiscoverable(o.isDiscoverable());
        vo.setTaskable(o.isTaskable());
        return vo;
    }

}
