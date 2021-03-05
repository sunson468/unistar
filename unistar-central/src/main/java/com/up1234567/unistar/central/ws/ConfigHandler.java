package com.up1234567.unistar.central.ws;

import com.up1234567.unistar.central.data.us.ConfigDepend;
import com.up1234567.unistar.central.service.base.impl.BaseService;
import com.up1234567.unistar.central.service.us.impl.ConfigService;
import com.up1234567.unistar.central.support.util.AesUtil;
import com.up1234567.unistar.central.support.ws.IUnistarWebSocketHandler;
import com.up1234567.unistar.central.data.us.Config;
import com.up1234567.unistar.central.support.ws.AUnistarWebSocketHandler;
import com.up1234567.unistar.common.IUnistarConst;
import com.up1234567.unistar.common.UnistarParam;
import com.up1234567.unistar.common.config.UnistarConfigData;
import com.up1234567.unistar.common.event.IUnistarEventConst;
import com.up1234567.unistar.common.util.JsonUtil;
import lombok.CustomLog;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.socket.WebSocketSession;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;

@CustomLog(topic = IUnistarConst.DEFAULT_LOGGER_UNISTAR)
@AUnistarWebSocketHandler(IUnistarEventConst.HANDLE_CONFIG)
public class ConfigHandler implements IUnistarWebSocketHandler<UnistarConfigData> {

    @Autowired
    private ConfigService configService;

    @Autowired
    private BaseService baseService;

    @Override
    public String handle(WebSocketSession session, UnistarConfigData params) {
        UnistarParam attrs = (UnistarParam) session.getAttributes().get(UnistarParam.UNISTAR_PARAM);
        return JsonUtil.toJsonString(findConfig(attrs.getNamespace(), attrs.getName(), params.getProfiles()));
    }

    /**
     * @param namespace
     * @param appname
     * @param profiles
     * @return
     */
    public Map<String, Object> findConfig(String namespace, String appname, List<String> profiles) {
        // 获取依赖
        ConfigDepend configDepend = configService.findConfigDepend(namespace, appname);
        Map<String, Object> properties = new HashMap<>();
        if (configDepend != null) {
            configService.listConfig(namespace, configDepend.dependsAsList()).forEach(c -> properties.putAll(JsonUtil.toMap(c.getProperties())));
        }
        // 获取所有的配置
        configService.listConfig(namespace, appname)
                .stream()
                .sorted(Comparator.comparing(Config::getProfile))
                .filter(c -> StringUtils.isEmpty(c.getProfile()) || (CollectionUtils.isNotEmpty(profiles) && profiles.contains(c.getProfile())))
                .forEach(c -> properties.putAll((JsonUtil.toMap(c.getProperties()))));
        // 解析替换
        return resolveConfig(properties);
    }

    /**
     * 解析配置
     *
     * @param properties
     */
    private Map<String, Object> resolveConfig(Map<String, Object> properties) {
        Map<String, Object> config = new HashMap<>();
        properties.forEach((k, v) -> {
            if (v instanceof String) {
                String val = String.valueOf(v);
                if (val.startsWith(Config.ENC_PREFIX)) {
                    // 解密
                    config.put(k, AesUtil.aesDecrypt(val.substring(2), baseService.findSetting().getConfigkey()));
                } else {
                    // 判断是否引用
                    Matcher matcher = Config.REGEX_IMPL.matcher(val);
                    if (matcher.find()) {
                        String matched = matcher.group();
                        config.put(k, properties.get(matched.substring(5, val.length() - 1)));
                    } else {
                        config.put(k, val);
                    }
                }
            }
        });
        return config;
    }

}
