package com.up1234567.unistar.central.api;

import com.up1234567.unistar.central.api.model.BaseDataInModel;
import com.up1234567.unistar.central.api.model.BaseOutModel;
import com.up1234567.unistar.central.data.base.BaseGroup;
import com.up1234567.unistar.central.data.base.BaseOperator;
import com.up1234567.unistar.central.data.base.BaseTask;
import com.up1234567.unistar.central.service.base.impl.BaseService;
import com.up1234567.unistar.central.service.base.impl.OperatorService;
import com.up1234567.unistar.central.support.aop.AOperatorLog;
import com.up1234567.unistar.central.support.auth.ARoleAuth;
import com.up1234567.unistar.central.support.auth.AuthToken;
import com.up1234567.unistar.central.support.auth.EAuthRole;
import com.up1234567.unistar.central.data.base.BaseNamespace;
import com.up1234567.unistar.common.util.StringUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@ARoleAuth(EAuthRole.SUPER)
@RequestMapping(AuthToken.PATH_PREFIX + "base")
public class BaseController {

    @Autowired
    private BaseService baseService;

    @Autowired
    private OperatorService operatorService;

    @PostMapping("ns/list")
    public BaseOutModel listNamespace(@RequestAttribute(AuthToken.REQ_AUTH) AuthToken authToken) {
        BaseOutModel retModel = new BaseOutModel();
        List<BaseNamespace> namespaces = baseService.listNamespace();
        if (!authToken.isSuper()) {
            BaseOperator operator = operatorService.findOperator(authToken.getAccount());
            List<String> authNamespaces = StringUtil.fromCommaString(operator.getNamespaces());
            retModel.setData(namespaces.stream().filter(ns -> authNamespaces.contains(ns.getNamespace())).collect(Collectors.toList()));
        } else {
            retModel.setData(namespaces);
        }
        retModel.setRetCode(BaseOutModel.RET_OK);
        return retModel;
    }

    @AOperatorLog
    @PostMapping("ns/edit")
    public BaseOutModel editNamespace(@RequestAttribute(AuthToken.REQ_AUTH) AuthToken authToken, @RequestBody BaseDataInModel<BaseNamespace> inModel) {
        BaseOutModel retModel = new BaseOutModel();
        BaseNamespace vo = inModel.getData();
        BaseNamespace namespace = baseService.findNamespace(vo.getNamespace());
        if (namespace == null) {
            namespace = new BaseNamespace();
            namespace.setNamespace(vo.getNamespace());
            baseService.createNamespace(namespace);
        }
        namespace.setRemark(vo.getRemark());
        baseService.updateNamespace(namespace);
        retModel.setRetCode(BaseOutModel.RET_OK);
        return retModel;
    }

    @PostMapping("group/list")
    public BaseOutModel listGroup(@RequestAttribute(AuthToken.REQ_AUTH) AuthToken authToken) {
        BaseOutModel retModel = new BaseOutModel();
        List<BaseGroup> groups = baseService.listGroup();
        retModel.setData(groups);
        retModel.setRetCode(BaseOutModel.RET_OK);
        return retModel;
    }

    @AOperatorLog
    @PostMapping("group/edit")
    public BaseOutModel editGroup(@RequestAttribute(AuthToken.REQ_AUTH) AuthToken authToken, @RequestBody BaseDataInModel<BaseGroup> inModel) {
        BaseOutModel retModel = new BaseOutModel();
        BaseGroup vo = inModel.getData();
        String namespace = StringUtil.withDefault(vo.getNamespace());
        BaseGroup group = baseService.findGroup(namespace, vo.getGroup());
        if (group == null) {
            group = new BaseGroup();
            group.setNamespace(namespace);
            group.setGroup(vo.getGroup());
            baseService.createGroup(group);
        }
        group.setRemark(vo.getRemark());
        baseService.updateGroup(group);
        retModel.setRetCode(BaseOutModel.RET_OK);
        return retModel;
    }

    @PostMapping("task/list")
    public BaseOutModel listTask(@RequestAttribute(AuthToken.REQ_AUTH) AuthToken authToken) {
        BaseOutModel retModel = new BaseOutModel();
        List<BaseTask> groups = baseService.listTask();
        retModel.setData(groups);
        retModel.setRetCode(BaseOutModel.RET_OK);
        return retModel;
    }

    @AOperatorLog
    @PostMapping("task/edit")
    public BaseOutModel editTask(@RequestAttribute(AuthToken.REQ_AUTH) AuthToken authToken, @RequestBody BaseDataInModel<BaseTask> inModel) {
        BaseOutModel retModel = new BaseOutModel();
        BaseTask vo = inModel.getData();
        String namespace = StringUtil.withDefault(vo.getNamespace());
        BaseTask task = baseService.findTask(namespace, vo.getTask());
        if (task == null) {
            task = new BaseTask();
            task.setNamespace(namespace);
            task.setTask(vo.getTask());
            baseService.createTask(task);
        }
        task.setRemark(vo.getRemark());
        baseService.updateTask(task);
        retModel.setRetCode(BaseOutModel.RET_OK);
        return retModel;
    }

    @AOperatorLog
    @PostMapping("task/del")
    public BaseOutModel delTask(@RequestAttribute(AuthToken.REQ_AUTH) AuthToken authToken, @RequestBody BaseDataInModel<BaseTask> inModel) {
        BaseOutModel retModel = new BaseOutModel();
        BaseTask vo = inModel.getData();
        BaseTask task = baseService.findTask(vo.getNamespace(), vo.getTask());
        if (task == null) {
            retModel.setRetMsg("server.notfound");
            return retModel;
        }
        baseService.deleteTask(task);
        retModel.setRetCode(BaseOutModel.RET_OK);
        return retModel;
    }

}
