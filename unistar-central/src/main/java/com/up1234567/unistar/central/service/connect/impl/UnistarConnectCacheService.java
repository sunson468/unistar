package com.up1234567.unistar.central.service.connect.impl;

import com.up1234567.unistar.central.data.us.AppNode;
import com.up1234567.unistar.central.service.connect.IUnistarConnectCacheService;
import com.up1234567.unistar.central.support.cache.IUnistarCache;
import com.up1234567.unistar.common.IUnistarConst;
import com.up1234567.unistar.common.util.JsonUtil;
import com.up1234567.unistar.common.util.RandomUtil;
import com.up1234567.unistar.common.util.StringUtil;
import lombok.CustomLog;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 缓存的作用是为了更快的获取连接及其所在的中心
 */
@CustomLog(topic = IUnistarConst.DEFAULT_LOGGER_UNISTAR)
@Service
public class UnistarConnectCacheService implements IUnistarConnectCacheService {

    @Autowired
    private IUnistarCache unistarCache;

    /**
     * @param node
     * @return
     */
    private String wrapNodeKey(AppNode node) {
        return wrapNodeKey(node.getNamespace(), node.getNodeId());
    }

    /**
     * @param namespace
     * @param nodeId
     * @return
     */
    private String wrapNodeKey(String namespace, String nodeId) {
        return String.format(CK_NS_NODE, namespace, nodeId);
    }

    /**
     * @param centerAddress
     */
    public void clean(String centerAddress) {
        // 清理数据
        String centralKey = String.format(CK_CENTRAL, centerAddress);
        Collection<String> caches = unistarCache.setGet(centralKey);
        unistarCache.del(centralKey);
        if (CollectionUtils.isEmpty(caches)) return;
        unistarCache.del(caches);
    }

    /**
     * @param node
     */
    public void connected(AppNode node) {
        String nodeKey = wrapNodeKey(node);
        // ========================================
        // 加入所有的节点列表内 IP:PORT
        unistarCache.set(nodeKey, JsonUtil.toJsonString(node), NODE_EXPIRE);
        // ========================================
        // 加入空间列表
        unistarCache.setAdd(String.format(CK_NS_ALL, node.getNamespace()), nodeKey);
        // 加入中央列表
        unistarCache.setAdd(String.format(CK_CENTRAL, node.getConnectCenter()), nodeKey);
        // ========================================
        // 是否提供服务
        serverable(node);
        // 是否为服务发现者
        if (node.isDiscoverable()) discoverable(node);
        // 是否为任务执行器
        if (node.isTaskable()) taskerable(node);
    }

    /**
     * 更新节点
     *
     * @param node
     */
    public void updateNode(AppNode node) {
        String nodeKey = wrapNodeKey(node);
        if (unistarCache.has(nodeKey)) {
            unistarCache.set(nodeKey, JsonUtil.toJsonString(node), NODE_EXPIRE);
        }
    }

    /**
     * @param node
     */
    public void disconnected(AppNode node) {
        String nodeKey = wrapNodeKey(node);
        unistarCache.del(nodeKey);
        // ========================================
        // 移出空间列表
        unistarCache.setAdd(String.format(CK_NS_ALL, node.getNamespace()), nodeKey);
        // 移出中央列表
        unistarCache.setAdd(String.format(CK_CENTRAL, node.getConnectCenter()), nodeKey);
        // ========================================
        // 是否提供服务
        unserverable(node);
        // 是否为发现者
        if (node.isDiscoverable()) undiscoverable(node);
        // 是否为任务执行器
        if (node.isTaskable()) untaskerable(node);
    }

    /**
     * 加入到服务节点
     *
     * @param node
     */
    private void serverable(AppNode node) {
        String nodeKey = wrapNodeKey(node);
        // 加入注册的服务 服务列表
        unistarCache.setAdd(String.format(CK_SVR_NS_SERVICE, node.getNamespace(), node.getAppname()), nodeKey);
    }

    /**
     * 节点下线，控台主动控制下线
     *
     * @param node
     */
    private void unserverable(AppNode node) {
        String nodeKey = wrapNodeKey(node);
        // 加入注册的服务 服务列表
        unistarCache.setAdd(String.format(CK_SVR_NS_SERVICE, node.getNamespace(), node.getAppname()), nodeKey);
    }

    /**
     * 加入到服务节点
     *
     * @param node
     */
    private void taskerable(AppNode node) {
        String nodeKey = wrapNodeKey(node);
        // 加入任务执行器列表
        StringUtil.fromCommaString(node.getTasks())
                .parallelStream()
                .forEach(t -> unistarCache.setAdd(String.format(CK_NS_TASKER, node.getNamespace(), t), nodeKey));
    }

    /**
     * 加入到服务节点
     *
     * @param node
     */
    private void untaskerable(AppNode node) {
        String nodeKey = wrapNodeKey(node);
        // 加入任务执行器列表
        StringUtil.fromCommaString(node.getTasks())
                .parallelStream()
                .forEach(t -> unistarCache.setDel(String.format(CK_NS_TASKER, node.getNamespace(), t), nodeKey));
    }

