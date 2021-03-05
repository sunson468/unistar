package com.up1234567.unistar.springcloud.limit;

import com.up1234567.unistar.common.limit.UnistarAppLimit;
import com.up1234567.unistar.common.util.DateUtil;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class UnistarLimitManager {

    // Controller层控制器
    private final ConcurrentHashMap<String, UnistarLimitWrapper> controllerLimits = new ConcurrentHashMap<>();
    // Feign层控制器
    private final ConcurrentHashMap<String, UnistarLimitWrapper> feignLimits = new ConcurrentHashMap<>();
    // Feign层熔断器
    private final ConcurrentHashMap<String, UnistarLimitWrapper> feignRuleLimits = new ConcurrentHashMap<>();

    /**
     * 设置Controller的控制
     *
     * @param limits
     */
    public void addControllerLimits(List<UnistarAppLimit> limits) {
        if (CollectionUtils.isEmpty(limits)) return;
        limits.forEach(this::updateUnistarAppLimit);
    }

    /**
     * 添加Feign
     *
     * @param limits
     */
    public void addFeignLimits(List<UnistarAppLimit> limits) {
        if (CollectionUtils.isEmpty(limits)) return;
        limits.forEach(this::updateUnistarAppLimit);
    }

    /**
     * @param appLimit
     */
    public void updateUnistarAppLimit(UnistarAppLimit appLimit) {
        if (appLimit.isBefore()) {
            // Feign请求控制
            if (appLimit.isAuto()) {
                // 常规控制
                feignLimits.put(appLimit.getPath(), new UnistarLimitWrapper(appLimit, true));
            } else {
                // 熔断规则
                feignRuleLimits.put(appLimit.getPath(), new UnistarLimitWrapper(appLimit));
            }
        } else {
            // Controller来源控制
            controllerLimits.put(appLimit.getPath(), new UnistarLimitWrapper(appLimit, true));
        }
    }

    /**
     * 校验规则是否有效
     */
    public void validCheck() {
        long now = DateUtil.now();
        if (!CollectionUtils.isEmpty(controllerLimits)) controllerLimits.forEach((p, l) -> l.checkValid(now));
        if (!CollectionUtils.isEmpty(feignLimits)) feignLimits.forEach((s, l) -> l.checkValid(now));
        if (!CollectionUtils.isEmpty(feignRuleLimits)) feignRuleLimits.forEach((s, l) -> l.checkValid(now));

    }

    /**
     * 检测Controller层是否需要控制
     *
     * @param matchPath
     * @param preService
     * @param preServiceGroup
     */
    public void controllerLimit(String matchPath, String preService, String preServiceGroup) {
        if (CollectionUtils.isEmpty(controllerLimits)) return;
        UnistarLimitWrapper appLimit = controllerLimits.get(matchPath);
        if (appLimit != null && appLimit.matched(preService, preServiceGroup)) appLimit.limitRequest();
    }

    /**
     * 检测Controller层是否需要控制
     *
     * @param matchPath
     */
    public void feignLimit(String matchPath) {
        // 是否有熔断规则
        if (CollectionUtils.isEmpty(feignRuleLimits)) return;
        // 先检测
        UnistarLimitWrapper appLimit = feignRuleLimits.get(matchPath);
        if (appLimit != null && appLimit.matched()) {
            // 熔断规则限流
            appLimit.limitRequest();
        } else {
            // 常规限流
            if (CollectionUtils.isEmpty(feignLimits)) return;
            appLimit = feignLimits.get(matchPath);
            if (appLimit == null || !appLimit.matched()) return;
            appLimit.limitRequest();
        }
    }

    /**
     * 触发限流规则
     *
     * @param matchPath
     */
    public void triggerFeignLimitRule(String matchPath) {
        if (CollectionUtils.isEmpty(feignRuleLimits)) return;
        UnistarLimitWrapper appLimit = feignRuleLimits.get(matchPath);
        if (appLimit == null) return;
        appLimit.addErrors();
    }

}
