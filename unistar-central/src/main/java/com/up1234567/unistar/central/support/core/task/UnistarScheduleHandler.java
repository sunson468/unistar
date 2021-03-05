package com.up1234567.unistar.central.support.core.task;

import com.up1234567.unistar.common.async.PoolExecutorService;

public class UnistarScheduleHandler {

    private IUnistarTaskRunner unistarTaskRunner;

    private PoolExecutorService executor = new PoolExecutorService();

    /**
     * @param unistarTaskRunner
     */
    public UnistarScheduleHandler(IUnistarTaskRunner unistarTaskRunner) {
        this.unistarTaskRunner = unistarTaskRunner;
    }

    /**
     * 开始分解计划
     *
     * @param startTime
     */
    public void handle(final long startTime) {
        // 执行任务
        executor.execute(() -> unistarTaskRunner.scheduleTasksRun(startTime));
        // 拆解任务，等待下一个周期执行
        executor.execute(() -> unistarTaskRunner.schedulesRun(startTime));
    }

}
