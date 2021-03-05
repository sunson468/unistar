package com.up1234567.unistar.common.util;

/**
 * 字符串的工具类<br>
 */
public final class BytesUtil {

    /**
     * 将byte[] 转换成字符串
     *
     * @param srcBytes
     * @return
     */
    public static String byte2Hex(byte[] srcBytes) {
        StringBuilder hexRetSB = new StringBuilder();
        for (byte b : srcBytes) {
            String hexString = Integer.toHexString(0x00ff & b);
            hexRetSB.append(hexString.length() == 1 ? 0 : "").append(hexString);
        }
        return hexRetSB.toString();


    }

    /**
     * 将16进制字符串转为转换成字符串
     *
     * @param source
     * @return
     */
    public static byte[] hex2Bytes(String source) {
        byte[] sourceBytes = new byte[source.length() / 2];
        for (int i = 0; i < sourceBytes.length; i++) {
            sourceBytes[i] = (byte) Integer.parseInt(source.substring(i * 2, i * 2 + 2), 16);
        }
        return sourceBytes;
    }
}
