package com.up1234567.unistar.springcloud.limit;

import com.google.common.util.concurrent.RateLimiter;
import com.up1234567.unistar.common.ds.TimeFrameCounter;
import com.up1234567.unistar.common.exception.UnistarLimitException;
import com.up1234567.unistar.common.limit.UnistarAppLimit;
import com.up1234567.unistar.common.util.DateUtil;
import lombok.Getter;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.time.Duration;

public class UnistarLimitWrapper {

    private final static int ONE = 1;

    private UnistarAppLimit appLimit;
    private TimeFrameCounter frameCounter;  // 错误计数器
    private Duration timeout; // 阻塞等待时间

    // 当前规则实例是指实际作用的规则数据
    private long startTime; // 当前规则实例有效开始时间
    private long endTime; // 当前规则实例有效结束时间
    private boolean valid = false; // 当前规则实例是否生效

    // 本体代表该规则是否已经完全失效
    @Getter
    private boolean destoryed = false; // 当前规则本体是否已废弃

    // 限流器
    private RateLimiter rateLimiter;
    // 是否正在降级限流中
    private boolean rating;
    // 降级锁
    private final Object rateLocker = new Object();

    public UnistarLimitWrapper(UnistarAppLimit appLimit) {
        this(appLimit, false);
    }

    public UnistarLimitWrapper(UnistarAppLimit appLimit, boolean inited) {
        this.appLimit = appLimit;
        this.frameCounter = new TimeFrameCounter(appLimit.getPeriod() * DateUtil.SECOND);
        this.timeout = appLimit.isFastfail() ? Duration.ZERO : Duration.ofSeconds(appLimit.getTimeout());
        // 构建一个限流器
        if (appLimit.getTimeout() == 0) appLimit.setTimeout(Integer.MAX_VALUE);
        if (appLimit.getEndTime() == 0) appLimit.setEndTime(Long.MAX_VALUE);
        // 如果可以就初始化
        if (inited) {
            initLimiter(appLimit.getStartTime(), appLimit.getEndTime());
            checkValid(DateUtil.now());
        }
    }

    /**
     * 初始化限制器
     */
    public void initLimiter(long startTime, long endTime) {
        this.rateLimiter = appLimit.getQps() == 0 ? null : RateLimiter.create(appLimit.getQps(), Duration.ofSeconds(appLimit.getWarmup()));
        this.startTime = startTime;
        if (endTime == 0) endTime = Long.MAX_VALUE;
        this.endTime = endTime;
    }

    /**
     * 校验限流本体是否在有效期内
     *
     * @return
     */
    public void checkValid(long now) {
        // 如果毁掉了则直接返回
        if (destoryed) return;
        // 先判断实体时间是否有效
        boolean isValid = startTime <= now && now < endTime;
        if (isValid) {
            // 实体有效，就有效
            valid = true;
        } else {
            // 实体无效
            valid = false;
            rating = false;
            // 是否过期了
            if (now > endTime) {
                // 判断本体否有效
                isValid = appLimit.getStartTime() <= now && now < appLimit.getEndTime();
                // 如果本体无效
                if (!isValid) {
                    // 设置为废弃
                    destoryed = true;
                }
            }
        }
    }

    /**
     * @return
     */
    public boolean matched() {
        return matched(null, null);
    }

    /**
     * 是否匹配
     *
     * @param service      针对Controller层的来源控制
     * @param serviceGroup 针对Controller层的来源控制
     * @return
     */
    public boolean matched(String service, String serviceGroup) {
        // 是否在有效期内
        boolean matched = valid;
        if (!matched) return false;
        // 是否在白名单分组内
        if (!StringUtils.isEmpty(serviceGroup) && !CollectionUtils.isEmpty(appLimit.getWhiteGroupList())) matched = !appLimit.getWhiteGroupList().contains(serviceGroup);
        if (!matched) return false;
        // 是否在白名单应用内
        if (!StringUtils.isEmpty(service) && !CollectionUtils.isEmpty(appLimit.getWhiteServiceList())) matched = !appLimit.getWhiteServiceList().contains(service);
        if (!matched) return false;
        // 匹配上
        return true;
    }

    /**
     * 开始限制
     */
    public void limitRequest() {
        // QPS为0
        // 有限流器，且争取到锁，说明可以执行
        if (appLimit.getQps() == 0
                || (rateLimiter != null && !rateLimiter.tryAcquire(ONE, timeout))) {
            throw new UnistarLimitException("unistar client's request has been limited");
        }
    }

    /**
     * 异常情况下
     */
    public void addErrors() {
        if (rating) return;
        synchronized (rateLocker) {
            if (rating) return;
            // 异常监控异步化
            long now = DateUtil.now();
            int errors = frameCounter.incCounter(now);
            // 超出了
            if (errors > appLimit.getErrors()) {
                rating = true;
                // 启动熔断限流
                initLimiter(0, now + appLimit.getRecover() * DateUtil.SECOND);
                // 有效期校准
                checkValid(now);
            }
        }
    }
}
