package com.up1234567.unistar.central.data.base;

import lombok.Data;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Document(collection = "base_operator_app")
@CompoundIndexes({
        @CompoundIndex(name = "idx_a_ns", def = "{'account':1,'namespace':1}"),
})
public class BaseOperatorApp {

    public final static String SPLIT = " - ";

    @Indexed
    private String account;

    private String namespace;
    private String appname;

}
