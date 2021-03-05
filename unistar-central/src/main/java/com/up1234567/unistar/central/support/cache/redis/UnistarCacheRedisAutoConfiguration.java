package com.up1234567.unistar.central.support.cache.redis;


import com.up1234567.unistar.central.support.cache.IUnistarCache;
import com.up1234567.unistar.central.support.cache.UnistarCacheProperties;
import com.up1234567.unistar.central.support.core.UnistarProperties;
import com.up1234567.unistar.central.support.core.clust.IUnistarCluster;
import com.up1234567.unistar.common.IUnistarConst;
import com.up1234567.unistar.common.exception.UnistarInvalidConfigException;
import lombok.CustomLog;
import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.*;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.listener.adapter.MessageListenerAdapter;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Executors;

@CustomLog(topic = IUnistarConst.DEFAULT_LOGGER_UNISTAR)
@Configuration(proxyBeanMethods = false)
@ConditionalOnProperty(value = UnistarCacheProperties.TYPE, havingValue = "redis")
@EnableConfigurationProperties({UnistarCacheRedisProperties.class})
public class UnistarCacheRedisAutoConfiguration {

    private final static String SPLIT_N = ";";
    private final static String SPLIT_HP = ":";

    /**
     * 解析节点列表 <br/>
     * 192.168.0.1:6379;192.168.0.2:6379;192.168.0.3:6379
     *
     * @param nodes
     * @return
     */
    private Set<RedisNode> resolveNodes(String nodes) {
        if (StringUtils.isEmpty(nodes)) {
            throw new UnistarInvalidConfigException("nodes must be config");
        }
        Set<RedisNode> rets = new HashSet<>();
        for (String str : nodes.split(SPLIT_N)) {
            String[] hp = str.split(SPLIT_HP, 2);
            rets.add(new RedisNode(hp[0], Integer.parseInt(hp[1])));
        }
        return rets;
    }

    @Bean
    public LettuceConnectionFactory redisConnectionFactory(UnistarCacheRedisProperties properties) {
        if (StringUtils.isNotEmpty(properties.getHost())) {
            // 单节点
            RedisStandaloneConfiguration configuration = new RedisStandaloneConfiguration();
            configuration.setHostName(properties.getHost());
            if (StringUtils.isNotEmpty(properties.getPassword())) {
                configuration.setPassword(properties.getPassword());
            }
            configuration.setDatabase(properties.getDatabase());
            configuration.setPort(properties.getPort());
            return new LettuceConnectionFactory(configuration);
        } else if (StringUtils.isNotEmpty(properties.getMaster())) {
            // 哨兵模式
            RedisSentinelConfiguration configuration = new RedisSentinelConfiguration();
            if (StringUtils.isNotEmpty(properties.getPassword())) {
                configuration.setPassword(properties.getPassword());
            }
            configuration.setMaster(properties.getMaster());
            configuration.setDatabase(properties.getDatabase());
            configuration.setSentinels(resolveNodes(properties.getNodes()));
            return new LettuceConnectionFactory(configuration);
        } else if (StringUtils.isNotEmpty(properties.getNodes())) {
            // 集群模式
            RedisClusterConfiguration configuration = new RedisClusterConfiguration();
            if (StringUtils.isNotEmpty(properties.getPassword())) {
                configuration.setPassword(properties.getPassword());
            }
            configuration.setClusterNodes(resolveNodes(properties.getNodes()));
            return new LettuceConnectionFactory(configuration);
        }
        throw new UnistarInvalidConfigException("need redis config one of standlone, sentinel, cluster");
    }

    @Bean("redisTemplate")
    public RedisTemplate<String, String> redisTemplate(RedisConnectionFactory redisConnectionFactory) {
        RedisTemplate<String, String> template = new RedisTemplate<>();
        template.setConnectionFactory(redisConnectionFactory);
        // 系统中都采用String作为属性值
        template.setDefaultSerializer(new StringRedisSerializer());
        template.afterPropertiesSet();
        return template;
    }

    @Bean
    @ConditionalOnProperty(UnistarProperties.PREFIX + ".clust")
    public MessageListenerAdapter unistarClusterReceiver(IUnistarCluster unistarCluster) {
        MessageListenerAdapter adapter = new MessageListenerAdapter(unistarCluster);
        adapter.afterPropertiesSet();
        return adapter;
    }

    @Bean
    public IUnistarCache unistarCache(RedisTemplate<String, String> redisTemplate) {
        log.debug("use redis cache");
        return new UnistarCacheRedis(redisTemplate);
    }

    @Bean
    @ConditionalOnProperty(UnistarProperties.PREFIX + ".clust")
    public RedisMessageListenerContainer redisMessageListenerContainer(MessageListenerAdapter unistarClusterReceiver, RedisConnectionFactory redisConnectionFactory) {
        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(redisConnectionFactory);
        container.addMessageListener(unistarClusterReceiver, ChannelTopic.of(IUnistarCluster.CENTRAL_CHANNEL));
        container.setTaskExecutor(Executors.newFixedThreadPool(1));
        return container;
    }


}
