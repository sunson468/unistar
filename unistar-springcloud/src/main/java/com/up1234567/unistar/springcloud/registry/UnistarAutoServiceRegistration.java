package com.up1234567.unistar.springcloud.registry;

import com.up1234567.unistar.springcloud.UnistarProperties;
import com.up1234567.unistar.springcloud.core.event.IUnistarClientDispatcher;
import org.springframework.cloud.client.serviceregistry.AbstractAutoServiceRegistration;

/**
 * 包装注册器
 */
public class UnistarAutoServiceRegistration extends AbstractAutoServiceRegistration<UnistarRegistration> {

    private UnistarProperties unistarProperties;
    private UnistarRegistration unistarRegistration;

    public UnistarAutoServiceRegistration(
            UnistarProperties unistarProperties,
            IUnistarClientDispatcher unistarEventPublisher) {
        super(new UnistarServiceRegistration(unistarEventPublisher), null);
        this.unistarProperties = unistarProperties;
        this.unistarRegistration = new UnistarRegistration(unistarProperties);
    }

    @Override
    protected Object getConfiguration() {
        return null;
    }

    @Override
    protected boolean isEnabled() {
        return unistarProperties.getRegistry().isEnabled();
    }

    @Override
    protected UnistarRegistration getRegistration() {
        return unistarRegistration;
    }

    @Override
    protected UnistarRegistration getManagementRegistration() {
        return null;
    }
}
