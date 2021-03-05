package com.up1234567.unistar.springcloud.discover.servlet;

import com.up1234567.unistar.springcloud.discover.trace.IUnistarTraceWatcher;
import com.up1234567.unistar.springcloud.discover.trace.UnistarTraceContext;
import com.up1234567.unistar.common.IUnistarConst;
import com.up1234567.unistar.common.exception.UnistarLimitException;
import com.up1234567.unistar.common.logger.IUnistarLogger;
import com.up1234567.unistar.common.logger.UnistarLoggerFactory;
import com.up1234567.unistar.common.util.StringUtil;
import com.up1234567.unistar.springcloud.UnistarProperties;
import com.up1234567.unistar.springcloud.limit.UnistarLimitManager;
import org.springframework.util.StringUtils;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.HandlerMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class UnistarServletHandlerInterceptor implements HandlerInterceptor, IUnistarTraceWatcher {

    private static final IUnistarLogger logger = UnistarLoggerFactory.getLogger(IUnistarConst.DEFAULT_LOGGER_UNISTAR);

    private List<String> ignoredPathes = Arrays.asList("/error", "/**", "/favicon.ico");

    private final UnistarProperties unistarProperties;
    private UnistarLimitManager unistarLimitManager;
    //
    private final Set<String> watchingPathes = new HashSet<>();

    public UnistarServletHandlerInterceptor(UnistarProperties unistarProperties, UnistarLimitManager unistarLimitManager) {
        this.unistarProperties = unistarProperties;
        this.unistarLimitManager = unistarLimitManager;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 只对Controller类请求进行监控
        if (handler instanceof HandlerMethod) {
            Object matched = request.getAttribute(HandlerMapping.BEST_MATCHING_PATTERN_ATTRIBUTE);
            if (matched instanceof String) {
                String matchedPath = (String) matched;
                if (ignoredPathes.contains(matchedPath)) return true;
                // 所有监控路径都为 http://服务名称/服务路径 的形式
                String matchedUrl = StringUtil.wrapHttpUrl(unistarProperties.getName(), matchedPath);
                // 增加
                UnistarTraceContext.setContextGroup("SpringBoot Context");
                // 监听
                boolean watching = isWatching(matchedUrl);
                UnistarTraceContext.addServletTrace(request, matchedUrl, watching);
                // 限流拦截器
                try {
                    String preService = request.getHeader(IUnistarConst.HTTP_SERVICE);
                    String preServiceGroup = request.getHeader(IUnistarConst.HTTP_SERVICE_GROUP);
                    unistarLimitManager.controllerLimit(matchedUrl, preService, preServiceGroup);
                } catch (UnistarLimitException e) {
                    this.afterCompletion(request, response, handler, e);
                    throw e;
                }
            }
        }
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        if (handler instanceof HandlerMethod) {
            UnistarTraceContext.endTrace(ex == null, ex == null ? null : ex.getMessage());
        }
    }

    // 监听后则立即移除，只会进行一次监听
    private boolean isWatching(String path) {
        if (StringUtils.isEmpty(path)) return false;
        synchronized (watchingPathes) {
            return watchingPathes.remove(path);
        }
    }

    @Override
    public void addWatch(String path) {
        logger.debug("unstar client start watch path: {}", path);
        if (StringUtils.isEmpty(path)) return;
        synchronized (watchingPathes) {
            watchingPathes.add(path);
        }
    }

}
