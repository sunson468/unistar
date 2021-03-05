package com.up1234567.unistar.central.data.cent;

import com.up1234567.unistar.common.util.DateUtil;
import lombok.Data;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Document(collection = "cent_central")
@CompoundIndexes({
        @CompoundIndex(name = "uidx_h_p", def = "{'host':1,'port':1}", unique = true),
})
public class Central {

    private String host;    // 应用地址
    private int port;       // 应用端口
    private String version; // 版本号

    // Master选举时，采用空闲内存最大选取策略
    private boolean master;
    private long memoryFree;    // 已分配空闲内存(MB)
    private long memoryTotal;   // 已分配总内存(MB)
    private long memoryMax;   // 最大可分配内存(MB)
    private int processors;   // 可用处理器数量
    private long lastActiveTime; // 上次活跃时间

    private boolean online; // 是否在线
    private long createTime;

    /**
     * @param host
     * @param port
     * @return
     */
    public boolean isSelf(String host, int port) {
        return host.equals(this.host) && port == this.port;
    }

    /**
     * 判断是否在线，一分钟内活跃过
     */
    public void checkOnline() {
        if (online) {
            online = ((DateUtil.now() - lastActiveTime) < 2 * DateUtil.MINUTE);
        }
    }
}
