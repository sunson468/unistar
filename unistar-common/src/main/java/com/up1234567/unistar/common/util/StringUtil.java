package com.up1234567.unistar.common.util;

import org.slf4j.helpers.MessageFormatter;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.net.URI;
import java.util.*;
import java.util.stream.Collectors;

public final class StringUtil {

    public final static String COLON = ":";
    public final static String EMPTY = "";
    public final static String BLANK = " ";
    public final static String AND = "&";
    public final static String EQUAL = "=";
    public final static String COMMA = ",";
    public final static String H_LINE = "-";
    public final static String SLASH = "/";
    public final static String XHX = "_";
    public final static String QMARK = "?";
    public final static String QUATE = "'";
    public final static String D_QUATE = "\"";
    //
    public final static String HTTP_PROTO = "http://";
    public final static String HTTPS_PROTO = "https://";

    /**
     * @param list
     * @return
     */
    public static String toCommaString(Collection<String> list) {
        if (CollectionUtils.isEmpty(list)) return EMPTY;
        return list.stream().reduce(EMPTY, (result, str) -> result += COMMA + str).substring(1);
    }

    /**
     * @param commastr
     * @return
     */
    public static List<String> fromCommaString(String commastr) {
        if (StringUtils.isEmpty(commastr)) return new ArrayList<>();
        return Arrays.stream(commastr.split(COMMA)).filter(Objects::nonNull).collect(Collectors.toList());
    }

    /**
     * 包装为Http路径
     *
     * @param pathes
     * @return
     */
    public static String wrapHttpUrl(String... pathes) {
        String starter = EMPTY;
        if (pathes == null || pathes.length == 0) return starter;
        if (!pathes[0].startsWith(HTTP_PROTO)) {
            starter = HTTP_PROTO;
        }
        return Arrays.stream(pathes).reduce(starter, (result, path) -> result += path);
    }

    /**
     * 包装为Http路径
     *
     * @param pathes
     * @return
     */
    public static String wrapHttpsUrl(String... pathes) {
        String starter = EMPTY;
        if (pathes == null || pathes.length == 0) return starter;
        if (!pathes[0].startsWith(HTTPS_PROTO)) {
            starter = HTTPS_PROTO;
        }
        return Arrays.stream(pathes).reduce(starter, (result, path) -> result += path);
    }

    /**
     * @param val
     * @return
     */
    public static String withDefault(String val) {
        return withDefault(val, EMPTY);
    }

    /**
     * @param val
     * @param def
     * @return
     */
    public static String withDefault(String val, String def) {
        return StringUtils.isEmpty(val) ? def : val;
    }

    /**
     * 从URL中提取host
     *
     * @param url
     * @return
     */
    public static String resolveHost(String url) {
        if (StringUtils.isEmpty(url)) return EMPTY;
        if (!url.startsWith(HTTP_PROTO)) return EMPTY;
        return url.substring(7, url.indexOf(SLASH, 8));
    }

    /**
     * 字符串替换
     *
     * @param str
     * @param params
     * @return
     */
    public static String relace(String str, Object... params) {
        if (params == null || params.length == 0) return str;
        return MessageFormatter.format(str, Arrays.stream(params).toArray()).getMessage();
    }

    /**
     * @param uri
     * @return
     */
    public static String toBaseUrl(URI uri) {
        String url = uri.toString();
        int qidx = url.indexOf(QMARK);
        return (qidx == -1) ? url : url.substring(0, qidx);
    }

}
