package com.ruerpc.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.Random;

/**
 * @author Rue
 * @date 2025/6/17 20:21
 *
 * 异常重试注解加在方法上
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface TryTimes {
    int tryTimes() default 3;
    int intervalTime() default 2000;
}
