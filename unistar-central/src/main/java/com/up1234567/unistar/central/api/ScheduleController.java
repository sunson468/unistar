package com.up1234567.unistar.central.api;

import com.up1234567.unistar.central.api.model.BaseDataInModel;
import com.up1234567.unistar.central.api.model.BaseInModel;
import com.up1234567.unistar.central.api.model.BaseOutModel;
import com.up1234567.unistar.central.api.model.cent.vo.ScheduleVo;
import com.up1234567.unistar.central.service.cent.impl.CentralCacheService;
import com.up1234567.unistar.central.service.cent.impl.ScheduleService;
import com.up1234567.unistar.central.support.aop.AOperatorLog;
import com.up1234567.unistar.central.support.auth.AuthToken;
import com.up1234567.unistar.central.api.model.BasePageOutModel;
import com.up1234567.unistar.central.api.model.cent.ScheduleNearTaskModel;
import com.up1234567.unistar.central.api.model.cent.vo.ScheduleTaskVo;
import com.up1234567.unistar.central.data.cent.Schedule;
import com.up1234567.unistar.central.data.cent.ScheduleTask;
import com.up1234567.unistar.central.support.auth.ARoleAuth;
import com.up1234567.unistar.central.support.auth.EAuthRole;
import com.up1234567.unistar.central.support.data.extend.UnistarPageCondition;
import com.up1234567.unistar.central.support.data.extend.UnistarPageResult;
import com.up1234567.unistar.central.support.util.CronUtil;
import com.up1234567.unistar.common.task.UnistarTaskData;
import com.up1234567.unistar.common.util.DateUtil;
import com.up1234567.unistar.common.util.StringUtil;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

@RestController
@ARoleAuth(EAuthRole.SCHEDULER)
@RequestMapping(AuthToken.PATH_PREFIX + "schedule")
public class ScheduleController {

    @Autowired
    private ScheduleService scheduleService;

    @Autowired
    private CentralCacheService centralCacheService;

    @PostMapping("list")
    public BaseOutModel list(@RequestAttribute(AuthToken.REQ_AUTH) AuthToken authToken, @RequestBody BaseInModel inModel) {
        BaseOutModel retModel = new BaseOutModel();
        List<ScheduleVo> scheduleVos = new ArrayList<>();
        scheduleService.listSchedule(inModel.getNamespace())
                .stream().filter(s -> !s.isInner())
                .forEach(o -> {
                    ScheduleVo vo = ScheduleVo.wrap(o);
                    scheduleVos.add(vo);
                });
        retModel.setData(scheduleVos);
        retModel.setRetCode(BaseOutModel.RET_OK);
        return retModel;
    }

    @AOperatorLog
    @PostMapping("edit")
    public BaseOutModel edit(@RequestAttribute(AuthToken.REQ_AUTH) AuthToken authToken, @RequestBody BaseDataInModel<ScheduleVo> inModel) {
        BaseOutModel retModel = new BaseOutModel();
        ScheduleVo vo = inModel.getData();
        //
        if (!vo.isPost() && !CronUtil.validCron(vo.getCron())) {
            retModel.setRetMsg("page.schedule.form.cronless5sec");
            return retModel;
        }
        // 计划
        Schedule schedule = scheduleService.findSchedule(inModel.getNamespace(), vo.getName());
        if (schedule == null) {
            schedule = new Schedule();
            schedule.setNamespace(inModel.getNamespace());
            schedule.setName(vo.getName());
            schedule.setTask(vo.getTask());
            scheduleService.createSchedule(schedule);
        }
        schedule.setPost(vo.isPost());
        if (schedule.isPost()) {
            schedule.setCron(StringUtil.EMPTY);
        } else {
            schedule.setCron(vo.getCron());
        }
        schedule.setRemark(vo.getRemark());
        schedule.setGroup(StringUtil.withDefault(vo.getGroup()));
        schedule.setTask(vo.getTask());
        schedule.setParams(StringUtil.withDefault(vo.getParams()));
        schedule.setParallel(vo.getParallel());
        schedule.setPostSchedules(StringUtil.toCommaString(vo.getPostSchedules()));
        if (StringUtils.isNotEmpty(vo.getStartTime())) {
            try {
                schedule.setStartTime(DateUtils.parseDate(vo.getStartTime(), DateUtil.FMT_YYYY_MM_DD).getTime());
            } catch (ParseException e) {
                schedule.setStartTime(DateUtil.today());
            }
        } else {
            schedule.setStartTime(DateUtil.today());
        }
        if (StringUtils.isNotEmpty(vo.getEndTime())) {
            try {
                schedule.setEndTime(DateUtils.parseDate(vo.getEndTime(), DateUtil.FMT_YYYY_MM_DD).getTime());
            } catch (ParseException e) {
                schedule.setEndTime(DateUtil.today());
            }
        } else {
            schedule.setEndTime(Long.MAX_VALUE);
        }
        // 设置下一次运行时间
        schedule.resetNextTime();
        scheduleService.updateSchedule(schedule);
        retModel.setRetCode(BaseOutModel.RET_OK);
        return retModel;
    }

