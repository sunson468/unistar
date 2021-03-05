package com.up1234567.unistar.common.limit;

import lombok.Data;

import java.util.List;

/**
 * 限流参数
 */
@Data
public class UnistarAppLimit {

    // 限制目标
    private String appname; // 路径所属应用
    private String path; // 应用路径
    private boolean before; // 是否请求前控制，是则在Feign请求前控制，否则在Controller拦截器内控制
    private boolean auto; // 触发规则，true：常态限流  false：熔断限流

    // 有效期
    private long startTime; // 有效开始时间
    private long endTime; // 有效结束时间

    // 熔断参数
    // period秒内出现errors次错误即可触发限流
    private int period; // 错误检测秒数
    private int errors; // 触发的错误次数
    private int recover; // 恢复秒数

    // 控制参数
    private int qps; // 每秒控制数
    private int warmup; // 预热时长（秒）

    // 处理方式
    private boolean fastfail; // 快速失败 | 等待
    private int timeout; // 等待时长（秒）

    // ===========================================
    // 临时用
    // 限制排除
    private List<String> whiteGroupList; // 白名单分组
    private List<String> whiteServiceList; // 白名单应用

}
