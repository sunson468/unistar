package com.up1234567.unistar.central.service.cent.impl;

import com.up1234567.unistar.central.data.cent.Schedule;
import com.up1234567.unistar.central.data.cent.ScheduleTask;
import com.up1234567.unistar.central.data.us.AppNode;
import com.up1234567.unistar.central.service.cent.IScheduleInnerService;
import com.up1234567.unistar.central.support.data.IUnistarDao;
import com.up1234567.unistar.central.support.data.extend.UnistarPageCondition;
import com.up1234567.unistar.central.support.data.extend.UnistarPageResult;
import com.up1234567.unistar.common.IUnistarConst;
import com.up1234567.unistar.common.ds.AtomicCounter;
import com.up1234567.unistar.common.task.UnistarTaskData;
import com.up1234567.unistar.common.util.DateUtil;
import com.up1234567.unistar.common.util.JsonUtil;
import com.up1234567.unistar.common.util.SecurityUtil;
import com.up1234567.unistar.common.util.StringUtil;
import lombok.CustomLog;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.*;

@CustomLog(topic = IUnistarConst.DEFAULT_LOGGER_UNISTAR)
@Service
public class ScheduleService {

    @Autowired
    private IUnistarDao unistarDao;

    @Autowired
    private CentralCacheService centralCacheService;

    private String uuid;
    private AtomicCounter atomicScheduleTask = AtomicCounter.newCounter(99999);

    @PostConstruct
    public void init() {
        randomUUID();
    }

    private void randomUUID() {
        uuid = UUID.randomUUID().toString() + StringUtil.H_LINE;
    }

    /**
     * 生成NO
     *
     * @param idCounter
     * @return
     */
    private String genNo(AtomicCounter idCounter) {
        if (idCounter.isArrived()) randomUUID();
        return SecurityUtil.md5(uuid + idCounter.current());
    }

    /**
     * @param schedule
     */
    public void createSchedule(Schedule schedule) {
        if (schedule.getStatus() == null) schedule.setStatus(Schedule.EStatus.OFF);
        schedule.setCreateTime(DateUtil.now());
        unistarDao.insert(schedule);
    }

    /**
     * @param namespace
     * @param name
     * @return
     */
    public Schedule findSchedule(String namespace, String name) {
        Map<String, Object> props = new HashMap<>();
        props.put("namespace", namespace);
        props.put("name", name);
        return unistarDao.findOneByProps(props, Schedule.class);
    }

    /**
     * @param namespace
     * @param name      计划名称
     * @return
     */
    public Schedule findScheduleFromCache(String namespace, String name) {
        Schedule schedule = centralCacheService.findSchedule(namespace, name);
        if (schedule != null) return schedule;
        schedule = findSchedule(namespace, name);
        if (schedule == null) return null;
        centralCacheService.saveSchedule(schedule);
        return schedule;
    }

    /**
     * 获取所有计划
     *
     * @return
     */
    public List<Schedule> allSchedules() {
        return unistarDao.listAll(Schedule.class);
    }

    /**
     * @param name
     * @param cron
     * @param remark
     */
    public void createInnerSchedule(String name, String cron, String remark) {
        Schedule schedule = new Schedule();
        schedule.setName(name);
        schedule.setPost(false);
        schedule.setCron(cron);
        schedule.setRemark(remark);
        schedule.setNamespace(IScheduleInnerService.SCHEDULE_NS);
        schedule.setStartTime(DateUtil.today());
        schedule.setEndTime(Long.MAX_VALUE);
        schedule.resetNextTime();
        schedule.setInner(true);
        schedule.setStatus(Schedule.EStatus.ON);
        createSchedule(schedule);
    }

    /**
     * 查询计划
     *
     * @return
     */
    public List<Schedule> listSchedule(String namespace) {
        Map<String, Object> props = new HashMap<>();
        props.put("namespace", namespace);
        return unistarDao.listByProps(props, Schedule.class);
    }

    /**
     * 等待排期中的计划
     *
     * @return
     */
    public List<Schedule> listWaitingSchedule(long now) {
        Map<String, Object> props = new HashMap<>();
        props.put("post", false);
        props.put("status", Schedule.EStatus.ON);
        props.put("nextTime<", now);
        props.put("startTime<=", now);
        props.put("endTime>=", DateUtil.dayStart(now));
        return unistarDao.listByProps(props, Schedule.class);
    }

