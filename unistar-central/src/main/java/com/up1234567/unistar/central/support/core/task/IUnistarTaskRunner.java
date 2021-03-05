package com.up1234567.unistar.central.support.core.task;

public interface IUnistarTaskRunner {

    /**
     * 心跳执行
     *
     * @param master 是否为Master
     */
    void heartbeat(boolean master);

    /**
     * 开始对计划生成任务
     *
     * @param before
     */
    void schedulesRun(long before);

    /**
     * 执行时间点之前的运行任务
     *
     * @param before
     */
    void scheduleTasksRun(long before);

}
