package com.up1234567.unistar.springcloud.discover.event;

import com.up1234567.unistar.springcloud.discover.trace.IUnistarTraceWatcher;
import com.up1234567.unistar.common.event.IUnistarEventConst;
import com.up1234567.unistar.springcloud.core.event.AUnistarEventListener;

@AUnistarEventListener(IUnistarEventConst.EVENT_TRACE_WATCH)
public class UnistarTraceWatchEventListener {

    private IUnistarTraceWatcher unistarTraceWatcher;

    public UnistarTraceWatchEventListener(IUnistarTraceWatcher unistarTraceWatcher) {
        this.unistarTraceWatcher = unistarTraceWatcher;
    }

    public void handle(String path) {
        unistarTraceWatcher.addWatch(path);
    }

}
