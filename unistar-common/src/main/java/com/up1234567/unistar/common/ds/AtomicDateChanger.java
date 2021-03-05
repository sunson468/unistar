package com.up1234567.unistar.common.ds;

import com.up1234567.unistar.common.util.DateUtil;

/**
 * 一个原子化的计数器，达到计数器的总值后，会返回一个True
 * 该类用于在常规短周期心跳内控制执行周期
 */
public class AtomicDateChanger {

    private long current;
    private int hour; // 时间切换点的

    public AtomicDateChanger() {
        this(DateUtil.now(), 0);
    }

    public AtomicDateChanger(int hour) {
        this(DateUtil.now(), hour);
    }

    /**
     * @param current
     * @param hour
     */
    public AtomicDateChanger(long current, int hour) {
        this.current = (current - hour * DateUtil.HOUR) / DateUtil.DAY;
        this.hour = hour;
    }

    /**
     * @return
     */
    public synchronized boolean isDayChanged() {
        long current = (DateUtil.now() - hour * DateUtil.HOUR) / DateUtil.DAY;
        if (current != this.current) {
            this.current = current;
            return true;
        }
        return false;
    }

}
