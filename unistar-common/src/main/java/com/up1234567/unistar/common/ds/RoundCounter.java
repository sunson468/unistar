package com.up1234567.unistar.common.ds;

import java.util.concurrent.atomic.AtomicLong;

/**
 * 循环计数器
 */
public final class RoundCounter {

    // 当前计数
    private AtomicLong counter = new AtomicLong();
    // 最大计数
    private long counterMax;

    public RoundCounter() {
        this(999999999999L);
    }

    public RoundCounter(long counterMax) {
        this.counterMax = counterMax;
    }

    /**
     * @return
     */
    public long get() {
        try {
            return counter.incrementAndGet();
        } finally {
            counter.compareAndSet(counterMax, 0);
        }
    }

    /**
     * 重置
     */
    public void reset() {
        counter.set(0);
    }

}
