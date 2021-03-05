package com.up1234567.unistar.central.api.model.us;

import com.up1234567.unistar.central.api.model.BaseDataInModel;
import com.up1234567.unistar.central.api.model.us.vo.ConfigVo;
import lombok.Data;

import java.util.List;

@Data
public class ConfigSaveInModel extends BaseDataInModel {

    private String appname; // 所属应用名称
    private boolean dependable; // 是否可以被依赖
    private List<ConfigVo> configs;

}
