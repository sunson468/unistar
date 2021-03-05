package com.up1234567.unistar.springcloud.registry;

import com.up1234567.unistar.springcloud.UnistarProperties;
import com.up1234567.unistar.springcloud.core.event.IUnistarClientDispatcher;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.client.ConditionalOnDiscoveryEnabled;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration(proxyBeanMethods = false)
@ConditionalOnDiscoveryEnabled
@EnableConfigurationProperties(UnistarProperties.class)
@ConditionalOnProperty(value = {UnistarProperties.PREFIX + ".registry.enabled", "spring.cloud.service-registry.auto-registration.enabled"}, matchIfMissing = true)
public class UnistarServiceRegistryAutoConfiguration {

    @Bean
    @Primary
    @ConditionalOnMissingBean(UnistarAutoServiceRegistration.class)
    public UnistarAutoServiceRegistration unistarAutoServiceRegistration(
            UnistarProperties unistarProperties,
            IUnistarClientDispatcher unistarEventPublisher) {
        return new UnistarAutoServiceRegistration(unistarProperties, unistarEventPublisher);
    }


}
