package com.up1234567.unistar.central.data.us;

import com.up1234567.unistar.common.limit.UnistarAppLimit;
import lombok.Data;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Document(collection = "us_app_limit")
@CompoundIndexes({
        @CompoundIndex(name = "uidx_ns_an_p_b_a", def = "{'namespace':1,'appname':1,'path':1,'before':1,'auto':1}", unique = true),
})
public class AppLimit extends UnistarAppLimit {

    @Indexed
    private String namespace;

    // 限制排除
    private String whiteGroups; // 白名单分组
    private String whiteServices; // 白名单应用

    private EStatus status;
    private long createTime;

    public enum EStatus {
        ON, OFF;
    }

}
