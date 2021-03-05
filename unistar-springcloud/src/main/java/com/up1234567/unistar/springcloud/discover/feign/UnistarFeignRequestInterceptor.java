package com.up1234567.unistar.springcloud.discover.feign;

import com.up1234567.unistar.springcloud.discover.trace.UnistarTraceContext;
import com.up1234567.unistar.common.util.StringUtil;
import com.up1234567.unistar.springcloud.limit.UnistarLimitManager;
import feign.RequestInterceptor;
import feign.RequestTemplate;

public class UnistarFeignRequestInterceptor implements RequestInterceptor {

    private final UnistarLimitManager unistarLimitManager;

    public UnistarFeignRequestInterceptor(UnistarLimitManager unistarLimitManager) {
        this.unistarLimitManager = unistarLimitManager;
    }

    @Override
    public void apply(RequestTemplate template) {
        unistarLimitManager.feignLimit(StringUtil.wrapHttpUrl(template.feignTarget().url(), template.path()));
        UnistarTraceContext.addFeignTrace(template);
    }

}
