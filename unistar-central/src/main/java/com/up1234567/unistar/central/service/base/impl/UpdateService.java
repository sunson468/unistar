package com.up1234567.unistar.central.service.base.impl;

import com.up1234567.unistar.central.service.base.IUpdateService;
import com.up1234567.unistar.central.support.data.extend.UnistarPageCondition;
import com.up1234567.unistar.central.data.base.BaseVersion;
import com.up1234567.unistar.central.support.data.IUnistarDao;
import com.up1234567.unistar.central.support.data.extend.UnistarPageResult;
import com.up1234567.unistar.common.IUnistarConst;
import com.up1234567.unistar.common.exception.UnistarUpdateException;
import com.up1234567.unistar.common.util.DateUtil;
import lombok.CustomLog;
import lombok.Getter;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

import java.util.*;


@CustomLog(topic = IUnistarConst.DEFAULT_LOGGER_UNISTAR)
@Service
public class UpdateService {

    @Autowired
    private IUnistarDao unistarDao;

    @Autowired
    private ApplicationContext context;

    @Getter
    private BaseVersion currentVersion;

    /**
     * 最新版本号
     *
     * @return
     */
    public BaseVersion newestVersion() {
        UnistarPageCondition condition = new UnistarPageCondition();
        condition.setPage(UnistarPageCondition.PAGE_1);
        condition.setLimit(UnistarPageCondition.SIZE_1);
        condition.setCountable(false);
        condition.addSort("createTime", "DESC");
        UnistarPageResult<BaseVersion> result = unistarDao.listByPageCondition(condition, BaseVersion.class);
        return CollectionUtils.isNotEmpty(result.getData()) ? result.getData().get(0) : null;
    }

    /**
     * 校验程序版本是否一致
     */
    public void checkVersion() {
        long dbVer = 0;
        BaseVersion curDbVer = newestVersion();
        // 版本相同就跳过
        if (curDbVer != null) {
            if (!curDbVer.isSuccess()) {
                throw new RuntimeException("上次版本更新出现异常，系统启动失败，请检查系统状态");
            }
            dbVer = curDbVer.getVersion();
        } else {
            dbVer = 0;
        }
        List<Long> needUpdateVersions = new ArrayList<>();
        String[] beanNames = context.getBeanNamesForType(IUpdateService.class);
        if (ArrayUtils.isEmpty(beanNames)) {
            return;
        }
        // 首次启动，或者版本需要升级，则执行升级服务，升级服务为
        for (String bn : beanNames) {
            long bnv = Long.parseLong(bn.substring(13));
            if (bnv > dbVer) {
                needUpdateVersions.add(bnv);
            }
        }
        // ===============================================================================
        // 逐个升级
        if (CollectionUtils.isNotEmpty(needUpdateVersions)) {
            // 排序
            Long[] needUpdates = needUpdateVersions.toArray(new Long[0]);
            Arrays.sort(needUpdates, Comparator.comparingInt(Long::intValue));
            //
            for (Long ver : needUpdates) {
                log.info("开始更新版本：{}", ver);
                //
                IUpdateService updateService = context.getBean("updateService" + ver, IUpdateService.class);
                // 记录新的版本
                curDbVer = new BaseVersion();
                curDbVer.setVersion(ver);
                curDbVer.setVershow(updateService.version());
                curDbVer.setCreateTime(DateUtil.now());
                unistarDao.insert(curDbVer);
                // 执行更新逻辑
                try {
                    updateService.update();
                } catch (Exception e) {
                    // 异常捕获，并执行回退策略
                    updateService.fallback();
                    throw new UnistarUpdateException("version " + ver + " update failed: " + e.getMessage());
                }
                //
                Map<String, Object> updates = new HashMap<>();
                updates.put("success", true);
                unistarDao.updateOneByProp("version", curDbVer.getVersion(), updates, BaseVersion.class);
                log.info("更新版本成功：{}", ver);
            }
        }
        //
        currentVersion = curDbVer;
    }
}
