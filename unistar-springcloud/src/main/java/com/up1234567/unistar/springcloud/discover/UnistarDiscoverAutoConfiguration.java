package com.up1234567.unistar.springcloud.discover;

import com.up1234567.unistar.springcloud.discover.event.UnistarServiceChangedEventListener;
import com.up1234567.unistar.springcloud.discover.event.UnistarTraceWatchEventListener;
import com.up1234567.unistar.springcloud.discover.feign.UnistarFeignBuilder;
import com.up1234567.unistar.springcloud.discover.feign.UnistarFeignRequestInterceptor;
import com.up1234567.unistar.springcloud.discover.loadbalancer.IUnistarLoadBalancer;
import com.up1234567.unistar.springcloud.discover.loadbalancer.UnistarWeightLoadBalancer;
import com.up1234567.unistar.springcloud.discover.servlet.UnistarServletAutoConfiguration;
import com.up1234567.unistar.springcloud.discover.servlet.UnistarServletHandlerInterceptor;
import com.up1234567.unistar.springcloud.discover.trace.IUnistarTraceWatcher;
import com.up1234567.unistar.springcloud.discover.trace.UnistarTraceContext;
import com.up1234567.unistar.springcloud.discover.trace.UnistarTraceStater;
import com.up1234567.unistar.springcloud.UnistarProperties;
import com.up1234567.unistar.springcloud.core.event.IUnistarClientDispatcher;
import com.up1234567.unistar.springcloud.limit.UnistarLimitManager;
import feign.Client;
import feign.Feign;
import feign.okhttp.OkHttpClient;
import okhttp3.ConnectionPool;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cache.support.NoOpCacheManager;
import org.springframework.cloud.client.ConditionalOnDiscoveryEnabled;
import org.springframework.cloud.commons.httpclient.OkHttpClientConnectionPoolFactory;
import org.springframework.cloud.commons.httpclient.OkHttpClientFactory;
import org.springframework.cloud.loadbalancer.blocking.client.BlockingLoadBalancerClient;
import org.springframework.cloud.openfeign.loadbalancer.FeignBlockingLoadBalancerClient;
import org.springframework.cloud.openfeign.support.FeignHttpClientProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

import javax.annotation.PreDestroy;
import java.util.concurrent.TimeUnit;

@Configuration(proxyBeanMethods = false)
@ConditionalOnDiscoveryEnabled
@EnableConfigurationProperties(UnistarProperties.class)
@ImportAutoConfiguration(UnistarServletAutoConfiguration.class)
public class UnistarDiscoverAutoConfiguration {

    @Bean
    public IUnistarLoadBalancer unistarLoadBalancer() {
        return new UnistarWeightLoadBalancer();
    }

    @Bean
    @ConditionalOnMissingBean(UnistarDiscoverClient.class)
    public UnistarDiscoverClient discoveryClient(UnistarProperties unistarProperties,
                                                 IUnistarClientDispatcher unistarClientDispatcher,
                                                 IUnistarLoadBalancer unistarLoadBalancer,
                                                 UnistarLimitManager unistarLimitManager) {
        UnistarDiscoverClient discoverClient = new UnistarDiscoverClient(unistarProperties, unistarClientDispatcher, unistarLoadBalancer, unistarLimitManager);
        unistarClientDispatcher.addClientListener(discoverClient);
        UnistarTraceContext.setUnistarProperties(unistarProperties);
        return discoverClient;
    }

    @Bean
    public NoOpCacheManager loadBalancerCacheManager() {
        return new NoOpCacheManager();
    }

    @Bean
    public UnistarServletHandlerInterceptor webHandlerInterceptor(UnistarProperties unistarProperties, UnistarLimitManager unistarLimitManager) {
        return new UnistarServletHandlerInterceptor(unistarProperties, unistarLimitManager);
    }

