package com.up1234567.unistar.central.support.core;

import com.up1234567.unistar.common.util.InetUtil;
import com.up1234567.unistar.common.util.StringUtil;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.core.env.Environment;
import org.springframework.util.StringUtils;

import javax.annotation.PostConstruct;

@Data
@ConfigurationProperties(UnistarProperties.PREFIX)
public class UnistarProperties {

    public final static String PREFIX = "unistar";

    // 是否开启Clust模式
    private boolean clust = true;
    // 应用的对外host
    private String host;
    // 应用的对外端口
    private int port = -1;

    private CleanCycle clean = new CleanCycle(); // 清理周期

    @Autowired
    private Environment environment;

    @PostConstruct
    public void init() {
        if (StringUtils.isEmpty(this.getHost())) {
            if (clust) {
                this.setHost(InetUtil.getInet4Address());
            } else {
                this.setHost("localhost");
            }
        }
        //
        if (this.getPort() == -1) {
            this.setPort(Integer.parseInt(environment.resolvePlaceholders("${server.port:8080}")));
        }
    }

    /**
     * @return
     */
    public String centerAddress() {
        return host + StringUtil.COLON + port;
    }

    @Data
    public static class CleanCycle {

        // 数据统计保存周期，超过trace天数的将在凌晨被移除
        private int trace = 3;
        // 任务执行记录保存周期，超过task天数的将在凌晨被移除
        private int task = 3;
        // 操作员操作日志保存周期，超过oplog天数的将在凌晨被移除
        private int oplog = 7;

    }

}
