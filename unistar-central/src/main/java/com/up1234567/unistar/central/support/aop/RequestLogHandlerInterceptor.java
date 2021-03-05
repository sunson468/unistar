package com.up1234567.unistar.central.support.aop;

import com.up1234567.unistar.central.service.base.impl.OperatorService;
import com.up1234567.unistar.central.support.auth.AuthToken;
import com.up1234567.unistar.central.support.util.RequestUtil;
import com.up1234567.unistar.common.IUnistarConst;
import com.up1234567.unistar.common.util.DateUtil;
import lombok.CustomLog;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@CustomLog(topic = IUnistarConst.DEFAULT_LOGGER_UNISTAR)
@Component
public class RequestLogHandlerInterceptor extends HandlerInterceptorAdapter {

    private final static String REQUEST_START_TIME = "_REQ_startTime";

    @Autowired
    private OperatorService operatorService;

    /**
     * Controller处理前
     *
     * @param request
     * @param response
     * @param handler
     * @return
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        if (request.getRequestURI().startsWith(AuthToken.PATH_PREFIX)) {
            if (handler instanceof HandlerMethod) {
                request.setAttribute(REQUEST_START_TIME, System.currentTimeMillis());
            }
        }
        return true;
    }

    /**
     * 渲染模板之后
     *
     * @param request
     * @param response
     * @param handler
     * @param e
     */
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception e) {
        if (request.getRequestURI().startsWith(AuthToken.PATH_PREFIX)) {
            if (handler instanceof HandlerMethod) {
                Long start = (Long) request.getAttribute(REQUEST_START_TIME);
                long now = DateUtil.now();
                String ip = RequestUtil.clientIP(request);
                AuthToken authToken = (AuthToken) request.getAttribute(AuthToken.REQ_AUTH);
                log.debug("[{}] [{}] [{}] [{}] ===> [{}.{}], 耗时: {} ms, [{}]",
                        ip,
                        authToken == null ? "" : authToken.getAccount(),
                        request.getHeader(AuthToken.HEADER_TOKEN),
                        request.getRequestURI(),
                        ((HandlerMethod) handler).getBeanType().getSimpleName(),
                        ((HandlerMethod) handler).getMethod().getName(),
                        (now - start),
                        e == null ? "" : e.getMessage());
                // 对于登录的用户，记录登录操作
                if (authToken != null && ((HandlerMethod) handler).getMethod().isAnnotationPresent(AOperatorLog.class)) operatorService.addOperatorLog(authToken.getAccount(), request.getRequestURI(), ip, now);
            }
        }
    }

}
