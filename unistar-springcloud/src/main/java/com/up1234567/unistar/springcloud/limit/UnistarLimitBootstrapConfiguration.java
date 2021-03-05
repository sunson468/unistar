package com.up1234567.unistar.springcloud.limit;

import com.up1234567.unistar.springcloud.UnistarBootstrapConfiguration;
import com.up1234567.unistar.springcloud.core.event.IUnistarClientDispatcher;
import com.up1234567.unistar.springcloud.limit.event.UnistarLimitChangedEventListener;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
@AutoConfigureAfter(UnistarBootstrapConfiguration.class)
public class UnistarLimitBootstrapConfiguration {

    @Bean
    public UnistarLimitManager unistarLimitManager() {
        return new UnistarLimitManager();
    }

    @Bean
    @ConditionalOnMissingBean(UnistarLimitChangedEventListener.class)
    public UnistarLimitChangedEventListener unistarLimitChangedEventListener(IUnistarClientDispatcher unistarClientDispatcher, UnistarLimitManager unistarLimitManager) {
        UnistarLimitChangedEventListener listener = new UnistarLimitChangedEventListener(unistarLimitManager);
        unistarClientDispatcher.addEventListener(listener);
        return listener;
    }

}