    /**
     * 加入到发现者节点
     *
     * @param node
     */
    public void discoverable(AppNode node) {
        String nodeKey = wrapNodeKey(node);
        // 加入发现服务客户端列表
        StringUtil.fromCommaString(node.getDiscovers())
                .parallelStream()
                .forEach(service -> unistarCache.setAdd(String.format(CK_SVR_NS_DISCOVER, node.getNamespace(), service), nodeKey));
    }

    /**
     * 移出发现者节点
     *
     * @param node
     */
    private void undiscoverable(AppNode node) {
        String nodeKey = wrapNodeKey(node);
        // 从发现服务客户端列表内移除
        StringUtil.fromCommaString(node.getDiscovers())
                .parallelStream()
                .forEach(service -> unistarCache.setDel(String.format(CK_SVR_NS_DISCOVER, node.getNamespace(), service), nodeKey));
    }

    /**
     * @param node
     */
    public void hearbeat(AppNode node) {
        String nodeKey = wrapNodeKey(node);
        // ==========================
        unistarCache.expire(nodeKey, NODE_EXPIRE);
    }

    /**
     * 获取总在线数
     *
     * @param namespace
     * @return
     */
    public long onlines(String namespace) {
        return CollectionUtils.size(allOnlineSpecial(namespace, CK_NS_ALL));
    }

    /**
     * 获取某个service所有在线的节点，用于发现服务
     *
     * @param namespace
     * @param service
     * @return
     */
    public List<AppNode> allOnlineServer(String namespace, String service) {
        return allOnlineSpecialNode(namespace, CK_SVR_NS_SERVICE, service);
    }

    /**
     * 获取某个服务的所有调用者，用于通知服务变更
     *
     * @param namespace
     * @param service
     * @return
     */
    public List<AppNode> allOnlineDiscover(String namespace, String service) {
        return allOnlineSpecialNode(namespace, CK_SVR_NS_DISCOVER, service);
    }

    /**
     * 获取某个任务所有的执行器
     *
     * @param namespace
     * @param task
     * @return
     */
    public List<AppNode> allOnlineTasker(String namespace, String task) {
        return allOnlineSpecialNode(namespace, CK_NS_TASKER, task);
    }

    /**
     * @param namespace
     * @param specialKey
     * @return
     */
    public List<String> allOnlineSpecial(String namespace, String specialKey) {
        return allOnlineSpecial(namespace, specialKey, null);
    }

    /**
     * @param namespace
     * @param specialKey
     * @param specialValue
     * @return
     */
    public List<String> allOnlineSpecial(String namespace, String specialKey, String specialValue) {
        String listKey = ((specialValue == null) ? String.format(specialKey, namespace) : String.format(specialKey, namespace, specialValue));
        Collection<String> nodeKeys = unistarCache.setGet(listKey);
        if (CollectionUtils.isEmpty(nodeKeys)) return new ArrayList<>();
        List<String> realValues = unistarCache.get(nodeKeys);
        removeInvalidSpecialNode(namespace, specialKey, specialValue, nodeKeys, realValues);
        return realValues;
    }

    /**
     * 查找某个类别的对应类别值的缓存
     *
     * @param namespace    所属空间
     * @param specialKey   查找的缓存Key分类
     * @param specialValue 查找的缓存Key具体值，非必须
     * @return
     */
    public List<AppNode> allOnlineSpecialNode(String namespace, String specialKey, String specialValue) {
        List<String> nodeCaches = allOnlineSpecial(namespace, specialKey, specialValue);
        return nodeCaches
                .parallelStream()
                .filter(Objects::nonNull)
                .map(cache -> JsonUtil.toClass(cache, AppNode.class))
                .collect(Collectors.toList());
    }

    /**
     * 移除无效的节点缓存
     *
     * @param namespace
     * @param specialKey
     * @param specialValue
     * @param existKeys    存在的key
     * @param realValues   实际存在的节点Key
     */
    @Async
    public void removeInvalidSpecialNode(String namespace, String specialKey, String specialValue, Collection<String> existKeys, Collection<String> realValues) {
        // 移除已经存在的，作为字符串包含关系来看
        existKeys.removeIf(k -> realValues.stream().filter(Objects::nonNull).anyMatch(r -> r.contains(k.substring(k.lastIndexOf(StringUtil.XHX) + 1))));
        if (CollectionUtils.isEmpty(existKeys)) return;
        String listKey = ((specialValue == null) ? String.format(specialKey, namespace) : String.format(specialKey, namespace, specialValue));
        unistarCache.setDel(listKey, existKeys);
    }

    /**
     * 该节点是否在线
     *
     * @param node
     * @return
     */
    public boolean isOnline(AppNode node) {
        String nodeKey = wrapNodeKey(node);
        return unistarCache.has(nodeKey);
    }

    /**
     * 随机抽取一个任务执行器
     *
     * @param namespace
     * @param task
     * @param group
     * @return
     */
    public AppNode randomOnlineTasker(String namespace, String task, String group) {
        List<AppNode> taskers = allOnlineTasker(namespace, task);
        taskers.removeIf(server -> !server.isTaskAvailable());
        if (StringUtils.isNotEmpty(group)) taskers.removeIf(server -> !group.equals(server.getGroup()));
        return RandomUtil.randomOne(taskers);
    }

}
