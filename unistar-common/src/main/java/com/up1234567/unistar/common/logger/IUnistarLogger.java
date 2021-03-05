package com.up1234567.unistar.common.logger;

public interface IUnistarLogger {

    /**
     * 日志级别，级别越小，日志打印范围越窄
     */
    enum Level {
        OFF(0),
        ERROR(10),
        WARN(20),
        INFO(30),
        DEBUG(40),
        ;

        private int val;

        Level(int val) {
            this.val = val;
        }

        public boolean opened(Level level) {
            return this.val >= level.val;
        }

        /**
         * @param o
         * @return
         */
        public static Level toLevel(Object o) {
            if (o == null) return null;
            for (Level l : Level.values()) {
                if (o.equals(l.name())) {
                    return l;
                }
            }
            return null;
        }
    }

    /**
     * 记录日志
     *
     * @param msg
     * @param params 替代参数
     */
    void error(String msg, Object... params);

    /**
     * 记录日志
     *
     * @param msg
     * @param params 替代参数
     */
    void warn(String msg, Object... params);

    /**
     * 记录日志
     *
     * @param msg
     * @param params 替代参数
     */
    void info(String msg, Object... params);

    /**
     * 记录日志
     *
     * @param msg
     * @param params 替代参数
     */
    void debug(String msg, Object... params);

}
