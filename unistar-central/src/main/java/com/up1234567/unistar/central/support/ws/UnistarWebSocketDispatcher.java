package com.up1234567.unistar.central.support.ws;

import com.up1234567.unistar.central.support.util.WsSendUtil;
import com.up1234567.unistar.common.IUnistarConst;
import com.up1234567.unistar.common.async.PoolExecutorService;
import com.up1234567.unistar.common.event.UnistarEventData;
import com.up1234567.unistar.common.exception.UnistarInvalidConfigException;
import com.up1234567.unistar.common.exception.UnistarMethodNotFoundException;
import com.up1234567.unistar.common.util.JsonUtil;
import com.up1234567.unistar.common.util.ReflectUtil;
import lombok.CustomLog;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

@CustomLog(topic = IUnistarConst.DEFAULT_LOGGER_UNISTAR)
@Component
public class UnistarWebSocketDispatcher extends TextWebSocketHandler {

    private final AtomicBoolean inited = new AtomicBoolean(false);
    private final Map<String, IUnistarWebSocketHandler<?>> handlers = new HashMap<>();
    private final Map<String, Class> handlerPramas = new HashMap<>();
    private final Map<String, PoolExecutorService> asyncExecutors = new HashMap<>();

    @Autowired
    private ApplicationContext context;

    @Autowired
    private IUnistarConnectorManager unistarConnectorManager;

    @PostConstruct
    public void init() {
        if (inited.compareAndSet(false, true)) {
            Map<String, Object> beans = context.getBeansWithAnnotation(AUnistarWebSocketHandler.class);
            beans.forEach((bname, handler) -> {
                Class clazz = handler.getClass();
                if (IUnistarWebSocketHandler.class.isAssignableFrom(clazz)) {
                    AUnistarWebSocketHandler anno = (AUnistarWebSocketHandler) clazz.getAnnotation(AUnistarWebSocketHandler.class);
                    if (handlers.containsKey(anno.value())) {
                        throw new UnistarInvalidConfigException(anno.value() + " event can have only one handler, but now have " + handlers.get(anno.value()).getClass().getSimpleName() + " and " + clazz.getSimpleName() + " two handlers");
                    }
                    handlers.put(anno.value(), (IUnistarWebSocketHandler) handler);
                    handlerPramas.put(anno.value(), ReflectUtil.findClassInterfaceParamType(clazz, IUnistarWebSocketHandler.class));
                    asyncExecutors.put(anno.value(), new PoolExecutorService(anno.pool()));
                }
            });
        }
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        unistarConnectorManager.connect(session);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        unistarConnectorManager.disconnect(session);
    }

    @Override
    public void handleTextMessage(WebSocketSession session, TextMessage message) {
        try {
            UnistarEventData event = JsonUtil.toClass(message.getPayload(), UnistarEventData.class);
            if (event != null && StringUtils.isNotEmpty(event.getAction())) asyncExecutors.get(event.getAction()).execute(() -> doEventServer(session, event));
        } catch (Exception e) {
            log.error("消息处理发生异常", e);
        }
    }

    /**
     * @param session
     * @param event
     */
    private void doEventServer(WebSocketSession session, UnistarEventData event) {
        IUnistarWebSocketHandler handler = handlers.get(event.getAction());
        if (handler == null) {
            throw new UnistarMethodNotFoundException("event [" + event.getAction() + "] hasn't it's hanlder");
        }
        Class paramClazz = handlerPramas.get(event.getAction());
        Object ret;
        if (paramClazz != null && !paramClazz.getClass().equals(Object.class)) {
            ret = handler.handle(session, JsonUtil.toClass(event.getParams(), paramClazz));
        } else {
            ret = handler.handle(session, event.getParams());
        }
        // ===============================================
        WsSendUtil.reback(session, event.getId(), event.getAction(), ret);
    }

}
