package com.up1234567.unistar.central.ws;

import com.up1234567.unistar.central.data.us.AppNode;
import com.up1234567.unistar.central.service.connect.impl.UnistarConnectCacheService;
import com.up1234567.unistar.central.service.connect.impl.UnistarConnectService;
import com.up1234567.unistar.central.service.us.impl.AppCacheService;
import com.up1234567.unistar.central.service.us.impl.AppService;
import com.up1234567.unistar.central.support.ws.AUnistarWebSocketHandler;
import com.up1234567.unistar.central.support.ws.IUnistarWebSocketHandler;
import com.up1234567.unistar.common.IUnistarConst;
import com.up1234567.unistar.common.UnistarParam;
import com.up1234567.unistar.common.UnistarReadyOutParam;
import com.up1234567.unistar.common.UnistarReadyParam;
import com.up1234567.unistar.common.event.IUnistarEventConst;
import com.up1234567.unistar.common.exception.UnistarRegistraionException;
import com.up1234567.unistar.common.limit.UnistarAppLimit;
import com.up1234567.unistar.common.util.JsonUtil;
import com.up1234567.unistar.common.util.StringUtil;
import lombok.CustomLog;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.socket.WebSocketSession;

import java.util.List;
import java.util.stream.Collectors;

@CustomLog(topic = IUnistarConst.DEFAULT_LOGGER_UNISTAR)
@AUnistarWebSocketHandler(IUnistarEventConst.HANDLE_READY)
public class ReadyHandler implements IUnistarWebSocketHandler<UnistarReadyParam> {

    @Autowired
    private AppService appService;

    @Autowired
    private AppCacheService appCacheService;

    @Autowired
    private UnistarConnectCacheService unistarConnectCacheService;

    @Autowired
    private UnistarConnectService connectService;

    @Override
    public String handle(WebSocketSession session, UnistarReadyParam param) {
        UnistarParam attrs = (UnistarParam) session.getAttributes().get(UnistarParam.UNISTAR_PARAM);
        if (!attrs.getNamespace().equals(param.getNamespace()) || !attrs.getName().equals(param.getName())) {
            throw new UnistarRegistraionException("????????????????????????????????????");
        }
        attrs.setPort(param.getPort());
        // ?????????????????????????????????????????????
        AppNode node = appService.findAppNodeWithCreate(param.getNamespace(), param.getName(), param.getHost(), param.getPort());
        // ??????
        node.setGroup(param.getGroup());
        node.setProfiles(StringUtil.toCommaString(param.getProfiles()));
        appService.syncNodeReadyParam(node, param);
        // ???????????????
        node.setConnectId(session.getId());
        //
        appService.initAppNode(node);
        // ???????????????????????????
        unistarConnectCacheService.connected(node);
        //
        if (node.isServerable()) {
            connectService.serviceNodeChanged(node);
        }
        // ????????????
        if (CollectionUtils.isNotEmpty(param.getLoggerParam())) appCacheService.saveLoggerData(param.getNamespace(), param.getName(), param.getLoggerParam());
        // ??????????????????
        List<UnistarAppLimit> appLimits = appService.listAppLimit(node.getNamespace(), node.getAppname(), false)
                .stream()
                .peek(l -> {
                    l.setWhiteGroupList(StringUtil.fromCommaString(l.getWhiteGroups()));
                    l.setWhiteServiceList(StringUtil.fromCommaString(l.getWhiteServices()));
                })
                .filter(l -> {
                    // ?????????????????????,??????
                    boolean inwhite = CollectionUtils.isNotEmpty(l.getWhiteGroupList()) && l.getWhiteGroupList().contains(node.getGroup());
                    // ???????????????????????????
                    if (!inwhite) inwhite = CollectionUtils.isNotEmpty(l.getWhiteServiceList()) && l.getWhiteServices().contains(node.getAppname());
                    // ??????????????????
                    return !inwhite;
                })
                .collect(Collectors.toList());
        // ??????
        try {
            UnistarReadyOutParam readyOutParam = new UnistarReadyOutParam();
            readyOutParam.setAppLimits(appLimits);
            return JsonUtil.toJsonString(readyOutParam);
        } finally {
            log.debug("?????? {} ????????? {} ????????? {} ??????????????? {}:{} ?????????", param.getNamespace(), param.getName(), param.getGroup(), param.getHost(), param.getPort());
        }
    }

}
