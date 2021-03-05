package com.up1234567.unistar.central.api;

import com.up1234567.unistar.central.api.model.BaseDataInModel;
import com.up1234567.unistar.central.api.model.BaseInModel;
import com.up1234567.unistar.central.api.model.BaseOutModel;
import com.up1234567.unistar.central.api.model.us.*;
import com.up1234567.unistar.central.api.model.us.vo.*;
import com.up1234567.unistar.central.data.stat.StatTrace;
import com.up1234567.unistar.central.data.us.*;
import com.up1234567.unistar.central.service.base.impl.BaseService;
import com.up1234567.unistar.central.service.base.impl.OperatorService;
import com.up1234567.unistar.central.service.connect.impl.UnistarConnectCacheService;
import com.up1234567.unistar.central.service.connect.impl.UnistarConnectService;
import com.up1234567.unistar.central.service.stat.dto.StatTracesCache;
import com.up1234567.unistar.central.service.stat.impl.StatTraceCacheService;
import com.up1234567.unistar.central.service.stat.impl.StatTraceService;
import com.up1234567.unistar.central.service.us.impl.AppCacheService;
import com.up1234567.unistar.central.service.us.impl.AppService;
import com.up1234567.unistar.central.service.us.impl.ConfigService;
import com.up1234567.unistar.central.support.aop.AOperatorLog;
import com.up1234567.unistar.central.support.auth.ARoleAuth;
import com.up1234567.unistar.central.support.auth.AuthToken;
import com.up1234567.unistar.central.support.auth.EAuthRole;
import com.up1234567.unistar.central.support.util.AesUtil;
import com.up1234567.unistar.common.IUnistarConst;
import com.up1234567.unistar.common.logger.IUnistarLogger;
import com.up1234567.unistar.common.util.DateUtil;
import com.up1234567.unistar.common.util.JsonUtil;
import com.up1234567.unistar.common.util.SecurityUtil;
import com.up1234567.unistar.common.util.StringUtil;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.text.ParseException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.stream.Collectors;

@RestController
@ARoleAuth(EAuthRole.APPER)
@RequestMapping(AuthToken.PATH_PREFIX + "app")
public class AppController {

    @Autowired
    private BaseService baseService;

    @Autowired
    private AppService appService;

    @Autowired
    private AppCacheService appCacheService;

    @Autowired
    private OperatorService operatorService;

    @Autowired
    private ConfigService configService;

    @Autowired
    private UnistarConnectService unistarConnectService;

    @Autowired
    private UnistarConnectCacheService unistarConnectCacheService;

    @Autowired
    private UnistarConnectService connectService;

    @Autowired
    private StatTraceService statTraceService;

    @Autowired
    private StatTraceCacheService statTraceCacheService;

    @PostMapping("list")
    public BaseOutModel list(@RequestAttribute(AuthToken.REQ_AUTH) AuthToken authToken, @RequestBody BaseInModel inModel) {
        BaseOutModel retModel = new BaseOutModel();
        List<AppVo> appVos = new ArrayList<>();
        appService.listApp(inModel.getNamespace(), authToken.isSuper() ? null : operatorService.listOperatorApp(authToken.getAccount(), inModel.getNamespace())).forEach(app -> appVos.add(AppVo.wrap(app)));
        retModel.setData(appVos);
        retModel.setRetCode(BaseOutModel.RET_OK);
        return retModel;
    }

    @AOperatorLog
    @PostMapping("add")
    public BaseOutModel configAdd(@RequestAttribute(AuthToken.REQ_AUTH) AuthToken authToken, @RequestBody AppInModel inModel) {
        BaseOutModel retModel = new BaseOutModel();
        App app = appService.findApp(inModel.getNamespace(), inModel.getName());
        if (app != null) {
            retModel.setRetMsg("server.already");
            return retModel;
        }
        app = new App();
        app.setNamespace(inModel.getNamespace());
        app.setName(inModel.getName());
        app.setRemark(inModel.getRemark());
        if (inModel.isToken()) app.setToken(SecurityUtil.md5(RandomStringUtils.random(16)));
        app.setCreateTime(DateUtil.now());
        appService.createApp(app);
        //
        retModel.setData(app.getToken());
        retModel.setRetCode(BaseOutModel.RET_OK);
        return retModel;
    }

