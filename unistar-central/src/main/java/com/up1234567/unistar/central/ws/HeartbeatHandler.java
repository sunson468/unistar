package com.up1234567.unistar.central.ws;

import com.up1234567.unistar.central.data.us.AppNode;
import com.up1234567.unistar.central.service.connect.impl.UnistarConnectCacheService;
import com.up1234567.unistar.central.service.stat.impl.StatTraceService;
import com.up1234567.unistar.central.service.us.impl.AppService;
import com.up1234567.unistar.central.support.ws.AUnistarWebSocketHandler;
import com.up1234567.unistar.central.support.ws.IUnistarWebSocketHandler;
import com.up1234567.unistar.common.IUnistarConst;
import com.up1234567.unistar.common.UnistarParam;
import com.up1234567.unistar.common.event.IUnistarEventConst;
import com.up1234567.unistar.common.exception.UnistarRegistraionException;
import com.up1234567.unistar.common.heartbeat.UnistarHeartbeatData;
import lombok.CustomLog;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.socket.WebSocketSession;

@CustomLog(topic = IUnistarConst.DEFAULT_LOGGER_UNISTAR)
@AUnistarWebSocketHandler(IUnistarEventConst.HANDLE_HEARTBEAT)
public class HeartbeatHandler implements IUnistarWebSocketHandler<UnistarHeartbeatData> {

    @Autowired
    private AppService appService;

    @Autowired
    private UnistarConnectCacheService unistarConnectCacheService;

    @Autowired
    private StatTraceService statTraceService;

    @Override
    public String handle(WebSocketSession session, UnistarHeartbeatData params) {
        UnistarParam attrs = (UnistarParam) session.getAttributes().get(UnistarParam.UNISTAR_PARAM);
        AppNode node = appService.findAppNode(attrs.getNamespace(), attrs.getName(), attrs.getHost(), attrs.getPort());
        if (node == null) throw new UnistarRegistraionException("发现服务的节点不存在");
        // 心跳维持
        unistarConnectCacheService.hearbeat(node);
        // 记录心跳数据
        statTraceService.statTraceHeartbeat(node, params.getTraces());
        log.debug("unistar service node's heartbeat: {}", params);
        return null;
    }

}
