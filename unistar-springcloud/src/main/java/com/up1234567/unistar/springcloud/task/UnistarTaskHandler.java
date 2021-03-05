package com.up1234567.unistar.springcloud.task;

import com.up1234567.unistar.springcloud.UnistarProperties;
import com.up1234567.unistar.springcloud.core.event.IUnistarClientDispatcher;
import com.up1234567.unistar.springcloud.discover.trace.UnistarTraceContext;
import com.up1234567.unistar.common.IUnistarConst;
import com.up1234567.unistar.common.async.PoolExecutorService;
import com.up1234567.unistar.common.event.IUnistarEventConst;
import com.up1234567.unistar.common.exception.UnistarNotSupportException;
import com.up1234567.unistar.common.task.UnistarTaskData;
import com.up1234567.unistar.common.task.UnistarTaskParam;
import com.up1234567.unistar.common.util.JsonUtil;
import lombok.CustomLog;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@CustomLog(topic = IUnistarConst.DEFAULT_LOGGER_UNISTAR)
public class UnistarTaskHandler implements ApplicationContextAware {

    private Map<String, IUnistarTasker> unistarTaskerMap = new HashMap<>();
    private UnistarProperties unistarProperties;
    private IUnistarClientDispatcher unistarEventPublisher;

    private final PoolExecutorService executor;

    public UnistarTaskHandler(UnistarProperties unistarProperties, IUnistarClientDispatcher unistarEventPublisher) {
        this.unistarProperties = unistarProperties;
        this.unistarEventPublisher = unistarEventPublisher;
        this.executor = new PoolExecutorService(unistarProperties.getTask().getParallel());
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        Map<String, IUnistarTasker> taskers = applicationContext.getBeansOfType(IUnistarTasker.class);
        List<String> tasks = new ArrayList<>();
        for (Map.Entry<String, IUnistarTasker> entry : taskers.entrySet()) {
            IUnistarTasker task = entry.getValue();
            if (unistarTaskerMap.containsKey(task.task())) {
                throw new UnistarNotSupportException("unistar task can only be one handler.");
            }
            unistarTaskerMap.put(task.task(), task);
            // =======================================================
            tasks.add(task.task());
        }
        // 不为空则代表存在
        if (!CollectionUtils.isEmpty(tasks)) {
            log.debug("task is ready for handle of: {}", tasks);
            UnistarTaskParam taskParam = new UnistarTaskParam();
            taskParam.setAvailable(unistarProperties.getTask().isAvailable());
            taskParam.setTasks(tasks);
            unistarEventPublisher.readyParam().setTaskParam(taskParam);
        }
    }

    /**
     * @param task
     */
    public void handle(UnistarTaskData task) {
        // 异步化执行，任务执行器单步执行
        executor.execute(() -> {
            Map<String, Object> result = new HashMap<>();
            try {
                UnistarTraceContext.setContextGroup("Unistar Tasker");
                IUnistarTasker tasker = unistarTaskerMap.get(task.getName());
                if (tasker == null) {
                    log.warn("unistar task {} has none handler", task.getName());
                    result.put("error", "no handler");
                    callback(task, UnistarTaskData.EStatus.FAILED, result);
                } else {
                    Map<String, Object> retMap = tasker.handle(task.getParams());
                    if (!CollectionUtils.isEmpty(retMap)) result.putAll(retMap);
                    callback(task, UnistarTaskData.EStatus.FINISHED, result);
                }
            } catch (Exception e) {
                result.put("error", e.getMessage());
                callback(task, UnistarTaskData.EStatus.FAILED, result);
            }
        });
    }

    /**
     * 回调
     *
     * @param task
     * @param status
     * @param result
     */
    private void callback(UnistarTaskData task, UnistarTaskData.EStatus status, Map<String, Object> result) {
        task.setParams(null);
        task.setStatus(status);
        task.setResult(JsonUtil.toJsonString(result));
        unistarEventPublisher.publish(IUnistarEventConst.HANDLE_TASK_STATUS, task);
    }

}