    @AOperatorLog
    @PostMapping("edit")
    public BaseOutModel edit(@RequestAttribute(AuthToken.REQ_AUTH) AuthToken authToken, @RequestBody AppInModel inModel) {
        BaseOutModel retModel = new BaseOutModel();
        App app = appService.findApp(inModel.getNamespace(), inModel.getName());
        if (app == null) return retModel;
        app.setRemark(inModel.getRemark());
        appService.updateApp(app);
        retModel.setRetCode(BaseOutModel.RET_OK);
        return retModel;
    }

    @AOperatorLog
    @PostMapping("depend/edit")
    public BaseOutModel dependedit(@RequestAttribute(AuthToken.REQ_AUTH) AuthToken authToken, @RequestBody BaseDataInModel<ConfigDependVo> inModel) {
        BaseOutModel retModel = new BaseOutModel();
        ConfigDependVo vo = inModel.getData();
        if (vo == null) return retModel;
        App app = appService.findApp(inModel.getNamespace(), vo.getName());
        if (app == null) return retModel;
        ConfigDepend configDepend = configService.findConfigDepend(app.getNamespace(), app.getName());
        if (configDepend == null) {
            configDepend = new ConfigDepend();
            configDepend.setNamespace(app.getNamespace());
            configDepend.setName(app.getName());
            configDepend.setCreateTime(DateUtil.now());
            configService.createConfigDepend(configDepend);
        }
        // =============================================
        configService.updateConfigDepend(app.getNamespace(), app.getName(), vo.getDepends());
        retModel.setRetCode(BaseOutModel.RET_OK);
        return retModel;
    }

    @ARoleAuth(EAuthRole.SUPER)
    @PostMapping("config/list")
    public BaseOutModel configList(@RequestAttribute(AuthToken.REQ_AUTH) AuthToken authToken, @RequestBody ConfigInModel inModel) {
        BaseOutModel retModel = new BaseOutModel();
        List<ConfigVo> configs = new ArrayList<>();
        configService.listConfig(inModel.getNamespace(), inModel.getAppname()).forEach(config -> configs.add(ConfigVo.wrap(config)));
        retModel.setData(configs);
        retModel.setRetCode(BaseOutModel.RET_OK);
        return retModel;
    }

    @AOperatorLog
    @PostMapping("config/save")
    public BaseOutModel configSave(@RequestAttribute(AuthToken.REQ_AUTH) AuthToken authToken, @RequestBody ConfigSaveInModel inModel) {
        BaseOutModel retModel = new BaseOutModel();
        configService.removeConfig(inModel.getNamespace(), inModel.getAppname());
        List<Config> configs = new ArrayList<>();
        if (CollectionUtils.isEmpty(inModel.getConfigs())) return retModel;
        inModel.getConfigs().forEach(c -> {
            Config config = new Config();
            config.setNamespace(inModel.getNamespace());
            config.setName(StringUtils.trimToEmpty(inModel.getAppname()));
            config.setDependable(inModel.isDependable());
            config.setProfile(StringUtils.trimToEmpty(c.getProfile()));
            // 处理加密
            String properties = c.getProperties();
            if (StringUtils.isNotEmpty(properties)) {
                Matcher matcher = Config.REGEX_ENC.matcher(properties);
                while (matcher.find()) {
                    String val = matcher.group();
                    properties = properties.replace(val, Config.ENC_PREFIX + AesUtil.aesEncrypt(val.substring(5, val.length() - 1), baseService.findSetting().getConfigkey()));
                }
            }
            config.setProperties(properties);
            //
            configs.add(config);
        });
        configService.saveConfig(configs);
        retModel.setRetCode(BaseOutModel.RET_OK);
        return retModel;
    }

