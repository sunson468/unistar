package com.up1234567.unistar.central.api.model.us;

import com.up1234567.unistar.central.api.model.BaseDataInModel;
import com.up1234567.unistar.central.api.model.us.vo.AppNodeVo;
import lombok.Data;

@Data
public class NodeLoggerInModel extends BaseDataInModel<AppNodeVo> {

    private String loggers;

}
