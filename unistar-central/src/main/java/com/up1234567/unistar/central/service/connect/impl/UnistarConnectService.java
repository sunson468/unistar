package com.up1234567.unistar.central.service.connect.impl;

import com.up1234567.unistar.central.data.cent.ScheduleTask;
import com.up1234567.unistar.central.data.us.AppLimit;
import com.up1234567.unistar.central.data.us.AppNode;
import com.up1234567.unistar.central.service.base.impl.BaseService;
import com.up1234567.unistar.central.service.connect.UnistarNamespaceRoom;
import com.up1234567.unistar.central.service.us.impl.AppService;
import com.up1234567.unistar.central.support.core.UnistarNode;
import com.up1234567.unistar.central.support.core.UnistarProperties;
import com.up1234567.unistar.central.support.core.clust.IUnistarClustListener;
import com.up1234567.unistar.central.support.core.clust.IUnistarClustMsg;
import com.up1234567.unistar.central.support.core.clust.IUnistarCluster;
import com.up1234567.unistar.central.support.core.clust.UnistarClustMsg;
import com.up1234567.unistar.central.support.core.clust.msg.*;
import com.up1234567.unistar.central.support.ws.IUnistarConnectorManager;
import com.up1234567.unistar.common.IUnistarConst;
import com.up1234567.unistar.common.UnistarParam;
import com.up1234567.unistar.common.event.IUnistarEventConst;
import com.up1234567.unistar.common.exception.UnistarRemoteException;
import com.up1234567.unistar.common.logger.UnistarLoggerSearchParam;
import com.up1234567.unistar.common.task.UnistarTaskData;
import com.up1234567.unistar.common.util.JsonUtil;
import com.up1234567.unistar.common.util.StringUtil;
import lombok.CustomLog;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.WebSocketSession;

import javax.annotation.PostConstruct;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collectors;

@CustomLog(topic = IUnistarConst.DEFAULT_LOGGER_UNISTAR)
@Service
public class UnistarConnectService implements IUnistarConnectorManager, IUnistarClustListener {

    @Autowired
    private BaseService baseService;

    @Autowired
    private AppService appService;

    @Autowired
    private UnistarConnectCacheService unistarConnectCacheService;

    @Autowired
    private UnistarProperties clustProperties;

    @Autowired
    private IUnistarCluster unistarCluster;

    /* ????????????????????????????????????????????????????????? */
    private ConcurrentMap<String, UnistarNamespaceRoom> connectors;

    @PostConstruct
    public void init() {
        // ????????????
        unistarCluster.addListener(this
                , IUnistarClustMsg.TYPE_CONFIG_CHANGED
                , IUnistarClustMsg.TYPE_TASKER
                , IUnistarClustMsg.TYPE_SERVICE_CHANGED
                , IUnistarClustMsg.TYPE_WATCH
                , IUnistarClustMsg.TYPE_LIMIT_CHANGED
                , IUnistarClustMsg.TYPE_LOGGER_CHANGED
                , IUnistarClustMsg.TYPE_LOGGER_SEARCH
        );
        // ???????????????
        connectors = new ConcurrentHashMap<>();
        reload();
    }

    public synchronized void reload() {
        baseService.listNamespace().forEach(o -> connectors.put(o.getNamespace(), new UnistarNamespaceRoom()));
    }

