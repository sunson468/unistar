package com.up1234567.unistar.central.support.core.clust.msg;

import com.up1234567.unistar.common.limit.UnistarAppLimit;
import lombok.Data;

@Data
public class LimitMsg extends BaseMsg {

    private UnistarAppLimit appLimit;

}