    @PostMapping("config/depends")
    public ConfigDependOutModel configDepends(@RequestAttribute(AuthToken.REQ_AUTH) AuthToken authToken, @RequestBody BaseDataInModel<String> inModel) {
        ConfigDependOutModel retModel = new ConfigDependOutModel();
        List<ConfigVo> configs = new ArrayList<>();
        configService.listDependableConfig(inModel.getNamespace()).forEach(config -> configs.add(ConfigVo.wrap(config)));
        retModel.setData(configs);
        //
        ConfigDepend configDepend = configService.findConfigDepend(inModel.getNamespace(), inModel.getData());
        if (configDepend != null) {
            retModel.setDepends(configDepend.dependsAsList());
        }
        retModel.setRetCode(BaseOutModel.RET_OK);
        return retModel;
    }

    @PostMapping("node/list")
    public BaseOutModel nodeList(@RequestAttribute(AuthToken.REQ_AUTH) AuthToken authToken, @RequestBody BaseDataInModel<String> inModel) {
        BaseOutModel retModel = new BaseOutModel();
        List<AppNodeVo> appNodeVos = new ArrayList<>();
        appService.listAppNode(inModel.getNamespace(), inModel.getData()).forEach(o -> {
            AppNodeVo vo = AppNodeVo.wrap(o);
            vo.setOnline(unistarConnectCacheService.isOnline(o));
            appNodeVos.add(vo);
        });
        retModel.setData(appNodeVos);
        retModel.setRetCode(BaseOutModel.RET_OK);
        return retModel;
    }

    @AOperatorLog
    @PostMapping("node/status")
    public BaseOutModel nodeStatus(@RequestAttribute(AuthToken.REQ_AUTH) AuthToken authToken, @RequestBody NodeStatusInModel inModel) {
        BaseOutModel retModel = new BaseOutModel();
        AppNode node = appService.findAppNodeByNodeId(inModel.getNamespace(), inModel.getAppname(), inModel.getNodeId());
        if (node == null) return retModel;
        if (!unistarConnectCacheService.isOnline(node)) return retModel;
        switch (inModel.getType()) {
            case 1:
                if (node.isServerable()) {
                    node.setServiceStatus(AppNode.EStatus.OFF.equals(node.getServiceStatus()) ? AppNode.EStatus.ON : AppNode.EStatus.OFF);
                    appService.appNodeServiceStatus(node);
                    unistarConnectCacheService.updateNode(node);
                    // 上下线通知
                    connectService.serviceNodeChanged(node);
                }
                break;
            case 2:
                if (node.isTaskable()) {
                    node.setTaskStatus(AppNode.EStatus.OFF.equals(node.getTaskStatus()) ? AppNode.EStatus.ON : AppNode.EStatus.OFF);
                    appService.appNodeTaskStatus(node);
                    unistarConnectCacheService.updateNode(node);
                }
                break;
        }
        retModel.setRetCode(BaseOutModel.RET_OK);
        return retModel;
    }

    @AOperatorLog
    @PostMapping("node/weight")
    public BaseOutModel nodeWeight(@RequestAttribute(AuthToken.REQ_AUTH) AuthToken authToken, @RequestBody BaseDataInModel<AppNodeVo> inModel) {
        BaseOutModel retModel = new BaseOutModel();
        AppNodeVo vo = inModel.getData();
        AppNode node = appService.findAppNodeByNodeId(inModel.getNamespace(), vo.getAppname(), vo.getNodeId());
        if (node == null) return retModel;
        if (!unistarConnectCacheService.isOnline(node)) return retModel;
        node.setWeight(vo.getWeight());
        connectService.serviceNodeChanged(node);
        retModel.setRetCode(BaseOutModel.RET_OK);
        return retModel;
    }

