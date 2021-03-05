package com.up1234567.unistar.central.data.base;

import lombok.Data;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Document(collection = "base_ns")
public class BaseNamespace {

    @Indexed(unique = true)
    private String namespace;

    private String remark;

}
