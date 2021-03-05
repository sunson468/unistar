package com.up1234567.unistar.central.api.model.us;

import com.up1234567.unistar.central.api.model.BaseDataInModel;
import com.up1234567.unistar.central.api.model.us.vo.AppNodeVo;
import lombok.Data;

@Data
public class NodeWatchInModel extends BaseDataInModel<AppNodeVo> {

    private String path;

}
