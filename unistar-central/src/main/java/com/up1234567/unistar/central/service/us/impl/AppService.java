package com.up1234567.unistar.central.service.us.impl;

import com.up1234567.unistar.central.data.base.BaseNamespace;
import com.up1234567.unistar.central.data.us.App;
import com.up1234567.unistar.central.data.us.AppLimit;
import com.up1234567.unistar.central.data.us.AppNode;
import com.up1234567.unistar.central.service.base.impl.BaseService;
import com.up1234567.unistar.central.service.stat.impl.StatTraceService;
import com.up1234567.unistar.central.service.us.IAppService;
import com.up1234567.unistar.central.support.util.HttpUtil;
import com.up1234567.unistar.central.support.ws.IUnistarConnectorAuthor;
import com.up1234567.unistar.central.support.core.UnistarProperties;
import com.up1234567.unistar.central.support.data.IUnistarDao;
import com.up1234567.unistar.common.IUnistarConst;
import com.up1234567.unistar.common.UnistarParam;
import com.up1234567.unistar.common.UnistarReadyParam;
import com.up1234567.unistar.common.UnistarSyncParam;
import com.up1234567.unistar.common.util.DateUtil;
import com.up1234567.unistar.common.util.JsonUtil;
import com.up1234567.unistar.common.util.StringUtil;
import lombok.CustomLog;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@CustomLog(topic = IUnistarConst.DEFAULT_LOGGER_UNISTAR)
@Service
public class AppService implements IAppService, IUnistarConnectorAuthor {

    @Autowired
    private IUnistarDao unistarDao;

    @Autowired
    private BaseService baseService;

    @Autowired
    private StatTraceService statTraceService;

    @Autowired
    private UnistarProperties clustProperties;

    /**
     * 查找应用
     *
     * @param namespace
     * @param name
     * @return
     */
    public App findApp(String namespace, String name) {
        Map<String, Object> props = new HashMap<>();
        props.put("namespace", namespace);
        props.put("name", name);
        return unistarDao.findOneByProps(props, App.class);
    }

    /**
     * 查找服务
     *
     * @param app
     * @return
     */
    public void createApp(App app) {
        unistarDao.insert(app);
    }

    /**
     * @param namespaces
     * @return
     */
    public List<App> listApp(Collection<String> namespaces) {
        Map<String, Object> props = new HashMap<>();
        props.put("namespace", namespaces);
        return unistarDao.listByProps(props, App.class);
    }

    /**
     * @param namespace
     * @param appNames
     * @return
     */
    public List<App> listApp(String namespace, Collection<String> appNames) {
        Map<String, Object> props = new HashMap<>();
        props.put("namespace", namespace);
        if (CollectionUtils.isNotEmpty(appNames)) props.put("name", appNames);
        return unistarDao.listByProps(props, App.class);
    }

    /**
     * @param namespace
     * @return
     */
    public long countApp(String namespace) {
        Map<String, Object> props = new HashMap<>();
        props.put("namespace", namespace);
        return unistarDao.count(props, App.class);
    }

    /**
     * @param app
     */
    public void updateApp(App app) {
        Map<String, Object> props = new HashMap<>();
        props.put("namespace", app.getNamespace());
        props.put("name", app.getName());
        Map<String, Object> updates = new HashMap<>();
        updates.put("remark", app.getRemark());
        unistarDao.updateOneByProps(props, updates, App.class);
    }

    /**
     * 校验服务是否可以接入
     *
     * @param param
     * @return
     */
    @Override
    public boolean checkApp(UnistarParam param) {
        BaseNamespace namespace = baseService.findNamespace(param.getNamespace());
        if (namespace == null) {
            log.warn("节点空间不存在：{}", param.getNamespace());
            return false;
        }
        App app = findApp(param.getNamespace(), param.getName());
        if (app == null) {
            log.debug("节点应用首次接入：{}", param.getName());
            app = new App();
            app.setNamespace(param.getNamespace());
            app.setName(param.getName());
            app.setToken(param.getToken());
            app.setCreateTime(DateUtil.now());
            createApp(app);
        } else {
            if (StringUtils.isNotEmpty(app.getToken()) && !app.getToken().equals(param.getToken())) {
                log.warn("空间 {} 的应用 {} 授权校验失败", param.getNamespace(), param.getName());
                return false;
            }
        }
        return true;
    }

