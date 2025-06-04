package com.ruerpc.transport.message;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author Rue
 * @date 2025/5/28 14:37
 * 服务调用方发起的请求内容
 */
@Data //添加getter setter toString equals方法的
@AllArgsConstructor
@NoArgsConstructor
@Builder //使用创建者设计模式构建实例
public class RueRPCRequest {

    //请求的id
    private long requestId;

    //请求类型
    private byte requestType;
    //压缩类型
    private byte compressType;
    //序列化的方式
    private byte serializeType;

    //具体的消息体
    private RequestPayload requestPayload;

}
