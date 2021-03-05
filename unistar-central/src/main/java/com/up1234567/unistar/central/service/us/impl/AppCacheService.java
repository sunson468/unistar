package com.up1234567.unistar.central.service.us.impl;

import com.up1234567.unistar.central.service.us.IAppCacheService;
import com.up1234567.unistar.central.support.cache.IUnistarCache;
import com.up1234567.unistar.common.util.StringUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class AppCacheService implements IAppCacheService {

    @Autowired
    private IUnistarCache unistarCache;

    /**
     * @param namespace
     * @param appname
     * @return
     */
    public List<String> listLoggerData(String namespace, String appname) {
        String key = String.format(CK_APP_NS_LOGGER, namespace, appname);
        try {
            return StringUtil.fromCommaString(unistarCache.get(key));
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    /**
     * @param namespace
     * @param appname
     * @param loggers
     */
    @Async
    public void saveLoggerData(String namespace, String appname, List<String> loggers) {
        String key = String.format(CK_APP_NS_LOGGER, namespace, appname);
        unistarCache.set(key, StringUtil.toCommaString(loggers));
    }


}
