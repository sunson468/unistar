package com.up1234567.unistar.common.task;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
public class UnistarTaskParam {

    private boolean available;

    // 任务，Null不作为任务执行器
    private List<String> tasks;

}
