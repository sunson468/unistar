package com.up1234567.unistar.springcloud.discover;

import com.up1234567.unistar.springcloud.core.IUnistarClientListener;
import com.up1234567.unistar.springcloud.discover.loadbalancer.IUnistarLoadBalancer;
import com.up1234567.unistar.common.IUnistarConst;
import com.up1234567.unistar.common.discover.UnistarDiscoverOutParam;
import com.up1234567.unistar.common.discover.UnistarServiceNode;
import com.up1234567.unistar.common.ds.AtomicDateChanger;
import com.up1234567.unistar.common.event.IUnistarEventConst;
import com.up1234567.unistar.common.exception.UnistarNotSupportException;
import com.up1234567.unistar.common.heartbeat.UnistarHeartbeatData;
import com.up1234567.unistar.common.logger.IUnistarLogger;
import com.up1234567.unistar.common.logger.UnistarLoggerFactory;
import com.up1234567.unistar.common.util.JsonUtil;
import com.up1234567.unistar.common.util.ThreadUtil;
import com.up1234567.unistar.springcloud.UnistarProperties;
import com.up1234567.unistar.springcloud.core.event.IUnistarClientDispatcher;
import com.up1234567.unistar.springcloud.limit.UnistarLimitManager;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

/**
 * 服务发现客户端
 */
public class UnistarDiscoverClient implements DiscoveryClient, IUnistarClientListener {

    private static final IUnistarLogger logger = UnistarLoggerFactory.getLogger(IUnistarConst.DEFAULT_LOGGER_UNISTAR);

    public final static String DESCRIPTION = "Spring Cloud Unistar Discovery Client";

    private UnistarProperties unistarProperties;
    private IUnistarClientDispatcher unistarEventPublisher;
    private IUnistarLoadBalancer unistarLoadBalancer;
    private UnistarLimitManager unistarLimitManager;

    // 日切计数器，每天的凌晨3点作为切换点，全同步
    private AtomicDateChanger dateChanger = new AtomicDateChanger(3);

    // 实例存储
    private final ConcurrentMap<String, List<UnistarServiceInstance>> serviceInstances = new ConcurrentHashMap<>();

    public UnistarDiscoverClient(UnistarProperties unistarProperties, IUnistarClientDispatcher unistarEventPublisher, IUnistarLoadBalancer unistarLoadBalancer, UnistarLimitManager unistarLimitManager) {
        this.unistarProperties = unistarProperties;
        this.unistarEventPublisher = unistarEventPublisher;
        this.unistarLoadBalancer = unistarLoadBalancer;
        this.unistarLimitManager = unistarLimitManager;
    }

    @Override
    public void heartbeat(UnistarHeartbeatData heartbeatData) {
        if (dateChanger.isDayChanged() && !CollectionUtils.isEmpty(getServices())) {
            List<String> serviceIds = getServices();
            // 重置为空
            resetInstance();
            // 同步已有的调动
            serviceIds.forEach(this::syncRemote);
        }
    }

    @Override
    public void reconnected() {
        resetInstance();
    }

    /**
     * 将实例缓存置为空
     */
    public void resetInstance() {
        logger.debug("unistar client current instances before reset: {}", JsonUtil.toJsonString(serviceInstances));
        serviceInstances.clear();
    }

    /**
     * @param serviceId
     */
    private void syncRemote(String serviceId) {
        logger.debug("get unistar service node from unistar central: {}", serviceId);
        final AtomicBoolean checker = new AtomicBoolean(false);
        // 开始远程获取
        this.unistarEventPublisher.publish(IUnistarEventConst.HANDLE_DISCOVER, unistarProperties.wrapUnistarDiscoverData(serviceId), (success, param) -> {
            if (success) {
                logger.debug("discover successful from unistar central server");
                UnistarDiscoverOutParam outParam = JsonUtil.toClass(param, UnistarDiscoverOutParam.class);
                if (outParam != null) {
                    unistarLimitManager.addFeignLimits(outParam.getAppLimits());
                    syncAll(serviceId, outParam.getServiceNodes());
                }
            } else {
                logger.debug("discover failed from unistar central server, connect timeout");
            }
            checker.set(true);
        });
        // 等待5 * 100ms
        ThreadUtil.loopUtilByTimes(checker::get, 5, 100, null);
    }

    /**
     * @param serviceId
     * @param services
     */
    private void syncAll(String serviceId, List<UnistarServiceNode> services) {
        List<UnistarServiceInstance> tmpServiceInstances = new ArrayList<>();
        services.forEach(node -> tmpServiceInstances.add(wrapInstance(node)));
        serviceInstances.put(serviceId, tmpServiceInstances);
    }

    /**
     * 更新某个
     *
     * @param node
     */
    public void sync(UnistarServiceNode node) {
        // 不包含，则无需处理
        if (!serviceInstances.containsKey(node.getService())) return;
        logger.debug("unistar service node changed: {}", node);
        List<UnistarServiceInstance> instances = serviceInstances.get(node.getService());
        synchronized (instances) {
            boolean contained = false;
            for (UnistarServiceInstance instance : instances) {
                if (instance.isSameFrom(node)) {
                    contained = true;
                    update(instance, node);
                }
            }
            if (!contained) {
                instances.add(wrapInstance(node));
            }
        }
    }

    /**
     * @param node
     * @return
     */
    private UnistarServiceInstance wrapInstance(UnistarServiceNode node) {
        UnistarServiceInstance instance = new UnistarServiceInstance();
        instance.setServiceId(node.getService());
        instance.setHost(node.getHost());
        instance.setPort(node.getPort());
        update(instance, node);
        return instance;
    }

    /**
     * @param instance
     * @param param
     */
    private void update(UnistarServiceInstance instance, UnistarServiceNode param) {
        instance.setAvailable(param.isAvailable());
        instance.setWeight(param.getWeight());
    }

    @Override
    public String description() {
        return DESCRIPTION;
    }

    @Override
    public List<ServiceInstance> getInstances(String serviceId) {
        if (unistarProperties.getName().equals(serviceId)) {
            throw new UnistarNotSupportException("you cann't call myself by disvover client");
        }
        List<UnistarServiceInstance> instances = serviceInstances.get(serviceId);
        // 不存在就远程获取
        if (instances == null) {
            syncRemote(serviceId);
            instances = serviceInstances.get(serviceId);
        }
        // 找不到直接返回
        if (CollectionUtils.isEmpty(instances)) return Collections.emptyList();
        // =======================================================
        ServiceInstance instance = unistarLoadBalancer.choose(
                instances.parallelStream()
                        .filter(UnistarServiceInstance::isAvailable)
                        .collect(Collectors.toList())
        );
        return instance != null ? Collections.singletonList(instance) : Collections.emptyList();
    }

    @Override
    public List<String> getServices() {
        return new ArrayList<>(serviceInstances.keySet());
    }
}
