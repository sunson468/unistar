package com.up1234567.unistar.central.support.cache.redis;

import com.up1234567.unistar.central.support.core.clust.IUnistarCluster;
import com.up1234567.unistar.central.support.cache.IUnistarCache;
import org.springframework.data.redis.core.RedisTemplate;

import java.time.Duration;
import java.util.Collection;
import java.util.List;
import java.util.Set;

public class UnistarCacheRedis implements IUnistarCache {

    private final RedisTemplate<String, String> redisTemplate;

    public UnistarCacheRedis(RedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Override
    public boolean has(String k) {
        return Boolean.TRUE.equals(redisTemplate.hasKey(k));
    }

    @Override
    public void set(String k, String v, int seconds) {
        if (seconds > 0) {
            redisTemplate.boundValueOps(k).set(v, Duration.ofSeconds(seconds));
        } else {
            redisTemplate.boundValueOps(k).set(v);
        }
    }

    @Override
    public void expire(String k, int seconds) {
        redisTemplate.boundValueOps(k).expire(Duration.ofSeconds(seconds));
    }

    @Override
    public void del(String k) {
        redisTemplate.delete(k);
    }

    @Override
    public void del(Collection<String> ks) {
        redisTemplate.delete(ks);
    }

    @Override
    public String get(String k) {
        return redisTemplate.boundValueOps(k).get();
    }

    @Override
    public List<String> get(Collection<String> ks) {
        return redisTemplate.opsForValue().multiGet(ks);
    }

    @Override
    public void inc(String k, int num) {
        if (num > 0) {
            redisTemplate.boundValueOps(k).increment(num);
        } else if (num < 0) {
            redisTemplate.boundValueOps(k).decrement(num);
        }
    }

    @Override
    public void setAdd(String k, Collection<String> vs) {
        redisTemplate.boundSetOps(k).add(vs.toArray(new String[0]));
    }

    @Override
    public void setDel(String k, Collection<String> vs) {
        redisTemplate.boundSetOps(k).remove(vs.toArray());
    }

    @Override
    public long setLen(String k) {
        Long size = redisTemplate.boundSetOps(k).size();
        return size == null ? 0 : size;
    }

    @Override
    public Set<String> setGet(String k) {
        return redisTemplate.boundSetOps(k).members();
    }

    @Override
    public long listAdd(String k, String v) {
        Long size = redisTemplate.boundListOps(k).rightPush(v);
        return size == null ? 0 : size;
    }

    @Override
    public void listDel(String k, Collection<String> vs) {
        vs.parallelStream().forEach(v -> redisTemplate.boundListOps(k).remove(0, v));
    }

    @Override
    public void listTrim(String k, int start, int end) {
        redisTemplate.boundListOps(k).range(start, end);
    }

    @Override
    public List<String> listGet(String k) {
        return redisTemplate.boundListOps(k).range(0, -1);
    }

    @Override
    public long listLen(String k) {
        Long size = redisTemplate.boundListOps(k).size();
        return size == null ? 0 : size;
    }

    @Override
    public void publish(String message) {
        redisTemplate.convertAndSend(IUnistarCluster.CENTRAL_CHANNEL, message);
    }

}
