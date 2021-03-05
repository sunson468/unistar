package com.up1234567.unistar.central;

import com.up1234567.unistar.central.service.base.impl.UpdateService;
import com.up1234567.unistar.common.util.StringUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import javax.annotation.PostConstruct;

@EnableAsync(proxyTargetClass = true)
@SpringBootApplication(
        scanBasePackages = {"com.up1234567"},
        exclude = {
                MongoAutoConfiguration.class // 禁用MongoDB的自动配置
                , DataSourceAutoConfiguration.class // 禁用JDBC的自动配置
                , RedisAutoConfiguration.class // 禁用Redis的自动配置
        }
)
public class CentralApplication {

    private final static String DEFAULT_ASYNC_TASKER = "Unistar-Async-TaskPool";

    @Autowired
    private UpdateService updateService;

    @PostConstruct
    public void init() {
        updateService.checkVersion();
    }

    public static void main(String[] args) {
        SpringApplication.run(CentralApplication.class, args);
    }

    @Bean
    public TaskExecutor defaultExecuter() {
        ThreadPoolTaskExecutor defaultExecuter = new ThreadPoolTaskExecutor();
        defaultExecuter.setThreadGroupName(DEFAULT_ASYNC_TASKER);
        defaultExecuter.setThreadNamePrefix(DEFAULT_ASYNC_TASKER + StringUtil.H_LINE);
        defaultExecuter.setMaxPoolSize(2);
        return defaultExecuter;
    }

}
