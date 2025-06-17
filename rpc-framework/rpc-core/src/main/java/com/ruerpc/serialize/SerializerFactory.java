package com.ruerpc.serialize;

import com.ruerpc.compress.Compressor;
import com.ruerpc.config.ObjectWrapper;
import com.ruerpc.serialize.impl.HessianSerializer;
import com.ruerpc.serialize.impl.JdkSerializer;
import com.ruerpc.serialize.impl.JsonSerializer;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Rue
 * @date 2025/6/9 14:51
 *
 * 两个工厂方法
 */
@Slf4j
public class SerializerFactory {

    private static final Map<String, ObjectWrapper<Serializer>> SERIALIZER_CACHE = new ConcurrentHashMap<>(8);
    private static final Map<Byte, ObjectWrapper<Serializer>> SERIALIZER_CACHE_CODE = new ConcurrentHashMap<>(8);

    static {
        ObjectWrapper<Serializer> jdkSerializerWrapper = new ObjectWrapper<>((byte) 1, "jdk", new JdkSerializer());
        ObjectWrapper<Serializer> jsonSerializerWrapper = new ObjectWrapper<>((byte) 2, "json", new JsonSerializer());
        ObjectWrapper<Serializer> hessianSerializerWrapper = new ObjectWrapper<>((byte) 3, "hessian", new HessianSerializer());
        SERIALIZER_CACHE.put("jdk", jdkSerializerWrapper);
        SERIALIZER_CACHE.put("json", jsonSerializerWrapper);
        SERIALIZER_CACHE.put("hessian", hessianSerializerWrapper);

        SERIALIZER_CACHE_CODE.put((byte)1, jdkSerializerWrapper);
        SERIALIZER_CACHE_CODE.put((byte)2, jsonSerializerWrapper);
        SERIALIZER_CACHE_CODE.put((byte)3, hessianSerializerWrapper);
    }

    /**
     * 使用工厂方法获取一个SerializerWrapper
     * @param serializeType 序列化类型
     * @return
     */
    public static ObjectWrapper<Serializer> getSerializer(String serializeType) {
        ObjectWrapper<Serializer> serializerObjectWrapper = SERIALIZER_CACHE.get(serializeType);
        if (serializerObjectWrapper == null) {
           log.error("未找到配置的序列化类型【{}】,默认选用jdk序列化工具",serializeType);
           return SERIALIZER_CACHE.get("jdk");
        }
        return serializerObjectWrapper;
    }

    /**
     * 使用工厂方法获取一个SerializerWrapper
     * @param serializeCode 序列化类型对应的编码
     * @return
     */
    public static ObjectWrapper<Serializer> getSerializer(byte serializeCode) {
        ObjectWrapper<Serializer> serializerObjectWrapper = SERIALIZER_CACHE_CODE.get(serializeCode);
        if (serializerObjectWrapper == null) {
            log.error("未找到配置的序列化类型【{}】，默认选用jdk序列化工具",serializeCode);
            return SERIALIZER_CACHE.get("jdk");
        }
        return serializerObjectWrapper;
    }

    /**
     * 添加一个新的序列化策略
     * @param serializerObjectWrapper 序列化器的包装类
     */
    public static void addSerializer(ObjectWrapper<Serializer> serializerObjectWrapper) {
        SERIALIZER_CACHE.put(serializerObjectWrapper.getName(), serializerObjectWrapper);
        SERIALIZER_CACHE_CODE.put(serializerObjectWrapper.getCode(), serializerObjectWrapper);
    }

}
