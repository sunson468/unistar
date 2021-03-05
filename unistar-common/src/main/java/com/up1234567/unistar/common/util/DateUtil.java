package com.up1234567.unistar.common.util;

import java.util.Date;

public final class DateUtil {

    public final static long SECOND = 1000L;
    public final static long MINUTE = 60L * SECOND;
    public final static long HOUR = 60L * MINUTE;
    public final static long DAY = 24L * HOUR;

    public final static long SECOND_5 = 5L * SECOND;
    public final static long SECOND_10 = 2L * SECOND_5;

    public final static long MINUTE_5 = 5L * MINUTE;

    public final static String FMT_YYYY_MM_DD = "yyyy-MM-dd";
    public final static String FMT_YYYY_MM_DD_HH_MM_SS = "yyyy-MM-dd HH:mm:ss";
    public final static String FMT_YYYY_MM_DD_HH_MM_SS_SSS = "yyyy-MM-dd HH:mm:ss:SSS";

    /**
     * 现在时间
     *
     * @return
     */
    public static long now() {
        return System.currentTimeMillis();
    }

    /**
     * 现在时间
     *
     * @return
     */
    public static long today() {
        return dayStart(now());
    }

    /**
     * 现在时间(UTC-8)
     *
     * @return
     */
    public static long dayStart(long millis) {
        return ((millis + 8L * HOUR) / DAY) * DAY - 8L * HOUR;
    }

    /**
     * 现在时间
     *
     * @return
     */
    public static long dayEnd(long millis) {
        return dayStart(millis) + DAY;
    }

    /**
     * 下一个整分钟
     *
     * @return
     */
    public static long nextMinute() {
        return nextMinute(now());
    }

    /**
     * 下一个整分钟
     *
     * @return
     */
    public static long nextMinute(long millis) {
        return ((millis + 8L * HOUR) / MINUTE) * MINUTE - 8L * HOUR + MINUTE;
    }

    /**
     * @return
     */
    public static Date nextMinuteDate() {
        return new Date(nextMinute());
    }

}
