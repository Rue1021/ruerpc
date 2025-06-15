package com.ruerpc.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author Rue
 * @date 2025/6/15 19:06
 */
@Target(ElementType.TYPE)  //注解在类上定义
@Retention(RetentionPolicy.RUNTIME)  //在运行时生效
public @interface RueRPCApi {
}
