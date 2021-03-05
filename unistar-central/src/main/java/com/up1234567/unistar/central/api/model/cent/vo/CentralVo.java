package com.up1234567.unistar.central.api.model.cent.vo;

import com.up1234567.unistar.central.data.cent.Central;
import com.up1234567.unistar.common.util.DateUtil;
import com.up1234567.unistar.common.util.StringUtil;
import lombok.Data;
import org.apache.commons.lang3.time.DateFormatUtils;

@Data
public class CentralVo {

    private String address;    // 应用地址
    private String version; // 版本号

    private boolean master;

    private long memoryFree;    // 已分配空闲内存(MB)
    private long memoryTotal;   // 已分配总内存(MB)
    private long memoryMax;   // 最大可分配内存(MB)
    private int processors;   // 可用处理器数量

    private boolean online; // 是否在线
    private String lastActiveTime; // 上次活跃时间

    /**
     * @param o
     * @return
     */
    public static CentralVo wrap(Central o) {
        CentralVo vo = new CentralVo();
        vo.setAddress(o.getHost() + StringUtil.COLON + o.getPort());
        vo.setVersion(o.getVersion());
        vo.setMaster(o.isMaster());
        vo.setMemoryFree(o.getMemoryFree());
        vo.setMemoryTotal(o.getMemoryTotal());
        vo.setMemoryMax(o.getMemoryMax());
        vo.setProcessors(o.getProcessors());
        o.checkOnline();
        vo.setOnline(o.isOnline());
        if (!vo.isOnline()) {
            vo.setLastActiveTime(DateFormatUtils.format(o.getLastActiveTime(), DateUtil.FMT_YYYY_MM_DD_HH_MM_SS));
        }
        return vo;
    }

}
