package com.up1234567.unistar.springcloud.core.event;

import com.up1234567.unistar.common.event.UnistarEventData;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class UnistarEventCache {
    private UnistarEventData event;
    private IUnistarEventDispatcherAck ack;
}
