package com.up1234567.unistar.central.service.base.impl;

import com.up1234567.unistar.central.data.base.BaseOperator;
import com.up1234567.unistar.central.data.base.BaseOperatorApp;
import com.up1234567.unistar.central.data.base.BaseOperatorLog;
import com.up1234567.unistar.central.support.data.IUnistarDao;
import com.up1234567.unistar.central.support.data.extend.UnistarPageCondition;
import com.up1234567.unistar.central.support.data.extend.UnistarPageResult;
import com.up1234567.unistar.common.util.DateUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class OperatorService {

    @Autowired
    private IUnistarDao unistarDao;

    /**
     * @param account
     * @return
     */
    public BaseOperator findOperator(String account) {
        return unistarDao.findOneByProp("account", account, BaseOperator.class);
    }

    /**
     * @param operator
     */
    public void createOperator(BaseOperator operator) {
        unistarDao.insert(operator);
    }

    /**
     * @return
     */
    public List<BaseOperator> listOperator() {
        return unistarDao.listAll(BaseOperator.class);
    }

    /**
     * @param account
     * @param password
     */
    public void updateOperatorPassword(String account, String password) {
        Map<String, Object> updates = new HashMap<>();
        updates.put("inited", true);
        updates.put("password", BaseOperator.encrypt(password));
        unistarDao.updateOneByProp("account", account, updates, BaseOperator.class);
    }

    /**
     * @param account
     * @param ip
     */
    public void updateOperatorLastLogin(String account, String ip) {
        Map<String, Object> updates = new HashMap<>();
        updates.put("lastLoginIp", ip);
        updates.put("lastLoginTime", DateUtil.now());
        unistarDao.updateOneByProp("account", account, updates, BaseOperator.class);
    }

    /**
     * @param account
     * @param status
     */
    public void updateOperatorStatus(String account, BaseOperator.EStatus status) {
        Map<String, Object> updates = new HashMap<>();
        updates.put("status", status);
        unistarDao.updateOneByProp("account", account, updates, BaseOperator.class);
    }

    /**
     * @param operator
     */
    public void updateOperatorInfo(BaseOperator operator) {
        Map<String, Object> updates = new HashMap<>();
        updates.put("nick", operator.getNick());
        updates.put("roles", operator.getRoles());
        updates.put("namespaces", operator.getNamespaces());
        unistarDao.updateOneByProp("account", operator.getAccount(), updates, BaseOperator.class);
    }

    /**
     * @param account
     */
    public void removeOperatorApp(String account) {
        unistarDao.removeByProp("account", account, BaseOperatorApp.class);
    }

    /**
     * @param account
     * @param namespace
     * @param appname
     */
    public void addOperatorApp(String account, String namespace, String appname) {
        BaseOperatorApp operatorApp = new BaseOperatorApp();
        operatorApp.setAccount(account);
        operatorApp.setNamespace(namespace);
        operatorApp.setAppname(appname);
        unistarDao.insert(operatorApp);
    }

    /**
     * @param account
     * @param namespace
     * @return
     */
    public List<String> listOperatorApp(String account, String namespace) {
        Map<String, Object> props = new HashMap<>();
        props.put("account", account);
        props.put("namespace", namespace);
        return unistarDao.listByProps(props, BaseOperatorApp.class).stream().map(BaseOperatorApp::getAppname).collect(Collectors.toList());
    }

    /**
     * @param account
     * @return
     */
    public List<BaseOperatorApp> listOperatorApp(String account) {
        return unistarDao.listByProp("account", account, BaseOperatorApp.class);
    }

    @Async
    public void addOperatorLog(String account, String path, String ip, long now) {
        BaseOperatorLog log = new BaseOperatorLog();
        log.setAccount(account);
        log.setPath(path);
        log.setIp(ip);
        log.setCreateTime(now);
        unistarDao.insert(log);
    }

    /**
     * @param condition
     * @return
     */
    public UnistarPageResult<BaseOperatorLog> listOperatorLog(UnistarPageCondition condition) {
        condition.addSort("createTime", "DESC");
        return unistarDao.listByPageCondition(condition, BaseOperatorLog.class);
    }
}
