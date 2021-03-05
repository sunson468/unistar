package com.up1234567.unistar.springcloud.discover.loadbalancer;

import com.up1234567.unistar.common.IUnistarConst;
import com.up1234567.unistar.common.logger.IUnistarLogger;
import com.up1234567.unistar.common.logger.UnistarLoggerFactory;
import com.up1234567.unistar.springcloud.discover.UnistarServiceInstance;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class UnistarWeightLoadBalancer implements IUnistarLoadBalancer {

    private static final IUnistarLogger logger = UnistarLoggerFactory.getLogger(IUnistarConst.DEFAULT_LOGGER_UNISTAR);

    @Override
    public ServiceInstance choose(List<ServiceInstance> instances) {
        if (CollectionUtils.isEmpty(instances)) {
            return null;
        }

        int total = 0;
        UnistarServiceInstance instance = null;
        List<UnistarServiceInstance> weightInstances = new ArrayList<>();
        for (ServiceInstance ins : instances) {
            if (ins instanceof UnistarServiceInstance) {
                instance = (UnistarServiceInstance) ins;
                total += instance.getWeight();
                weightInstances.add(instance);
            }
        }
        int random = ThreadLocalRandom.current().nextInt(total);
        int idx = 0;
        for (; idx < weightInstances.size(); idx++) {
            instance = weightInstances.get(idx);
            if (instance.getWeight() <= random) break;
            random -= instance.getWeight();
        }

        if (instance == null) return null;
        logger.debug("choose instance: {}:{} from {} instances", instance.getHost(), instance.getPort(), instances.size());
        return instance;
    }

}
