package com.up1234567.unistar.springcloud.registry;

import com.up1234567.unistar.common.registry.UnistarRegistraionParam;
import com.up1234567.unistar.springcloud.core.event.IUnistarClientDispatcher;
import org.springframework.cloud.client.serviceregistry.ServiceRegistry;

/**
 * 服务的注册，注销。状态设置和获取行为
 */
public class UnistarServiceRegistration implements ServiceRegistry<UnistarRegistration> {

    private IUnistarClientDispatcher unistarEventPublisher;

    public UnistarServiceRegistration(IUnistarClientDispatcher unistarEventPublisher) {
        // =====================
        this.unistarEventPublisher = unistarEventPublisher;
    }

    @Override
    public void register(UnistarRegistration registration) {
        UnistarRegistraionParam registraionParam = new UnistarRegistraionParam();
        UnistarRegistryProperties registryProperties = registration.getUnistarProperties().getRegistry();
        registraionParam.setAvailable(registryProperties.isAvailable());
        registraionParam.setWeight(registryProperties.getWeight());
        unistarEventPublisher.readyParam().setRegistraionParam(registraionParam);
    }

    @Override
    public void deregister(UnistarRegistration registration) {
        // unistar deregister not supported, please use unistar console
    }

    @Override
    public void close() {
        // unistar close not supported, please use unistar console
    }

    @Override
    public void setStatus(UnistarRegistration registration, String status) {
        throw new IllegalArgumentException("unistar setStatus not supported, please use unistar console");
    }

    @Override
    public <T> T getStatus(UnistarRegistration registration) {
        throw new IllegalArgumentException("unistar getStatus not supported, please use unistar console");
    }

}
