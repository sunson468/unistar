package com.up1234567.unistar.common.ds;

import com.up1234567.unistar.common.util.DateUtil;
import org.springframework.util.CollectionUtils;

import java.util.LinkedList;

/**
 * 基于时间窗口的计数器
 */
public class TimeFrameCounter {

    private final long period;         // 检测周期（毫秒）
    private final LinkedList<Long> counters = new LinkedList<>();

    public TimeFrameCounter() {
        this(DateUtil.SECOND);
    }

    /**
     * @param period
     */
    public TimeFrameCounter(long period) {
        this.period = period;
    }

    /**
     * 增加
     *
     * @param now
     */
    public int incCounter(long now) {
        return innerIncCounter(now);
    }

    /**
     * 增加
     *
     * @param now
     */
    public int syncIncCounter(long now) {
        synchronized (counters) {
            return innerIncCounter(now);
        }
    }

    /**
     * @param now
     * @return
     */
    private int innerIncCounter(long now) {
        // 没记录过
        if (!CollectionUtils.isEmpty(counters)) {
            // 根据时间窗口移除作废的
            long header = counters.getFirst();
            while ((now - header) > period) {
                counters.removeFirst();
                if (CollectionUtils.isEmpty(counters)) break;
                header = counters.getFirst();
            }
        }
        // 加入链表
        counters.addLast(now);
        return counters.size();
    }

}
