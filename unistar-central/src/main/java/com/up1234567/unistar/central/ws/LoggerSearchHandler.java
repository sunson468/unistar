package com.up1234567.unistar.central.ws;

import com.up1234567.unistar.central.data.us.AppNode;
import com.up1234567.unistar.central.service.us.impl.AppCacheService;
import com.up1234567.unistar.central.service.us.impl.AppService;
import com.up1234567.unistar.central.support.ws.AUnistarWebSocketHandler;
import com.up1234567.unistar.central.support.ws.IUnistarWebSocketHandler;
import com.up1234567.unistar.common.IUnistarConst;
import com.up1234567.unistar.common.UnistarParam;
import com.up1234567.unistar.common.event.IUnistarEventConst;
import com.up1234567.unistar.common.exception.UnistarRegistraionException;
import com.up1234567.unistar.common.logger.UnistarLoggerSearchParam;
import lombok.CustomLog;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.socket.WebSocketSession;

@CustomLog(topic = IUnistarConst.DEFAULT_LOGGER_UNISTAR)
@AUnistarWebSocketHandler(value = IUnistarEventConst.HANDLE_LOG_SEARCH, pool = 1)
public class LoggerSearchHandler implements IUnistarWebSocketHandler<UnistarLoggerSearchParam> {

    @Autowired
    private AppService appService;

    @Autowired
    private AppCacheService appCacheService;

    @Override
    public String handle(WebSocketSession session, UnistarLoggerSearchParam params) {
        UnistarParam attrs = (UnistarParam) session.getAttributes().get(UnistarParam.UNISTAR_PARAM);
        AppNode node = appService.findAppNode(attrs.getNamespace(), attrs.getName(), attrs.getHost(), attrs.getPort());
        if (node == null) throw new UnistarRegistraionException("获取配置的节点不存在");
        if (CollectionUtils.isNotEmpty(params.getResults())) {
            params.setName(attrs.getName());
            params.setHost(attrs.getHost());
            params.setPort(attrs.getPort());
            appCacheService.addLoggerSearchResult(node.getNamespace(), params.getSearchId(), params);
        }
        return null;
    }

}
