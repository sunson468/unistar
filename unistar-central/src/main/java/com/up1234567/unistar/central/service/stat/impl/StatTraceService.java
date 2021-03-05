package com.up1234567.unistar.central.service.stat.impl;

import com.up1234567.unistar.central.data.stat.StatTrace;
import com.up1234567.unistar.central.data.us.AppNode;
import com.up1234567.unistar.central.service.stat.dto.StatTracesCache;
import com.up1234567.unistar.central.data.stat.StatTraceTotal;
import com.up1234567.unistar.central.service.stat.IStatTraceService;
import com.up1234567.unistar.central.support.data.IUnistarDao;
import com.up1234567.unistar.common.discover.UnistarDiscoverStat;
import com.up1234567.unistar.common.util.DateUtil;
import com.up1234567.unistar.common.util.JsonUtil;
import com.up1234567.unistar.common.util.SecurityUtil;
import com.up1234567.unistar.common.util.StringUtil;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class StatTraceService implements IStatTraceService {

    @Autowired
    private IUnistarDao unistarDao;

    @Autowired
    private StatTraceCacheService statTraceCacheService;

    /**
     * @param statTrace
     */
    public void createStatTrace(StatTrace statTrace) {
        unistarDao.insert(statTrace);
    }

    /**
     * 更新
     *
     * @param statTrace
     */
    private void updateStatTrace(StatTrace statTrace) {
        Map<String, Object> updates = new HashMap<>();
        updates.put("maxqps" + IUnistarDao.UPT_MAX, statTrace.getMaxqps());
        updates.put("count" + IUnistarDao.UPT_ADD, statTrace.getCount());
        updates.put("errors" + IUnistarDao.UPT_ADD, statTrace.getErrors());
        updates.put("minTime" + IUnistarDao.UPT_MIN, statTrace.getMinTime());
        updates.put("maxTime" + IUnistarDao.UPT_MAX, statTrace.getMaxTime());
        updates.put("totalTime" + IUnistarDao.UPT_ADD, statTrace.getTotalTime());
        updates.put("updateTime" + IUnistarDao.UPT_MAX, statTrace.getUpdateTime());
        unistarDao.updateOneByProp("statId", statTrace.getStatId(), updates, StatTrace.class);
    }

    /**
     * @param namespace
     * @param appname
     * @param nodeId
     * @param daydate
     * @return
     */
    public List<StatTrace> dateStatTraces(String namespace, String appname, String nodeId, long daydate) {
        Map<String, Object> props = new HashMap<>();
        props.put("namespace", namespace);
        props.put("appname", appname);
        props.put("nodeId", nodeId);
        props.put("daydate", daydate);
        return unistarDao.listByProps(props, StatTrace.class);
    }

    /**
     * 只保留 N 天之内的数据
     */
    public void removeOverStatTrace(long day) {
        Map<String, Object> props = new HashMap<>();
        // 移除 今日零点 - 保留天数
        props.put("daydate<", DateUtil.today() - day * DateUtil.DAY);
    }

    /**
     * @param namespace
     * @param appname
     * @param nodeId
     * @return
     */
    public List<StatTrace> listStatTrace(String namespace, String appname, String nodeId) {
        Map<String, Object> props = new HashMap<>();
        props.put("namespace", namespace);
        props.put("appname", appname);
        if (StringUtils.isEmpty(nodeId)) {
            props.put("updateTime>", DateUtil.today() - DateUtil.DAY);
        } else {
            props.put("nodeId", nodeId);
        }
        return unistarDao.listByProps(props, StatTrace.class);
    }

    /**
     * 心跳的统计数据采集
     *
     * @param node
     * @param traces
     */
    public void statTraceHeartbeat(AppNode node, String traces) {
        // 记录统计数据信息
        if (StringUtils.isNotEmpty(traces)) {
            long now = DateUtil.now();
            List<UnistarDiscoverStat> tracesDatas = JsonUtil.toClassAsList(traces, UnistarDiscoverStat.class);
            // 设置缓存
            StatTracesCache cache = new StatTracesCache();
            cache.setUpdateTime(now);
            cache.setTraces(tracesDatas);
            statTraceCacheService.cacheAppNodeStat(node, cache);
            // 查出今天所有的
            long daydate = DateUtil.dayStart(now);
            List<StatTrace> statTraces = dateStatTraces(node.getNamespace(), node.getAppname(), node.getNodeId(), daydate);
            tracesDatas.forEach(td -> wrapStatTrace(statTraces, node, td, null, now, daydate));
        }
    }

    /**
     * @param statTraces
     * @param node
     * @param tracesData
     * @param prepath
     * @param now
     * @param daydate
     */
    private void wrapStatTrace(List<StatTrace> statTraces, AppNode node, UnistarDiscoverStat tracesData, String prepath, long now, long daydate) {
        // 有请求记录才记录到数据库
        if (tracesData.getCount() <= 0) return;
        StatTrace statTrace = statTraces
                .stream()
                .filter(st -> StringUtils.equals(tracesData.getTgroup(), st.getTgroup())
                        && StringUtils.equals(prepath, st.getPrepath())
                        && StringUtils.equals(tracesData.getPath(), st.getPath()))
                .findFirst()
                .orElse(null);
        if (statTrace == null) {
            statTrace = new StatTrace();
            statTrace.setStatId(SecurityUtil.md5(StringUtils.join(new Object[]{
                    StringUtil.withDefault(node.getNamespace()),
                    StringUtil.withDefault(node.getAppname()),
                    StringUtil.withDefault(node.getNodeId()),
                    daydate,
                    StringUtil.withDefault(tracesData.getTgroup()),
                    StringUtil.withDefault(prepath),
                    StringUtil.withDefault(tracesData.getPath()),
            }, StringUtil.H_LINE)));
            statTrace.setNamespace(node.getNamespace());
            statTrace.setAppname(node.getAppname());
            statTrace.setNodeId(node.getNodeId());
            statTrace.setDaydate(daydate);
            statTrace.setTgroup(tracesData.getTgroup());
            statTrace.setPrepath(prepath);
            statTrace.setPath(tracesData.getPath());
            createStatTrace(statTrace);
        }
        statTrace.setMaxqps(tracesData.getMaxqps());
        statTrace.setCount(tracesData.getCount());
        statTrace.setErrors(tracesData.getErrors());
        statTrace.setMinTime(tracesData.getMinTime() == Integer.MAX_VALUE ? 0 : tracesData.getMinTime());
        statTrace.setMaxTime(tracesData.getMaxTime());
        statTrace.setTotalTime(tracesData.getTotalTime());
        statTrace.setUpdateTime(now);
        updateStatTrace(statTrace);
        // 汇总主请求
        if (StringUtils.isEmpty(prepath)) {
            // 更新应用
            addIntoTotal(statTrace.getAppname(), statTrace, daydate);
            // 添加到总记录内
            statTraceCacheService.incGlobalStat(statTrace.getNamespace(), (int) statTrace.getCount(), (int) statTrace.getErrors());
        }
        //
        if (MapUtils.isEmpty(tracesData.getCallees())) return;
        tracesData.getCallees().values().forEach(td -> wrapStatTrace(statTraces, node, td, tracesData.getPath(), now, daydate));
    }

    /**
     * 汇总统计
     *
     * @param statTrace
     * @param daydate
     */
    @Async
    public void addIntoTotal(String appname, StatTrace statTrace, long daydate) {
        String totalId = SecurityUtil.md5(StringUtils.join(new Object[]{
                StringUtil.withDefault(statTrace.getNamespace()),
                StringUtil.withDefault(appname),
                daydate,
        }, StringUtil.H_LINE));
        //
        StatTraceTotal statTraceTotal = findStatTraceTotal(totalId);
        if (statTraceTotal == null) {
            try {
                statTraceTotal = new StatTraceTotal();
                statTraceTotal.setTotalId(totalId);
                statTraceTotal.setNamespace(statTrace.getNamespace());
                statTraceTotal.setAppname(statTrace.getAppname());
                statTraceTotal.setDaydate(daydate);
                createStatTraceTotal(statTraceTotal);
            } catch (DuplicateKeyException ignore) {
                // 已经创建了
            }
        }
        // 更新
        updateStatTraceTotal(totalId, statTrace);
    }

    /**
     * @param totalId
     * @return
     */
    private StatTraceTotal findStatTraceTotal(String totalId) {
        return unistarDao.findOneByProp("totalId", totalId, StatTraceTotal.class);
    }

    /**
     * @param statTraceTotal
     */
    private void createStatTraceTotal(StatTraceTotal statTraceTotal) {
        unistarDao.insert(statTraceTotal);
    }

    /**
     * @param totalId
     * @param statTrace
     */
    private void updateStatTraceTotal(String totalId, StatTrace statTrace) {
        Map<String, Object> updates = new HashMap<>();
        updates.put("maxqps" + IUnistarDao.UPT_MAX, statTrace.getMaxqps());
        updates.put("count" + IUnistarDao.UPT_ADD, statTrace.getCount());
        updates.put("errors" + IUnistarDao.UPT_ADD, statTrace.getErrors());
        updates.put("maxTime" + IUnistarDao.UPT_MAX, statTrace.getMaxTime());
        unistarDao.updateOneByProp("totalId", totalId, updates, StatTraceTotal.class);
    }

    /**
     * 获取近期的数据
     *
     * @param namespace
     * @param days
     * @return
     */
    public List<StatTraceTotal> passedDayStatTotals(String namespace, long days) {
        List<StatTraceTotal> retList = statTraceCacheService.findStatTotalsCache(namespace);
        if (CollectionUtils.isEmpty(retList)) {
            Map<String, Object> props = new HashMap<>();
            props.put("namespace", namespace);
            long todaydate = DateUtil.today();
            props.put("daydate" + IUnistarDao.OPS_GTE, todaydate - days * DateUtil.DAY);
            props.put("daydate" + IUnistarDao.OPS_LT, todaydate);
            retList = unistarDao.listByProps(props, StatTraceTotal.class);
            if (CollectionUtils.isNotEmpty(retList)) statTraceCacheService.saveStatTotalsCache(namespace, retList);
        }
        return retList;
    }

    /**
     * @param namespace
     * @return
     */
    public List<StatTraceTotal> todayStatTotals(String namespace) {
        Map<String, Object> props = new HashMap<>();
        props.put("namespace", namespace);
        props.put("daydate", DateUtil.today());
        return unistarDao.listByProps(props, StatTraceTotal.class);

    }

}
