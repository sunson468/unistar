package com.up1234567.unistar.springcloud.config.event;

import com.up1234567.unistar.springcloud.config.UnistarPropertySourceLocator;
import com.up1234567.unistar.springcloud.core.event.AUnistarEventListener;
import com.up1234567.unistar.common.event.IUnistarEventConst;

import java.util.Map;

@AUnistarEventListener(IUnistarEventConst.EVENT_CONFIG_CHANGED)
public class UnistarConfigChangedEventListener {

    private UnistarPropertySourceLocator unistarPropertySourceLocator;

    public UnistarConfigChangedEventListener(UnistarPropertySourceLocator unistarPropertySourceLocator) {
        this.unistarPropertySourceLocator = unistarPropertySourceLocator;
    }

    public void handle(Map<String, Object> param) {
        if (param != null && param.size() > 0) {
            unistarPropertySourceLocator.updateConfig(param);
        }
    }

}
