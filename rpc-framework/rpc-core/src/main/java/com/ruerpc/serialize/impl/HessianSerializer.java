package com.ruerpc.serialize.impl;

import com.caucho.hessian.io.Hessian2Input;
import com.caucho.hessian.io.Hessian2Output;
import com.ruerpc.exceptions.SerializeException;
import com.ruerpc.serialize.Serializer;
import lombok.extern.slf4j.Slf4j;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;

/**
 * @author Rue
 * @date 2025/6/9 15:53
 */
@Slf4j
public class HessianSerializer implements Serializer {
    @Override
    public byte[] serialize(Object object) {
        if (object == null) {
            return null;
        }
        try (
                ByteArrayOutputStream baos = new ByteArrayOutputStream()
                ) {
            Hessian2Output hessian2Output = new Hessian2Output(baos);
            hessian2Output.writeObject(object);
            hessian2Output.flush();
            byte[] result = baos.toByteArray();
            if (log.isDebugEnabled()) {
                log.debug("使用hessian完成了对象【{}】的序列化操作,序列化后的字节数为【{}】", object,result.length);
            }
            return result;
        } catch (Exception e) {
            log.error("使用hessian序列化对象【{}】时发生异常", object);
            throw new SerializeException();
        }
    }

    @Override
    public <T> T deserialize(byte[] bytes, Class<T> clazz) {
        if (bytes == null || clazz == null) {
            return null;
        }
        try (
                ByteArrayInputStream bais = new ByteArrayInputStream(bytes)
        ) {
            Hessian2Input hessian2Input = new Hessian2Input(bais);
            Object object = hessian2Input.readObject();
            if (log.isDebugEnabled()) {
                log.debug("类【{}】已经使用hessian完成了反序列化操作", clazz);
            }
            return (T)object;
        } catch (IOException e) {
            log.error("使用hessian反序列化对象【{}】时发生异常", clazz);
            throw new SerializeException();
        }
    }
}