    /**
     * 手动添加节点
     *
     * @param authToken
     * @param inModel
     * @return
     */
    @AOperatorLog
    @PostMapping("node/add")
    public BaseOutModel nodeAdd(@RequestAttribute(AuthToken.REQ_AUTH) AuthToken authToken, @RequestBody BaseDataInModel<AppNodeVo> inModel) {
        BaseOutModel retModel = new BaseOutModel();
        AppNodeVo vo = inModel.getData();
        if (vo == null) return retModel;
        AppNode node = appService.findAppNode(inModel.getNamespace(), vo.getAppname(), vo.getHost(), vo.getPort());
        if (node != null) {
            retModel.setRetMsg("server.already");
            return retModel;
        }
        node = new AppNode();
        node.setNamespace(inModel.getNamespace());
        node.setAppname(vo.getAppname());
        node.setHost(vo.getHost());
        node.setPort(vo.getPort());
        node.toNodeId();
        node.setGroup(vo.getGroup());
        node.setServerable(vo.isServerable());
        node.setWeight(vo.getWeight());
        node.setServiceStatus(AppNode.EStatus.OFF);
        node.setTaskable(vo.isTaskable());
        node.setDiscoverable(false);
        node.setDiscoverStatus(AppNode.EStatus.OFF);
        node.setTasks(vo.getTasks());
        node.setTaskStatus(AppNode.EStatus.OFF);
        node.setManual(true);
        node.setManualAnurl(vo.getManualAnurl());
        if (node.getManualAnurl().startsWith(StringUtil.HTTP_PROTO)) {
            node.setManualAnurl(node.getManualAnurl().substring(7));
        }
        appService.createServiceNode(node);
        retModel.setRetCode(BaseOutModel.RET_OK);
        return retModel;
    }

    /**
     * 手动添加节点
     *
     * @param authToken
     * @param inModel
     * @return
     */
    @AOperatorLog
    @PostMapping("node/sync")
    public BaseOutModel nodeSync(@RequestAttribute(AuthToken.REQ_AUTH) AuthToken authToken, @RequestBody BaseDataInModel<AppNodeVo> inModel) {
        BaseOutModel retModel = new BaseOutModel();
        AppNodeVo vo = inModel.getData();
        if (vo == null) return retModel;
        AppNode node = appService.findAppNodeByNodeId(inModel.getNamespace(), vo.getAppname(), vo.getNodeId());
        if (node == null) return retModel;
        // 同步节点
        if (appService.syncManualAppNode(node)) {
            // 加入到总的连接列表
            unistarConnectCacheService.connected(node);
            //
            if (node.isServerable()) {
                connectService.serviceNodeChanged(node);
            }
        }
        //
        retModel.setRetCode(BaseOutModel.RET_OK);
        return retModel;
    }

    /**
     * 移除手动节点
     *
     * @param authToken
     * @param inModel
     * @return
     */
    @AOperatorLog
    @PostMapping("node/del")
    public BaseOutModel nodeDel(@RequestAttribute(AuthToken.REQ_AUTH) AuthToken authToken, @RequestBody BaseDataInModel<AppNodeVo> inModel) {
        BaseOutModel retModel = new BaseOutModel();
        AppNodeVo vo = inModel.getData();
        if (vo == null) return retModel;
        AppNode node = appService.findAppNodeByNodeId(inModel.getNamespace(), vo.getAppname(), vo.getNodeId());
        if (node == null) return retModel;
        // 从缓存中移除
        unistarConnectCacheService.disconnected(node);
        // 从库中移除
        appService.removeServiceNode(node);
        //
        retModel.setRetCode(BaseOutModel.RET_OK);
        return retModel;
    }

