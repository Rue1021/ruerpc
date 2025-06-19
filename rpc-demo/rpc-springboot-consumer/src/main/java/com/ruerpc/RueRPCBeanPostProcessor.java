package com.ruerpc;

import com.ruerpc.annotation.RueRPCService;
import com.ruerpc.proxy.ProxyFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;
import java.util.Arrays;

/**
 * @author Rue
 * @date 2025/6/19 18:57
 */
@Component
public class RueRPCBeanPostProcessor implements BeanPostProcessor {

    //这个方法会拦截所有的bean创建，在一个bean初始化后被调用
    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        Field[] declaredFields = bean.getClass().getDeclaredFields();
        for (Field field : declaredFields) {
            RueRPCService annotation = field.getAnnotation(RueRPCService.class);
            if (annotation != null) {
                //获取一个代理
                Class<?> clazz = field.getType();
                Object proxy = ProxyFactory.getProxy(clazz);
                field.setAccessible(true);
                try {
                    field.set(clazz, proxy);
                } catch (IllegalAccessException e) {
                    throw new RuntimeException(e);
                }
            }
        }
        return bean;
    }
}
