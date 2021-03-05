package com.up1234567.unistar.springcloud.discover.trace;

import com.up1234567.unistar.common.IUnistarConst;
import com.up1234567.unistar.common.discover.UnistarTraceData;
import com.up1234567.unistar.common.util.DateUtil;
import com.up1234567.unistar.common.util.SecurityUtil;
import com.up1234567.unistar.common.util.StringUtil;
import com.up1234567.unistar.springcloud.UnistarProperties;
import feign.RequestTemplate;
import feign.Target;
import lombok.Setter;
import org.springframework.util.StringUtils;

import javax.servlet.http.HttpServletRequest;
import java.net.URI;
import java.util.UUID;

/**
 * 基于Threadlocal的记录仪
 */
public final class UnistarTraceContext {

    private static final ThreadLocal<UnistarTraceRecorder> traceThreadLocal = new ThreadLocal<>();
    private static final ThreadLocal<String> groupThreadLocal = new ThreadLocal<>();
    private static final ThreadLocal<String> instanceThreadLocal = new ThreadLocal<>();

    @Setter
    private static UnistarProperties unistarProperties;

    @Setter
    private static UnistarTraceStater traceStater;

    /**
     * 获取跟踪ID，业务应用程序可以将TraceId加入到自己的应用逻辑中
     *
     * @return
     */
    public static String traceId() {
        UnistarTraceRecorder recorder = traceThreadLocal.get();
        return recorder == null ? StringUtil.EMPTY : recorder.getTraceId();
    }

    /**
     * 创建一个MD5码的跟踪ID，通过参数来确保分布式的唯一性
     *
     * @param unistarProperties
     * @return
     */
    private static String genTraceId(UnistarProperties unistarProperties) {
        return SecurityUtil.md5(unistarProperties.wrapUnistarParam().toQueryParam() + StringUtil.H_LINE + UUID.randomUUID().toString());
    }

    /**
     * 设置当前的工作组
     *
     * @param contextGroup
     */
    public static void setContextGroup(String contextGroup) {
        if (StringUtils.isEmpty(groupThreadLocal.get())) groupThreadLocal.set(contextGroup);
    }

    /**
     * 针对Feign引用，设定实例的地址
     *
     * @param realTarget
     */
    public static void setFeignTarget(String realTarget) {
        instanceThreadLocal.set(realTarget);
    }

    /**
     * 获取线程记录
     *
     * @param traceId
     * @return
     */
    private static UnistarTraceRecorder threadRecord(String traceId) {
        UnistarTraceRecorder recorder = traceThreadLocal.get();
        if (recorder == null) {
            // 检测是否存在父线程
            recorder = new UnistarTraceRecorder();
            if (StringUtils.isEmpty(traceId)) {
                recorder.setTraceId(genTraceId(unistarProperties));
            } else {
                recorder.setTraceId(traceId);
            }
            recorder.setTgroup(groupThreadLocal.get());
            if (StringUtils.isEmpty(recorder.getTgroup())) recorder.setTgroup("Inner Thread");
            traceThreadLocal.set(recorder);
            groupThreadLocal.remove();
        }
        return recorder;
    }

    /**
     * 增加监听，当前线程内
     *
     * @param traceId
     * @param matchedPath
     * @param watching    是否正在观察中
     * @param index       跟踪索引
     */
    public static void addTrace(String traceId, String matchedPath, boolean watching, int index) {
        // 记录
        UnistarTraceRecorder recorder = threadRecord(traceId);
        if (watching) recorder.setWatching(true);
        // 创建记录
        UnistarTraceData traceData = new UnistarTraceData();
        traceData.setTraceId(recorder.getTraceId());
        traceData.setIndex(index);
        traceData.setPath(matchedPath);
        traceData.setStartTime(DateUtil.now());
        traceStater.addTrace(recorder, traceData);
    }

    /**
     * Servlet请求的跟踪
     *
     * @param request
     * @param matchedPath
     * @param watching
     */
    public static void addServletTrace(HttpServletRequest request, String matchedPath, boolean watching) {
        // 首先先查看请求Header中是否已经包含TraceId
        // 如果已经包含，则是来自于上一个服务的请求
        String traceId = request.getHeader(IUnistarConst.HTTP_TRACE_ID);
        String watchedIndex = request.getHeader(IUnistarConst.HTTP_TRACE_WATCHING);
        // 添加路径记录
        addTrace(traceId, matchedPath, watching || watchedIndex != null, watchedIndex != null ? (Integer.parseInt(watchedIndex) + 1) : 0);
    }

    /**
     * Feign请求的跟踪
     *
     * @param template
     */
    public static void addFeignTrace(RequestTemplate template) {
        if (template.feignTarget() instanceof Target.HardCodedTarget) {
            String matchedUrl = StringUtil.wrapHttpUrl(template.feignTarget().url(), template.path());
            UnistarTraceRecorder recorder = threadRecord(null);
            addTrace(recorder.getTraceId(), matchedUrl, false, recorder.currentIndex() + 1);
            // 发起的应用传递至下一个服务
            template.header(IUnistarConst.HTTP_SERVICE, unistarProperties.getName());
            template.header(IUnistarConst.HTTP_SERVICE_GROUP, unistarProperties.getGroup());
            // 将跟踪ID传递至下一个服务
            template.header(IUnistarConst.HTTP_TRACE_ID, recorder.getTraceId());
            // 如果正在观察中，则加入Header中，将观察状态传递至下一个服务
            if (recorder.isWatching()) {
                // 标记为观察中
                template.header(IUnistarConst.HTTP_TRACE_WATCHING, String.valueOf(recorder.currentIndex()));
            } else {
                // 移除观察状态
                template.removeHeader(IUnistarConst.HTTP_TRACE_WATCHING);
            }
        }
    }

    /**
     * RestTemplate请求的跟踪
     *
     * @param url
     */
    public static void addRestTrace(URI url) {
        String matchedUrl = StringUtil.toBaseUrl(url);
        UnistarTraceRecorder recorder = threadRecord(null);
        addTrace(recorder.getTraceId(), matchedUrl, false, recorder.currentIndex() + 1);
    }

    /**
     * 当前处理的路径
     *
     * @return
     */
    public static String currentTracePath() {
        UnistarTraceRecorder recorder = traceThreadLocal.get();
        if (recorder == null) return null;
        UnistarTraceData traceData = recorder.getTraces().getFirst();
        if (traceData == null) return null;
        return traceData.getPath();
    }

    /**
     * 结束跟踪，每次结束Link的首位
     *
     * @param success 处理成功还是失败
     * @param error   处理失败的错误
     */
    public static void endTrace(boolean success, String error) {
        UnistarTraceRecorder recorder = traceThreadLocal.get();
        if (recorder == null) return;
        UnistarTraceData traceData = recorder.getTraces().getFirst();
        if (traceData == null) return;
        traceData.setSuccess(success);
        traceData.setError(error);
        String realTarget = instanceThreadLocal.get();
        if (realTarget != null) {
            traceData.setTarget(realTarget);
            instanceThreadLocal.remove();
        }
        traceStater.endTrace(recorder);
        if (recorder.isEmpty()) traceThreadLocal.remove();
    }

}
