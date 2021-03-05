package com.up1234567.unistar.central.api.model.user;

import com.up1234567.unistar.central.api.model.BaseOutModel;
import com.up1234567.unistar.central.api.model.us.vo.AppVo;
import lombok.Data;

import java.util.List;

@Data
public class OpCondOutModel extends BaseOutModel {

    private List<String> roles;
    private List<String> namespaces;

    private List<String> opApps;
    private List<AppVo> apps;

}
