package com.up1234567.unistar.springcloud.discover.loadbalancer;

import org.springframework.cloud.client.ServiceInstance;

import java.util.List;

public interface IUnistarLoadBalancer {

    ServiceInstance choose(List<ServiceInstance> instances);

}
