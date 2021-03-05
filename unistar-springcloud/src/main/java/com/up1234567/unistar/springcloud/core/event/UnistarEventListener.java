package com.up1234567.unistar.springcloud.core.event;

import com.up1234567.unistar.common.exception.UnistarMethodInvokeException;
import com.up1234567.unistar.common.exception.UnistarMethodNotFoundException;
import com.up1234567.unistar.common.exception.UnistarMethodParamInvalidException;
import com.up1234567.unistar.common.util.JsonUtil;
import lombok.Data;
import org.springframework.beans.BeanUtils;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

@Data
public class UnistarEventListener {

    private String event;
    //
    private Object bean;
    private Method method;
    private Class paramType;

    /**
     * @param bean
     * @return
     */
    public static UnistarEventListener wrap(Object bean) {
        UnistarEventListener eventListener = new UnistarEventListener();
        AUnistarEventListener anno = bean.getClass().getAnnotation(AUnistarEventListener.class);
        eventListener.setEvent(anno.value());
        eventListener.setBean(bean);
        Method method = BeanUtils.findMethodWithMinimalParameters(bean.getClass(), anno.method());
        if (method == null) {
            throw new UnistarMethodNotFoundException("unistarEventListener must have it's handle method, default is handle");
        }
        eventListener.setMethod(method);
        if (method.getParameterCount() > 1) {
            throw new UnistarMethodParamInvalidException("unistarEventListener's handle method only has one argument at most");
        } else if (method.getParameterCount() == 1) {
            eventListener.setParamType(method.getParameterTypes()[0]);
        }
        return eventListener;
    }

    /**
     * @param arg
     */
    public void invoke(String arg) {
        try {
            if (paramType != null) {
                method.invoke(bean, arg != null ? JsonUtil.toClass(arg, paramType) : null);
            } else {
                method.invoke(bean);
            }
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new UnistarMethodInvokeException("fail to invoke unistarEventListener's handle method: " + e.getMessage());
        }
    }

}
