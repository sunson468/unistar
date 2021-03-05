package com.up1234567.unistar.central.support.core.clust;

import com.up1234567.unistar.central.support.core.UnistarNode;

public interface IUnistarClustRunner {


    /**
     * 发布clust消息
     *
     * @param msg
     */
    void publish(UnistarClustMsg msg);

    /**
     * 投票新的Master
     *
     * @param now
     * @return
     */
    UnistarNode voteMaster(long now);


}
