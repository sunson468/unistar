package com.up1234567.unistar.central.api.model.us.vo;

import com.up1234567.unistar.central.data.us.AppLimit;
import com.up1234567.unistar.common.util.DateUtil;
import com.up1234567.unistar.common.util.StringUtil;
import lombok.Data;
import org.apache.commons.lang3.time.DateFormatUtils;

import java.util.List;

@Data
public class AppLimitVo {

    private String appname; // 路径所属应用
    private String path; // 应用路径
    private boolean before; // 是否请求前控制，是则在Feign请求前控制，否则在Controller拦截器内控制
    private boolean auto; // 触发规则，true：常态限流  false：熔断限流

    // 有效期
    private String startTime; // 有效开始时间
    private String endTime; // 有效结束时间

    // 熔断参数
    private int period; // 错误检测秒数
    private int errors; // 触发的错误次数
    private int recover; // 恢复秒数

    // 控制参数
    private int qps; // 每秒控制数
    private int warmup; // 预热时长（秒）

    // 处理方式
    private boolean fastfail; // 快速失败 | 等待
    private int timeout; // 等待时长（秒）

    private List<String> whiteGroupList; // 白名单分组
    private List<String> whiteServiceList; // 白名单应用

    private String status;

    public static AppLimitVo wrap(AppLimit o) {
        AppLimitVo vo = new AppLimitVo();
        vo.setAppname(o.getAppname());
        vo.setPath(o.getPath());
        vo.setBefore(o.isBefore());
        vo.setStartTime(o.getStartTime() == 0 ? StringUtil.EMPTY : DateFormatUtils.format(o.getStartTime(), DateUtil.FMT_YYYY_MM_DD));
        long endTime = o.getEndTime() == Long.MAX_VALUE ? 0 : o.getEndTime();
        vo.setEndTime(endTime == 0 ? StringUtil.EMPTY : DateFormatUtils.format(endTime, DateUtil.FMT_YYYY_MM_DD));
        vo.setQps(o.getQps());
        vo.setWarmup(o.getWarmup());
        vo.setFastfail(o.isFastfail());
        vo.setTimeout(o.getTimeout());
        vo.setAuto(o.isAuto());
        if (!o.isAuto()) {
            vo.setPeriod(o.getPeriod());
            vo.setErrors(o.getErrors());
            vo.setRecover(o.getRecover());
        }
        vo.setWhiteGroupList(StringUtil.fromCommaString(o.getWhiteGroups()));
        vo.setWhiteServiceList(StringUtil.fromCommaString(o.getWhiteServices()));
        vo.setStatus(o.getStatus().name());
        return vo;
    }

}
