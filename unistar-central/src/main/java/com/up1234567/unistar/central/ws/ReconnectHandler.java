package com.up1234567.unistar.central.ws;

import com.up1234567.unistar.central.data.us.AppNode;
import com.up1234567.unistar.central.service.connect.impl.UnistarConnectCacheService;
import com.up1234567.unistar.central.service.connect.impl.UnistarConnectService;
import com.up1234567.unistar.central.service.us.impl.AppService;
import com.up1234567.unistar.central.support.core.UnistarProperties;
import com.up1234567.unistar.central.support.ws.AUnistarWebSocketHandler;
import com.up1234567.unistar.central.support.ws.IUnistarWebSocketHandler;
import com.up1234567.unistar.common.IUnistarConst;
import com.up1234567.unistar.common.UnistarParam;
import com.up1234567.unistar.common.UnistarReadyParam;
import com.up1234567.unistar.common.event.IUnistarEventConst;
import com.up1234567.unistar.common.exception.UnistarRegistraionException;
import com.up1234567.unistar.common.util.DateUtil;
import lombok.CustomLog;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.socket.WebSocketSession;

@CustomLog(topic = IUnistarConst.DEFAULT_LOGGER_UNISTAR)
@AUnistarWebSocketHandler(IUnistarEventConst.HANDLE_RECONNECT)
public class ReconnectHandler implements IUnistarWebSocketHandler<UnistarReadyParam> {

    @Autowired
    private AppService appService;

    @Autowired
    private UnistarProperties clustProperties;

    @Autowired
    private UnistarConnectCacheService unistarConnectCacheService;

    @Autowired
    private UnistarConnectService connectService;

    @Override
    public String handle(WebSocketSession session, UnistarReadyParam param) {
        UnistarParam attrs = (UnistarParam) session.getAttributes().get(UnistarParam.UNISTAR_PARAM);
        if (!attrs.getNamespace().equals(param.getNamespace()) || !attrs.getName().equals(param.getName())) {
            throw new UnistarRegistraionException("注册的服务与连接时不匹配");
        }
        attrs.setPort(param.getPort());
        AppNode node = appService.findAppNode(param.getNamespace(), param.getName(), param.getHost(), param.getPort());
        // 服务注册设置
        if (node.isServerable()) {
            node.setServiceStatus(param.getRegistraionParam().isAvailable() ? AppNode.EStatus.ON : AppNode.EStatus.OFF);
        }
        // 任务执行器相关
        if (node.isTaskable()) {
            node.setTaskStatus(param.getTaskParam().isAvailable() ? AppNode.EStatus.ON : AppNode.EStatus.OFF);
        }
        // 连接点参数
        node.setConnectId(session.getId());
        node.setConnectCenter(clustProperties.centerAddress());
        node.setLastConnectTime(DateUtil.now());
        //
        appService.initAppNode(node);
        // 加入到总的连接列表
        unistarConnectCacheService.connected(node);
        // 上下线通知
        if (node.isServerable()) connectService.serviceNodeChanged(node);
        //

        log.debug("空间 {} 的应用 {} 所属组 {} 的实例节点 {}:{} 已重新接入", param.getNamespace(), param.getName(), param.getGroup(), param.getHost(), param.getPort());
        //
        return null;
    }
}
