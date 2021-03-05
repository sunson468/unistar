package com.up1234567.unistar.central.data.us;

import lombok.Data;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Document(collection = "us_app")
@CompoundIndexes(
        @CompoundIndex(name = "uidx_ns_n", def = "{'namespace':1,'name':1}", unique = true)
)
public class App {

    private String namespace;
    private String name;

    private String remark;

    private boolean serverable; // 可提供服务
    private boolean discoverable; // 可发现服务
    private boolean taskable; // 可执行任务

    private String token;

    private long createTime;

}