    /**
     * 查找应用接入的节点
     *
     * @param namespace
     * @param appname
     * @param host
     * @param port
     * @return
     */
    public AppNode findAppNode(String namespace, String appname, String host, int port) {
        Map<String, Object> props = new HashMap<>();
        props.put("namespace", namespace);
        props.put("appname", appname);
        props.put("host", host);
        props.put("port", port);
        return unistarDao.findOneByProps(props, AppNode.class);
    }

    /**
     * @param namespace
     * @param appname
     * @param host
     * @param port
     * @return
     */
    public AppNode findAppNodeWithCreate(String namespace, String appname, String host, int port) {
        AppNode node = findAppNode(namespace, appname, host, port);
        if (node == null) {
            node = new AppNode();
            node.setNamespace(namespace);
            node.setAppname(appname);
            node.setHost(host);
            node.setPort(port);
            node.toNodeId();
            createServiceNode(node);
        }
        return node;
    }

    /**
     * 查找应用接入的节点
     *
     * @param namespace
     * @param appname
     * @param nodeId
     * @return
     */
    public AppNode findAppNodeByNodeId(String namespace, String appname, String nodeId) {
        Map<String, Object> props = new HashMap<>();
        props.put("namespace", namespace);
        props.put("appname", appname);
        props.put("nodeId", nodeId);
        return unistarDao.findOneByProps(props, AppNode.class);
    }

    /**
     * @param namespace
     * @return
     */
    public long countAppNode(String namespace) {
        Map<String, Object> props = new HashMap<>();
        props.put("namespace", namespace);
        return unistarDao.count(props, AppNode.class);
    }

    /**
     * @param node
     */
    public void createServiceNode(AppNode node) {
        unistarDao.insert(node);
    }

    /**
     * 移除节点
     *
     * @param node
     */
    public void removeServiceNode(AppNode node) {
        Map<String, Object> props = new HashMap<>();
        props.put("namespace", node.getNamespace());
        props.put("nodeId", node.getNodeId());
        unistarDao.removeByProps(props, AppNode.class);
    }

    /**
     * 包装node的查询条件
     *
     * @param node
     * @return
     */
    private Map<String, Object> wrapAppNodeKey(AppNode node) {
        Map<String, Object> props = new HashMap<>();
        props.put("namespace", node.getNamespace());
        props.put("appname", node.getAppname());
        props.put("host", node.getHost());
        props.put("port", node.getPort());
        return props;
    }

    /**
     * 连接时 初始化节点
     *
     * @param node
     */
    public void initAppNode(AppNode node) {
        Map<String, Object> updates = new HashMap<>();
        // 重置服务标识
        updates.put("serverable", node.isServerable());
        updates.put("weight", node.getWeight());
        updates.put("serviceStatus", node.getServiceStatus());
        // ==========================================
        updates.put("discoverable", node.isDiscoverable());
        // ==========================================
        updates.put("taskable", node.isTaskable());
        updates.put("tasks", node.getTasks());
        updates.put("taskStatus", node.getTaskStatus());
        //
        updates.put("group", node.getGroup());
        updates.put("profiles", node.getProfiles());
        //
        updates.put("connectId", node.getConnectId());
        updates.put("connectCenter", node.getConnectCenter());
        updates.put("lastConnectTime", node.getLastConnectTime());
        unistarDao.updateOneByProps(wrapAppNodeKey(node), updates, AppNode.class);
        //
        appAs(node);
    }

    /**
     * 应用也标记
     *
     * @param node
     */
    @Async
    public void appAs(AppNode node) {
        Map<String, Object> props = new HashMap<>();
        props.put("namespace", node.getNamespace());
        props.put("name", node.getAppname());
        Map<String, Object> updates = new HashMap<>();
        updates.put("serverable", node.isServerable());
        updates.put("taskable", node.isTaskable());
        unistarDao.updateOneByProps(props, updates, App.class);
    }

