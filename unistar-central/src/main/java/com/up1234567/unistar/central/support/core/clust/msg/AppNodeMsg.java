package com.up1234567.unistar.central.support.core.clust.msg;

import com.up1234567.unistar.common.discover.UnistarServiceNode;
import lombok.Data;

@Data
public class AppNodeMsg extends BaseMsg {
    private UnistarServiceNode serviceNode;
}
