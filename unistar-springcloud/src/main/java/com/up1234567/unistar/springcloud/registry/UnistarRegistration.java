package com.up1234567.unistar.springcloud.registry;

import com.up1234567.unistar.springcloud.UnistarProperties;
import lombok.Getter;
import org.springframework.cloud.client.DefaultServiceInstance;
import org.springframework.cloud.client.serviceregistry.Registration;

import java.net.URI;
import java.util.Map;

/**
 * 定义服务属性
 */
public class UnistarRegistration implements Registration {

    @Getter
    private UnistarProperties unistarProperties;

    public UnistarRegistration(UnistarProperties unistarProperties) {
        this.unistarProperties = unistarProperties;
    }

    @Override
    public String getServiceId() {
        return unistarProperties.getName();
    }

    @Override
    public String getHost() {
        return this.unistarProperties.getHost();
    }

    @Override
    public int getPort() {
        return this.unistarProperties.getPort();
    }

    @Override
    public boolean isSecure() {
        return false;
    }

    @Override
    public URI getUri() {
        return DefaultServiceInstance.getUri(this);
    }

    @Override
    public Map<String, String> getMetadata() {
        return null;
    }
}
