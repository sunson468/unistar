package com.up1234567.unistar.springcloud.config;

import com.up1234567.unistar.common.IUnistarConst;
import com.up1234567.unistar.common.config.UnistarConfigData;
import com.up1234567.unistar.common.event.IUnistarEventConst;
import com.up1234567.unistar.common.exception.UnistarRemoteException;
import com.up1234567.unistar.common.logger.IUnistarLogger;
import com.up1234567.unistar.common.logger.UnistarLoggerFactory;
import com.up1234567.unistar.common.util.JsonUtil;
import com.up1234567.unistar.common.util.ThreadUtil;
import com.up1234567.unistar.springcloud.core.event.IUnistarClientDispatcher;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.cloud.bootstrap.config.PropertySourceLocator;
import org.springframework.cloud.context.environment.EnvironmentChangeEvent;
import org.springframework.cloud.endpoint.event.RefreshEvent;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.core.env.CompositePropertySource;
import org.springframework.core.env.Environment;
import org.springframework.core.env.PropertySource;
import org.springframework.util.CollectionUtils;

import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

public class UnistarPropertySourceLocator implements PropertySourceLocator, ApplicationListener<ApplicationReadyEvent> {

    private static final IUnistarLogger logger = UnistarLoggerFactory.getLogger(IUnistarConst.DEFAULT_LOGGER_UNISTAR);

    private final static String UNISTAR_SOURCE_NAME = "unistar-config";
    private final static String UNISTAR_SOURCE_REMOTE_NAME = "unistar-remote-config";

    private AtomicBoolean remoted = new AtomicBoolean(false);   // 控制只主动读取一次远程
    private AtomicBoolean inited = new AtomicBoolean(false);    // 配置是否已经拉取完成
    private AtomicBoolean ready = new AtomicBoolean(false);     // 应用是否准备就绪

    private IUnistarClientDispatcher unistarEventPublisher;

    private ApplicationContext context;

    private CompositePropertySource composite = null;
    private UnistarPropertySource unistarPropertySource = null;

    public UnistarPropertySourceLocator(IUnistarClientDispatcher unistarEventPublisher) {
        this.unistarEventPublisher = unistarEventPublisher;
    }

    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        this.context = event.getApplicationContext();
        ready.set(true);
    }

    @Override
    public PropertySource<?> locate(Environment environment) {
        if (remoted.compareAndSet(false, true)) {
            String[] profiles = environment.getActiveProfiles();
            if (profiles.length == 0) profiles = environment.getDefaultProfiles();
            UnistarConfigData configData = new UnistarConfigData();
            configData.setProfiles(Arrays.asList(profiles));
            unistarEventPublisher.readyParam().setProfiles(configData.getProfiles());
            unistarEventPublisher.publish(IUnistarEventConst.HANDLE_CONFIG, configData, (success, param) -> {
                if (success) {
                    logger.debug("load configs from unistar central server");
                    composite = new CompositePropertySource(UNISTAR_SOURCE_NAME);
                    unistarPropertySource = new UnistarPropertySource(UNISTAR_SOURCE_REMOTE_NAME, JsonUtil.toMap(param));
                    composite.addPropertySource(unistarPropertySource);
                }
            });
            ThreadUtil.loopUtilByTimes(() -> composite != null, 3, () -> {
                throw new UnistarRemoteException("cann't get config from unistar central server");
            });
        }
        return composite;
    }

    /**
     * 初次获取配置
     */
    public void refreshConfig() {
        // 刷新所有，包括环境变量和Bean
        context.publishEvent(new RefreshEvent(this, null, "unistar configs reload"));
    }

    /**
     * 只适用于存量更新，一般用于开关类
     *
     * @param updates
     */
    public void updateConfig(Map<String, Object> updates) {
        if (!ready.get()) {
            logger.warn("application hasn't bean ready for update config");
            return;
        }
        if (CollectionUtils.isEmpty(updates)) return;
        logger.debug("UnistarPropertySourceLocator updateConfig: {}", updates.keySet());
        unistarPropertySource.updateConfigs(updates);
        context.publishEvent(new EnvironmentChangeEvent(context, updates.keySet()));
    }
}
