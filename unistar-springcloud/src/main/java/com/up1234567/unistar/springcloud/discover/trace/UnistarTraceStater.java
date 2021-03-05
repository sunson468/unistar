package com.up1234567.unistar.springcloud.discover.trace;

import com.up1234567.unistar.common.IUnistarConst;
import com.up1234567.unistar.common.async.PoolExecutorService;
import com.up1234567.unistar.common.discover.UnistarTraceData;
import com.up1234567.unistar.common.discover.UnistarTraceWatch;
import com.up1234567.unistar.common.event.IUnistarEventConst;
import com.up1234567.unistar.common.heartbeat.UnistarHeartbeatData;
import com.up1234567.unistar.common.logger.IUnistarLogger;
import com.up1234567.unistar.common.logger.UnistarLoggerFactory;
import com.up1234567.unistar.common.util.DateUtil;
import com.up1234567.unistar.common.util.JsonUtil;
import com.up1234567.unistar.common.util.StringUtil;
import com.up1234567.unistar.springcloud.core.IUnistarClientListener;
import com.up1234567.unistar.springcloud.core.event.IUnistarClientDispatcher;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * Trace跟踪记录
 */
public class UnistarTraceStater implements IUnistarClientListener {

    private static final IUnistarLogger logger = UnistarLoggerFactory.getLogger(IUnistarConst.DEFAULT_LOGGER_UNISTAR);

    // 发布器，向Unistar中心发送信息
    private final IUnistarClientDispatcher unistarEventDispatcher;

    // 记录跟踪
    private final Map<String, UnistarTraces> tracesMap = new HashMap<>();

    // 单线程异步处理器
    private final PoolExecutorService executor = new PoolExecutorService();

    public UnistarTraceStater(IUnistarClientDispatcher unistarEventDispatcher) {
        this.unistarEventDispatcher = unistarEventDispatcher;
    }

    /**
     * 追加
     *
     * @param recorder
     * @param traceData
     */
    public void addTrace(UnistarTraceRecorder recorder, UnistarTraceData traceData) {
        logger.debug("unistar client start trace: {}", traceData);
        recorder.addTrace(traceData);
        // 进程内首次，添加首地址
        if (StringUtils.isEmpty(recorder.getStartPath())) {
            recorder.setStartPath(traceData.getPath());
            if (recorder.isWatching() && traceData.getIndex() == 0) startWatch(recorder.getStartPath(), traceData.getTraceId());
        }
        // 观察中的同步观察数据
        if (recorder.isWatching()) sendWatch(recorder.getTraceId(), traceData);
    }

    /**
     * 新的调用结束
     *
     * @param recorder
     */
    public void endTrace(UnistarTraceRecorder recorder) {
        UnistarTraceData traceData = recorder.pop();
        if (traceData == null) return;
        traceData.setEndTime(DateUtil.now());
        // 观察中的同步观察数据
        if (recorder.isWatching()) sendWatch(recorder.getTraceId(), traceData);
        // 判断是否完结
        if (recorder.isEmpty()) statWatch(recorder);
    }

    /**
     * @param path
     * @param traceId
     */
    private void startWatch(String path, String traceId) {
        executor.execute(() -> {
            UnistarTraceWatch watch = new UnistarTraceWatch();
            watch.setPath(path);
            watch.setTraceId(traceId);
            watch.setIndex(-1);
            unistarEventDispatcher.publish(IUnistarEventConst.HANDLE_TRACE_WATCH, watch);
        });
    }

    /**
     * @param traceId
     * @param traceData
     */
    private void sendWatch(String traceId, UnistarTraceData traceData) {
        // 发布同步信息
        executor.execute(() -> {
            UnistarTraceWatch traceWatch = UnistarTraceWatch.wrap(traceData);
            traceWatch.setTraceId(traceId);
            unistarEventDispatcher.publish(IUnistarEventConst.HANDLE_TRACE_WATCH, traceWatch);
        });
    }

    /**
     * 统计
     *
     * @param recorder
     */
    private void statWatch(UnistarTraceRecorder recorder) {
        executor.execute(() -> {
            synchronized (tracesMap) {
                String key = recorder.getTgroup() + StringUtil.H_LINE + recorder.getStartPath();
                if (!tracesMap.containsKey(key)) tracesMap.put(key, new UnistarTraces());
                tracesMap.get(key).record(recorder);
            }
        });
    }

    @Override
    public void heartbeat(UnistarHeartbeatData heartbeatData) {
        // 同步锁定执行
        synchronized (tracesMap) {
            try {
                // 借助json序列化来深度复制
                heartbeatData.setTraces(JsonUtil.toJsonString(tracesMap.values()));
                // 清空统计
                tracesMap.forEach((p, trace) -> trace.clear());
            } catch (Exception ignore) {
            }
        }
    }
}
