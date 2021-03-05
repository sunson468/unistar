package com.up1234567.unistar.central.data.us;

import com.up1234567.unistar.common.util.StringUtil;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Data
@Document(collection = "us_config_depend")
@CompoundIndexes({
        @CompoundIndex(name = "uidx_ns_n", def = "{'namespace':1,'name':1}", unique = true),
})
public class ConfigDepend {

    private String namespace;

    private String name; // 应用名 + profile

    private String depends; // 依赖No

    private long createTime;

    /**
     * 依赖作为List
     *
     * @return
     */
    public List<String> dependsAsList() {
        if (StringUtils.isEmpty(depends)) return new ArrayList<>();
        return Arrays.stream(depends.split(StringUtil.COMMA)).filter(Objects::nonNull).collect(Collectors.toList());
    }

}
