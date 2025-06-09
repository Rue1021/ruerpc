package com.ruerpc.serialize;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author Rue
 * @date 2025/6/9 14:59
 *
 * 包装类
 */
@AllArgsConstructor
@NoArgsConstructor
@Data
public class SerializerWrapper {

    private byte code;
    private String serializeType;
    private Serializer serializer;
}
