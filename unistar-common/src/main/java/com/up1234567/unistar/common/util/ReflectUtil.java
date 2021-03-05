package com.up1234567.unistar.common.util;

import org.springframework.beans.BeanUtils;

import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

public final class ReflectUtil {

    /**
     * 获取继承接口内的泛型类
     *
     * @param instance
     * @param martched 匹配的接口
     * @return
     */
    public static Class findInstanceInterfaceParamType(Object instance, Class martched) {
        return findClassInterfaceParamType(instance.getClass(), martched);
    }

    /**
     * 获取继承接口内的泛型类
     *
     * @param clazz
     * @param martched 匹配的接口
     * @return
     */
    public static Class findClassInterfaceParamType(Class clazz, Class martched) {
        Type[] interfaces = clazz.getGenericInterfaces();
        for (Type t : interfaces) {
            if (t instanceof Class) continue;
            ParameterizedType pt = (ParameterizedType) t;
            if (!martched.isAssignableFrom((Class) pt.getRawType())) continue;
            Type[] types = pt.getActualTypeArguments();
            if (types == null || types.length <= 0) continue;
            return (Class) types[0];
        }
        return Object.class;
    }

    /**
     * 获取实例方法的参数类
     *
     * @param instance
     * @param methodName
     * @return
     */
    public static Class[] listMethodParamType(Object instance, String methodName) {
        Method method = BeanUtils.findMethodWithMinimalParameters(instance.getClass(), methodName);
        if (method.getParameterCount() == 0) return null;
        return method.getParameterTypes();
    }

}