    /**
     * @param schedule
     */
    public void updateSchedule(Schedule schedule) {
        Map<String, Object> props = new HashMap<>();
        props.put("namespace", schedule.getNamespace());
        props.put("name", schedule.getName());
        Map<String, Object> updates = new HashMap<>();
        updates.put("post", schedule.isPost());
        updates.put("cron", schedule.getCron());
        updates.put("remark", schedule.getRemark());
        updates.put("group", schedule.getGroup());
        updates.put("params", schedule.getParams());
        updates.put("parallel", schedule.getParallel());
        updates.put("postSchedules", schedule.getPostSchedules());
        updates.put("startTime", schedule.getStartTime());
        updates.put("endTime", schedule.getEndTime());
        updates.put("nextTime", schedule.getNextTime());
        unistarDao.updateOneByProps(props, updates, Schedule.class);
        // 移除缓存
        centralCacheService.delSchedule(schedule.getNamespace(), schedule.getName());
    }

    /**
     * 校验并更新计划
     *
     * @param schedule
     */
    @Async
    public void checkSchedule(Schedule schedule) {
        if (schedule.isValid()) {
            // 更新下一次执行时间
            schedule.resetNextTime();
        } else {
            schedule.setStatus(Schedule.EStatus.OFF);
        }
        updateScheduleStatus(schedule);
    }

    /**
     * @param schedule
     */
    public void updateScheduleStatus(Schedule schedule) {
        Map<String, Object> props = new HashMap<>();
        props.put("namespace", schedule.getNamespace());
        props.put("name", schedule.getName());
        Map<String, Object> updates = new HashMap<>();
        updates.put("status", schedule.getStatus());
        updates.put("nextTime", schedule.getNextTime());
        unistarDao.updateOneByProps(props, updates, Schedule.class);
    }

    /**
     * 根据任务编号获取任务
     *
     * @param no
     * @return
     */
    public ScheduleTask findTask(String no) {
        return unistarDao.findOneByProp("no", no, ScheduleTask.class);
    }

    /**
     * 创建任务
     *
     * @param schedule
     * @param preNo
     * @param preResult
     */
    public boolean createScheduleTask(Schedule schedule, long excuteTime, String preSchedule, String preNo, String preResult) {
        if (schedule == null) return false;
        // 检测是否存在还未执行完成的任务
        if (waitedTask(schedule) != null) return false;
        if (schedule.getParallel() < 1) schedule.setParallel(1);
        boolean parallel = schedule.getParallel() > 1;
        // ===========================================================
        for (int i = 0; i < schedule.getParallel(); i++) {
            ScheduleTask task = new ScheduleTask();
            task.setNo(genNo(atomicScheduleTask));
            task.setNamespace(schedule.getNamespace());
            task.setSchedule(schedule.getName());
            task.setExecuteTime(excuteTime);
            task.setPreSchedule(preSchedule);
            task.setPreNo(preNo);
            task.setPreResult(preResult);
            task.setTask(schedule.getTask());
            Map<String, Object> params = JsonUtil.toMap(schedule.getParams());
            if (MapUtils.isEmpty(params)) params = new HashMap<>();
            if (StringUtils.isNotEmpty(preResult)) params.putAll(JsonUtil.toMap(preResult));
            if (parallel) params.put("_parallel", i); // 并发编号作为参数
            task.setParams(JsonUtil.toJsonString(params));
            task.setType(ScheduleTask.EType.AUTO);
            task.setStatus(UnistarTaskData.EStatus.WAIT);
            task.setCreateTime(DateUtil.now());
            unistarDao.insert(task);
        }
        return true;
    }

    /**
     * 查找所有可运行的任务
     *
     * @param executeTime
     * @return
     */
    public List<ScheduleTask> tasksForRun(long executeTime) {
        Map<String, Object> props = new HashMap<>();
        props.put("status", UnistarTaskData.EStatus.WAIT);
        props.put("executeTime<=", executeTime);
        return unistarDao.listByProps(props, ScheduleTask.class);
    }

    /**
     * 分页查询任务执行记录
     *
     * @param namespace
     * @param schedule
     * @param page
     * @return
     */
    public UnistarPageResult<ScheduleTask> listTaskByPage(String namespace, String schedule, int page, int size) {
        UnistarPageCondition condition = new UnistarPageCondition();
        condition.setPage(page);
        condition.setLimit(size);
        condition.addFilter("namespace", namespace);
        condition.addFilter("schedule", schedule);
        return listTaskByPage(condition);
    }

