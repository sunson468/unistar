package com.up1234567.unistar.common.util;

import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public final class OsUtil {

    // Windows
    private final static String CMD = "cmd";
    private final static String CMD_C = "/c";
    private final static String FIND = "find ";
    private final static String FIND_I = " /i ";

    // Linux
    private final static String SH = "/bin/sh";
    private final static String SH_C = "-c";
    private final static String GREP = " grep ";
    private final static String GREP_I = " -i ";
    private final static String GREP_M = " -m ";
    private final static String GREP_B = " -B ";
    private final static String GREP_C = " -C ";

    /**
     * 是否为window系统
     *
     * @return
     */
    public static boolean isWindow() {
        String os = System.getProperty("os.name");
        return os.toLowerCase().startsWith("win");
    }

    /**
     * 查找文本
     *
     * @param filepath
     * @param search
     * @param before
     * @param after
     * @param maxline
     * @return
     */
    public static List<String> grep(String filepath, String search, int before, int after, int maxline) {
        List<String> lines = new ArrayList<>();
        boolean isWindows = isWindow();
        try {
            List<String> commands = new ArrayList<>();
            StringBuilder sb = new StringBuilder();
            if (isWindows) {
                commands.add(CMD);
                commands.add(CMD_C);
                sb.append(FIND);
                sb.append(FIND_I);
            } else {
                commands.add(SH);
                commands.add(SH_C);
                sb.append(GREP);
                sb.append(GREP_I);
                sb.append(GREP_M).append(maxline);
                if (before > 0) sb.append(GREP_B).append(before);
                if (after > 0) sb.append(GREP_C).append(after);
            }
            sb.append(StringUtil.BLANK).append(StringUtil.D_QUATE).append(search).append(StringUtil.D_QUATE);
            sb.append(StringUtil.BLANK).append(StringUtil.D_QUATE).append(filepath).append(StringUtil.D_QUATE);
            commands.add(sb.toString());
            Process process = Runtime.getRuntime().exec(commands.toArray(new String[0]));
            Thread readThread = new Thread(() -> {
                try {
                    BufferedInputStream in = new BufferedInputStream(process.getInputStream());
                    BufferedReader br = new BufferedReader(new InputStreamReader(in));
                    String line;
                    while ((line = br.readLine()) != null) {
                        if (!StringUtils.isEmpty(line)) {
                            lines.add(line);
                        }
                    }
                } catch (Exception ignored) {
                }
            });
            readThread.start();
            // 5秒后自动关闭
            new Thread(() -> ThreadUtil.delayRun(5000, () -> {
                readThread.interrupt();
                process.destroy();
            })).start();
            // 等待执行完成
            process.waitFor();
            // 等待读取完
            readThread.join();
        } catch (Exception ignored) {
        }
        if (CollectionUtils.isEmpty(lines)) return lines;
        if (isWindows) lines.remove(0);
        return lines;
    }

}
