package com.ruerpc.serialize;

/**
 * @author Rue
 * @date 2025/6/9 14:22
 *
 * 序列化器
 */
public interface Serializer {

    /**
     * 抽象的用来做序列化的方法
     * @param object 给定一个对象
     * @return 返回一个二进制数组
     */
    byte[] serialize(Object object);

    /**
     * 反序列化的方法
     * @param bytes 待反序列化的字节数组
     * @param clazz 目标类的class对象
     * @return 返回一个目标实例
     * @param <T> 目标类泛型
     */
    //Object deserialize(byte[] bytes); --这需要强转 -> 泛型
    <T> T deserialize(byte[] bytes, Class<T> clazz);
}
