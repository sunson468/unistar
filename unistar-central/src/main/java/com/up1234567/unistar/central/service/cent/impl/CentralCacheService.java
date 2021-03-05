package com.up1234567.unistar.central.service.cent.impl;

import com.up1234567.unistar.central.service.cent.ICentralCacheService;
import com.up1234567.unistar.central.data.cent.Schedule;
import com.up1234567.unistar.central.support.cache.IUnistarCache;
import com.up1234567.unistar.common.IUnistarConst;
import com.up1234567.unistar.common.util.DateUtil;
import com.up1234567.unistar.common.util.JsonUtil;
import com.up1234567.unistar.common.util.StringUtil;
import lombok.CustomLog;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@CustomLog(topic = IUnistarConst.DEFAULT_LOGGER_UNISTAR)
@Service
public class CentralCacheService implements ICentralCacheService {

    @Autowired
    private IUnistarCache unistarCache;

    /**
     * @param k
     * @param clazz
     * @param <T>
     * @return
     */
    private <T> T getAs(String k, Class<T> clazz) {
        String cache = unistarCache.get(k);
        if (StringUtils.isEmpty(cache)) {
            return null;
        }
        return JsonUtil.toClass(cache, clazz);
    }

    /**
     * @param now
     */
    public void setVoting(long now) {
        unistarCache.set(CK_VOTING, String.valueOf(now), 30);
    }

    /**
     * @param now
     * @return
     */
    public boolean canVoting(long now) {
        String voteNow = unistarCache.get(CK_VOTING);
        if (StringUtils.isEmpty(voteNow)) return true;
        return now - Long.parseLong(voteNow) < DateUtil.SECOND_5;
    }

    /**
     * 取消Voting状态
     */
    public void finishVoting() {
        unistarCache.del(CK_VOTING);
    }

    /**
     * 是否定时器中
     *
     * @return
     */
    public boolean isVoting() {
        return unistarCache.has(CK_VOTING);
    }

    /**
     * 设置定时器在运行中
     */
    public void setTimering() {
        unistarCache.set(CK_TIMERING, StringUtil.H_LINE, 10);
    }

    /**
     * 是否定时器中
     *
     * @return
     */
    public boolean isTimering() {
        return unistarCache.has(CK_TIMERING);
    }

    /**
     * @param namespace
     * @param name
     * @return
     */
    public Schedule findSchedule(String namespace, String name) {
        String k = String.format(CK_SCHEDULE, namespace, name);
        return getAs(k, Schedule.class);
    }

    /**
     * @param schedule
     * @return
     */
    public void saveSchedule(Schedule schedule) {
        String k = String.format(CK_SCHEDULE, schedule.getNamespace(), schedule.getName());
        unistarCache.set(k, JsonUtil.toJsonString(schedule));
    }

    /**
     * @param namespace
     * @param name
     * @return
     */
    public void delSchedule(String namespace, String name) {
        String k = String.format(CK_SCHEDULE, namespace, name);
        unistarCache.del(k);
    }

}
