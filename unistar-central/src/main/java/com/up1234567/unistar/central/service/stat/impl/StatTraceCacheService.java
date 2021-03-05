package com.up1234567.unistar.central.service.stat.impl;

import com.up1234567.unistar.central.service.stat.dto.StatTracesCache;
import com.up1234567.unistar.central.data.stat.StatTraceTotal;
import com.up1234567.unistar.central.data.us.AppNode;
import com.up1234567.unistar.central.service.stat.IStatTraceCacheService;
import com.up1234567.unistar.central.service.stat.dto.StatTotalsCache;
import com.up1234567.unistar.central.support.cache.IUnistarCache;
import com.up1234567.unistar.common.discover.UnistarTraceWatch;
import com.up1234567.unistar.common.util.DateUtil;
import com.up1234567.unistar.common.util.JsonUtil;
import com.up1234567.unistar.common.util.StringUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class StatTraceCacheService implements IStatTraceCacheService {

    @Autowired
    private IUnistarCache unistarCache;

    /**
     * @param node
     * @return
     */
    private String appNodeStatKey(AppNode node) {
        return String.format(CK_STAT, node.getNamespace(), node.getAppname(), node.getNodeId());
    }

    /**
     * @param node
     * @param stats
     */
    public void cacheAppNodeStat(AppNode node, StatTracesCache stats) {
        String k = appNodeStatKey(node);
        unistarCache.set(k, JsonUtil.toJsonString(stats));
    }

    /**
     * @param node
     * @return
     */
    public StatTracesCache appNodeStat(AppNode node) {
        String k = appNodeStatKey(node);
        String stats = unistarCache.get(k);
        if (StringUtils.isEmpty(stats)) return null;
        return JsonUtil.toClass(stats, StatTracesCache.class);
    }

    /**
     * @param node
     * @param traceId
     * @return
     */
    public void setNodeWatchTraceId(AppNode node, String path, String traceId) {
        String k = String.format(CK_WATCH, node.getNamespace(), node.getAppname(), path);
        unistarCache.set(k, traceId, 60);
        // 标记正在记录
        unistarCache.set(String.format(CK_WATCH_TRACE, traceId), StringUtil.H_LINE);
    }

    /**
     * @param traceId
     * @return
     */
    public boolean isNodeWatching(String traceId) {
        return unistarCache.has(String.format(CK_WATCH_TRACE, traceId));
    }

    /**
     * @param traceId
     */
    public void cancelNodeWatch(String traceId) {
        unistarCache.del(String.format(CK_WATCH_TRACE, traceId));
        unistarCache.del(String.format(CK_WATCH_TRACES, traceId));
    }

    /**
     * @param node
     * @return
     */
    public String getNodeWatchTraceId(AppNode node, String path) {
        String k = String.format(CK_WATCH, node.getNamespace(), node.getAppname(), path);
        return unistarCache.get(k);
    }

    /**
     * @param traceId
     * @param watch
     */
    public void addNodeWatchResult(String traceId, UnistarTraceWatch watch) {
        String k = String.format(CK_WATCH_TRACES, traceId);
        unistarCache.listAdd(k, JsonUtil.toJsonString(watch));
    }

    /**
     * @param traceId
     * @return
     */
    public List<UnistarTraceWatch> listNodeWatch(String traceId) {
        List<UnistarTraceWatch> watches = new ArrayList<>();
        String k = String.format(CK_WATCH_TRACES, traceId);
        unistarCache.listGet(k).forEach(v -> watches.add(JsonUtil.toClass(v, UnistarTraceWatch.class)));
        return watches;
    }

    /**
     * @param namespace
     * @param count
     * @param errors
     */
    public void incGlobalStat(String namespace, int count, int errors) {
        unistarCache.inc(String.format(CK_STAT_TOTAL_C, namespace), count);
        unistarCache.inc(String.format(CK_STAT_TOTAL_E, namespace), errors);
    }

    /**
     * @param namespace
     * @return
     */
    public long[] globalStat(String namespace) {
        long[] stats = new long[2];
        String d = unistarCache.get(String.format(CK_STAT_TOTAL_C, namespace));
        if (StringUtils.isNotEmpty(d)) stats[0] = Long.parseLong(d);
        String e = unistarCache.get(String.format(CK_STAT_TOTAL_E, namespace));
        if (StringUtils.isNotEmpty(e)) stats[1] = Long.parseLong(e);
        return stats;
    }

    /**
     * @param namespace
     * @param totals
     */
    public void saveStatTotalsCache(String namespace, List<StatTraceTotal> totals) {
        StatTotalsCache totalsCache = new StatTotalsCache();
        totalsCache.setUpdateTime(DateUtil.today());
        totalsCache.setTotals(totals);
        unistarCache.set(String.format(CK_STAT_TOTALS, namespace), JsonUtil.toJsonString(totalsCache));
    }

    /**
     * @param namespace
     * @return
     */
    public List<StatTraceTotal> findStatTotalsCache(String namespace) {
        String cached = unistarCache.get(String.format(CK_STAT_TOTALS, namespace));
        if (StringUtils.isEmpty(cached)) return null;
        StatTotalsCache totalsCache = JsonUtil.toClass(cached, StatTotalsCache.class);
        if (totalsCache == null) return null;
        // 不是今天的缓存
        if (totalsCache.getUpdateTime() != DateUtil.today()) return null;
        return totalsCache.getTotals();
    }

}
