package com.up1234567.unistar.central.support.core.clust;

import com.up1234567.unistar.central.support.core.timer.UnistarTimer;
import com.up1234567.unistar.central.support.core.UnistarNode;
import com.up1234567.unistar.central.support.core.UnistarProperties;
import com.up1234567.unistar.central.support.core.task.IUnistarTaskRunner;
import com.up1234567.unistar.common.IUnistarConst;
import com.up1234567.unistar.common.async.PoolExecutorService;
import com.up1234567.unistar.common.util.JsonUtil;
import lombok.CustomLog;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Unistar分布式服务程序
 */
@CustomLog(topic = IUnistarConst.DEFAULT_LOGGER_UNISTAR)
public class UnistarClustNodeServer implements IUnistarCluster, ApplicationListener<ApplicationReadyEvent>, DisposableBean {

    private UnistarProperties clustProperties;
    private UnistarNode currentNode;
    private IUnistarClustRunner unistarClustRunner;
    private ConcurrentMap<Integer, IUnistarClustListener> handles = new ConcurrentHashMap<>();

    private UnistarTimer unistarTimer; // 定时器
    // 收发处理器
    private PoolExecutorService executor = new PoolExecutorService();

    public UnistarClustNodeServer(UnistarProperties clustProperties, IUnistarTaskRunner unistarTaskRunner, IUnistarClustRunner unistarClustRunner) {
        this.clustProperties = clustProperties;
        this.unistarClustRunner = unistarClustRunner;
        // 当前节点
        this.currentNode = new UnistarNode(clustProperties.getHost(), clustProperties.getPort());
        // 启动定时器
        unistarTimer = new UnistarTimer(unistarTaskRunner, unistarClustRunner);
    }

    /**
     * @param type
     * @param body
     * @return
     */
    private UnistarClustMsg wrapMsg(int type, Object body) {
        UnistarClustMsg msg = new UnistarClustMsg();
        msg.setFrom(currentNode);
        msg.setType(type);
        if (body != null) {
            if (body instanceof String) {
                msg.setBody(String.valueOf(body));
            } else {
                msg.setBody(JsonUtil.toJsonString(body));
            }
        }
        return msg;
    }

    @Override
    public void send(UnistarNode node, int type, Object body) {
        // 一条条发送
        executor.execute(() -> {
            UnistarClustMsg msg = wrapMsg(type, body);
            msg.setTo(node);
            unistarClustRunner.publish(msg);
        });
    }

    @Override
    public void multicast(int type, Object body) {
        send(null, type, body);
    }

    @Override
    public void addListener(IUnistarClustListener handler, Integer... types) {
        if (types == null || types.length == 0) return;
        for (Integer type : types) handles.put(type, handler);
    }

    @Override
    public void handleMessage(String message) {
        try {
            UnistarClustMsg msg = JsonUtil.toClass(message, UnistarClustMsg.class);
            if (msg == null) return;
            // 消息处理条件，没有固定目标，或者固定目标是自己
            if (msg.getTo() == null || msg.getTo().equals(currentNode)) {
                IUnistarClustListener handler = handles.get(msg.getType());
                if (handler != null) handler.handle(msg);
            }
        } catch (Exception e) {
            log.error("unistar clust subscribe error", e);
        }
    }

    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        unistarTimer.start(this, currentNode, clustProperties.isClust());
    }

    @Override
    public void destroy() throws Exception {
        unistarTimer.destroy();
    }
}
