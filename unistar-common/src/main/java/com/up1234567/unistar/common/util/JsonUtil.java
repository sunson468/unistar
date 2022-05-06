package com.up1234567.unistar.common.util;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class JsonUtil {

    private final static ObjectMapper O_M = new ObjectMapper();

    static {
        O_M.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        O_M.setSerializationInclusion(JsonInclude.Include.NON_DEFAULT);
        O_M.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    /**
     * @param o
     * @return
     */
    public static String toJsonString(Object o) {
        if (o instanceof String) return (String) o;
        try {
            return O_M.writeValueAsString(o);
        } catch (Exception e) {
            throw new RuntimeException("序列化为字符串失败: " + e.getMessage());
        }
    }

    /**
     * @param str
     * @return
     */
    public static Map<String, Object> toMap(String str) {
        return toClass(str, HashMap.class);
    }

    /**
     * @param str
     * @param clazz
     * @param <T>
     * @return
     */
    public static <T> T toClass(String str, Class<T> clazz) {
        try {
            if (StringUtils.isEmpty(str)) return null;
            if (String.class.isAssignableFrom(clazz)) return (T) str;
            return O_M.readValue(str, clazz);
        } catch (Exception e) {
            throw new RuntimeException("反序列化为对象失败, " + clazz.getName() + ": " + e.getMessage());
        }
    }

    /**
     * @param json
     * @param clazz
     * @param <T>
     * @return
     */
    public static <T> List<T> toClassAsList(String json, Class<T> clazz) {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        try {

            List<T> retList = new ArrayList<>();
            objectMapper.readTree(json).forEach(el -> retList.add(toClass(el.toString(), clazz)));
            return retList;
        } catch (IOException e) {
            throw new RuntimeException("JSON反序列化失败");
        }
    }

}
