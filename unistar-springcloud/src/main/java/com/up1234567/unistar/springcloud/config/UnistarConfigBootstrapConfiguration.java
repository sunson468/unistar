package com.up1234567.unistar.springcloud.config;

import com.up1234567.unistar.springcloud.UnistarBootstrapConfiguration;
import com.up1234567.unistar.springcloud.config.event.UnistarConfigChangedEventListener;
import com.up1234567.unistar.springcloud.core.event.IUnistarClientDispatcher;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration(proxyBeanMethods = false)
@AutoConfigureAfter(UnistarBootstrapConfiguration.class)
public class UnistarConfigBootstrapConfiguration {

    @Bean
    @Primary
    public UnistarPropertySourceLocator unistarPropertySourceLocator(IUnistarClientDispatcher unistarEventPublisher) {
        return new UnistarPropertySourceLocator(unistarEventPublisher);
    }

    @Bean
    @ConditionalOnMissingBean(UnistarConfigChangedEventListener.class)
    public UnistarConfigChangedEventListener unistarConfigChangedEventListener(IUnistarClientDispatcher unistarClientDispatcher, UnistarPropertySourceLocator unistarPropertySourceLocator) {
        UnistarConfigChangedEventListener listener = new UnistarConfigChangedEventListener(unistarPropertySourceLocator);
        unistarClientDispatcher.addEventListener(listener);
        return listener;
    }

}
