package com.up1234567.unistar.common.discover;

import lombok.Data;

@Data
public class UnistarTraceWatch {

    private String traceId;
    private int index; // 跟踪索引
    private String path;
    private long watchTime;
    private boolean inited; // 开始
    private boolean success; // 是否处理成功

    private String target;
    private String error;

    private UnistarServiceNode node; // 哪个

    /**
     * @param traceData
     * @return
     */
    public static UnistarTraceWatch wrap(UnistarTraceData traceData) {
        UnistarTraceWatch traceWatch = new UnistarTraceWatch();
        traceWatch.setTraceId(traceData.getTraceId());
        traceWatch.setIndex(traceData.getIndex());
        traceWatch.setPath(traceData.getPath());
        if (traceData.getEndTime() > 0) {
            traceWatch.setWatchTime(traceData.getEndTime());
            traceWatch.setInited(false);
            traceWatch.setSuccess(traceData.isSuccess());

        } else {
            traceWatch.setWatchTime(traceData.getStartTime());
            traceWatch.setInited(true);
            traceWatch.setSuccess(true);
        }
        traceWatch.setTarget(traceData.getTarget());
        traceWatch.setError(traceData.getError());
        return traceWatch;
    }

}
