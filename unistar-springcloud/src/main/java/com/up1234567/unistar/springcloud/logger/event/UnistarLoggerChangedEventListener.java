package com.up1234567.unistar.springcloud.logger.event;

import com.up1234567.unistar.common.IUnistarConst;
import com.up1234567.unistar.common.event.IUnistarEventConst;
import com.up1234567.unistar.common.logger.IUnistarLogger;
import com.up1234567.unistar.common.logger.UnistarLoggerFactory;
import com.up1234567.unistar.common.util.JsonUtil;
import com.up1234567.unistar.springcloud.core.event.AUnistarEventListener;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.Map;

@AUnistarEventListener(IUnistarEventConst.EVENT_LOGGER_CHANGED)
public class UnistarLoggerChangedEventListener {

    private static final IUnistarLogger logger = UnistarLoggerFactory.getLogger(IUnistarConst.DEFAULT_LOGGER_UNISTAR);

    public void handle(String loggers) {
        if (StringUtils.isEmpty(loggers)) return;
        Map<String, Object> loggerDatas = JsonUtil.toMap(loggers);
        if (CollectionUtils.isEmpty(loggerDatas)) return;
        logger.debug("unistar client logger level changed: {}", loggers);
        UnistarLoggerFactory.setLoggers(loggerDatas);
    }

}
