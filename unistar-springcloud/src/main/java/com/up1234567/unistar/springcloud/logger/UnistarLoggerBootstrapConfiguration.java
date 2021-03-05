package com.up1234567.unistar.springcloud.logger;

import com.up1234567.unistar.springcloud.logger.event.UnistarLoggerChangedEventListener;
import com.up1234567.unistar.springcloud.UnistarBootstrapConfiguration;
import com.up1234567.unistar.springcloud.core.event.IUnistarClientDispatcher;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
@AutoConfigureAfter(UnistarBootstrapConfiguration.class)
public class UnistarLoggerBootstrapConfiguration {

    @Bean
    @ConditionalOnMissingBean(UnistarLoggerChangedEventListener.class)
    public UnistarLoggerChangedEventListener unistarLoggerChangedEventListener(IUnistarClientDispatcher unistarClientDispatcher) {
        UnistarLoggerChangedEventListener listener = new UnistarLoggerChangedEventListener();
        unistarClientDispatcher.addEventListener(listener);
        return listener;
    }

}
