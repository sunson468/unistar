package com.up1234567.unistar.springcloud.core;

import com.up1234567.unistar.common.IUnistarConst;
import com.up1234567.unistar.common.UnistarReadyOutParam;
import com.up1234567.unistar.common.UnistarReadyParam;
import com.up1234567.unistar.common.async.PoolExecutorService;
import com.up1234567.unistar.common.ds.AtomicCounter;
import com.up1234567.unistar.common.ds.RoundCounter;
import com.up1234567.unistar.common.event.IUnistarEventConst;
import com.up1234567.unistar.common.event.UnistarEventData;
import com.up1234567.unistar.common.exception.UnistarNotSupportException;
import com.up1234567.unistar.common.exception.UnistarRemoteException;
import com.up1234567.unistar.common.heartbeat.UnistarHeartbeatData;
import com.up1234567.unistar.common.logger.IUnistarLogger;
import com.up1234567.unistar.common.logger.UnistarLoggerFactory;
import com.up1234567.unistar.common.util.DateUtil;
import com.up1234567.unistar.common.util.JsonUtil;
import com.up1234567.unistar.common.util.StringUtil;
import com.up1234567.unistar.common.util.ThreadUtil;
import com.up1234567.unistar.springcloud.UnistarProperties;
import com.up1234567.unistar.springcloud.core.event.IUnistarClientDispatcher;
import com.up1234567.unistar.springcloud.core.event.IUnistarEventDispatcherAck;
import com.up1234567.unistar.springcloud.core.event.UnistarEventCache;
import com.up1234567.unistar.springcloud.core.event.UnistarEventListener;
import lombok.Getter;
import org.eclipse.jetty.util.BlockingArrayQueue;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.boot.web.context.WebServerInitializedEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

public class UnistarClientManager implements IUnistarClientDispatcher, DisposableBean, ApplicationListener<WebServerInitializedEvent>, IUnistarClientListener {

    private static final IUnistarLogger logger = UnistarLoggerFactory.getLogger(IUnistarConst.DEFAULT_LOGGER_UNISTAR);

    private final static String TIMER_NAME = "Unistar-Heartbeat-Timer";

    private UnistarReadyParam readyParam;
    private UnistarProperties unistarProperties;
    private String centralWs;
    // 标记是否已经启动
    private AtomicBoolean inited = new AtomicBoolean(false);
    // 标记是否关闭了
    private AtomicBoolean closed = new AtomicBoolean(false);
    // 标记是否重连中，链接成功后会自动重置
    private AtomicBoolean reconnecting = new AtomicBoolean(false);
    private int reconectTimes;

    // 客户端连接，断开后会自动重建
    @Getter
    private volatile UnistarClient client;

    // 监听Unstar中心事件
    private Map<String, UnistarEventListener> eventListeners = new HashMap<>();
    // 客户端内部监听器
    private List<IUnistarClientListener> clientListeners = new ArrayList<>();
    // 客户端定时器，用于同步和发送信息
    private Timer timer = new Timer(TIMER_NAME);

    // 心跳同步控制
    // 6时钟(6*10秒)执行一次心跳通讯
    private AtomicCounter counter = AtomicCounter.newCounter(6);

    // 消息处理线程，单线程
    // 发送消息
    private final PoolExecutorService sendExecutor = new PoolExecutorService();
    private BlockingArrayQueue<UnistarEventCache> eventCaches = new BlockingArrayQueue<>();
    // 接受消息
    private final PoolExecutorService receiveExecutor = new PoolExecutorService();
    // 事件回调控制，通过EventId来进行控制
    private final RoundCounter eventId = new RoundCounter();
    private ConcurrentHashMap<Long, IUnistarEventDispatcherAck> eventCallbacks = new ConcurrentHashMap<>();


    /**
     * @param unistarProperties
     */
    public UnistarClientManager(UnistarProperties unistarProperties) {
        this.unistarProperties = unistarProperties;
        //
        this.readyParam = unistarProperties.wrapUnistarReadyParam();
        //
        addClientListener(this);
        //
        String server = unistarProperties.getServer();
        this.centralWs = "ws://" + server + "/ws" + unistarProperties.wrapUnistarParam().toQueryParam();
        // 连接远程
        this.clientConnect();
    }

    /**
     * 客户端连接
     */
    private void clientConnect() {
        client = new UnistarClient(this, centralWs);
        // 链接已经断开，
        ThreadUtil.loopUtilByTimes(() -> {
            if (getClient() != null && getClient().isOpen()) {
                // 是否重连
                if (reconnecting.compareAndSet(true, false)) {
                    logger.warn("unistar central server：{} reconnected", unistarProperties.getServer());
                    // 重连成功，重置参数
                    reconectTimes = 0;
                    // 将未发出去的消息发送出去
                    sendExecutor.execute(() -> eventCaches.forEach(cache -> dispatchEvent(cache.getEvent(), cache.getAck())));
                    // =========================
                    // 通知内部重连成功
                    clientListeners.parallelStream().forEach(IUnistarClientListener::reconnected);
                } else {
                    logger.debug("unistar central server：{} connected", unistarProperties.getServer());
                }
                return true;
            } else {
                return false;
            }
        }, 3, () -> {
            // 连接失败处理
            // 如果在重连中
            if (reconnecting.get()) {
                logger.warn("recconnect to unistar central server, try {} times", ++reconectTimes);
                // 重连时间越来越长，最长5分钟
                ThreadUtil.sleep(Math.min(reconectTimes * DateUtil.SECOND, DateUtil.MINUTE_5));
                // 重新连接远程
                clientConnect();
            } else {
                throw new UnistarRemoteException("unistar central server: {} cann't be connected", unistarProperties.getServer());
            }
        });
    }