    @PostMapping("node/stat")
    public NodeStatOutModel nodeStat(@RequestAttribute(AuthToken.REQ_AUTH) AuthToken authToken, @RequestBody NodeStatInModel inModel) {
        NodeStatOutModel retModel = new NodeStatOutModel();
        AppNodeVo vo = inModel.getData();
        if (vo == null) return retModel;
        AppNode node = appService.findAppNodeByNodeId(inModel.getNamespace(), vo.getAppname(), vo.getNodeId());
        if (node == null) return retModel;
        List<StatTraceVo> statTraceVos = new ArrayList<>();
        if (inModel.getViewtype() == 0) {
            StatTracesCache statTracesCache = statTraceCacheService.appNodeStat(node);
            if (statTracesCache != null && CollectionUtils.isNotEmpty(statTracesCache.getTraces())) {
                retModel.setUpdateTime(DateFormatUtils.format(statTracesCache.getUpdateTime(), DateUtil.FMT_YYYY_MM_DD_HH_MM_SS));
                statTracesCache.getTraces().forEach(o -> {
                    final StatTraceVo traceVo = StatTraceVo.wrap(o);
                    statTraceVos.add(traceVo);
                    //
                    if (MapUtils.isNotEmpty(o.getCallees())) {
                        o.getCallees().forEach((p, callee) -> {
                            StatTraceVo calleeVo = StatTraceVo.wrap(callee);
                            calleeVo.setPrepath(traceVo.getPath());
                            statTraceVos.add(calleeVo);
                        });
                    }
                });
            }
        } else if (inModel.getViewtype() == 1) {
            List<StatTrace> statTraces = statTraceService.listStatTrace(node.getNamespace(), node.getAppname(), node.getNodeId());
            // 先合并
            Map<String, StatTrace> statTraceMap = new HashMap<>();
            statTraces.forEach(s -> {
                String pathKey = s.pathKey();
                StatTrace statTrace = statTraceMap.get(pathKey);
                if (statTrace == null) {
                    statTrace = new StatTrace();
                    statTrace.setTgroup(s.getTgroup());
                    statTrace.setPrepath(s.getPrepath());
                    statTrace.setPath(s.getPath());
                    statTraceMap.put(pathKey, statTrace);
                }
                statTrace.setMaxqps(Math.max(statTrace.getMaxqps(), s.getMaxqps()));
                statTrace.setCount(statTrace.getCount() + s.getCount());
                statTrace.setErrors(statTrace.getErrors() + s.getErrors());
                statTrace.setMinTime(Math.min(statTrace.getMinTime(), s.getMinTime()));
                statTrace.setMaxTime(Math.max(statTrace.getMaxTime(), s.getMaxTime()));
                statTrace.setTotalTime(statTrace.getTotalTime() + s.getTotalTime());
                statTrace.setUpdateTime(Math.max(statTrace.getUpdateTime(), s.getUpdateTime()));
            });
            //
            long lastUpdateTime = 0;
            for (Map.Entry<String, StatTrace> entry : statTraceMap.entrySet()) {
                StatTrace s = entry.getValue();
                statTraceVos.add(StatTraceVo.wrap(s));
                lastUpdateTime = Math.max(lastUpdateTime, s.getUpdateTime());
            }
            //
            if (lastUpdateTime > 0) retModel.setUpdateTime(DateFormatUtils.format(lastUpdateTime, DateUtil.FMT_YYYY_MM_DD_HH_MM_SS));
        }
        retModel.setData(statTraceVos);
        retModel.setRetCode(BaseOutModel.RET_OK);
        return retModel;
    }

    @AOperatorLog
    @PostMapping("node/watch")
    public BaseOutModel nodeWatch(@RequestAttribute(AuthToken.REQ_AUTH) AuthToken authToken, @RequestBody NodeWatchInModel inModel) {
        BaseOutModel retModel = new BaseOutModel();
        AppNodeVo vo = inModel.getData();
        if (vo == null) return retModel;
        AppNode node = appService.findAppNodeByNodeId(inModel.getNamespace(), vo.getAppname(), vo.getNodeId());
        if (node == null) return retModel;
        // 给节点发送观察通知
        unistarConnectService.traceWatch(node, inModel.getPath());
        retModel.setRetCode(BaseOutModel.RET_OK);
        return retModel;
    }

