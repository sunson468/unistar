package com.up1234567.unistar.central.support.util;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public final class HttpUtil {

    /**
     * @param target
     * @return
     */
    public static String get(String target) {
        try {
            URL url = new URL(target);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(1000);
            conn.setDoInput(true);
            conn.setDoOutput(true);
            conn.setUseCaches(false);
            conn.setRequestProperty("Accept", "*/*");
            conn.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/84.0.4147.89 Safari/537.36 Edg/84.0.522.44 Unistar/Heartbeat");
            if (conn.getResponseCode() == 200) {
                //用getInputStream()方法获得服务器返回的输入流
                InputStream in = conn.getInputStream();
                byte[] data = new byte[in.available()];
                in.read(data);
                String result = new String(data);
                in.close();
                return result;
            }
        } catch (Exception e) {
            //
        }
        return null;
    }

}
