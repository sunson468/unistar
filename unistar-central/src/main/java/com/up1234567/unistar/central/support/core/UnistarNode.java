package com.up1234567.unistar.central.support.core;

import com.up1234567.unistar.common.util.StringUtil;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class UnistarNode {

    private String host;
    private int port;

    public UnistarNode(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public UnistarNode(String hostport) {
        String[] strs = hostport.split(StringUtil.COLON);
        this.host = strs[0];
        this.port = Integer.parseInt(strs[1]);
    }

    @Override
    public String toString() {
        return host + StringUtil.COLON + port;
    }
}