    @Override
    public void handle(UnistarClustMsg msg) {
        switch (msg.getType()) {
            case IUnistarClustMsg.TYPE_CONFIG_CHANGED:
                ConfigMsg configMsg = JsonUtil.toClass(msg.getBody(), ConfigMsg.class);
                if (configMsg == null || !checkNamespace(configMsg)) return;
                doConfigChanged(configMsg);
                break;
            case IUnistarClustMsg.TYPE_TASKER:
                TaskerMsg taskerMsg = JsonUtil.toClass(msg.getBody(), TaskerMsg.class);
                if (taskerMsg == null || !checkNamespace(taskerMsg)) return;
                doTaskExcute(taskerMsg);
                break;
            case IUnistarClustMsg.TYPE_SERVICE_CHANGED:
                AppNodeMsg nodeMsg = JsonUtil.toClass(msg.getBody(), AppNodeMsg.class);
                if (nodeMsg == null || !checkNamespace(nodeMsg)) return;
                doServiceNodeChanged(nodeMsg);
                break;
            case IUnistarClustMsg.TYPE_WATCH:
                WatchMsg watchMsg = JsonUtil.toClass(msg.getBody(), WatchMsg.class);
                if (watchMsg == null || !checkNamespace(watchMsg)) return;
                doTraceWatch(watchMsg);
                break;
            case IUnistarClustMsg.TYPE_LIMIT_CHANGED:
                LimitMsg limitMsg = JsonUtil.toClass(msg.getBody(), LimitMsg.class);
                if (limitMsg == null || !checkNamespace(limitMsg)) return;
                doLimitChanged(limitMsg);
                break;
            case IUnistarClustMsg.TYPE_LOGGER_CHANGED:
                LoggerMsg loggerMsg = JsonUtil.toClass(msg.getBody(), LoggerMsg.class);
                if (loggerMsg == null || !checkNamespace(loggerMsg)) return;
                doLoggerChanged(loggerMsg);
                break;
            case IUnistarClustMsg.TYPE_LOGGER_SEARCH:
                LoggerMsg searchMsg = JsonUtil.toClass(msg.getBody(), LoggerMsg.class);
                if (searchMsg == null || !checkNamespace(searchMsg)) return;
                doLoggerSearched(searchMsg);
                break;
        }
    }

    /**
     * ??????????????????
     *
     * @param baseMsg
     * @return
     */
    private boolean checkNamespace(BaseMsg baseMsg) {
        return connectors.containsKey(baseMsg.getNamespace());
    }

    @Override
    public void clean() {
        unistarConnectCacheService.clean(clustProperties.centerAddress());
    }

    @Override
    public void connect(WebSocketSession session) {
        UnistarParam param = (UnistarParam) session.getAttributes().get(UnistarParam.UNISTAR_PARAM);
        if (param == null) throw new UnistarRemoteException("???????????????????????????????????????");
        if (!connectors.containsKey(param.getNamespace())) {
            reload();
            if (!connectors.containsKey(param.getNamespace())) {
                throw new UnistarRemoteException("??????????????????????????????????????????");
            }
        }
        connectors.get(param.getNamespace()).join(session);
    }

    @Override
    public void disconnect(WebSocketSession session) {
        UnistarParam param = (UnistarParam) session.getAttributes().get(UnistarParam.UNISTAR_PARAM);
        if (param == null) return;
        if (connectors.containsKey(param.getNamespace())) {
            connectors.get(param.getNamespace()).left(session);
        }
        AppNode node = appService.findAppNode(param.getNamespace(), param.getName(), param.getHost(), param.getPort());
        if (node != null) {
            // ????????????
            node.setServiceStatus(AppNode.EStatus.OFF);
            node.setDiscoverStatus(AppNode.EStatus.OFF);
            node.setTaskStatus(AppNode.EStatus.OFF);
            // ??????????????????
            appService.appNodeDisconnect(node);
            // ????????????????????????
            unistarConnectCacheService.disconnected(node);
            // ???????????????????????????????????????????????????
            if (node.isServerable()) serviceNodeChanged(node);
        }
        //
        log.debug("?????? {} ????????? {} ????????? {} ??????????????? {}:{} ?????????", param.getNamespace(), param.getName(), param.getGroup(), param.getHost(), param.getPort());
    }

    /**
     * ??????????????????
     *
     * @param node
     * @param configs
     */
    public void nodeConfigChanged(AppNode node, String configs) {
        if (node == null) return;
        ConfigMsg configMsg = new ConfigMsg();
        configMsg.setNamespace(node.getNamespace());
        configMsg.setClients(Collections.singletonList(node.getConnectId()));
        configMsg.setConfigs(configs);
        //
        if (clustProperties.isClust()) {
            unistarCluster.multicast(IUnistarClustMsg.TYPE_CONFIG_CHANGED, configMsg);
        } else {
            doConfigChanged(configMsg);
        }
    }

