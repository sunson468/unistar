package com.up1234567.unistar.springcloud;

import com.up1234567.unistar.springcloud.core.event.IUnistarClientDispatcher;
import com.up1234567.unistar.springcloud.limit.UnistarLimitManager;
import com.up1234567.unistar.springcloud.core.UnistarClientManager;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
public class UnistarBootstrapConfiguration {

    @Bean
    public UnistarProperties unistarProperties() {
        return new UnistarProperties();
    }

    @Bean
    @ConditionalOnMissingBean(IUnistarClientDispatcher.class)
    public IUnistarClientDispatcher unistarClientDispatcher(UnistarProperties unistarProperties, UnistarLimitManager unistarLimitManager) {
        return new UnistarClientManager(unistarProperties, unistarLimitManager);
    }

}
