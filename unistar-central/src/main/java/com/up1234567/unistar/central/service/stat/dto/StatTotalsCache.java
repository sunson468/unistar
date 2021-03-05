package com.up1234567.unistar.central.service.stat.dto;

import com.up1234567.unistar.central.data.stat.StatTraceTotal;
import lombok.Data;

import java.util.List;

@Data
public class StatTotalsCache {

    private long updateTime;
    private List<StatTraceTotal> totals;

}
