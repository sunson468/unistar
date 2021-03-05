package com.up1234567.unistar.central.service.base.impl;

import com.up1234567.unistar.central.data.base.BaseTask;
import com.up1234567.unistar.central.data.base.BaseGroup;
import com.up1234567.unistar.central.data.base.BaseNamespace;
import com.up1234567.unistar.central.data.base.BaseSetting;
import com.up1234567.unistar.central.support.data.IUnistarDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class BaseService {

    @Autowired
    private IUnistarDao unistarDao;

    private BaseSetting baseSetting;

    /**
     * @return
     */
    public BaseSetting findSetting() {
        if (baseSetting == null) baseSetting = unistarDao.findOneByProp("no", 0, BaseSetting.class);
        return baseSetting;
    }

    /**
     * @param setting
     */
    public void createSetting(BaseSetting setting) {
        unistarDao.insert(setting);
    }

    /**
     * @return
     */
    public List<BaseNamespace> listNamespace() {
        return unistarDao.listAll(BaseNamespace.class);
    }

    /**
     * 查找命名空间
     *
     * @param namespace
     * @return
     */
    public BaseNamespace findNamespace(String namespace) {
        return unistarDao.findOneByProp("namespace", namespace, BaseNamespace.class);
    }

    /**
     * @param namespace
     */
    public void createNamespace(BaseNamespace namespace) {
        unistarDao.insert(namespace);
    }

    /**
     * @param namespace
     */
    public void updateNamespace(BaseNamespace namespace) {
        Map<String, Object> updates = new HashMap<>();
        updates.put("remark", namespace.getRemark());
        unistarDao.updateOneByProp("namespace", namespace.getNamespace(), updates, BaseNamespace.class);
    }

    /**
     * @return
     */
    public List<BaseGroup> listGroup() {
        return unistarDao.listAll(BaseGroup.class);
    }

    /**
     * @param groups
     * @return
     */
    public List<BaseGroup> listGroup(Collection<String> groups) {
        return unistarDao.listByProp("group", groups, BaseGroup.class);
    }

    /**
     * @param namespace
     * @param group
     * @return
     */
    public BaseGroup findGroup(String namespace, String group) {
        Map<String, Object> props = new HashMap<>();
        props.put("namespace", namespace);
        props.put("group", group);
        return unistarDao.findOneByProps(props, BaseGroup.class);
    }

    /**
     * @param group
     */
    public void createGroup(BaseGroup group) {
        unistarDao.insert(group);
    }

    /**
     * @param group
     */
    public void updateGroup(BaseGroup group) {
        Map<String, Object> props = new HashMap<>();
        props.put("namespace", group.getNamespace());
        props.put("group", group.getGroup());
        Map<String, Object> updates = new HashMap<>();
        updates.put("remark", group.getRemark());
        unistarDao.updateOneByProps(props, updates, BaseGroup.class);
    }

    /**
     * @return
     */
    public List<BaseTask> listTask() {
        return unistarDao.listAll(BaseTask.class);
    }

    /**
     * @param namespace
     * @param task
     * @return
     */
    public BaseTask findTask(String namespace, String task) {
        Map<String, Object> props = new HashMap<>();
        props.put("namespace", namespace);
        props.put("task", task);
        return unistarDao.findOneByProps(props, BaseTask.class);
    }

    /**
     * @param task
     */
    public void createTask(BaseTask task) {
        unistarDao.insert(task);
    }

    /**
     * @param task
     */
    public void updateTask(BaseTask task) {
        Map<String, Object> props = new HashMap<>();
        props.put("namespace", task.getNamespace());
        props.put("task", task.getTask());
        Map<String, Object> updates = new HashMap<>();
        updates.put("remark", task.getRemark());
        unistarDao.updateOneByProps(props, updates, BaseTask.class);
    }

    /**
     * @param task
     */
    public void deleteTask(BaseTask task) {
        Map<String, Object> props = new HashMap<>();
        props.put("namespace", task.getNamespace());
        props.put("task", task.getTask());
        unistarDao.removeByProps(props, BaseTask.class);
    }
}
