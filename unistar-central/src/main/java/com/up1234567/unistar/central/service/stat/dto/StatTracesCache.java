package com.up1234567.unistar.central.service.stat.dto;

import com.up1234567.unistar.common.discover.UnistarDiscoverStat;
import lombok.Data;

import java.util.List;

@Data
public class StatTracesCache {

    private long updateTime;
    private List<UnistarDiscoverStat> traces;

}
