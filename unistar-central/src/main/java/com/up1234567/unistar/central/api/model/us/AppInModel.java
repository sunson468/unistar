package com.up1234567.unistar.central.api.model.us;

import com.up1234567.unistar.central.api.model.BaseDataInModel;
import lombok.Data;

@Data
public class AppInModel extends BaseDataInModel {

    private String name;
    private String remark;

    private boolean token; // 是否设置Token

}
