package com.up1234567.unistar.springcloud.core.event;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface AUnistarEventListener {

    /**
     * 事件名称
     *
     * @return
     */
    String value();

    String method() default "handle";

}
