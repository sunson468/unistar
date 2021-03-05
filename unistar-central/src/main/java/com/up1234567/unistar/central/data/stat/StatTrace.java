package com.up1234567.unistar.central.data.stat;


import com.up1234567.unistar.common.util.StringUtil;
import lombok.Data;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Document(collection = "stat_trace")
@CompoundIndexes({
        @CompoundIndex(name = "idx_ns_a_n", def = "{'namespace':1,'appname':1,'nodeId':1}"),
})
public class StatTrace {

    @Indexed
    private String statId;

    // 所属空间
    private String namespace;
    // 所属节点
    private String nodeId;
    // 所属应用
    private String appname;
    // 所属日期
    @Indexed
    private long daydate;

    private String tgroup;
    // 请求路径 http://appname/路径
    private String path;
    // 前置请求 http://appname/路径
    private String prepath;

    // 最大QPS
    private int maxqps;
    // 请求次数
    private long count;
    // 请求失败次数
    private long errors;
    // 单次最长时间
    private long minTime = Long.MAX_VALUE;
    // 单次最短时间
    private long maxTime;
    // 总花费时间
    private long totalTime;
    // 更新时间
    private long updateTime;

    /**
     * @return
     */
    public String pathKey() {
        return tgroup + StringUtil.H_LINE + StringUtil.withDefault(prepath) + StringUtil.H_LINE + path;
    }

}