    @PostMapping("node/watch/traceId")
    public BaseOutModel nodeWatchTraceId(@RequestAttribute(AuthToken.REQ_AUTH) AuthToken authToken, @RequestBody NodeWatchInModel inModel) {
        BaseOutModel retModel = new BaseOutModel();
        AppNodeVo vo = inModel.getData();
        if (vo == null) return retModel;
        AppNode node = appService.findAppNodeByNodeId(inModel.getNamespace(), vo.getAppname(), vo.getNodeId());
        if (node == null) return retModel;
        retModel.setData(statTraceCacheService.getNodeWatchTraceId(node, inModel.getPath()));
        retModel.setRetCode(BaseOutModel.RET_OK);
        return retModel;
    }

    @PostMapping("node/watch/sync")
    public BaseOutModel nodeWatchSync(@RequestAttribute(AuthToken.REQ_AUTH) AuthToken authToken, @RequestBody BaseDataInModel<String> inModel) {
        BaseOutModel retModel = new BaseOutModel();
        retModel.setData(statTraceCacheService.listNodeWatch(inModel.getData()));
        retModel.setRetCode(BaseOutModel.RET_OK);
        return retModel;
    }

    @PostMapping("node/watch/cancel")
    public BaseOutModel nodeWatchCancel(@RequestAttribute(AuthToken.REQ_AUTH) AuthToken authToken, @RequestBody BaseDataInModel<String> inModel) {
        BaseOutModel retModel = new BaseOutModel();
        statTraceCacheService.cancelNodeWatch(inModel.getData());
        retModel.setRetCode(BaseOutModel.RET_OK);
        return retModel;
    }

    @PostMapping("node/config")
    public BaseOutModel nodeConfig(@RequestAttribute(AuthToken.REQ_AUTH) AuthToken authToken, @RequestBody BaseDataInModel<AppNodeVo> inModel) {
        BaseOutModel retModel = new BaseOutModel();
        AppNodeVo vo = inModel.getData();
        if (vo == null) return retModel;
        AppNode node = appService.findAppNodeByNodeId(inModel.getNamespace(), vo.getAppname(), vo.getNodeId());
        if (node == null) return retModel;
        List<String> profiles = StringUtil.fromCommaString(node.getProfiles());
        Map<String, Object> properties = new HashMap<>();
        configService.listConfig(node.getNamespace(), node.getAppname())
                .stream()
                .sorted(Comparator.comparing(Config::getProfile))
                .filter(c -> StringUtils.isEmpty(c.getProfile()) || profiles.contains(c.getProfile()))
                .forEach(c -> properties.putAll((JsonUtil.toMap(c.getProperties()))));
        retModel.setData(properties);
        retModel.setRetCode(BaseOutModel.RET_OK);
        return retModel;
    }

    @AOperatorLog
    @PostMapping("node/config/change")
    public BaseOutModel nodeConfigChange(@RequestAttribute(AuthToken.REQ_AUTH) AuthToken authToken, @RequestBody NodeConfigInModel inModel) {
        BaseOutModel retModel = new BaseOutModel();
        AppNodeVo vo = inModel.getData();
        if (vo == null) return retModel;
        AppNode node = appService.findAppNodeByNodeId(inModel.getNamespace(), vo.getAppname(), vo.getNodeId());
        if (node == null) return retModel;
        connectService.nodeConfigChanged(node, inModel.getConfigs());
        retModel.setRetCode(BaseOutModel.RET_OK);
        return retModel;
    }