    /**
     * ????????????????????????
     *
     * @param configMsg
     */
    private void doConfigChanged(ConfigMsg configMsg) {
        connectors.get(configMsg.getNamespace()).send(configMsg.getClients(), IUnistarEventConst.EVENT_CONFIG_CHANGED, configMsg.getConfigs());
    }

    /**
     * ?????????????????????????????????
     *
     * @param node
     */
    public void serviceNodeChanged(AppNode node) {
        if (node == null) return;
        AppNodeMsg nodeMsg = new AppNodeMsg();
        nodeMsg.setNamespace(node.getNamespace());
        nodeMsg.setServiceNode(node.toServiceNode());
        // ???????????????????????????
        List<AppNode> appNodes = unistarConnectCacheService.allOnlineDiscover(node.getNamespace(), node.getAppname());
        List<String> sessionIds = new ArrayList<>();
        appNodes.stream()
                .filter(n -> StringUtil.fromCommaString(n.getDiscovers()).contains(node.getAppname()))
                .forEach(n -> sessionIds.add(n.getConnectId()));
        if (CollectionUtils.isEmpty(sessionIds)) return;
        nodeMsg.setClients(sessionIds);
        //
        if (clustProperties.isClust()) {
            unistarCluster.multicast(IUnistarClustMsg.TYPE_SERVICE_CHANGED, nodeMsg);
        } else {
            doServiceNodeChanged(nodeMsg);
        }
    }

    /**
     * ??????????????????
     *
     * @param nodeMsg
     */
    private void doServiceNodeChanged(AppNodeMsg nodeMsg) {
        connectors.get(nodeMsg.getNamespace()).send(nodeMsg.getClients(), IUnistarEventConst.EVENT_DISCOVER_INSTANCE_CHANGED, JsonUtil.toJsonString(nodeMsg.getServiceNode()));
    }

    /**
     * ????????????
     *
     * @param node
     * @param task
     */
    public void taskExcute(AppNode node, ScheduleTask task) {
        if (node == null) return;
        TaskerMsg taskerMsg = new TaskerMsg();
        taskerMsg.setNamespace(node.getNamespace());
        taskerMsg.setClients(Collections.singletonList(node.getConnectId()));
        taskerMsg.setTask(task);
        //
        if (clustProperties.isClust()) {
            unistarCluster.send(new UnistarNode(node.getConnectCenter()), IUnistarClustMsg.TYPE_TASKER, taskerMsg);
        } else {
            doTaskExcute(taskerMsg);
        }
    }

    /**
     * ??????????????????
     *
     * @param taskerMsg
     */
    private void doTaskExcute(TaskerMsg taskerMsg) {
        ScheduleTask task = taskerMsg.getTask();
        if (task == null) return;
        UnistarTaskData taskData = new UnistarTaskData();
        taskData.setNo(task.getNo());
        taskData.setName(task.getTask());
        Map<String, Object> params = new HashMap<>();
        // ????????????
        if (StringUtils.isNotEmpty(task.getParams())) {
            params.putAll(JsonUtil.toMap(task.getParams()));
        }
        // ????????????????????????????????????
        if (StringUtils.isNotEmpty(task.getPreResult())) {
            params.putAll(JsonUtil.toMap(task.getPreResult()));
        }
        taskData.setParams(params);
        connectors.get(taskerMsg.getNamespace()).send(taskerMsg.getClients(), IUnistarEventConst.EVENT_TASK, JsonUtil.toJsonString(taskData));
    }

    /**
     * ????????????
     *
     * @param node
     * @param path
     */
    public void traceWatch(AppNode node, String path) {
        if (node == null) return;
        WatchMsg watchMsg = new WatchMsg();
        watchMsg.setNamespace(node.getNamespace());
        watchMsg.setClients(Collections.singletonList(node.getConnectId()));
        watchMsg.setPath(path);
        if (clustProperties.isClust()) {
            unistarCluster.multicast(IUnistarClustMsg.TYPE_WATCH, watchMsg);
        } else {
            doTraceWatch(watchMsg);
        }
    }