    /**
     * 注册为发现服务者
     *
     * @param node
     */
    @Async
    public void nodeAsDiscover(AppNode node) {
        Map<String, Object> updates = new HashMap<>();
        updates.put("discoverable", node.isDiscoverable());
        updates.put("discovers", node.getDiscovers());
        updates.put("discoverStatus", node.getDiscoverStatus());
        unistarDao.updateOneByProps(wrapAppNodeKey(node), updates, AppNode.class);
        appAsDiscover(node);
    }

    /**
     * 注册为发现服务者
     *
     * @param node
     */
    private void appAsDiscover(AppNode node) {
        Map<String, Object> props = new HashMap<>();
        props.put("namespace", node.getNamespace());
        props.put("name", node.getAppname());
        Map<String, Object> updates = new HashMap<>();
        updates.put("discoverable", node.isDiscoverable());
        unistarDao.updateOneByProps(props, updates, App.class);
    }

    /**
     * 更新上次断开时间点
     *
     * @param node
     */
    public void appNodeDisconnect(AppNode node) {
        Map<String, Object> updates = new HashMap<>();
        updates.put("serviceStatus", AppNode.EStatus.OFF);
        updates.put("taskStatus", AppNode.EStatus.OFF);
        updates.put("lastDisonnectTime", node.getLastDisonnectTime());
        unistarDao.updateOneByProps(wrapAppNodeKey(node), updates, AppNode.class);
    }

    /**
     * @param node
     */
    public void appNodeServiceStatus(AppNode node) {
        Map<String, Object> updates = new HashMap<>();
        updates.put("serviceStatus", node.getServiceStatus());
        unistarDao.updateOneByProps(wrapAppNodeKey(node), updates, AppNode.class);
    }

    /**
     * @param node
     */
    public void appNodeTaskStatus(AppNode node) {
        Map<String, Object> updates = new HashMap<>();
        updates.put("taskStatus", node.getTaskStatus());
        unistarDao.updateOneByProps(wrapAppNodeKey(node), updates, AppNode.class);
    }

    /**
     * @param namespace
     * @param appname
     * @return
     */
    public List<AppNode> listAppNode(String namespace, String appname) {
        Map<String, Object> props = new HashMap<>();
        props.put("namespace", namespace);
        if (StringUtils.isNotEmpty(appname)) {
            props.put("appname", appname);
        }
        return unistarDao.listByProps(props, AppNode.class);
    }

    /**
     * @return
     */
    public List<AppNode> listAllManualAppNode() {
        Map<String, Object> props = new HashMap<>();
        props.put("manual", true);
        return unistarDao.listByProps(props, AppNode.class);
    }

    /**
     * 同步节点
     *
     * @param node
     */
    public boolean syncManualAppNode(AppNode node) {
        try {
            String ret = HttpUtil.get(node.wrapManualAnurl());
            if (StringUtils.isNotEmpty(ret)) {
                UnistarSyncParam param = JsonUtil.toClass(ret, UnistarSyncParam.class);
                if (param == null) return false;
                syncNodeReadyParam(node, param);
                node.setConnectId(node.getNodeId());
                initAppNode(node);
                //
                statTraceService.statTraceHeartbeat(node, param.getTraces());
                return true;
            }
        } catch (Exception e) {
            log.error("sync manual node error", e);
        }
        return false;
    }

