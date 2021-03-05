package com.up1234567.unistar.central.data.base;


import lombok.Data;
import org.springframework.data.mongodb.core.index.IndexDirection;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Document(collection = "base_operator_log")
public class BaseOperatorLog {

    @Indexed
    private String account;

    // 请求地址
    private String path;

    // 请求来源IP
    private String ip;
    // 请求时间
    @Indexed(direction = IndexDirection.DESCENDING)
    private long createTime;

}
