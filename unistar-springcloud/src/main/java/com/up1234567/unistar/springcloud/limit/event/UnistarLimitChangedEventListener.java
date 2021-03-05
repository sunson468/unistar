package com.up1234567.unistar.springcloud.limit.event;

import com.up1234567.unistar.springcloud.core.event.AUnistarEventListener;
import com.up1234567.unistar.springcloud.limit.UnistarLimitManager;
import com.up1234567.unistar.common.IUnistarConst;
import com.up1234567.unistar.common.event.IUnistarEventConst;
import com.up1234567.unistar.common.limit.UnistarAppLimit;
import com.up1234567.unistar.common.logger.IUnistarLogger;
import com.up1234567.unistar.common.logger.UnistarLoggerFactory;

@AUnistarEventListener(IUnistarEventConst.EVENT_LIMIT_CHANGED)
public class UnistarLimitChangedEventListener {

    private static final IUnistarLogger logger = UnistarLoggerFactory.getLogger(IUnistarConst.DEFAULT_LOGGER_UNISTAR);

    private UnistarLimitManager unistarLimitManager;

    public UnistarLimitChangedEventListener(UnistarLimitManager unistarLimitManager) {
        this.unistarLimitManager = unistarLimitManager;
    }

    public void handle(UnistarAppLimit appLimit) {
        logger.debug("unistar client limit changed: {}", appLimit);
        unistarLimitManager.updateUnistarAppLimit(appLimit);
    }

}
