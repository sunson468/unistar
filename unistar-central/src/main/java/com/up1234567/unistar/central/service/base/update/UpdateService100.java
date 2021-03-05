package com.up1234567.unistar.central.service.base.update;

import com.up1234567.unistar.central.data.base.BaseGroup;
import com.up1234567.unistar.central.data.base.BaseOperator;
import com.up1234567.unistar.central.data.base.BaseSetting;
import com.up1234567.unistar.central.data.cent.Schedule;
import com.up1234567.unistar.central.service.base.IUpdateService;
import com.up1234567.unistar.central.service.base.impl.BaseService;
import com.up1234567.unistar.central.service.base.impl.OperatorService;
import com.up1234567.unistar.central.service.cent.impl.ScheduleService;
import com.up1234567.unistar.central.data.base.BaseNamespace;
import com.up1234567.unistar.central.service.base.IOperatorService;
import com.up1234567.unistar.central.service.cent.IScheduleInnerService;
import com.up1234567.unistar.central.support.auth.EAuthRole;
import com.up1234567.unistar.common.IUnistarConst;
import com.up1234567.unistar.common.util.DateUtil;
import com.up1234567.unistar.common.util.SecurityUtil;
import lombok.CustomLog;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@CustomLog(topic = IUnistarConst.DEFAULT_LOGGER_UNISTAR)
@Service
public class UpdateService100 implements IUpdateService {

    @Autowired
    private BaseService baseService;

    @Autowired
    private OperatorService operatorService;

    @Autowired
    private ScheduleService scheduleService;

    @Override
    public String version() {
        return "1.0.0";
    }

    @Override
    public void update() {
        // 初始化设置
        log.info("初始化管理员");
        initSetting();
        log.info("初始化管理员 OK");
        // 初始化基础
        log.info("初始化管理员");
        initAdmin();
        log.info("初始化管理员 OK");
        //
        log.info("初始化默认空间");
        initDefaultNS();
        log.info("初始化默认空间 OK");
        //
        log.info("初始化默认分组");
        initDefaultGroup();
        log.info("初始化默认分组 OK");
        //
        log.info("初始化默认计划");
        initInnerSchedule();
        log.info("初始化默认计划 OK");
    }

    private void initSetting() {
        BaseSetting setting = baseService.findSetting();
        if (setting != null) return;
        setting = new BaseSetting();
        setting.setConfigkey(SecurityUtil.md5(RandomStringUtils.random(16)).substring(8, 24));
        baseService.createSetting(setting);
    }

    private void initAdmin() {
        BaseOperator admin = operatorService.findOperator(IOperatorService.SUPER_ADMIN);
        if (admin != null) return;
        admin = new BaseOperator();
        admin.setAccount(IOperatorService.SUPER_ADMIN);
        admin.setNick("Super Admin");
        admin.setPassword(BaseOperator.encrypt("123456"));
        admin.addRole(EAuthRole.SUPER);
        admin.setStatus(BaseOperator.EStatus.OK);
        admin.setCreateTime(DateUtil.now());
        operatorService.createOperator(admin);
    }

    private void initDefaultNS() {
        List<BaseNamespace> namespaces = baseService.listNamespace();
        if (CollectionUtils.isNotEmpty(namespaces)) return;
        BaseNamespace namespace = new BaseNamespace();
        namespace.setNamespace(IUnistarConst.DEFAULT_NS);
        namespace.setRemark("Unistar default namespace");
        baseService.createNamespace(namespace);
    }

    private void initDefaultGroup() {
        List<BaseGroup> groups = baseService.listGroup();
        if (CollectionUtils.isNotEmpty(groups)) return;
        BaseGroup group = new BaseGroup();
        group.setGroup(IUnistarConst.DEFAULT_GROUP);
        group.setRemark("Unistar default group");
        baseService.createGroup(group);
    }

    private void initInnerSchedule() {
        List<Schedule> schedules = scheduleService.allSchedules();
        if (CollectionUtils.isNotEmpty(schedules)) return;
        // 移除超期的Task记录，凌晨2:30执行一次
        scheduleService.createInnerSchedule(IScheduleInnerService.SCHEDULE_REMOVE_TASK, "0 30 2 * * ?", "remove out-of-date trace records");
        // 移除超期的Trace记录，凌晨3:00执行一次
        scheduleService.createInnerSchedule(IScheduleInnerService.SCHEDULE_REMOVE_TRACE, "0 0 3 * * ?", "remove out-of-date trace records");
        // 同步手工节点的状态，每隔1分钟执行一次
        scheduleService.createInnerSchedule(IScheduleInnerService.SCHEDULE_NODE_HEARTBEAT, "30 0/1 * * * ?", "heartbeat to sync manual node status");
    }

    @Override
    public void fallback() {
        //
    }

}

