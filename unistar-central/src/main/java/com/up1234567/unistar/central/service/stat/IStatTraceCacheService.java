package com.up1234567.unistar.central.service.stat;

public interface IStatTraceCacheService {

    // 统计 ns + appName + nodeId
    String CK_STAT = "stat_ns_%s_name_%s_nid_%s";
    String CK_STAT_TOTAL_C = "stat_ns_%s_total_c";
    String CK_STAT_TOTAL_E = "stat_ns_%s_total_e";

    // 近期的所有总计的缓存
    String CK_STAT_TOTALS = "stat_ns_%s_totals";

    // 观察 ns + appName + path
    String CK_WATCH = "stat_ns_%s_name_%s_path_%s";
    String CK_WATCH_TRACE = "stat_trace_%s";
    String CK_WATCH_TRACES = "stat_traces_%s";

}
