package com.up1234567.unistar.central.data.base;

import lombok.Data;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Document(collection = "base_task")
@CompoundIndexes({
        @CompoundIndex(name = "idx_ns_t", def = "{'namespace':1,'task':1}", unique = true),
})
public class BaseTask {

    private String task;

    private String namespace;

    private String remark;

    public boolean isValid(String ns) {
        return StringUtils.isEmpty(namespace) || namespace.equals(ns);
    }

}
