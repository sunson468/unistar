package com.up1234567.unistar.common.util;

import java.security.MessageDigest;

public class SecurityUtil {
    private final static String ENCODE_UTF8 = "UTF-8";
    private final static String SECURITY_MD5 = "MD5";

    /**
     * md5签名，先用md5算法转化为byte数组，然后转化为hexString
     *
     * @param str
     * @return
     */
    public static String md5(String str) {
        return md5(str, ENCODE_UTF8);
    }

    /**
     * MD5加密函数
     *
     * @param str
     * @param encode
     * @return
     */
    public static String md5(String str, String encode) {
        if (str == null) {
            return null;
        }
        try {
            MessageDigest md5 = MessageDigest.getInstance(SECURITY_MD5);
            md5.update(str.getBytes(encode));
            byte[] digest = md5.digest();
            return BytesUtil.byte2Hex(digest);
        } catch (Exception e) {
            throw new RuntimeException("MD5加密发生错误", e);
        }
    }
}
