package com.up1234567.unistar.springcloud.discover.trace;

import com.up1234567.unistar.common.discover.UnistarTraceData;
import lombok.Data;
import org.springframework.util.CollectionUtils;

import java.util.LinkedList;

@Data
public class UnistarTraceRecorder {

    private String traceId;
    private String tgroup; // 线程分组
    private String startPath; // 首地址，连同分组来区分不同的请求
    private LinkedList<UnistarTraceData> traces = new LinkedList<>(); // 所有请求按照FIFO的方式记录
    // ==================================================================================
    private boolean watching; // 观察中
    private LinkedList<UnistarTraceData> calleds = new LinkedList<>(); // 已经处理完成出栈的请求

    /**
     * @param trace
     */
    public void addTrace(UnistarTraceData trace) {
        traces.addFirst(trace);
    }

    public boolean isEmpty() {
        return traces.isEmpty();
    }

    public UnistarTraceData pop() {
        if (CollectionUtils.isEmpty(traces)) return null;
        UnistarTraceData traceData = traces.pop();
        calleds.addFirst(traceData);
        return traceData;
    }

    /**
     * 当前索引
     *
     * @return
     */
    public int currentIndex() {
        if (CollectionUtils.isEmpty(traces)) return 0;
        return traces.getFirst().getIndex();
    }

}
