package com.up1234567.unistar.central.data.base;

import lombok.Data;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Document(collection = "base_setting")
public class BaseSetting {

    @Indexed(unique = true)
    private int no = 0;

    // 用于配置的加解密
    private String configkey;

}