    /**
     * 接收到来自Unistar中心的消息
     *
     * @param message
     */
    public void onMessage(String message) {
        receiveExecutor.execute(() -> {
            UnistarEventData event = JsonUtil.toClass(message, UnistarEventData.class);
            if (event == null) return;
            if (event.getId() > 0) {
                IUnistarEventDispatcherAck ack = eventCallbacks.remove(event.getId());
                if (ack != null) {
                    try {
                        ack.callback(true, event.getParams());
                    } catch (Exception e) {
                        ack.callback(false, StringUtil.relace("event {}'s callback has error: {}", event.getAction(), e.getMessage()));
                    }
                }
            } else {
                UnistarEventListener listener = eventListeners.get(event.getAction());
                if (listener != null) listener.invoke(event.getParams());
            }
        });
    }

    /**
     * 与中心连接断开
     */
    public void onClosed() {
        // 进入重连状态，让轮询机制进入查询阶段
        if (reconnecting.compareAndSet(false, true)) {
            logger.warn("disconnect from unistar center");
            // 将客户端先置为空
            client = null;
            // 开启重连
            clientConnect();
        }
    }

    @Override
    public void onApplicationEvent(WebServerInitializedEvent event) {
        if (inited.compareAndSet(false, true)) {
            readyParam.setHost(unistarProperties.getHost());
            readyParam.setPort(event.getWebServer().getPort());
            if (!CollectionUtils.isEmpty(UnistarLoggerFactory.getLOGGERS())) readyParam.setLoggerParam(new ArrayList<>(UnistarLoggerFactory.getLOGGERS().keySet()));
            // ==============================================
            publish(IUnistarEventConst.HANDLE_READY, readyParam, (success, s) -> {
                UnistarReadyOutParam readyOutParam = JsonUtil.toClass(s, UnistarReadyOutParam.class);
                // =========================
                // 服务准备好了
                if (readyOutParam != null) clientListeners.parallelStream().forEach(l -> l.ready(readyOutParam));
                // 启动定时器
                startTimer();
            });
        }
    }

    /**
     * 开启定时器
     */
    private void startTimer() {
        // 单线程同步执行器
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                try {
                    if (closed.get()) return; // 关闭中
                    if (reconnecting.get()) return; // 重连中
                    // 正常心跳
                    if (counter.isArrived()) {
                        UnistarHeartbeatData heartbeatData = new UnistarHeartbeatData();
                        clientListeners.forEach(b -> b.heartbeat(heartbeatData));
                        publish(IUnistarEventConst.HANDLE_HEARTBEAT, heartbeatData);
                    }
                } catch (Exception e) {
                    logger.error("unistar client heartbeat error, {}", e.getMessage());
                }
            }
        }, DateUtil.SECOND_10, DateUtil.SECOND_10);
    }

    @Override
    public void reconnected() {
        readyParam.setLoggerParam(null);
        publish(IUnistarEventConst.HANDLE_RECONNECT, readyParam);
    }

    @Override
    public UnistarReadyParam readyParam() {
        return readyParam;
    }

    @Override
    public void addClientListener(IUnistarClientListener clientListener) {
        clientListeners.add(clientListener);
        logger.debug("listener of unistar client by class [{}]", clientListener.getClass().getSimpleName());
    }

    /**
     * 发布信息
     *
     * @param event
     * @param ack
     */
    private void dispatchEvent(UnistarEventData event, IUnistarEventDispatcherAck ack) {
        // 客户端未连接成功
        // 缓存起来，重连后重新发送
        if (reconnecting.get() || getClient() == null || !getClient().isOpen()) {
            eventCaches.offer(new UnistarEventCache(event, ack));
            this.onClosed();
            return;
        }
        // 加入到请求队列
        sendExecutor.execute(() -> {
            if (ack != null) {
                event.setId(eventId.get());
                eventCallbacks.put(event.getId(), ack);
            }
            try {
                getClient().sendMessage(JsonUtil.toJsonString(event));
            } catch (Exception e) {
                throw new UnistarRemoteException("cann't send message to unistar central server");
            }
        });
    }

    /**
     * @param handler
     */
    @Override
    public void addEventListener(Object handler) {
        UnistarEventListener listener = UnistarEventListener.wrap(handler);
        if (eventListeners.containsKey(listener.getEvent())) {
            throw new UnistarNotSupportException("unistar event {} must be listen by only one handler", listener.getEvent());
        }
        eventListeners.put(listener.getEvent(), listener);
        logger.debug("lisenten event [{}] from unistar central server by class [{}.{}]", listener.getEvent(), listener.getBean().getClass().getSimpleName(), listener.getMethod().getName());
    }

    // ====================================================================

    @Override
    public void publish(String action) {
        this.publish(action, null, null);
    }

    @Override
    public <T> void publish(String action, T data) {
        this.publish(action, data, null);
    }

    @Override
    public <T> void publish(String action, T data, IUnistarEventDispatcherAck ack) {
        UnistarEventData event = new UnistarEventData();
        event.setAction(action);
        event.setParams(JsonUtil.toJsonString(data));
        dispatchEvent(event, ack);
    }

    @Override
    public void destroy() {
        if (closed.compareAndSet(false, true)) {
            // ===========================
            if (timer != null) {
                timer.cancel();
                timer = null;
            }
            if (client != null) {
                client.close();
                client = null;
            }
        }
    }

}
