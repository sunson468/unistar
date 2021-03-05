package com.up1234567.unistar.common.util;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;

public final class InetUtil {

    /**
     * 获取本机网内地址
     */
    public static String getInet4Address() {
        try {
            //获取所有网络接口
            Enumeration<NetworkInterface> allNetworkInterfaces = NetworkInterface.getNetworkInterfaces();
            //遍历所有网络接口
            for (; allNetworkInterfaces.hasMoreElements(); ) {
                NetworkInterface networkInterface = allNetworkInterfaces.nextElement();
                if (networkInterface.isLoopback() || networkInterface.isVirtual() || !networkInterface.isUp() || networkInterface.getDisplayName().contains("VM")) {
                    continue;
                }
                //遍历此接口下的所有IP（因为包括子网掩码各种信息）
                for (Enumeration<InetAddress> inetAddressEnumeration = networkInterface.getInetAddresses(); inetAddressEnumeration.hasMoreElements(); ) {
                    InetAddress inetAddress = inetAddressEnumeration.nextElement();
                    //如果此IP不为空
                    if (inetAddress != null) {
                        //如果此IP为IPV4 则返回
                        if (inetAddress instanceof Inet4Address) {
                            return inetAddress.getHostAddress();
                        }
                    }
                }
            }
        } catch (SocketException e) {
        }

        try {
            return InetAddress.getLocalHost().getHostAddress();
        } catch (Exception e) {
            return "Unkown Host";
        }
    }

}
