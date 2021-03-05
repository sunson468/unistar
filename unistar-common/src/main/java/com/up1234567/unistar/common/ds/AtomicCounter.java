package com.up1234567.unistar.common.ds;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * 一个原子化的计数器，达到计数器的总值后，会返回一个True
 * 该类用于在常规短周期心跳内控制执行周期
 */
public class AtomicCounter {

    private AtomicInteger counter = new AtomicInteger();
    private int total;

    public AtomicCounter(int total) {
        this.total = total;
    }

    /**
     * @return
     */
    public boolean isArrived() {
        counter.incrementAndGet();
        return counter.compareAndSet(total, 0);
    }

    /**
     * 获取当前值
     *
     * @return
     */
    public int current() {
        return counter.get();
    }

    /**
     * @param total
     * @return
     */
    public static AtomicCounter newCounter(int total) {
        return new AtomicCounter(Math.max(total, 1));
    }

}