    /**
     * 设置节点参数，在准备好，或者手动同步时设置
     *
     * @param node
     * @param param
     */
    public void syncNodeReadyParam(AppNode node, UnistarReadyParam param) {
        // 服务注册设置
        node.setServerable(param.getRegistraionParam() != null);
        if (node.isServerable()) {
            node.setWeight(param.getRegistraionParam().getWeight());
            node.setServiceStatus(param.getRegistraionParam().isAvailable() ? AppNode.EStatus.ON : AppNode.EStatus.OFF);
        } else {
            node.setServiceStatus(AppNode.EStatus.OFF);
        }
        // =========================================================================================
        // 任务执行器相关
        node.setTaskable(param.getTaskParam() != null);
        if (node.isTaskable()) {
            node.setTaskStatus(param.getTaskParam().isAvailable() ? AppNode.EStatus.ON : AppNode.EStatus.OFF);
            node.setTasks(StringUtil.toCommaString(param.getTaskParam().getTasks()));
        } else {
            node.setTaskStatus(AppNode.EStatus.OFF);
        }
        node.setConnectCenter(clustProperties.centerAddress());
        node.setLastConnectTime(DateUtil.now());
    }

    /**
     * @param namespace
     * @param appnames
     * @return
     */
    public List<AppLimit> listAppLimit(String namespace, List<String> appnames) {
        Map<String, Object> props = new HashMap<>();
        props.put("namespace", namespace);
        props.put("appname", appnames);
        return unistarDao.listByProps(props, AppLimit.class);
    }

    /**
     * @param namespace
     * @param appname
     * @param before    true:获取Feign层的控制|false:获取Controller层的控制
     * @return
     */
    public List<AppLimit> listAppLimit(String namespace, String appname, boolean before) {
        Map<String, Object> props = new HashMap<>();
        props.put("namespace", namespace);
        props.put("appname", appname);
        props.put("before", before);
        props.put("status", AppLimit.EStatus.ON);
        return unistarDao.listByProps(props, AppLimit.class);
    }

    /**
     * @param namespace
     * @param appname
     * @param path
     * @param before
     * @param auto
     * @return
     */
    public AppLimit findAppLimit(String namespace, String appname, String path, boolean before, boolean auto) {
        Map<String, Object> props = new HashMap<>();
        props.put("namespace", namespace);
        props.put("appname", appname);
        props.put("path", path);
        props.put("before", before);
        props.put("auto", auto);
        return unistarDao.findOneByProps(props, AppLimit.class);
    }

    /**
     * @param appLimit
     */
    public void createAppLimit(AppLimit appLimit) {
        appLimit.setCreateTime(DateUtil.now());
        unistarDao.insert(appLimit);
    }

    /**
     * @param appLimit
     */
    public void updateAppLimit(AppLimit appLimit) {
        Map<String, Object> props = new HashMap<>();
        props.put("namespace", appLimit.getNamespace());
        props.put("appname", appLimit.getAppname());
        props.put("path", appLimit.getPath());
        props.put("before", appLimit.isBefore());
        props.put("auto", appLimit.isAuto());
        Map<String, Object> updates = new HashMap<>();
        updates.put("startTime", appLimit.getStartTime());
        updates.put("endTime", appLimit.getEndTime());
        updates.put("qps", appLimit.getQps());
        updates.put("warmup", appLimit.getWarmup());
        updates.put("fastfail", appLimit.isFastfail());
        updates.put("timeout", appLimit.getTimeout());
        if (!appLimit.isAuto()) {
            updates.put("period", appLimit.getPeriod());
            updates.put("errors", appLimit.getErrors());
            updates.put("recover", appLimit.getRecover());
        }
        updates.put("whiteGroups", appLimit.getWhiteGroups());
        updates.put("whiteServices", appLimit.getWhiteServices());
        unistarDao.updateOneByProps(props, updates, AppLimit.class);
    }

    /**
     * @param appLimit
     */
    public void updateAppLimitStatus(AppLimit appLimit) {
        Map<String, Object> props = new HashMap<>();
        props.put("namespace", appLimit.getNamespace());
        props.put("appname", appLimit.getAppname());
        props.put("path", appLimit.getPath());
        props.put("before", appLimit.isBefore());
        props.put("auto", appLimit.isAuto());
        Map<String, Object> updates = new HashMap<>();
        updates.put("status", appLimit.getStatus());
        unistarDao.updateOneByProps(props, updates, AppLimit.class);
    }
}
