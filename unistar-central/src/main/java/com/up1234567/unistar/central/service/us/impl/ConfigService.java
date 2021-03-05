package com.up1234567.unistar.central.service.us.impl;

import com.up1234567.unistar.central.data.us.ConfigDepend;
import com.up1234567.unistar.central.data.us.Config;
import com.up1234567.unistar.central.support.data.IUnistarDao;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ConfigService {

    @Autowired
    private IUnistarDao unistarDao;

    /**
     * 所有可被继承的
     *
     * @return
     */
    public List<Config> listDependableConfig(String namespace) {
        Map<String, Object> props = new HashMap<>();
        props.put("namespace", namespace);
        props.put("dependable", true);
        return unistarDao.listByProps(props, Config.class);
    }

    /**
     * 查找应用所有Profile对应的配置
     *
     * @param namespace
     * @param name
     * @return
     */
    public List<Config> listConfig(String namespace, String name) {
        Map<String, Object> props = new HashMap<>();
        props.put("namespace", namespace);
        props.put("name", name);
        return unistarDao.listByProps(props, Config.class);
    }

    /**
     * 查找应用所有依赖的配置
     *
     * @param namespace
     * @param names
     * @return
     */
    public List<Config> listConfig(String namespace, List<String> names) {
        Map<String, Object> props = new HashMap<>();
        props.put("namespace", namespace);
        props.put("name", names);
        return unistarDao.listByProps(props, Config.class);
    }

    /**
     * 查找应用所有Profile对应的配置
     *
     * @param namespace
     * @param name
     * @return
     */
    public void removeConfig(String namespace, String name) {
        Map<String, Object> props = new HashMap<>();
        props.put("namespace", namespace);
        props.put("name", name);
        unistarDao.removeByProps(props, Config.class);
    }

    /**
     * @param configs
     */
    public void saveConfig(List<Config> configs) {
        if (CollectionUtils.isEmpty(configs)) return;
        unistarDao.insertAll(configs);
    }

    /**
     * @param namespace
     * @param appname
     * @return
     */
    public ConfigDepend findConfigDepend(String namespace, String appname) {
        Map<String, Object> props = new HashMap<>();
        props.put("namespace", namespace);
        props.put("name", appname);
        return unistarDao.findOneByProps(props, ConfigDepend.class);
    }

    /**
     * @param namespace
     * @param appname
     * @param depends
     */
    public void updateConfigDepend(String namespace, String appname, String depends) {
        Map<String, Object> props = new HashMap<>();
        props.put("namespace", namespace);
        props.put("name", appname);
        Map<String, Object> updates = new HashMap<>();
        updates.put("depends", depends);
        unistarDao.updateOneByProps(props, updates, ConfigDepend.class);
    }

    /**
     * @param configDepend
     */
    public void createConfigDepend(ConfigDepend configDepend) {
        unistarDao.insert(configDepend);
    }
}
