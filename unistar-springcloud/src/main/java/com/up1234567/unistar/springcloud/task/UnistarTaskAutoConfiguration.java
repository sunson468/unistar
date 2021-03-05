package com.up1234567.unistar.springcloud.task;

import com.up1234567.unistar.springcloud.UnistarProperties;
import com.up1234567.unistar.springcloud.core.event.IUnistarClientDispatcher;
import com.up1234567.unistar.springcloud.task.event.UnistarTaskEventListener;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(UnistarProperties.class)
public class UnistarTaskAutoConfiguration {

    @Bean
    public UnistarTaskHandler unistarTaskHandler(UnistarProperties unistarProperties, IUnistarClientDispatcher unistarClientDispatcher) {
        return new UnistarTaskHandler(unistarProperties, unistarClientDispatcher);
    }

    @Bean
    @ConditionalOnMissingBean(UnistarTaskEventListener.class)
    public UnistarTaskEventListener unistarTaskEventListener(IUnistarClientDispatcher unistarClientDispatcher, UnistarTaskHandler unistarTaskHandler) {
        UnistarTaskEventListener listener = new UnistarTaskEventListener(unistarTaskHandler);
        unistarClientDispatcher.addEventListener(listener);
        return listener;
    }


}
