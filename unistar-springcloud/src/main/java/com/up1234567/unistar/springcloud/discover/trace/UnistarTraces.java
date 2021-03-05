package com.up1234567.unistar.springcloud.discover.trace;

import com.up1234567.unistar.common.discover.UnistarDiscoverStat;
import com.up1234567.unistar.common.discover.UnistarTraceData;
import lombok.Data;

import java.util.HashMap;
import java.util.Map;

@Data
public class UnistarTraces extends UnistarDiscoverStat {

    /**
     * 记录信息
     *
     * @param recorder
     */
    public void record(UnistarTraceRecorder recorder) {
        boolean first = true;
        for (UnistarTraceData traceData : recorder.getCalleds()) {
            if (first) {
                first = false;
                setPath(traceData.getPath());
                setTgroup(recorder.getTgroup());
                recordData(this, traceData);
            } else {
                Map<String, UnistarDiscoverStat> callees = getCallees();
                if (callees == null) setCallees(new HashMap<>());
                if (!getCallees().containsKey(traceData.getPath())) {
                    getCallees().put(traceData.getPath(), new UnistarTraces());
                }
                UnistarDiscoverStat traces = getCallees().get(traceData.getPath());
                traces.setPath(traceData.getPath());
                traces.setTgroup(recorder.getTgroup());
                recordData(traces, traceData);
            }
        }
    }

    /**
     * @param traces
     * @param traceData
     */
    private void recordData(UnistarDiscoverStat traces, UnistarTraceData traceData) {
        traces.setCount(traces.getCount() + 1);
        if (traceData.isSuccess()) {
            recordDataTime(traces, traceData);
        } else {
            traces.setErrors(traces.getErrors() + 1);
        }
    }

    /**
     * 只有成功的数据才会进行效率计算
     *
     * @param traces
     * @param traceData
     */
    private void recordDataTime(UnistarDiscoverStat traces, UnistarTraceData traceData) {
        int cost = (int) (traceData.getEndTime() - traceData.getStartTime());
        traces.setMinTime(Math.min(traces.getMinTime(), cost));
        traces.setMaxTime(Math.max(traces.getMaxTime(), cost));
        traces.setTotalTime(traces.getTotalTime() + cost);
        // 计算QPS，以开始时间统计
        traces.addStatTime(traceData.getStartTime());
    }

}
