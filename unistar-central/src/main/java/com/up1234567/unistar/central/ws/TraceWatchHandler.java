package com.up1234567.unistar.central.ws;

import com.up1234567.unistar.central.data.us.AppNode;
import com.up1234567.unistar.central.service.stat.impl.StatTraceCacheService;
import com.up1234567.unistar.central.service.us.impl.AppService;
import com.up1234567.unistar.central.support.ws.AUnistarWebSocketHandler;
import com.up1234567.unistar.central.support.ws.IUnistarWebSocketHandler;
import com.up1234567.unistar.common.IUnistarConst;
import com.up1234567.unistar.common.UnistarParam;
import com.up1234567.unistar.common.discover.UnistarServiceNode;
import com.up1234567.unistar.common.discover.UnistarTraceWatch;
import com.up1234567.unistar.common.event.IUnistarEventConst;
import com.up1234567.unistar.common.exception.UnistarRegistraionException;
import lombok.CustomLog;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.socket.WebSocketSession;

@CustomLog(topic = IUnistarConst.DEFAULT_LOGGER_UNISTAR)
@AUnistarWebSocketHandler(value = IUnistarEventConst.HANDLE_TRACE_WATCH, pool = 1)
public class TraceWatchHandler implements IUnistarWebSocketHandler<UnistarTraceWatch> {

    @Autowired
    private AppService appService;

    @Autowired
    private StatTraceCacheService statTraceCacheService;

    @Override
    public String handle(WebSocketSession session, UnistarTraceWatch params) {
        UnistarParam attrs = (UnistarParam) session.getAttributes().get(UnistarParam.UNISTAR_PARAM);
        AppNode node = appService.findAppNode(attrs.getNamespace(), attrs.getName(), attrs.getHost(), attrs.getPort());
        if (node == null) throw new UnistarRegistraionException("获取配置的节点不存在");
        if (params.getIndex() == -1) {
            log.debug("开始监听: {}", params);
            // 创建监听标记
            statTraceCacheService.setNodeWatchTraceId(node, params.getPath(), params.getTraceId());
        } else {
            UnistarServiceNode serviceNode = new UnistarServiceNode();
            serviceNode.setService(node.getAppname());
            serviceNode.setHost(node.getHost());
            serviceNode.setPort(node.getPort());
            params.setNode(serviceNode);
            log.debug("监听通知: {}", params);
            if (statTraceCacheService.isNodeWatching(params.getTraceId())) statTraceCacheService.addNodeWatchResult(params.getTraceId(), params);
        }
        return null;
    }

}
