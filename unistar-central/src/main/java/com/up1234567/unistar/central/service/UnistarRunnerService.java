package com.up1234567.unistar.central.service;

import com.up1234567.unistar.central.support.core.clust.IUnistarClustRunner;
import com.up1234567.unistar.central.data.cent.Central;
import com.up1234567.unistar.central.data.us.AppNode;
import com.up1234567.unistar.central.service.base.impl.UpdateService;
import com.up1234567.unistar.central.service.cent.impl.CentralCacheService;
import com.up1234567.unistar.central.service.cent.impl.CentralService;
import com.up1234567.unistar.central.service.cent.impl.ScheduleInnerService;
import com.up1234567.unistar.central.service.cent.impl.ScheduleService;
import com.up1234567.unistar.central.service.connect.impl.UnistarConnectCacheService;
import com.up1234567.unistar.central.service.connect.impl.UnistarConnectService;
import com.up1234567.unistar.central.support.cache.IUnistarCache;
import com.up1234567.unistar.central.support.core.UnistarNode;
import com.up1234567.unistar.central.support.core.UnistarProperties;
import com.up1234567.unistar.central.support.core.clust.UnistarClustMsg;
import com.up1234567.unistar.central.support.core.task.IUnistarTaskRunner;
import com.up1234567.unistar.common.IUnistarConst;
import com.up1234567.unistar.common.util.DateUtil;
import com.up1234567.unistar.common.util.JsonUtil;
import lombok.CustomLog;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;

@CustomLog(topic = IUnistarConst.DEFAULT_LOGGER_UNISTAR)
@Service
public class UnistarRunnerService implements IUnistarTaskRunner, IUnistarClustRunner, ApplicationListener<ApplicationReadyEvent> {

    @Autowired
    private UnistarProperties clustProperties;

    @Autowired
    private CentralService centralService;

    @Autowired
    private ScheduleService scheduleService;

    @Autowired
    private CentralCacheService centralCacheService;

    @Autowired
    private UnistarConnectService connectService;

    @Autowired
    private UpdateService updateService;

    @Autowired
    private ScheduleInnerService scheduleInnerService;

    @Autowired
    private IUnistarCache unistarCache;

    @Autowired
    private UnistarConnectCacheService unistarConnectCacheService;

    private Central central;

    private AtomicBoolean voteFlag = new AtomicBoolean(false);

    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        central = centralService.findCentral(clustProperties.getHost(), clustProperties.getPort());
        if (central == null) {
            central = new Central();
            central.setHost(clustProperties.getHost());
            central.setPort(clustProperties.getPort());
            central.setCreateTime(DateUtil.now());
            centralService.createCentral(central);
        }
        // ??????
        connectService.clean();
        //
        heartbeat(false);
    }

    @Override
    public void publish(UnistarClustMsg msg) {
        unistarCache.publish(JsonUtil.toJsonString(msg));
    }

    @Override
    public UnistarNode voteMaster(long now) {
        // ???????????????????????????
        List<Central> centrals = centralService.listCentral();
        centrals.parallelStream().forEach(Central::checkOnline);
        // ?????????master???
        if (centrals.stream().anyMatch(c -> c.isOnline() && c.isMaster())) return null;
        // ???????????????????????????
        if (!centralCacheService.canVoting(now)) return null;
        // ??????????????????
        centralCacheService.setVoting(now);
        voteFlag.set(true);
        // ?????????????????????????????????
        Optional<Central> central = centrals.stream()
                .filter(c -> c.isOnline() && c.getLastActiveTime() > 0 && c.getLastActiveTime() < now)
                .min((c1, c2) -> {
                    int ret = 0;
                    ret += c1.getLastActiveTime() < c2.getLastActiveTime() ? 1 : 0;
                    ret += c1.getMemoryMax() < c2.getMemoryMax() ? 1 : 0;
                    ret += c1.getMemoryFree() < c2.getMemoryFree() ? 1 : 0;
                    return ret;
                });
        // ??????
        return central.map(c -> new UnistarNode(c.getHost(), c.getPort())).orElse(null);
    }

    /**
     * ???????????? 5s
     *
     * @param master
     */
    @Override
    public void heartbeat(boolean master) {
        // ?????????master???
        if (master) {
            // ??????????????????
            if (voteFlag.compareAndSet(true, false)) centralCacheService.finishVoting();
            // ??????????????????
            centralCacheService.setTimering();
            // ?????????????????????master
            if (!central.isMaster()) centralService.cancelMasterCentral();
        }
        // ???????????????
        central.setOnline(true);
        central.setVersion(updateService.getCurrentVersion().getVershow());
        central.setMaster(master);
        central.setMemoryFree(Runtime.getRuntime().freeMemory());
        central.setMemoryTotal(Runtime.getRuntime().totalMemory());
        central.setMemoryMax(Runtime.getRuntime().maxMemory());
        central.setProcessors(Runtime.getRuntime().availableProcessors());
        central.setLastActiveTime(DateUtil.now());
        centralService.activeCentral(central);
    }

    /**
     * ????????????
     *
     * @param before
     */
    @Override
    public void schedulesRun(long before) {
        // ????????????????????????????????????
        scheduleService.listWaitingSchedule(before).parallelStream().forEach(s -> {
            try {
                // ???????????????????????????
                if (s.isInner()) {
                    scheduleInnerService.run(s);
                    scheduleService.checkSchedule(s);
                } else if (scheduleService.createScheduleTask(s, s.getNextTime(), null, null, null)) {
                    scheduleService.checkSchedule(s);
                }
            } catch (Exception ignore) {
            }
        });
    }

    @Override
    public void scheduleTasksRun(long before) {
        scheduleService.tasksForRun(before).parallelStream().forEach(task -> {
            // ??????????????????
            AppNode node = unistarConnectCacheService.randomOnlineTasker(task.getNamespace(), task.getTask(), task.getGroup());
            if (node == null) return;
            // ???????????????????????????????????????
            scheduleService.setTaskExecuteNode(task, node);
            // ??????????????????
            connectService.taskExcute(node, task);
        });
    }

}
