package com.ruerpc.transport.message;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author Rue
 * @date 2025/5/28 14:37
 * 服务提供方回复的响应
 */
@Data //添加getter setter toString equals方法的
@AllArgsConstructor
@NoArgsConstructor
@Builder //使用创建者设计模式构建实例
public class RueRPCResponse {

    //请求的id
    private long requestId;

    //压缩类型
    private byte compressType;
    //序列化的方式
    private byte serializeType;

    private long timeStamp;

    //响应码
    private byte responseCode;

    //具体的消息体
    private Object body;

}
