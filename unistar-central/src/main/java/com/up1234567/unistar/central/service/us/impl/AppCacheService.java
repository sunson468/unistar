package com.up1234567.unistar.central.service.us.impl;

import com.up1234567.unistar.central.service.us.IAppCacheService;
import com.up1234567.unistar.central.support.cache.IUnistarCache;
import com.up1234567.unistar.common.logger.UnistarLoggerSearchParam;
import com.up1234567.unistar.common.util.JsonUtil;
import com.up1234567.unistar.common.util.StringUtil;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

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

    /**
     * @param namespace
     * @param searchId
     * @param result
     */
    @Async
    public void addLoggerSearchResult(String namespace, String searchId, UnistarLoggerSearchParam result) {
        String k = String.format(CK_NS_LOGGER_SEARCH, namespace, searchId);
        unistarCache.listAdd(k, JsonUtil.toJsonString(result));
        unistarCache.expire(k, 600);
    }

    /**
     * @param namespace
     * @param searchId
     * @return
     */
    public List<UnistarLoggerSearchParam> listLoggerSearchResult(String namespace, String searchId) {
        String k = String.format(CK_NS_LOGGER_SEARCH, namespace, searchId);
        List<String> retList = unistarCache.listGet(k);
        if (CollectionUtils.isEmpty(retList)) return Collections.emptyList();
        return retList.stream().map(s -> JsonUtil.toClass(s, UnistarLoggerSearchParam.class)).collect(Collectors.toList());
    }


}
