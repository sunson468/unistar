package com.up1234567.unistar.springcloud.discover.event;

import com.up1234567.unistar.common.discover.UnistarServiceNode;
import com.up1234567.unistar.common.event.IUnistarEventConst;
import com.up1234567.unistar.springcloud.core.event.AUnistarEventListener;
import com.up1234567.unistar.springcloud.discover.UnistarDiscoverClient;

@AUnistarEventListener(IUnistarEventConst.EVENT_DISCOVER_INSTANCE_CHANGED)
public class UnistarServiceChangedEventListener {

    private UnistarDiscoverClient unistarDiscoverClient;

    public UnistarServiceChangedEventListener(UnistarDiscoverClient unistarDiscoverClient) {
        this.unistarDiscoverClient = unistarDiscoverClient;
    }

    public void handle(UnistarServiceNode node) {
        unistarDiscoverClient.sync(node);
    }

}
