package com.up1234567.unistar.springcloud.task;

import java.util.Map;

public interface IUnistarTasker {

    String task();

    /**
     * @param params
     */
    Map<String, Object> handle(Map<String, Object> params);

}
