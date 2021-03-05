package com.up1234567.unistar.springcloud.core.event;

public interface IUnistarEventDispatcherAck {

    void callback(boolean success, String param);

}
