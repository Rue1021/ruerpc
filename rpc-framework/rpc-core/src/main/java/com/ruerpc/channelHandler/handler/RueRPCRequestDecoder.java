package com.ruerpc.channelHandler.handler;

import com.ruerpc.enumeration.RequestType;
import com.ruerpc.transport.message.MessageFormatConstant;
import com.ruerpc.transport.message.RequestPayload;
import com.ruerpc.transport.message.RueRPCRequest;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import lombok.extern.slf4j.Slf4j;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.Arrays;

/**
 * @author Rue
 * @date 2025/5/28 18:41
 * 基于字段长度的帧解码器
 */
@Slf4j
public class RueRPCRequestDecoder extends LengthFieldBasedFrameDecoder {

    public RueRPCRequestDecoder() {
        super(MessageFormatConstant.MAX_FRAME_LENGTH,
                MessageFormatConstant.LENGTH_FIELD_OFFSET,
                MessageFormatConstant.FULL_FIELD_LENGTH,
                -(MessageFormatConstant.LENGTH_ADJUSTMENT),
                0);
    }

    @Override
    protected Object decode(ChannelHandlerContext ctx, ByteBuf in) throws Exception {
        Object decode = super.decode(ctx, in);
        if (decode instanceof ByteBuf byteBuf) {
            return decodeFrame(byteBuf);
        }
        return null;
    }

    private Object decodeFrame(ByteBuf byteBuf) {

        if (byteBuf.readableBytes() < MessageFormatConstant.LENGTH_FIELD_OFFSET) {
            throw new RuntimeException("Illegal byteBuf length");
        }

        // 标记当前读取位置，以便回退
        byteBuf.markReaderIndex();

        //1. 读取并验证魔数
        byte[] magicBytes = new byte[MessageFormatConstant.MAGIC_LENGTH];
        byteBuf.readBytes(magicBytes);
        if (!Arrays.equals(magicBytes, MessageFormatConstant.MAGIC)) {
            throw new RuntimeException("Invalid magic number: " + Arrays.toString(magicBytes));
        }

        //2. 读取版本号
        byte version = byteBuf.readByte();
        if (version != MessageFormatConstant.VERSION) {
            throw new RuntimeException("Unsupported version: " + version);
        }

        //3. 读取头部长度
        short headerLength = byteBuf.readShort();
        if (headerLength != MessageFormatConstant.HEADER_LENGTH) {
            throw new RuntimeException("Invalid header length: " + headerLength);
        }

        // 4. 读取完整消息长度
        int fullLength = byteBuf.readInt();
        // 检查是否有足够的数据 -> 减去已读的BASE_LENGTH和fullLength自身4字节
        if (byteBuf.readableBytes() < fullLength - MessageFormatConstant.LENGTH_FIELD_OFFSET - 4) {
            // 数据不足，重置读取位置
            byteBuf.resetReaderIndex();
            throw new RuntimeException("insufficient bytes");
        }

        // 5. 读取请求类型、序列化类型和压缩类型
        byte requestType = byteBuf.readByte();
        byte serializeType = byteBuf.readByte();
        byte compressType = byteBuf.readByte();

        // 6. 读取请求ID
        long requestId = byteBuf.readLong();

        RueRPCRequest rueRPCRequest = RueRPCRequest.builder()
                .requestType(requestType)
                .serializeType(serializeType)
                .compressType(compressType)
                .requestId(requestId)
                .build();

        //如果是心跳检测请求，就不需要再读请求体
        if (requestType == RequestType.HEART_BEAT.getId()) {
            return rueRPCRequest;
        }

        // 7. 读取请求体
        int bodyLength = fullLength - MessageFormatConstant.HEADER_LENGTH;
        byte[] bodyBytes = new byte[bodyLength];
        byteBuf.readBytes(bodyBytes);



        // 9. 反序列化请求体
        if (bodyLength > 0) {
            RequestPayload requestPayload = deserializeBody(bodyBytes);
            rueRPCRequest.setRequestPayload(requestPayload);
        }
        return rueRPCRequest;
    }

    private RequestPayload deserializeBody(byte[] bodyBytes) {
        try (ByteArrayInputStream bais = new ByteArrayInputStream(bodyBytes);
             ObjectInputStream ois = new ObjectInputStream(bais)) {
            return (RequestPayload) ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            log.error("反序列化时出现异常：", e);
            throw new RuntimeException(e);
        }
    }
}
