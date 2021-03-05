package com.up1234567.unistar.springcloud.discover.feign;

import com.up1234567.unistar.springcloud.discover.trace.UnistarTraceContext;
import com.up1234567.unistar.common.exception.UnistarLimitException;
import com.up1234567.unistar.springcloud.limit.UnistarLimitManager;
import feign.InvocationHandlerFactory;
import feign.Target;
import feign.hystrix.FallbackFactory;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.LinkedHashMap;
import java.util.Map;

import static feign.Util.checkNotNull;

public class UnistarFeignInvocationHandler implements InvocationHandler {

    private final static String FALL_BACK = "fallback";

    private final Target<?> target;
    private final Map<Method, InvocationHandlerFactory.MethodHandler> dispatch;
    private final UnistarLimitManager unistarLimitManager;
    //
    private FallbackFactory fallbackFactory;
    private Map<Method, Method> fallbackMethodMap;

    public UnistarFeignInvocationHandler(Target<?> target, Map<Method, InvocationHandlerFactory.MethodHandler> dispatch, UnistarLimitManager unistarLimitManager) {
        this.target = checkNotNull(target, "target");
        this.dispatch = checkNotNull(dispatch, "dispatch");
        this.unistarLimitManager = checkNotNull(unistarLimitManager, "dispatch");
    }

    public UnistarFeignInvocationHandler(Target<?> target, Map<Method, InvocationHandlerFactory.MethodHandler> dispatch, UnistarLimitManager unistarLimitManager, FallbackFactory fallbackFactory) {
        this(target, dispatch, unistarLimitManager);
        this.fallbackFactory = fallbackFactory;
        this.fallbackMethodMap = toFallbackMethod(dispatch);
    }

    @Override
    public Object invoke(final Object proxy, final Method method, final Object[] args) throws Throwable {
        if ("equals".equals(method.getName())) {
            try {
                Object otherHandler = args.length > 0 && args[0] != null ? Proxy.getInvocationHandler(args[0]) : null;
                return equals(otherHandler);
            } catch (IllegalArgumentException e) {
                return false;
            }
        } else if ("hashCode".equals(method.getName())) {
            return hashCode();
        } else if ("toString".equals(method.getName())) {
            return toString();
        }

        InvocationHandlerFactory.MethodHandler methodHandler = this.dispatch.get(method);
        Object result;
        try {
            if (target instanceof Target.HardCodedTarget) {
                try {
                    try {
                        result = methodHandler.invoke(args);
                        UnistarTraceContext.endTrace(true, null);
                        return result;
                    } catch (Exception e) {
                        // 如果是限流异常，则忽视
                        if (!(e instanceof UnistarLimitException)) unistarLimitManager.triggerFeignLimitRule(UnistarTraceContext.currentTracePath());
                        throw e;
                    }
                } catch (Throwable ex) {
                    if (fallbackFactory != null) {
                        try {
                            result = fallbackMethodMap.get(method).invoke(fallbackFactory.create(ex), args);
                            UnistarTraceContext.endTrace(false, FALL_BACK);
                            return result;
                        } catch (IllegalAccessException e) {
                            throw new AssertionError(e);
                        } catch (InvocationTargetException e) {
                            throw new AssertionError(e.getCause());
                        }
                    } else {
                        throw ex;
                    }
                }
            } else {
                result = methodHandler.invoke(args);
                UnistarTraceContext.endTrace(true, null);
                return result;
            }
        } catch (Exception e) {
            UnistarTraceContext.endTrace(false, e.getMessage());
            throw e;
        }
    }

    private static Map<Method, Method> toFallbackMethod(Map<Method, InvocationHandlerFactory.MethodHandler> dispatch) {
        Map<Method, Method> result = new LinkedHashMap<>();
        for (Method method : dispatch.keySet()) {
            method.setAccessible(true);
            result.put(method, method);
        }
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof UnistarFeignInvocationHandler) {
            UnistarFeignInvocationHandler other = (UnistarFeignInvocationHandler) obj;
            return target.equals(other.target);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return target.hashCode();
    }

    @Override
    public String toString() {
        return target.toString();
    }

}
