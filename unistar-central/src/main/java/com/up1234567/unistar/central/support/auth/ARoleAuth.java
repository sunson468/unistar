package com.up1234567.unistar.central.support.auth;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface ARoleAuth {

    /**
     * 权限校验，OR的关系
     *
     * @return
     */
    EAuthRole value();

}
