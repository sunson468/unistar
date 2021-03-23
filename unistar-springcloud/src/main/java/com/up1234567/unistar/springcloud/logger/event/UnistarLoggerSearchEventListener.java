package com.up1234567.unistar.springcloud.logger.event;

import com.up1234567.unistar.common.IUnistarConst;
import com.up1234567.unistar.common.event.IUnistarEventConst;
import com.up1234567.unistar.common.logger.IUnistarLogger;
import com.up1234567.unistar.common.logger.UnistarLoggerFactory;
import com.up1234567.unistar.common.logger.UnistarLoggerSearchParam;
import com.up1234567.unistar.common.util.OsUtil;
import com.up1234567.unistar.common.util.StringUtil;
import com.up1234567.unistar.springcloud.core.event.AUnistarEventListener;
import com.up1234567.unistar.springcloud.core.event.IUnistarClientDispatcher;
import org.springframework.boot.context.event.ApplicationPreparedEvent;
import org.springframework.boot.context.logging.LoggingApplicationListener;
import org.springframework.boot.logging.LogFile;
import org.springframework.boot.system.SystemProperties;
import org.springframework.context.ApplicationListener;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.io.File;

@AUnistarEventListener(IUnistarEventConst.EVENT_LOG_SEARCH)
public class UnistarLoggerSearchEventListener implements ApplicationListener<ApplicationPreparedEvent> {

    private static final IUnistarLogger logger = UnistarLoggerFactory.getLogger(IUnistarConst.DEFAULT_LOGGER_UNISTAR);
    private String logFilePath = null;
    private IUnistarClientDispatcher unistarClientDispatcher;

    public UnistarLoggerSearchEventListener(IUnistarClientDispatcher unistarClientDispatcher) {
        this.unistarClientDispatcher = unistarClientDispatcher;
    }

    @Override
    public void onApplicationEvent(ApplicationPreparedEvent event) {
        LogFile logFile = (LogFile) event.getApplicationContext().getBeanFactory().getSingleton(LoggingApplicationListener.LOG_FILE_BEAN_NAME);
        if (logFile != null) {
            String logFilePath = logFile.toString();
            if (!logFilePath.startsWith(StringUtil.SLASH)) {
                logFilePath = SystemProperties.get("user.dir") + File.separator + logFilePath;
            }
            this.logFilePath = logFilePath;
        }
    }

    public void handle(UnistarLoggerSearchParam search) {
        if (logFilePath == null) return;
        if (search == null) return;
        if (StringUtils.isEmpty(search.getSearchId())) return;
        if (StringUtils.isEmpty(search.getKeyword())) return;
        logger.debug("unistar client start to search logger with {}", search);
        //
        search.setResults(OsUtil.grep(logFilePath, search.getKeyword(), search.getBefore(), search.getAfter(), search.getMaxline()));
        //
        if (!CollectionUtils.isEmpty(search.getResults())) unistarClientDispatcher.publish(IUnistarEventConst.EVENT_LOG_SEARCH, search);
    }
}