    @PostMapping("node/logger")
    public NodeLoggerOutModel nodeLogger(@RequestAttribute(AuthToken.REQ_AUTH) AuthToken authToken, @RequestBody BaseDataInModel<String> inModel) {
        NodeLoggerOutModel retModel = new NodeLoggerOutModel();
        List<String> loggers = appCacheService.listLoggerData(inModel.getNamespace(), inModel.getData())
                .stream()
                .filter(l -> !l.equals(IUnistarConst.DEFAULT_LOGGER_UNISTAR))
                .collect(Collectors.toList());
        retModel.setData(loggers);
        retModel.setLevels(Arrays.stream(IUnistarLogger.Level.values()).map(Enum::name).collect(Collectors.toList()));
        retModel.setRetCode(BaseOutModel.RET_OK);
        return retModel;
    }

    @AOperatorLog
    @PostMapping("node/logger/change")
    public BaseOutModel nodeLoggerChange(@RequestAttribute(AuthToken.REQ_AUTH) AuthToken authToken, @RequestBody NodeLoggerInModel inModel) {
        BaseOutModel retModel = new BaseOutModel();
        AppNodeVo vo = inModel.getData();
        if (vo == null) return retModel;
        AppNode node = appService.findAppNodeByNodeId(inModel.getNamespace(), vo.getAppname(), vo.getNodeId());
        if (node == null) return retModel;
        connectService.nodeLoggerChanged(node, inModel.getLoggers());
        retModel.setRetCode(BaseOutModel.RET_OK);
        return retModel;
    }

    @PostMapping("limits")
    public BaseOutModel limits(@RequestAttribute(AuthToken.REQ_AUTH) AuthToken authToken, @RequestBody BaseInModel inModel) {
        BaseOutModel retModel = new BaseOutModel();
        List<AppLimitVo> appLimitVos = new ArrayList<>();
        appService.listAppLimit(inModel.getNamespace(), operatorService.listOperatorApp(authToken.getAccount(), inModel.getNamespace()))
                .forEach(l -> appLimitVos.add(AppLimitVo.wrap(l)));
        retModel.setData(appLimitVos);
        retModel.setRetCode(BaseOutModel.RET_OK);
        return retModel;
    }

    @PostMapping("limit/condition")
    public AppLimitConditionOutModel limitCondition(@RequestAttribute(AuthToken.REQ_AUTH) AuthToken authToken, @RequestBody BaseDataInModel<String> inModel) {
        AppLimitConditionOutModel retModel = new AppLimitConditionOutModel();
        //
        App app = appService.findApp(inModel.getNamespace(), inModel.getData());
        if (app == null) return retModel;
        retModel.setServerable(app.isServerable());
        //
        List<StatTrace> statTraces = statTraceService.listStatTrace(app.getNamespace(), app.getName(), null);
        List<String> pathes = new ArrayList<>();
        statTraces.stream().filter(s -> StringUtils.isEmpty(s.getPrepath())).forEach(s -> {
            if (!pathes.contains(s.getPath())) pathes.add(s.getPath());
        });
        retModel.setData(pathes);
        // 已知调用自己的服务
        Set<String> disovers = new HashSet<>();
        Set<String> groups = new HashSet<>();
        appService.listAppNode(app.getNamespace(), null).forEach(node -> {
            if (app.getName().equals(node.getAppname())) {
                // 是自己的节点
                groups.add(node.getGroup());
            } else if (StringUtil.fromCommaString(node.getDiscovers()).contains(app.getName())) {
                // 调用的列表内包含了自己
                disovers.add(node.getAppname());
            }
        });
        retModel.setDisovers(appService.listApp(inModel.getNamespace(), disovers).stream().map(AppVo::wrap).collect(Collectors.toList()));
        retModel.setGroups(baseService.listGroup(groups));
        retModel.setRetCode(BaseOutModel.RET_OK);
        return retModel;
    }

