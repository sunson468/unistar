package com.up1234567.unistar.central.api;


import com.up1234567.unistar.central.api.model.BaseInModel;
import com.up1234567.unistar.central.api.model.BaseOutModel;
import com.up1234567.unistar.central.api.model.dash.DashMonitorOutModel;
import com.up1234567.unistar.central.service.connect.impl.UnistarConnectCacheService;
import com.up1234567.unistar.central.service.stat.impl.StatTraceCacheService;
import com.up1234567.unistar.central.service.stat.impl.StatTraceService;
import com.up1234567.unistar.central.service.us.impl.AppService;
import com.up1234567.unistar.central.support.auth.ARoleAuth;
import com.up1234567.unistar.central.support.auth.AuthToken;
import com.up1234567.unistar.central.support.auth.EAuthRole;
import com.up1234567.unistar.central.support.core.UnistarProperties;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@ARoleAuth(EAuthRole.SUPER)
@RestController
@RequestMapping(AuthToken.PATH_PREFIX + "home")
public class HomeController {

    @Autowired
    private AppService appService;

    @Autowired
    private StatTraceService statTraceService;

    @Autowired
    private StatTraceCacheService statTraceCacheService;

    @Autowired
    private UnistarConnectCacheService unistarConnectCacheService;

    @Autowired
    private UnistarProperties unistarProperties;

    @PostMapping("dash")
    public DashMonitorOutModel index(@RequestAttribute(AuthToken.REQ_AUTH) AuthToken authToken, @RequestBody BaseInModel inModel) {
        DashMonitorOutModel retModel = new DashMonitorOutModel();
        retModel.setApps(appService.countApp(inModel.getNamespace()));
        retModel.setNodes(appService.countAppNode(inModel.getNamespace()));
        long[] stats = statTraceCacheService.globalStat(inModel.getNamespace());
        retModel.setCount(stats[0]);
        retModel.setErrors(stats[1]);
        retModel.setOnlines(unistarConnectCacheService.onlines(inModel.getNamespace()));
        retModel.setRetCode(BaseOutModel.RET_OK);
        return retModel;
    }

    @PostMapping("dashstat")
    public BaseOutModel dashstat(@RequestAttribute(AuthToken.REQ_AUTH) AuthToken authToken, @RequestBody BaseInModel inModel) {
        BaseOutModel retModel = new BaseOutModel();
        retModel.setData(CollectionUtils.union(statTraceService.passedDayStatTotals(inModel.getNamespace(), unistarProperties.getClean().getTrace()), statTraceService.todayStatTotals(inModel.getNamespace())));
        retModel.setRetCode(BaseOutModel.RET_OK);
        return retModel;
    }

}