    @Bean
    public UnistarFeignRequestInterceptor feignRequestInterceptor(UnistarLimitManager unistarLimitManager) {
        return new UnistarFeignRequestInterceptor(unistarLimitManager);
    }

    @Configuration(proxyBeanMethods = false)
    @ConditionalOnMissingBean(okhttp3.OkHttpClient.class)
    static class OkHttpFeignConfiguration {

        private okhttp3.OkHttpClient okHttpClient;

        @Bean
        @ConditionalOnMissingBean(ConnectionPool.class)
        public ConnectionPool httpClientConnectionPool(
                FeignHttpClientProperties httpClientProperties,
                OkHttpClientConnectionPoolFactory connectionPoolFactory) {
            Integer maxTotalConnections = httpClientProperties.getMaxConnections();
            Long timeToLive = httpClientProperties.getTimeToLive();
            TimeUnit ttlUnit = httpClientProperties.getTimeToLiveUnit();
            return connectionPoolFactory.create(maxTotalConnections, timeToLive, ttlUnit);
        }

        @Bean
        public okhttp3.OkHttpClient client(UnistarProperties unistarProperties,
                                           OkHttpClientFactory httpClientFactory,
                                           ConnectionPool connectionPool) {
            this.okHttpClient = httpClientFactory
                    .createBuilder(false) // 内部微服务请求，无需SSL
                    .connectTimeout(unistarProperties.getDiscover().getFeign().getConnectTimeout(), TimeUnit.MILLISECONDS)
                    .readTimeout(unistarProperties.getDiscover().getFeign().getReadTimeout(), TimeUnit.MILLISECONDS)
                    .writeTimeout(unistarProperties.getDiscover().getFeign().getWriteTimeout(), TimeUnit.MILLISECONDS)
                    .connectionPool(connectionPool).build();
            return this.okHttpClient;
        }

        @PreDestroy
        public void destroy() {
            if (this.okHttpClient != null) {
                this.okHttpClient.dispatcher().executorService().shutdown();
                this.okHttpClient.connectionPool().evictAll();
            }
        }

    }

    @Bean
    public Client feignClient(okhttp3.OkHttpClient okHttpClient, BlockingLoadBalancerClient loadBalancerClient) {
        OkHttpClient delegate = new OkHttpClient(okHttpClient);
        return new FeignBlockingLoadBalancerClient(delegate, loadBalancerClient);
    }

    @Bean
    @Scope("prototype")
    public Feign.Builder feignBuilder() {
        return UnistarFeignBuilder.builder();
    }

    @Bean
    @ConditionalOnMissingBean(UnistarTraceStater.class)
    public UnistarTraceStater traceStater(IUnistarClientDispatcher unistarClientDispatcher) {
        UnistarTraceStater traceStater = new UnistarTraceStater(unistarClientDispatcher);
        UnistarTraceContext.setTraceStater(traceStater);
        unistarClientDispatcher.addClientListener(traceStater);
        return traceStater;
    }

    @Bean
    @ConditionalOnMissingBean(UnistarServiceChangedEventListener.class)
    public UnistarServiceChangedEventListener unistarServiceChangedEventListener(IUnistarClientDispatcher unistarClientDispatcher, UnistarDiscoverClient unistarDiscoverClient) {
        UnistarServiceChangedEventListener listener = new UnistarServiceChangedEventListener(unistarDiscoverClient);
        unistarClientDispatcher.addEventListener(listener);
        return listener;
    }

    @Bean
    @ConditionalOnMissingBean(UnistarTraceWatchEventListener.class)
    public UnistarTraceWatchEventListener traceWatchEventListener(IUnistarClientDispatcher unistarClientDispatcher, IUnistarTraceWatcher unistarTraceWatcher) {
        UnistarTraceWatchEventListener listener = new UnistarTraceWatchEventListener(unistarTraceWatcher);
        unistarClientDispatcher.addEventListener(listener);
        return listener;
    }

}
