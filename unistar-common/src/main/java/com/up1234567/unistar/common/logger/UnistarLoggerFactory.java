package com.up1234567.unistar.common.logger;

import com.up1234567.unistar.common.IUnistarConst;
import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

public final class UnistarLoggerFactory {

    @Getter
    private final static Map<String, UnistarLogger> LOGGERS = new HashMap<>();

    private final static IUnistarLogger.Level DEFAULT_LEVEL = IUnistarLogger.Level.WARN;

    public static IUnistarLogger getLogger() {
        return getLogger(DEFAULT_LEVEL);
    }

    public static IUnistarLogger getLogger(IUnistarLogger.Level level) {
        return getLogger(IUnistarConst.DEFAULT_LOGGER, level);
    }

    public static IUnistarLogger getLogger(Class<?> clazz) {
        return getLogger(clazz, DEFAULT_LEVEL);
    }

    public static IUnistarLogger getLogger(Class<?> clazz, IUnistarLogger.Level level) {
        return getLogger(clazz.getName(), level);
    }

    public static IUnistarLogger getLogger(String name) {
        return getLogger(name, DEFAULT_LEVEL);
    }

    /**
     * 创建日志记录对象
     *
     * @param name
     * @param level
     * @return
     */
    public static IUnistarLogger getLogger(String name, IUnistarLogger.Level level) {
        synchronized (LOGGERS) {
            UnistarLogger logger = LOGGERS.get(name);
            if (logger == null) {
                logger = new UnistarLogger(name, level);
                LOGGERS.put(name, logger);
            }
            return logger;
        }
    }

    /**
     * 重新设定日志的级别
     *
     * @param loggers
     */
    public static void setLoggers(Map<String, Object> loggers) {
        synchronized (LOGGERS) {
            loggers.forEach((k, l) -> {
                if (IUnistarConst.DEFAULT_LOGGER_UNISTAR.equals(k)) return;
                IUnistarLogger.Level level = IUnistarLogger.Level.toLevel(l);
                if (level == null) return;
                if (!LOGGERS.containsKey(k)) return;
                LOGGERS.get(k).setLevel(level);
            });
        }
    }

}
