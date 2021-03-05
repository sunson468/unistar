package com.up1234567.unistar.common.util;

import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.Map;

public class QueryUtil {

    public static Map<String, String> resolveQuery(String query) {
        Map<String, String> ret = new HashMap<>();
        if (StringUtils.isEmpty(query)) {
            return ret;
        }
        for (String str : query.split(StringUtil.AND)) {
            String[] kv = str.split(StringUtil.EQUAL, 2);
            if (kv[1] != null) {
                ret.put(kv[0], kv[1]);
            }
        }
        return ret;
    }

}