    /**
     * 分页查询任务执行记录
     *
     * @param condition
     * @return
     */
    public UnistarPageResult<ScheduleTask> listTaskByPage(UnistarPageCondition condition) {
        condition.addSort("startTime", UnistarPageCondition.SORT_DESC);
        return unistarDao.listByPageCondition(condition, ScheduleTask.class);
    }

    /**
     * 查找后续任务
     *
     * @param preNo
     * @return
     */
    public List<ScheduleTask> afterTasks(String preNo) {
        Map<String, Object> props = new HashMap<>();
        props.put("preNo", preNo);
        return unistarDao.listByProps(props, ScheduleTask.class);
    }

    /**
     * 选择执行的节点
     *
     * @param task
     */
    public void setTaskExecuteNode(ScheduleTask task, AppNode node) {
        // 选择一个节点
        Schedule schedule = findScheduleFromCache(task.getNamespace(), task.getSchedule());
        // 如果是等待执行的计划，则重置
        // 此处逻辑是应对当系统停滞过久，导致任务执行周期过去多轮，当系统启动时，会造成任务执行重复执行
        if (schedule.isGoingRun()) checkSchedule(schedule);
        //
        task.setNamespace(schedule.getNamespace());
        task.setGroup(schedule.getGroup());
        //
        task.setAppname(node.getAppname());
        task.setGroup(node.getGroup());
        task.setHost(node.getHost());
        task.setPort(node.getPort());
        //
        Map<String, Object> updates = new HashMap<>();
        updates.put("appname", task.getAppname());
        updates.put("group", task.getGroup());
        updates.put("host", task.getHost());
        updates.put("port", task.getPort());
        updates.put("status", UnistarTaskData.EStatus.EXECUTING);
        updates.put("startTime", DateUtil.now());
        //
        unistarDao.updateOneByProp("no", task.getNo(), updates, ScheduleTask.class);
    }

    /**
     * 计划待执行的任务
     *
     * @param schedule
     * @return
     */
    public ScheduleTask waitedTask(Schedule schedule) {
        Map<String, Object> props = new HashMap<>();
        props.put("namespace", schedule.getNamespace());
        props.put("schedule", schedule.getName());
        props.put("status", Arrays.asList(UnistarTaskData.EStatus.WAIT, UnistarTaskData.EStatus.EXECUTING, UnistarTaskData.EStatus.OVERTIME, UnistarTaskData.EStatus.FAILED));
        return unistarDao.findOneByProps(props, ScheduleTask.class);
    }

    /**
     * 任务以新的方式执行
     *
     * @param task
     */
    public void retryTaskAs(ScheduleTask task) {
        Map<String, Object> updates = new HashMap<>();
        updates.put("type", task.getType());
        updates.put("group", StringUtil.EMPTY);
        updates.put("host", StringUtil.EMPTY);
        updates.put("port", 0);
        updates.put("result", StringUtil.EMPTY);
        updates.put("status", UnistarTaskData.EStatus.WAIT);
        updates.put("startTime", DateUtil.now());
        updates.put("finishTime", 0);
        unistarDao.updateOneByProp("no", task.getNo(), updates, ScheduleTask.class);
    }

    /**
     * 任务执行结束了，成功或失败
     *
     * @param task
     */
    public void updateTaskStatus(ScheduleTask task) {
        Map<String, Object> updates = new HashMap<>();
        updates.put("status", task.getStatus());
        unistarDao.updateOneByProp("no", task.getNo(), updates, ScheduleTask.class);
    }

    /**
     * 任务执行结束了，成功或失败
     *
     * @param task
     */
    public void taskFinished(ScheduleTask task) {
        Map<String, Object> updates = new HashMap<>();
        updates.put("status", task.getStatus());
        updates.put("finishTime", task.getFinishTime());
        updates.put("result", task.getResult());
        unistarDao.updateOneByProp("no", task.getNo(), updates, ScheduleTask.class);
    }

    /**
     * @param day
     */
    public void removeOverTask(long day) {
        Map<String, Object> props = new HashMap<>();
        props.put("executeTime<", DateUtil.now() - day * DateUtil.DAY);
        props.put("status", Arrays.asList(UnistarTaskData.EStatus.FINISHED, UnistarTaskData.EStatus.CANCELED));
        unistarDao.removeByProps(props, ScheduleTask.class);
    }
}
