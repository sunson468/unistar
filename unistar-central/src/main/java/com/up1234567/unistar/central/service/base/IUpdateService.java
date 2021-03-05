package com.up1234567.unistar.central.service.base;

public interface IUpdateService {

    /**
     * 展示的版本号
     *
     * @return
     */
    String version();

    /**
     * 更新逻辑
     */
    void update();

    /**
     * 执行异常的回退方案
     */
    void fallback();

}
