package com.up1234567.unistar.central.support.util;

import com.up1234567.unistar.common.util.BytesUtil;
import org.apache.commons.lang3.StringUtils;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.io.UnsupportedEncodingException;
import java.security.SecureRandom;

public final class AesUtil {

    private final static String ENCODE_UTF8 = "UTF-8";
    private final static String SECURITY_AES = "AES";
    private final static String SECURITY_AES_KEY = "SHA1PRNG";
    private final static char SECURITY_AES_KEY_PAD = '-';

    /**
     * @param ori
     * @param pass
     * @param mode
     * @param random
     * @return
     */
    private static byte[] aesCode(byte[] ori, String pass, int mode, boolean random) {
        if (StringUtils.isEmpty(pass)) {
            throw new RuntimeException("AES 密钥不能为空");
        }
        if (pass.length() < 16) {
            pass = StringUtils.leftPad(pass, 16, SECURITY_AES_KEY_PAD);
        } else if (pass.length() > 16) {
            pass = pass.substring(0, 16);
        }
        try {
            SecretKeySpec key;
            KeyGenerator keyGen = KeyGenerator.getInstance(SECURITY_AES);
            if (random) {
                SecureRandom secureRandom = SecureRandom.getInstance(SECURITY_AES_KEY);
                secureRandom.setSeed(pass.getBytes(ENCODE_UTF8));
                keyGen.init(128, secureRandom);
                SecretKey secretKey = keyGen.generateKey();
                key = new SecretKeySpec(secretKey.getEncoded(), SECURITY_AES);
            } else {
                keyGen.init(128);
                key = new SecretKeySpec(pass.getBytes(ENCODE_UTF8), SECURITY_AES);
            }
            Cipher cipher = Cipher.getInstance(SECURITY_AES);
            cipher.init(mode, key);
            return cipher.doFinal(ori);
        } catch (Exception e) {
            throw new RuntimeException("AES 错误", e);
        }
    }

    /**
     * aes加密-128位
     *
     * @param content  原文
     * @param password 密钥
     * @return 16进制密文
     */
    public static String aesEncrypt(String content, String password) {
        try {
            byte[] datas = aesCode(content.getBytes(ENCODE_UTF8), password, Cipher.ENCRYPT_MODE, true);
            return BytesUtil.byte2Hex(datas);
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("AES 加密错误", e);
        }
    }

    /**
     * aes解密-128位
     *
     * @param content  (16进制) 密文
     * @param password 密钥
     * @return 原文
     */
    public static String aesDecrypt(String content, String password) {
        byte[] datas = aesCode(BytesUtil.hex2Bytes(content), password, Cipher.DECRYPT_MODE, true);
        try {
            return new String(datas, ENCODE_UTF8);
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("AES 解密错误", e);
        }
    }

}
