package com.up1234567.unistar.central.api;

import com.up1234567.unistar.central.api.model.BaseDataInModel;
import com.up1234567.unistar.central.api.model.BaseOutModel;
import com.up1234567.unistar.central.api.model.us.vo.AppVo;
import com.up1234567.unistar.central.api.model.user.LoginInModel;
import com.up1234567.unistar.central.api.model.user.LoginOutModel;
import com.up1234567.unistar.central.api.model.user.OpCondOutModel;
import com.up1234567.unistar.central.data.base.BaseOperatorLog;
import com.up1234567.unistar.central.service.base.impl.BaseCacheService;
import com.up1234567.unistar.central.service.base.impl.BaseService;
import com.up1234567.unistar.central.service.base.impl.OperatorService;
import com.up1234567.unistar.central.support.aop.AOperatorLog;
import com.up1234567.unistar.central.support.auth.AuthToken;
import com.up1234567.unistar.central.support.util.RequestUtil;
import com.up1234567.unistar.central.api.model.BasePageOutModel;
import com.up1234567.unistar.central.api.model.user.ChangepassInModel;
import com.up1234567.unistar.central.api.model.user.vo.OperatorVo;
import com.up1234567.unistar.central.data.base.BaseOperator;
import com.up1234567.unistar.central.data.base.BaseOperatorApp;
import com.up1234567.unistar.central.service.base.IOperatorService;
import com.up1234567.unistar.central.service.base.impl.UpdateService;
import com.up1234567.unistar.central.service.us.impl.AppService;
import com.up1234567.unistar.central.support.auth.ARoleAuth;
import com.up1234567.unistar.central.support.auth.EAuthRole;
import com.up1234567.unistar.central.support.data.extend.UnistarPageCondition;
import com.up1234567.unistar.central.support.data.extend.UnistarPageResult;
import com.up1234567.unistar.common.util.DateUtil;
import com.up1234567.unistar.common.util.StringUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping(AuthToken.PATH_PREFIX + "user")
public class UserController {

    @Autowired
    private OperatorService operatorService;

    @Autowired
    private BaseCacheService baseCacheService;

    @Autowired
    private BaseService baseService;

    @Autowired
    private AppService appService;

    @Autowired
    private UpdateService updateService;

    @PostMapping("login")
    public LoginOutModel login(HttpServletRequest request, @RequestBody LoginInModel inModel) {
        LoginOutModel retModel = new LoginOutModel();
        BaseOperator operator = operatorService.findOperator(inModel.getAccount());
        if (operator == null) {
            retModel.setRetMsg("server.notfound");
            return retModel;
        } else if (!operator.checkPass(inModel.getPassword())) {
            retModel.setRetMsg("server.mismatch");
            return retModel;
        } else if (!BaseOperator.EStatus.OK.equals(operator.getStatus())) {
            retModel.setRetMsg("server.userbanned");
            return retModel;
        }
        retModel.setAccount(operator.getAccount());
        retModel.setNick(operator.getNick());
        if (!IOperatorService.SUPER_ADMIN.equals(operator.getAccount())) {
            retModel.setEmail(operator.getAccount());
        }
        retModel.setInited(operator.isInited());
        // ==============================================
        // 生成Token
        AuthToken token = new AuthToken();
        token.setAccount(operator.getAccount());
        token.setNick(operator.getNick());
        token.setRoles(operator.asRoles());
        token.setIp(RequestUtil.clientIP(request));
        token.setAgent(RequestUtil.getRerfer(request));
        String tokenId = baseCacheService.genLoginToken(token);
        retModel.setToken(tokenId);
        retModel.setRoles(token.getRoles());
        //
        retModel.setVersion(updateService.getCurrentVersion().getVershow());
        // 更新最后登录时间
        operatorService.updateOperatorLastLogin(token.getAccount(), token.getIp());
        // 日志记录
        operatorService.addOperatorLog(token.getAccount(), AuthToken.PATH_PREFIX + "user/login", token.getIp(), DateUtil.now());
        //
        retModel.setRetCode(BaseOutModel.RET_OK);
        return retModel;
    }

    @AOperatorLog
    @ARoleAuth(EAuthRole.ALL)
    @PostMapping("changepass")
    public BaseOutModel changepass(@RequestAttribute(AuthToken.REQ_AUTH) AuthToken authToken, @RequestBody ChangepassInModel inModel) {
        BaseOutModel retModel = new BaseOutModel();
        //
        BaseOperator operator = operatorService.findOperator(authToken.getAccount());
        if (operator == null) return retModel;
        //
        if (!operator.checkPass(inModel.getOldpass())) {
            retModel.setRetMsg("server.mismatch");
            return retModel;
        }
        //
        operatorService.updateOperatorPassword(authToken.getAccount(), inModel.getNewpass());
        //
        retModel.setRetCode(BaseOutModel.RET_OK);
        return retModel;

    }

    @ARoleAuth(EAuthRole.SUPER)
    @PostMapping("ops")
    public BasePageOutModel ops(@RequestAttribute(AuthToken.REQ_AUTH) AuthToken authToken) {
        BasePageOutModel retModel = new BasePageOutModel();
        List<BaseOperator> operators = operatorService.listOperator();
        List<OperatorVo> operatorVos = new ArrayList<>();
        operators.stream().filter(o -> !o.isSuper()).forEach(o -> operatorVos.add(OperatorVo.wrap(o)));
        retModel.setData(operatorVos);
        retModel.setRetCode(BaseOutModel.RET_OK);
        return retModel;
    }

