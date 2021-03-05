package com.up1234567.unistar.common.logger;

import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 日志工具，基于Log4j
 */
public class UnistarLogger implements IUnistarLogger {

    private final String name;
    @Setter
    private IUnistarLogger.Level level;

    private Logger logger = null;

    public UnistarLogger(String name, IUnistarLogger.Level level) {
        this.name = name;
        this.level = level;
    }

    private Logger getLogger() {
        if (logger == null) logger = LoggerFactory.getLogger(name);
        return logger;
    }

    @Override
    public void error(String msg, Object... params) {
        if (level.opened(Level.ERROR)) getLogger().error(msg, params);
    }

    @Override
    public void warn(String msg, Object... params) {
        if (level.opened(Level.WARN)) getLogger().warn(msg, params);
    }

    @Override
    public void info(String msg, Object... params) {
        if (level.opened(Level.INFO)) getLogger().info(msg, params);
    }

    @Override
    public void debug(String msg, Object... params) {
        if (level.opened(Level.DEBUG)) getLogger().debug(msg, params);
    }

}
