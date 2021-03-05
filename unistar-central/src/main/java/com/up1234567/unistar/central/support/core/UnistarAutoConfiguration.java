package com.up1234567.unistar.central.support.core;

import com.up1234567.unistar.central.support.core.clust.IUnistarClustRunner;
import com.up1234567.unistar.central.support.core.clust.IUnistarCluster;
import com.up1234567.unistar.central.support.core.clust.UnistarClustNodeServer;
import com.up1234567.unistar.central.support.core.task.IUnistarTaskRunner;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(UnistarProperties.class)
public class UnistarAutoConfiguration {

    @Bean
    @ConditionalOnBean({IUnistarTaskRunner.class, IUnistarClustRunner.class})
    public IUnistarCluster unistarCluster(UnistarProperties clustProperties, IUnistarTaskRunner unistarTaskRunner, IUnistarClustRunner unistarClustRunner) {
        return new UnistarClustNodeServer(clustProperties, unistarTaskRunner, unistarClustRunner);
    }

}
