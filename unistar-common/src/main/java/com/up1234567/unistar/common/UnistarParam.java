package com.up1234567.unistar.common;

import com.up1234567.unistar.common.util.QueryUtil;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@NoArgsConstructor
public class UnistarParam {

    public final static String UNISTAR_PARAM = "_unistar_param";

    private String namespace; // 所属空间
    private String name;      // 应用名称 ${spring.application.name}
    private String group;     // 应用分组
    private String host;      // 应用的Host
    private int port = -1;    // 应用的对外端口
    private String token;     // 应用连接需要验证Token

    public String toQueryParam() {
        return "?namespace=" + namespace
                + "&name=" + name
                + "&group=" + group
                + "&host=" + host
                ;
    }

    /**
     * @param query
     * @return
     */
    public static UnistarParam fromQuery(String query) {
        Map<String, String> attrs = QueryUtil.resolveQuery(query);
        UnistarParam param = new UnistarParam();
        param.setNamespace(attrs.get("namespace"));
        param.setName(attrs.get("name"));
        param.setGroup(attrs.get("group"));
        param.setHost(attrs.get("host"));
        return param;
    }
}
