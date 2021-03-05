package com.up1234567.unistar.common.task;

import lombok.Data;

import java.util.Map;

@Data
public class UnistarTaskData {

    private String name; // 任务名称
    private String no; // 任务编号
    private Map<String, Object> params; // 任务参数

    // =========================================
    private EStatus status; // 任务执行结果
    private String result; // 返回值，异常状态下为错误信息

    //
    public enum EStatus {
        WAIT,       // 等待执行
        EXECUTING,  // 执行中
        FINISHED,   // 执行完成
        OVERTIME,   // 超时未反馈
        FAILED,     // 执行失败
        CANCELED,   // 任务取消
        ;
    }

}
