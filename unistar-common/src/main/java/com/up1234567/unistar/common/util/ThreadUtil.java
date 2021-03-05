package com.up1234567.unistar.common.util;

import org.springframework.util.Assert;

public class ThreadUtil {

    public interface ICheckable {
        boolean check();
    }

    /**
     * @param checkable
     * @param period    loop的周期（毫秒）
     * @param timeout   超时回调
     */
    public static void loopUtil(ICheckable checkable, long period, Runnable timeout) {
        loopUtilByTimes(checkable, Integer.MAX_VALUE, period, false, timeout);
    }

    /**
     * @param checkable
     * @param times     运行次数，一次1秒
     * @param timeout
     */
    public static void loopUtilByTimes(ICheckable checkable, int times, Runnable timeout) {
        loopUtilByTimes(checkable, times, DateUtil.SECOND, timeout);
    }

    /**
     * @param checkable
     * @param times     运行次数
     * @param period    loop的周期（毫秒）
     * @param timeout   超时回调
     */
    public static void loopUtilByTimes(ICheckable checkable, int times, long period, Runnable timeout) {
        loopUtilByTimes(checkable, times, period, false, timeout);
    }

    /**
     * 阻塞线程，最长5分钟
     *
     * @param checkable
     * @param times     运行次数
     * @param period    loop的周期（毫秒）
     * @param stepadd   休眠时间是否累计递增
     * @param timeout   超时回调
     */
    public static void loopUtilByTimes(ICheckable checkable, int times, long period, boolean stepadd, Runnable timeout) {
        // 参数校验
        Assert.notNull(checkable, "thread loop must have checkable function");
        times = Math.max(times, 1);
        period = Math.max(period, 50);
        // 开始Loop
        long totalSleep = 0;
        long sleepTime = period;
        do {
            // 运行次数是否已经结束
            // 总睡眠时间超过5分钟，则立刻终止
            if (times-- < 0 || totalSleep >= DateUtil.MINUTE_5) {
                if (timeout != null) timeout.run();
                break;
            }
            // 睡眠一定时间
            sleep(sleepTime);
            // 总的睡眠时间统计
            totalSleep += sleepTime;
            // 如果是阶梯等待
            if (stepadd) sleepTime += period;
            // 执行校验，校验不通过，则进入下一轮
        } while (!checkable.check());
    }

    /**
     * 线程休眠
     *
     * @param millis
     */
    public static void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            // ignore
        }
    }

    /**
     * @param millis
     * @param maxMillis
     */
    public static void sleep(long millis, long maxMillis) {
        sleep(Math.min(millis, maxMillis));
    }

}
