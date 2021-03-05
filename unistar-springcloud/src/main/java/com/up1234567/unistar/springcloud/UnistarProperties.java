package com.up1234567.unistar.springcloud;

import com.up1234567.unistar.springcloud.discover.UnistarDiscoverProperties;
import com.up1234567.unistar.springcloud.registry.UnistarRegistryProperties;
import com.up1234567.unistar.common.IUnistarConst;
import com.up1234567.unistar.common.UnistarParam;
import com.up1234567.unistar.common.UnistarReadyParam;
import com.up1234567.unistar.common.discover.UnistarDiscoverData;
import com.up1234567.unistar.common.exception.UnistarNotSupportException;
import com.up1234567.unistar.common.util.InetUtil;
import com.up1234567.unistar.common.util.StringUtil;
import com.up1234567.unistar.springcloud.task.UnistarTaskProperties;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.core.env.Environment;
import org.springframework.util.StringUtils;

import javax.annotation.PostConstruct;

@Data
@ConfigurationProperties(UnistarProperties.PREFIX)
public class UnistarProperties {

    public final static String PREFIX = "spring.cloud.unistar";

    /**
     * unistar的控制中心地址，需同时支持Http和Websocket通讯
     */
    private String server = ":36524";

    /**
     * 授权Token
     * 非必须，初次注册时，如携带Token，则会注册为授权服务，所有该名称的服务都会进行授权校验，
     */
    private String token;

    /**
     * 命名空间
     */
    private String namespace = IUnistarConst.DEFAULT_NS;

    /**
     * 命名空间下的所属分组
     */
    private String group = IUnistarConst.DEFAULT_GROUP;

    /**
     * 服务名称，默认为应用名称
     * 注意：该名称作为服务的唯一标记，上线后请勿修改
     */
    private String name;

    /**
     * 注册的服务地址，如果不填写则自动获取内网IPv4
     */
    private String host;

    /**
     * 注册的服务端口，不设置自动获取${server.port}
     */
    private int port = -1;

    /**
     * 注册为服务的参数
     */
    private UnistarRegistryProperties registry = new UnistarRegistryProperties();

    /**
     * 注册为服务的参数
     */
    private UnistarDiscoverProperties discover = new UnistarDiscoverProperties();

    /**
     * 任务参数
     */
    private UnistarTaskProperties task = new UnistarTaskProperties();

    @Autowired
    private Environment environment;

    @PostConstruct
    public void init() {
        if (StringUtils.isEmpty(this.getHost())) {
            try {
                String host = environment.resolvePlaceholders("${spring.cloud.client.ip-address}");
                if (StringUtils.isEmpty(host)) {
                    host = InetUtil.getInet4Address();
                }
                this.setHost(host);
            } catch (Exception e) {
                throw new UnistarNotSupportException("unistar client must have a reachable host");
            }
        }
        // 查看server地址
        // 为空则默认为本地IP:36524
        // 冒号开头说明，端口自定义，但是Host沿用本机IP
        if (StringUtils.isEmpty(this.getServer())) {
            this.setServer(this.getHost() + StringUtil.COLON + "36524");
        } else if (this.getServer().startsWith(StringUtil.COLON)) {
            this.setServer(this.getHost() + this.getServer());
        }
        //
        this.setPort(Integer.parseInt(environment.resolvePlaceholders("${server.port:8080}")));
        //
        if (StringUtils.isEmpty(this.name)) {
            this.setName(environment.resolvePlaceholders("${spring.application.name}"));
        }
        // 重载
        this.registry.setWeight(Integer.parseInt(environment.getProperty(PREFIX + ".registry.weight", "1")));
    }

    private void wrapParam(UnistarParam param) {
        param.setNamespace(this.getNamespace());
        param.setName(this.getName());
        param.setGroup(this.getGroup());
        param.setHost(this.getHost());
        param.setPort(this.getPort());
        param.setToken(this.getToken());
    }

    /**
     * @return
     */
    public UnistarParam wrapUnistarParam() {
        UnistarParam unistarParam = new UnistarParam();
        wrapParam(unistarParam);
        return unistarParam;
    }

    /**
     * @param serviceId
     * @return
     */
    public UnistarDiscoverData wrapUnistarDiscoverData(String serviceId) {
        UnistarDiscoverData discoverData = new UnistarDiscoverData();
        wrapParam(discoverData);
        discoverData.setServiceId(serviceId);
        return discoverData;
    }

    /**
     * @return
     */
    public UnistarReadyParam wrapUnistarReadyParam() {
        UnistarReadyParam readyParam = new UnistarReadyParam();
        wrapParam(readyParam);
        return readyParam;
    }


}
