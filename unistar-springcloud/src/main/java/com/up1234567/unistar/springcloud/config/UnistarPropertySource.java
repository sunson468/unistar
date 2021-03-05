package com.up1234567.unistar.springcloud.config;

import org.springframework.core.env.MapPropertySource;

import java.util.Map;

public class UnistarPropertySource extends MapPropertySource {

    public UnistarPropertySource(String name, Map<String, Object> source) {
        super(name, source);
    }

    /**
     * @param configs
     */
    public void resetConfigs(Map<String, Object> configs) {
        Map<String, Object> source = this.getSource();
        source.clear();
        source.putAll(configs);
    }

    /**
     * @param updates
     */
    public void updateConfigs(Map<String, Object> updates) {
        Map<String, Object> source = this.getSource();
        source.putAll(updates);
    }
}
