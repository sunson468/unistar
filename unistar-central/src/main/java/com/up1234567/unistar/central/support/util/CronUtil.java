package com.up1234567.unistar.central.support.util;

import com.up1234567.unistar.common.util.DateUtil;
import org.springframework.scheduling.support.CronSequenceGenerator;

import java.util.Date;

/**
 * Cron校验
 * 不得低于5秒的周期
 * 秒 分 时 月天数 月 周天数
 */
public final class CronUtil {

    /**
     * @param cron
     * @return
     */
    public static boolean validCron(String cron) {
        long now = DateUtil.now();
        CronSequenceGenerator generator = new CronSequenceGenerator(cron);
        Date nextTime = generator.next(new Date(now));
        Date nextTime2 = generator.next(nextTime);
        return nextTime2.getTime() >= nextTime.getTime() + DateUtil.SECOND_5;
    }

    /**
     * @param cron
     * @param begin
     * @return
     */
    public static long nextRunTime(String cron, long begin) {
        CronSequenceGenerator generator = new CronSequenceGenerator(cron);
        Date nextTime = generator.next(new Date(begin));
        return nextTime.getTime();
    }

}
