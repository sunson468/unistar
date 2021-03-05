package com.up1234567.unistar.springcloud.task.event;

import com.up1234567.unistar.springcloud.core.event.AUnistarEventListener;
import com.up1234567.unistar.common.event.IUnistarEventConst;
import com.up1234567.unistar.common.task.UnistarTaskData;
import com.up1234567.unistar.springcloud.task.UnistarTaskHandler;

@AUnistarEventListener(IUnistarEventConst.EVENT_TASK)
public class UnistarTaskEventListener {

    private UnistarTaskHandler unistarTaskHandler;

    public UnistarTaskEventListener(UnistarTaskHandler unistarTaskHandler) {
        this.unistarTaskHandler = unistarTaskHandler;
    }

    /**
     * @param task
     */
    public void handle(UnistarTaskData task) {
        unistarTaskHandler.handle(task);
    }


}
