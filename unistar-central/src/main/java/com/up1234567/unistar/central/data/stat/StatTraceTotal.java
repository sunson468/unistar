package com.up1234567.unistar.central.data.stat;

import lombok.Data;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Document(collection = "stat_trace_total")
@CompoundIndexes({
        @CompoundIndex(name = "idx_ns_a_d", def = "{'namespace':1,'appname':1,'daydate':1}", unique = true),
        @CompoundIndex(name = "idx_ns_d", def = "{'namespace':1,'daydate':1}"),
})
public class StatTraceTotal {

    // 标记ID
    @Indexed
    private String totalId;

    // 所属空间
    private String namespace;
    // 所属应用
    private String appname;
    // 日期
    private long daydate;

    // 最大QPS
    private int maxqps;
    // 请求次数
    private long count;
    // 请求失败次数
    private long errors;
    // 单次最长时间
    private long maxTime;

}
