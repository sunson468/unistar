package com.up1234567.unistar.springcloud.discover.feign;

import com.up1234567.unistar.common.exception.UnistarNotSupportException;
import com.up1234567.unistar.springcloud.limit.UnistarLimitManager;
import feign.Client;
import feign.Feign;
import feign.InvocationHandlerFactory;
import feign.Target;
import feign.hystrix.FallbackFactory;
import org.springframework.beans.BeansException;
import org.springframework.cloud.openfeign.FeignContext;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.StringUtils;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.Map;

public class UnistarFeignBuilder {

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder extends Feign.Builder implements ApplicationContextAware {

        private ApplicationContext applicationContext;
        private FeignContext feignContext;
        private UnistarLimitManager unistarLimitManager;

        @Override
        public Feign.Builder client(Client client) {
            return super.client(new UnistarFeignClient(client));
        }

        @Override
        public Feign.Builder invocationHandlerFactory(InvocationHandlerFactory invocationHandlerFactory) {
            throw new UnistarNotSupportException("not support, because unistar rewrite");
        }

        @Override
        public Feign build() {
            super.invocationHandlerFactory(new InvocationHandlerFactory() {
                @Override
                public InvocationHandler create(Target target, Map<Method, MethodHandler> dispatch) {
                    Object feignClientFactoryBean = Builder.this.applicationContext.getBean("&" + target.type().getName());
                    Class fallback = (Class) getFieldValue(feignClientFactoryBean, "fallback");
                    Class fallbackFactory = (Class) getFieldValue(feignClientFactoryBean, "fallbackFactory");
                    String beanName = (String) getFieldValue(feignClientFactoryBean, "contextId");
                    if (!StringUtils.hasText(beanName)) {
                        beanName = (String) getFieldValue(feignClientFactoryBean, "name");
                    }

                    Object fallbackInstance;
                    FallbackFactory fallbackFactoryInstance;
                    // check fallback and fallbackFactory properties
                    if (void.class != fallback) {
                        fallbackInstance = getFromContext(beanName, "fallback", fallback, target.type());
                        return new UnistarFeignInvocationHandler(target, dispatch, unistarLimitManager, new FallbackFactory.Default(fallbackInstance));
                    }
                    if (void.class != fallbackFactory) {
                        fallbackFactoryInstance = (FallbackFactory) getFromContext(beanName, "fallbackFactory", fallbackFactory, FallbackFactory.class);
                        return new UnistarFeignInvocationHandler(target, dispatch, unistarLimitManager, fallbackFactoryInstance);
                    }
                    return new UnistarFeignInvocationHandler(target, dispatch, unistarLimitManager);
                }

                private Object getFromContext(String name, String type, Class fallbackType, Class targetType) {
                    Object fallbackInstance = feignContext.getInstance(name, fallbackType);
                    if (fallbackInstance == null) {
                        throw new IllegalStateException(String.format("No %s instance of type %s found for feign client %s", type, fallbackType, name));
                    }

                    if (!targetType.isAssignableFrom(fallbackType)) {
                        throw new IllegalStateException(String.format("Incompatible %s instance. Fallback/fallbackFactory of type %s is not assignable to %s for feign client %s", type, fallbackType, targetType, name));
                    }
                    return fallbackInstance;
                }
            });
            return super.build();
        }

        /**
         * @param instance
         * @param fieldName
         * @return
         */
        private Object getFieldValue(Object instance, String fieldName) {
            Field field = ReflectionUtils.findField(instance.getClass(), fieldName);
            field.setAccessible(true);
            try {
                return field.get(instance);
            } catch (IllegalAccessException e) {
                // ignore
            }
            return null;
        }

        @Override
        public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
            this.applicationContext = applicationContext;
            this.feignContext = applicationContext.getBean(FeignContext.class);
            this.unistarLimitManager = applicationContext.getBean(UnistarLimitManager.class);
        }
    }

}