    @AOperatorLog
    @PostMapping("limit/aoe")
    public BaseOutModel limitAddOrEdit(@RequestAttribute(AuthToken.REQ_AUTH) AuthToken authToken, @RequestBody BaseDataInModel<AppLimitVo> inModel) {
        BaseOutModel retModel = new BaseOutModel();
        //
        AppLimitVo vo = inModel.getData();
        AppLimit appLimit = appService.findAppLimit(inModel.getNamespace(), vo.getAppname(), vo.getPath(), vo.isBefore(), vo.isAuto());
        if (appLimit == null) {
            appLimit = new AppLimit();
            appLimit.setNamespace(inModel.getNamespace());
            appLimit.setAppname(vo.getAppname());
            appLimit.setPath(vo.getPath());
            appLimit.setBefore(vo.isBefore());
            appLimit.setAuto(vo.isAuto());
            appLimit.setStatus(AppLimit.EStatus.OFF);
            appService.createAppLimit(appLimit);
        }
        //
        if (StringUtils.isNotEmpty(vo.getStartTime())) {
            try {
                appLimit.setStartTime(DateUtils.parseDate(vo.getStartTime(), DateUtil.FMT_YYYY_MM_DD).getTime());
            } catch (ParseException e) {
                appLimit.setStartTime(DateUtil.today());
            }
        } else {
            appLimit.setStartTime(DateUtil.today());
        }
        if (StringUtils.isNotEmpty(vo.getEndTime())) {
            try {
                appLimit.setEndTime(DateUtils.parseDate(vo.getEndTime(), DateUtil.FMT_YYYY_MM_DD).getTime());
            } catch (ParseException e) {
                appLimit.setEndTime(DateUtil.today());
            }
        } else {
            appLimit.setEndTime(Long.MAX_VALUE);
        }
        appLimit.setQps(vo.getQps());
        appLimit.setWarmup(vo.getWarmup() == 0 ? 5 : vo.getWarmup());
        appLimit.setFastfail(vo.isFastfail());
        appLimit.setTimeout(vo.getTimeout() == 0 ? 60 : vo.getTimeout());
        if (!appLimit.isAuto()) {
            appLimit.setPeriod(vo.getPeriod() == 0 ? 5 : vo.getPeriod());
            appLimit.setErrors(vo.getErrors() == 0 ? 5 : vo.getErrors());
            appLimit.setRecover(vo.getRecover() == 0 ? 300 : vo.getRecover());
        }
        appLimit.setWhiteGroups(StringUtil.toCommaString(vo.getWhiteGroupList()));
        appLimit.setWhiteServices(StringUtil.toCommaString(vo.getWhiteServiceList()));
        appService.updateAppLimit(appLimit);
        // 通知变更
        if (AppLimit.EStatus.ON.equals(appLimit.getStatus())) {
            connectService.limitChanged(appLimit);
        }
        //
        retModel.setRetCode(BaseOutModel.RET_OK);
        return retModel;
    }

    @PostMapping("limit/status")
    public BaseOutModel limitStatus(@RequestAttribute(AuthToken.REQ_AUTH) AuthToken authToken, @RequestBody BaseDataInModel<AppLimitVo> inModel) {
        BaseOutModel retModel = new BaseOutModel();
        AppLimitVo vo = inModel.getData();
        AppLimit appLimit = appService.findAppLimit(inModel.getNamespace(), vo.getAppname(), vo.getPath(), vo.isBefore(), vo.isAuto());
        if (appLimit == null) return retModel;
        //
        appLimit.setStatus(AppLimit.EStatus.valueOf(vo.getStatus()));
        appService.updateAppLimitStatus(appLimit);
        // 通知变更
        if (AppLimit.EStatus.OFF.equals(appLimit.getStatus())) {
            appLimit.setEndTime(1L);
        }
        connectService.limitChanged(appLimit);
        //
        retModel.setRetCode(BaseOutModel.RET_OK);
        return retModel;
    }

}
