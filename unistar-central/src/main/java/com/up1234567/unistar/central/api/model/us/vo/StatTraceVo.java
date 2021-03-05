package com.up1234567.unistar.central.api.model.us.vo;

import com.up1234567.unistar.central.data.stat.StatTrace;
import com.up1234567.unistar.common.discover.UnistarDiscoverStat;
import lombok.Data;

@Data
public class StatTraceVo {

    // 所属线程组
    private String tgroup;
    // 请求路径 http://appname/路径
    private String path;
    // 前置请求 http://appname/路径
    private String prepath;

    // 最大QPS
    private int maxqps;
    // 请求次数
    private long count;
    // 请求失败次数
    private long errors;
    // 单次最短时间
    private long minTime = Long.MAX_VALUE;
    // 单次最长时间
    private long maxTime;
    // 总花费时间
    private long totalTime;

    private String updateTime;

    /**
     * @param o
     * @return
     */
    public static StatTraceVo wrap(UnistarDiscoverStat o) {
        StatTraceVo vo = new StatTraceVo();
        vo.setTgroup(o.getTgroup());
        vo.setMaxqps(o.getMaxqps());
        vo.setPath(o.getPath());
        vo.setCount(o.getCount());
        vo.setErrors(o.getErrors());
        vo.setMinTime(o.getMinTime() == Integer.MAX_VALUE ? 0 : o.getMinTime());
        vo.setMaxTime(o.getMaxTime());
        vo.setTotalTime(o.getTotalTime());
        return vo;
    }

    /**
     * @param o
     * @return
     */
    public static StatTraceVo wrap(StatTrace o) {
        StatTraceVo vo = new StatTraceVo();
        vo.setTgroup(o.getTgroup());
        vo.setPrepath(o.getPrepath());
        vo.setPath(o.getPath());
        vo.setMaxqps(o.getMaxqps());
        vo.setCount(o.getCount());
        vo.setErrors(o.getErrors());
        vo.setMinTime(o.getMinTime());
        vo.setMaxTime(o.getMaxTime());
        vo.setTotalTime(o.getTotalTime());
        return vo;
    }
}
