package com.ruerpc.serialize;

import com.ruerpc.serialize.impl.HessianSerializer;
import com.ruerpc.serialize.impl.JdkSerializer;
import com.ruerpc.serialize.impl.JsonSerializer;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Rue
 * @date 2025/6/9 14:51
 *
 * 两个工厂方法
 */
@Slf4j
public class SerializerFactory {

    private static final ConcurrentHashMap<String, SerializerWrapper> SERIALIZER_CACHE = new ConcurrentHashMap<>(8);
    private static final ConcurrentHashMap<Byte, SerializerWrapper> SERIALIZER_CACHE_CODE = new ConcurrentHashMap<>(8);

    static {
        SerializerWrapper jdkSerializerWrapper = new SerializerWrapper((byte) 1, "jdk", new JdkSerializer());
        SerializerWrapper jsosSerializerWrapper = new SerializerWrapper((byte) 2, "json", new JsonSerializer());
        SerializerWrapper hessianSerializerWrapper = new SerializerWrapper((byte) 3, "hessian", new HessianSerializer());
        SERIALIZER_CACHE.put("jdk", jdkSerializerWrapper);
        SERIALIZER_CACHE.put("json", jsosSerializerWrapper);
        SERIALIZER_CACHE.put("hessian", hessianSerializerWrapper);

        SERIALIZER_CACHE_CODE.put((byte)1, jdkSerializerWrapper);
        SERIALIZER_CACHE_CODE.put((byte)2, jsosSerializerWrapper);
        SERIALIZER_CACHE_CODE.put((byte)3, hessianSerializerWrapper);
    }

    /**
     * 使用工厂方法获取一个SerializerWrapper
     * @param serializeType 序列化类型
     * @return
     */
    public static SerializerWrapper getSerializer(String serializeType) {
        SerializerWrapper serializerWrapper = SERIALIZER_CACHE.get(serializeType);
        if (serializerWrapper == null) {
           log.error("未找到配置的序列化类型【{}】,默认选用jdk序列化工具",serializeType);
           return SERIALIZER_CACHE.get("jdk");
        }
        return serializerWrapper;
    }

    public static SerializerWrapper getSerializer(byte serializeCode) {
        SerializerWrapper serializerWrapper = SERIALIZER_CACHE_CODE.get(serializeCode);
        if (serializerWrapper == null) {
            log.error("未找到配置的序列化类型【{}】，默认选用jdk序列化工具",serializeCode);
            return SERIALIZER_CACHE.get("jdk");
        }
        return serializerWrapper;
    }
}
