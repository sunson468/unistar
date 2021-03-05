package com.up1234567.unistar.central.support.aop;

import com.up1234567.unistar.central.api.model.BaseOutModel;
import com.up1234567.unistar.central.support.auth.exception.WebAuthFailureException;
import com.up1234567.unistar.central.support.auth.exception.WebAuthRightException;
import com.up1234567.unistar.common.IUnistarConst;
import lombok.CustomLog;
import org.springframework.web.bind.ServletRequestBindingException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@CustomLog(topic = IUnistarConst.DEFAULT_LOGGER_UNISTAR)
@RestControllerAdvice
public class RestControllerExceptionHandler {

    @ExceptionHandler(Exception.class)
    public Object handle(Exception e) {
        log.error("请求出现异常", e);
        BaseOutModel retModel = new BaseOutModel();
        retModel.setRetMsg("server.responsefail");
        return retModel;
    }

    @ExceptionHandler({WebAuthFailureException.class, ServletRequestBindingException.class})
    public Object handleBindingAuth(Exception e) {
        BaseOutModel retModel = new BaseOutModel();
        retModel.setRetCode(BaseOutModel.RET_LOGIN_INVALID);
        retModel.setRetMsg("server.notlogin");
        return retModel;
    }

    @ExceptionHandler(WebAuthRightException.class)
    public Object handle(WebAuthRightException e) {
        BaseOutModel retModel = new BaseOutModel();
        retModel.setRetMsg("server.noneright");
        return retModel;
    }

}
