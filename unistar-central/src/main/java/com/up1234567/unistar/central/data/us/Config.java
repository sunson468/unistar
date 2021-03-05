package com.up1234567.unistar.central.data.us;

import lombok.Data;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.regex.Pattern;

@Data
@Document(collection = "us_config")
@CompoundIndexes({
        @CompoundIndex(name = "uidx_ns_n_p", def = "{'namespace':1,'name':1,'profile':1}", unique = true),
})
public class Config {
    // 匹配加密，文本匹配
    public final static Pattern REGEX_ENC = Pattern.compile("(\\$enc\\{[^}]*\\})");
    // 匹配引用，字段匹配
    public final static Pattern REGEX_IMPL = Pattern.compile("^\\$imp\\{([^}])*\\}$");

    public final static String ENC_PREFIX = "++";

    private String namespace;

    private String name; // 应用名 + profile

    private String profile;

    private String properties;

    @Indexed
    private boolean dependable; // 是否可以被依赖

}
