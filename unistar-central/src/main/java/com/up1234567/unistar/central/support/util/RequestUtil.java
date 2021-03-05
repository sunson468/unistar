package com.up1234567.unistar.central.support.util;

import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpHeaders;

import javax.servlet.http.HttpServletRequest;

public final class RequestUtil {

    private final static String UNKNOWN = "unknown";
    private final static String X_Forwarded_For = "X-Forwarded-For";

    private final static String IP_LOCAL_V4 = "127.0.0.1";
    private final static String IP_LOCAL_V6 = "[0:0:0:0:0:0:0:1]";

    private final static String HTTP_PREFIX = "http://";
    private final static String HTTPS_PREFIX = "https://";

    /**
     * 获取客户端IP
     *
     * @param request
     * @return
     */
    public static String clientIP(HttpServletRequest request) {
        String ip = request.getHeader(X_Forwarded_For);
        if (ip == null || ip.length() == 0 || UNKNOWN.equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        return isValidIp(ip) ? ip : IP_LOCAL_V4;
    }

    /**
     * 是否为有效IP
     *
     * @param ip
     * @return
     */
    public static boolean isValidIp(String ip) {
        return !IP_LOCAL_V6.equals(ip) && !IP_LOCAL_V4.equals(ip);
    }

    /**
     * @param request
     * @return
     */
    public static String getRerfer(HttpServletRequest request) {
        return request.getHeader(HttpHeaders.REFERER);
    }

    /**
     * @param url
     * @return
     */
    public static boolean isValidUrl(String url) {
        if (StringUtils.isNotEmpty(url)) return url.startsWith(HTTP_PREFIX) || url.startsWith(HTTPS_PREFIX);
        return false;
    }

}
