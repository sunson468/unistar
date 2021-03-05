package com.up1234567.unistar.central.support.auth;

import com.up1234567.unistar.central.service.base.impl.BaseCacheService;
import com.up1234567.unistar.central.support.auth.exception.WebAuthFailureException;
import com.up1234567.unistar.central.support.auth.exception.WebAuthRightException;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.resource.ResourceHttpRequestHandler;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Component
public class WebAuthInterceptor implements HandlerInterceptor {

    @Autowired
    private BaseCacheService baseCacheService;

    /**
     * @param request
     * @param response
     * @param handler
     * @return
     * @throws Exception
     */
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        // 过滤其他请求
        if (handler instanceof ResourceHttpRequestHandler) return true;
        if (!request.getRequestURI().startsWith(AuthToken.PATH_PREFIX)) return true;
        // 从方法开始
        HandlerMethod handlerMethod = ((HandlerMethod) handler);
        ARoleAuth requestAuth = handlerMethod.getMethodAnnotation(ARoleAuth.class);
        if (requestAuth == null) {
            requestAuth = handlerMethod.getBeanType().getAnnotation(ARoleAuth.class);
        }
        // ===============================================================
        // 如果请求的方法设定了认证
        if (requestAuth != null) {
            // ===============================================================
            // 从Header中获取Token
            String tokenId = request.getHeader(AuthToken.HEADER_TOKEN);
            if (StringUtils.isEmpty(tokenId)) throw new WebAuthFailureException();
            AuthToken authToken = baseCacheService.fromTokenId(tokenId);
            if (authToken == null) throw new WebAuthFailureException();
            // 重新激活
            baseCacheService.reactiveToken(tokenId);
            // ===============================================================
            // 校验认证
            if (!requestAuth.value().equals(EAuthRole.ALL)
                    && !authToken.getRoles().contains(EAuthRole.SUPER)
                    && !authToken.getRoles().contains(requestAuth.value())) {
                throw new WebAuthRightException();
            }

            request.setAttribute(AuthToken.REQ_AUTH, authToken);
        }

        return true;
    }

}
