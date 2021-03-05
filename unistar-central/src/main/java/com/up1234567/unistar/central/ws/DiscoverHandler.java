package com.up1234567.unistar.central.ws;

import com.up1234567.unistar.central.data.us.AppNode;
import com.up1234567.unistar.central.service.connect.impl.UnistarConnectCacheService;
import com.up1234567.unistar.central.service.us.impl.AppService;
import com.up1234567.unistar.central.support.ws.AUnistarWebSocketHandler;
import com.up1234567.unistar.central.support.ws.IUnistarWebSocketHandler;
import com.up1234567.unistar.common.IUnistarConst;
import com.up1234567.unistar.common.UnistarParam;
import com.up1234567.unistar.common.discover.UnistarDiscoverData;
import com.up1234567.unistar.common.discover.UnistarDiscoverOutParam;
import com.up1234567.unistar.common.discover.UnistarServiceNode;
import com.up1234567.unistar.common.event.IUnistarEventConst;
import com.up1234567.unistar.common.exception.UnistarRegistraionException;
import com.up1234567.unistar.common.limit.UnistarAppLimit;
import com.up1234567.unistar.common.util.JsonUtil;
import com.up1234567.unistar.common.util.StringUtil;
import lombok.CustomLog;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.socket.WebSocketSession;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@CustomLog(topic = IUnistarConst.DEFAULT_LOGGER_UNISTAR)
@AUnistarWebSocketHandler(IUnistarEventConst.HANDLE_DISCOVER)
public class DiscoverHandler implements IUnistarWebSocketHandler<UnistarDiscoverData> {

    @Autowired
    private AppService appService;

    @Autowired
    private UnistarConnectCacheService unistarConnectCacheService;

    @Override
    public String handle(WebSocketSession session, UnistarDiscoverData param) {
        UnistarParam attrs = (UnistarParam) session.getAttributes().get(UnistarParam.UNISTAR_PARAM);
        if (!attrs.getNamespace().equals(param.getNamespace()) || !attrs.getName().equals(param.getName())) {
            throw new UnistarRegistraionException("服务名称与连接时不匹配");
        }
        AppNode node = appService.findAppNode(attrs.getNamespace(), attrs.getName(), attrs.getHost(), attrs.getPort());
        if (node == null) {
            throw new UnistarRegistraionException("发现服务的节点不存在");
        }
        // =================================================
        node.setDiscoverable(true);
        node.addDiscovers(param.getServiceId());
        node.setDiscoverStatus(AppNode.EStatus.ON);
        appService.nodeAsDiscover(node);
        unistarConnectCacheService.discoverable(node);
        // =================================================
        List<UnistarServiceNode> serviceNodes = new ArrayList<>();
        //
        if (StringUtils.isNotEmpty(param.getServiceId())) {
            // 通知当前的非关闭服务节点
            List<AppNode> availableServiceNodes = unistarConnectCacheService.allOnlineServer(param.getNamespace(), param.getServiceId());
            availableServiceNodes.forEach(o -> serviceNodes.add(o.toServiceNode()));
        }
        // 获取应用限制
        List<UnistarAppLimit> appLimits = appService.listAppLimit(node.getNamespace(), param.getServiceId(), true)
                .stream()
                .peek(l -> {
                    l.setWhiteGroupList(StringUtil.fromCommaString(l.getWhiteGroups()));
                    l.setWhiteServiceList(StringUtil.fromCommaString(l.getWhiteServices()));
                })
                .filter(l -> {
                    // 在白名单分组内,忽略
                    boolean inwhite = CollectionUtils.isNotEmpty(l.getWhiteGroupList()) && l.getWhiteGroupList().contains(node.getGroup());
                    // 且不在白名单服务内
                    if (!inwhite) inwhite = CollectionUtils.isNotEmpty(l.getWhiteServiceList()) && l.getWhiteServices().contains(node.getAppname());
                    // 返回是否包含
                    return !inwhite;
                })
                .collect(Collectors.toList());
        //
        try {
            UnistarDiscoverOutParam outParam = new UnistarDiscoverOutParam();
            outParam.setServiceNodes(serviceNodes);
            outParam.setAppLimits(appLimits);
            return JsonUtil.toJsonString(outParam);
        } finally {
            log.debug("find unistar {} service nodes of {}", serviceNodes.size(), param.getServiceId());
        }
    }

}
