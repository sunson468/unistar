package com.up1234567.unistar.central.api.model.us;

import com.up1234567.unistar.central.api.model.us.vo.AppVo;
import com.up1234567.unistar.central.api.model.BaseOutModel;
import com.up1234567.unistar.central.data.base.BaseGroup;
import lombok.Data;

import java.util.List;

@Data
public class AppLimitConditionOutModel extends BaseOutModel {

    private boolean serverable;
    private List<BaseGroup> groups; // 分组列表
    private List<AppVo> disovers; // 发现者列表

}
