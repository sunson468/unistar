package com.up1234567.unistar.central.data.base;

import lombok.Data;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Document(collection = "base_version")
public class BaseVersion {

    @Indexed(unique = true)
    private long version;

    private String vershow; // 展示的版本号

    private boolean success;

    private long createTime;

}
