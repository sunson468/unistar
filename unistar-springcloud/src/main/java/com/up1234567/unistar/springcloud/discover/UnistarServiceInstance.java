package com.up1234567.unistar.springcloud.discover;

import com.up1234567.unistar.common.discover.UnistarServiceNode;
import lombok.Getter;
import lombok.Setter;
import org.springframework.cloud.client.DefaultServiceInstance;
import org.springframework.cloud.client.ServiceInstance;

import java.net.URI;
import java.util.Map;

/**
 * 服务实例
 */
@Setter
public class UnistarServiceInstance implements ServiceInstance {

    private String serviceId;

    private String host;

    private int port;

    private boolean secure;

    private Map<String, String> metadata;

    @Getter
    private int weight = 1;

    @Getter
    private boolean available;

    @Override
    public String getServiceId() {
        return serviceId;
    }

    @Override
    public String getHost() {
        return host;
    }

    @Override
    public int getPort() {
        return port;
    }

    @Override
    public boolean isSecure() {
        return secure;
    }

    @Override
    public Map<String, String> getMetadata() {
        return metadata;
    }

    @Override
    public URI getUri() {
        return DefaultServiceInstance.getUri(this);
    }

    /**
     * 是同一个节点
     *
     * @param node
     * @return
     */
    public boolean isSameFrom(UnistarServiceNode node) {
        return serviceId.equals(node.getService()) && host.equals(node.getHost()) && port == node.getPort();
    }

}