    @AOperatorLog
    @ARoleAuth(EAuthRole.SUPER)
    @PostMapping("status")
    public BasePageOutModel status(@RequestAttribute(AuthToken.REQ_AUTH) AuthToken authToken, @RequestBody BaseDataInModel<String> inModel) {
        BasePageOutModel retModel = new BasePageOutModel();
        BaseOperator operator = operatorService.findOperator(inModel.getData());
        if (IOperatorService.SUPER_ADMIN.equals(operator.getAccount())) return retModel;
        if (BaseOperator.EStatus.OK.equals(operator.getStatus())) {
            operator.setStatus(BaseOperator.EStatus.BANNED);
        } else {
            operator.setStatus(BaseOperator.EStatus.OK);
        }
        operatorService.updateOperatorStatus(operator.getAccount(), operator.getStatus());
        retModel.setRetCode(BaseOutModel.RET_OK);
        return retModel;
    }

    @ARoleAuth(EAuthRole.ALL)
    @PostMapping("opcond")
    public OpCondOutModel opcond(@RequestAttribute(AuthToken.REQ_AUTH) AuthToken authToken) {
        OpCondOutModel retModel = new OpCondOutModel();
        List<String> roles = new ArrayList<>();
        roles.add(EAuthRole.SUPER.name());
        roles.add(EAuthRole.APPER.name());
        roles.add(EAuthRole.SCHEDULER.name());
        retModel.setRoles(roles);
        // 空间
        List<String> namespaces = new ArrayList<>();
        baseService.listNamespace().forEach(ns -> namespaces.add(ns.getNamespace()));
        retModel.setNamespaces(namespaces);
        //
        retModel.setRetCode(BaseOutModel.RET_OK);
        return retModel;
    }

    @ARoleAuth(EAuthRole.ALL)
    @PostMapping("opcondapp")
    public OpCondOutModel opcondApp(@RequestAttribute(AuthToken.REQ_AUTH) AuthToken authToken, @RequestBody BaseDataInModel<String> inModel) {
        OpCondOutModel retModel = new OpCondOutModel();
        //
        List<AppVo> apps = new ArrayList<>();
        List<String> namespaces = StringUtil.fromCommaString(inModel.getData());
        appService.listApp(namespaces).forEach(app -> {
            AppVo vo = new AppVo();
            vo.setNamespace(app.getNamespace());
            vo.setName(app.getName());
            apps.add(vo);
        });
        retModel.setApps(apps);
        //
        retModel.setOpApps(operatorService.listOperatorApp(authToken.getAccount()).stream().map(app -> app.getNamespace() + BaseOperatorApp.SPLIT + app.getAppname()).collect(Collectors.toList()));
        //
        retModel.setRetCode(BaseOutModel.RET_OK);
        return retModel;
    }

    @AOperatorLog
    @ARoleAuth(EAuthRole.SUPER)
    @PostMapping("opedit")
    public BaseOutModel opedit(@RequestAttribute(AuthToken.REQ_AUTH) AuthToken authToken, @RequestBody BaseDataInModel<OperatorVo> inModel) {
        BaseOutModel retModel = new BaseOutModel();
        OperatorVo vo = inModel.getData();
        BaseOperator operator = operatorService.findOperator(vo.getAccount());
        if (operator == null) {
            operator = new BaseOperator();
            operator.setAccount(vo.getAccount());
            // 初始密码123456
            operator.setPassword(BaseOperator.encrypt("123456"));
            operator.setStatus(BaseOperator.EStatus.BANNED); // 默认禁用
            operatorService.createOperator(operator);
        } else if (IOperatorService.SUPER_ADMIN.equals(operator.getAccount())) {
            return retModel;
        }
        operator.setNick(vo.getNick());
        operator.setNamespaces(vo.getNamespaces());
        operator.setRoles(vo.getRoles());
        operatorService.updateOperatorInfo(operator);
        //
        operatorService.removeOperatorApp(vo.getAccount());
        //
        if (StringUtils.isNotEmpty(vo.getApps())) {
            StringUtil.fromCommaString(vo.getApps()).forEach(str -> {
                String[] arr = str.split(BaseOperatorApp.SPLIT);
                operatorService.addOperatorApp(vo.getAccount(), arr[0], arr[1]);
            });
        }
        //
        retModel.setRetCode(BaseOutModel.RET_OK);
        return retModel;
    }

    @ARoleAuth(EAuthRole.SUPER)
    @PostMapping("oplog")
    public BasePageOutModel oplogs(@RequestAttribute(AuthToken.REQ_AUTH) AuthToken authToken, @RequestBody UnistarPageCondition condition) {
        BasePageOutModel retModel = new BasePageOutModel();
        condition.setCountable(true);
        UnistarPageResult<BaseOperatorLog> operatorLogs = operatorService.listOperatorLog(condition);
        retModel.setData(operatorLogs.getData());
        retModel.setCount(operatorLogs.getCount());
        retModel.setMore(operatorLogs.more());
        //
        retModel.setRetCode(BaseOutModel.RET_OK);
        return retModel;
    }

}
