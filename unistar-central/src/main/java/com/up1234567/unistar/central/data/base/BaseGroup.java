package com.up1234567.unistar.central.data.base;

import lombok.Data;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Document(collection = "base_group")
public class BaseGroup {

    @Indexed(unique = true)
    private String group;

    private String namespace;

    private String remark;

    /**
     * @param ns
     * @return
     */
    public boolean isValid(String ns) {
        return StringUtils.isEmpty(namespace) || namespace.equals(ns);
    }

}
