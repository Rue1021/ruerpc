package com.ruerpc.serialize.impl;

import com.ruerpc.serialize.Serializer;

/**
 * @author Rue
 * @date 2025/6/9 14:56
 */
public class JsonSerializer implements Serializer {
    @Override
    public byte[] serialize(Object object) {
        return new byte[0];
    }

    @Override
    public <T> T deserialize(byte[] bytes, Class<T> clazz) {
        return null;
    }
}
