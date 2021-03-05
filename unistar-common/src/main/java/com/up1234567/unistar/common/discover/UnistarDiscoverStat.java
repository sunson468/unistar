package com.up1234567.unistar.common.discover;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.up1234567.unistar.common.ds.TimeFrameCounter;
import lombok.Data;
import org.springframework.util.CollectionUtils;

import java.util.Map;

@Data
public class UnistarDiscoverStat {

    // 所属线程组
    private String tgroup;
    // 路径
    private String path;

    // 最大QPS
    private int maxqps;
    // 请求总次数
    private int count;
    // 请求失败次数
    private int errors;
    // 单次最长时间
    private int minTime = Integer.MAX_VALUE;
    // 单次最短时间
    private int maxTime;
    // 总花费时间
    private int totalTime;
    // 对外的服务请求
    private Map<String, UnistarDiscoverStat> callees;

    // 统计QPS用
    @JsonIgnore
    private TimeFrameCounter frameCounter = new TimeFrameCounter();

    /**
     * 清空统计数据
     */
    public void clear() {
        setMaxqps(0);
        setCount(0);
        setErrors(0);
        setMinTime(Integer.MAX_VALUE);
        setMaxTime(0);
        setTotalTime(0);
        if (!CollectionUtils.isEmpty(callees)) callees.forEach((k, v) -> v.clear());
    }

    /**
     * 添加时间，以便统计QPS
     *
     * @param startTime
     */
    public void addStatTime(long startTime) {
        maxqps = Math.max(maxqps, frameCounter.incCounter(startTime));
    }

}
