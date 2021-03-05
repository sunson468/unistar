package com.up1234567.unistar.central.support.cache.mem;


import com.up1234567.unistar.central.support.cache.IUnistarCache;
import com.up1234567.unistar.central.support.cache.UnistarCacheProperties;
import com.up1234567.unistar.central.support.core.UnistarProperties;
import com.up1234567.unistar.common.IUnistarConst;
import com.up1234567.unistar.common.exception.UnistarNotSupportException;
import lombok.CustomLog;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@CustomLog(topic = IUnistarConst.DEFAULT_LOGGER_UNISTAR)
@Configuration(proxyBeanMethods = false)
@ConditionalOnProperty(value = UnistarCacheProperties.TYPE, havingValue = "memory", matchIfMissing = true)
@EnableConfigurationProperties({UnistarProperties.class})
public class UnistarCacheMemAutoConfiguration {

    @Bean
    public IUnistarCache unistarCache(UnistarProperties unistarProperties) {
        log.debug("use memory cache");
        if (unistarProperties.isClust()) {
            throw new UnistarNotSupportException("cann't support memory cache in clust mode");
        }
        return new UnistarCacheMemory();
    }

}