    @AOperatorLog
    @PostMapping("status")
    public BaseOutModel status(@RequestAttribute(AuthToken.REQ_AUTH) AuthToken authToken, @RequestBody BaseDataInModel<String> inModel) {
        BaseOutModel retModel = new BaseOutModel();
        // 计划
        Schedule schedule = scheduleService.findSchedule(inModel.getNamespace(), inModel.getData());
        if (schedule == null) {
            retModel.setRetMsg("server.notfound");
            return retModel;
        }
        if (Schedule.EStatus.ON.equals(schedule.getStatus())) {
            schedule.setStatus(Schedule.EStatus.OFF);
        } else {
            schedule.setStatus(Schedule.EStatus.ON);
            // 设置下一次运行时间
            schedule.resetNextTime();
        }
        scheduleService.updateScheduleStatus(schedule);
        centralCacheService.delSchedule(schedule.getNamespace(), schedule.getName());
        retModel.setRetCode(BaseOutModel.RET_OK);
        return retModel;
    }

    @AOperatorLog
    @PostMapping("runnow")
    public BaseOutModel runnow(@RequestAttribute(AuthToken.REQ_AUTH) AuthToken authToken, @RequestBody BaseDataInModel<String> inModel) {
        BaseOutModel retModel = new BaseOutModel();
        // 计划
        Schedule schedule = scheduleService.findSchedule(inModel.getNamespace(), inModel.getData());
        if (schedule == null) {
            retModel.setRetMsg("server.notfound");
            return retModel;
        }
        // 即刻生成一个任务，后台执行并不影响正常的计划执行，也不受计划状态影响
        scheduleService.createScheduleTask(schedule, DateUtil.now(), null, null, null);
        //
        retModel.setRetCode(BaseOutModel.RET_OK);
        return retModel;
    }

    @PostMapping("task/latest")
    public BaseOutModel latestTasks(@RequestAttribute(AuthToken.REQ_AUTH) AuthToken authToken, @RequestBody BaseDataInModel<String> inModel) {
        BaseOutModel retModel = new BaseOutModel();
        UnistarPageResult<ScheduleTask> taskResult = scheduleService.listTaskByPage(inModel.getNamespace(), inModel.getData(), UnistarPageCondition.PAGE_1, UnistarPageCondition.SIZE_5);
        List<ScheduleTaskVo> taskVos = new ArrayList<>();
        taskResult.getData().forEach(t -> {
            if (t.checkOvertime()) {
                t.setStatus(UnistarTaskData.EStatus.OVERTIME);
                scheduleService.updateTaskStatus(t);
            }
            taskVos.add(ScheduleTaskVo.wrap(t));
        });
        retModel.setData(taskVos);
        retModel.setRetCode(BaseOutModel.RET_OK);
        return retModel;
    }

    @PostMapping("task/near")
    public ScheduleNearTaskModel nearTasks(@RequestAttribute(AuthToken.REQ_AUTH) AuthToken authToken, @RequestBody BaseDataInModel<String> inModel) {
        //  后续任务
        ScheduleNearTaskModel retModel = new ScheduleNearTaskModel();
        ScheduleTask task = scheduleService.findTask(inModel.getData());
        if (task == null) {
            return retModel;
        }
        if (StringUtils.isNotEmpty(task.getPreNo())) {
            ScheduleTask preTask = scheduleService.findTask(task.getPreNo());
            if (preTask != null) retModel.setPreTask(ScheduleTaskVo.wrap(preTask));
        }

        List<ScheduleTask> afterTasks = scheduleService.afterTasks(inModel.getData());
        if (CollectionUtils.isNotEmpty(afterTasks)) {
            List<ScheduleTaskVo> afterTaskVos = new ArrayList<>();
            afterTasks.forEach(t -> afterTaskVos.add(ScheduleTaskVo.wrap(t)));
            retModel.setAfterTasks(afterTaskVos);
        }
        retModel.setRetCode(BaseOutModel.RET_OK);
        return retModel;
    }

    @AOperatorLog
    @PostMapping("task/retry")
    public BaseOutModel retryTask(@RequestAttribute(AuthToken.REQ_AUTH) AuthToken authToken, @RequestBody BaseDataInModel<String> inModel) {
        //  后续任务
        BaseOutModel retModel = new BaseOutModel();
        ScheduleTask task = scheduleService.findTask(inModel.getData());
        if (task == null || !task.canRetry()) {
            return retModel;
        }
        task.setType(ScheduleTask.EType.REMEDY);
        scheduleService.retryTaskAs(task);
        retModel.setRetCode(BaseOutModel.RET_OK);
        return retModel;
    }

    @PostMapping("task/list")
    public BasePageOutModel listTask(@RequestAttribute(AuthToken.REQ_AUTH) AuthToken authToken, @RequestBody BaseDataInModel<UnistarPageCondition> inModel) {
        //  后续任务
        BasePageOutModel retModel = new BasePageOutModel();
        UnistarPageCondition condition = inModel.getData();
        condition.addFilter("namespace", inModel.getNamespace());
        condition.setCountable(true);
        UnistarPageResult<ScheduleTask> taskResult = scheduleService.listTaskByPage(condition);
        List<ScheduleTaskVo> taskVos = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(taskResult.getData())) {
            taskResult.getData().forEach(t -> {
                if (t.checkOvertime()) {
                    t.setStatus(UnistarTaskData.EStatus.OVERTIME);
                    scheduleService.updateTaskStatus(t);
                }
                taskVos.add(ScheduleTaskVo.wrap(t));
            });
        }
        retModel.setData(taskVos);
        retModel.setCount(taskResult.getCount());
        retModel.setRetCode(BaseOutModel.RET_OK);
        return retModel;
    }

}
