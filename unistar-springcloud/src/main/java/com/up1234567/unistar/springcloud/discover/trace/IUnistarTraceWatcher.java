package com.up1234567.unistar.springcloud.discover.trace;

public interface IUnistarTraceWatcher {

    /**
     * 增加监听
     *
     * @param path
     */
    void addWatch(String path);

}