    /**
     * ??????????????????
     *
     * @param watchMsg
     */
    private void doTraceWatch(WatchMsg watchMsg) {
        connectors.get(watchMsg.getNamespace()).send(watchMsg.getClients(), IUnistarEventConst.EVENT_TRACE_WATCH, watchMsg.getPath());
    }

    /**
     * ????????????????????????
     *
     * @param appLimit
     */
    public void limitChanged(AppLimit appLimit) {
        LimitMsg limitMsg = new LimitMsg();
        limitMsg.setNamespace(appLimit.getNamespace());
        limitMsg.setAppLimit(appLimit);
        // ??????????????????
        List<String> sessionIds;
        if (appLimit.isBefore()) {
            // Feign??????????????????????????????
            sessionIds = unistarConnectCacheService.allOnlineDiscover(appLimit.getNamespace(), appLimit.getAppname())
                    .stream()
                    // ???????????????
                    .filter(n -> StringUtil.fromCommaString(n.getDiscovers()).contains(appLimit.getAppname()))
                    .map(AppNode::getConnectId)
                    .collect(Collectors.toList());
        } else {
            // Controller???????????????????????????
            sessionIds = unistarConnectCacheService.allOnlineServer(appLimit.getNamespace(), appLimit.getAppname())
                    .stream()
                    .map(AppNode::getConnectId)
                    .collect(Collectors.toList());
        }
        if (CollectionUtils.isEmpty(sessionIds)) return;
        limitMsg.setClients(sessionIds);
        //
        if (clustProperties.isClust()) {
            unistarCluster.multicast(IUnistarClustMsg.TYPE_LIMIT_CHANGED, limitMsg);
        } else {
            doLimitChanged(limitMsg);
        }
    }

    /**
     * ????????????????????????
     *
     * @param limitMsg
     */
    private void doLimitChanged(LimitMsg limitMsg) {
        connectors.get(limitMsg.getNamespace()).send(limitMsg.getClients(), IUnistarEventConst.EVENT_LIMIT_CHANGED, JsonUtil.toJsonString(limitMsg.getAppLimit()));
    }


    /**
     * ????????????????????????
     *
     * @param node
     * @param loggers
     */
    public void nodeLoggerChanged(AppNode node, String loggers) {
        if (node == null) return;
        LoggerMsg loggerMsg = new LoggerMsg();
        loggerMsg.setNamespace(node.getNamespace());
        loggerMsg.setClients(Collections.singletonList(node.getConnectId()));
        loggerMsg.setLoggers(loggers);
        //
        if (clustProperties.isClust()) {
            unistarCluster.multicast(IUnistarClustMsg.TYPE_LOGGER_CHANGED, loggerMsg);
        } else {
            doLoggerChanged(loggerMsg);
        }
    }

    private void doLoggerChanged(LoggerMsg loggerMsg) {
        connectors.get(loggerMsg.getNamespace()).send(loggerMsg.getClients(), IUnistarEventConst.EVENT_LOGGER_CHANGED, loggerMsg.getLoggers());
    }

    /**
     * ??????????????????
     *
     * @param namespace
     * @param param
     */
    public void nodeLoggerSearched(String namespace, UnistarLoggerSearchParam param) {
        LoggerMsg loggerMsg = new LoggerMsg();
        loggerMsg.setNamespace(namespace);
        loggerMsg.setLoggers(JsonUtil.toJsonString(param));
        //
        if (clustProperties.isClust()) {
            unistarCluster.multicast(IUnistarClustMsg.TYPE_LOGGER_SEARCH, loggerMsg);
        } else {
            doLoggerSearched(loggerMsg);
        }
    }

    /**
     * @param loggerMsg
     */
    private void doLoggerSearched(LoggerMsg loggerMsg) {
        connectors.get(loggerMsg.getNamespace()).sendAll(IUnistarEventConst.EVENT_LOG_SEARCH, loggerMsg.getLoggers());
    }

}
