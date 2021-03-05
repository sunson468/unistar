package com.up1234567.unistar.central.support.core.clust;

import com.up1234567.unistar.central.support.core.UnistarNode;
import lombok.Data;

@Data
public class UnistarClustMsg {

    private int type;
    private String body;

    // ===================================
    // 记录起始和目标，用于识别
    private UnistarNode from;
    private UnistarNode to;

}
