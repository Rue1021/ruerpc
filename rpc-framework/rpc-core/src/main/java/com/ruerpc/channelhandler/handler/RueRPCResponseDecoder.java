package com.ruerpc.channelhandler.handler;

import com.ruerpc.compress.Compressor;
import com.ruerpc.compress.CompressorFactory;
import com.ruerpc.serialize.Serializer;
import com.ruerpc.serialize.SerializerFactory;
import com.ruerpc.transport.message.MessageFormatConstant;
import com.ruerpc.transport.message.RueRPCResponse;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import lombok.extern.slf4j.Slf4j;

import java.util.Arrays;
import java.util.Date;

/**
 * @author Rue
 * @date 2025/5/28 18:41
 * 基于字段长度的帧解码器
 */
@Slf4j
public class RueRPCResponseDecoder extends LengthFieldBasedFrameDecoder {

    public RueRPCResponseDecoder() {
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

        // 5. 读取响应码、序列化类型和压缩类型
        byte responseCode = byteBuf.readByte();
        byte serializeType = byteBuf.readByte();
        byte compressType = byteBuf.readByte();

        // 6. 读取请求ID
        long requestId = byteBuf.readLong();

        //响应的时间戳
        long timeStamp = byteBuf.readLong();

        RueRPCResponse rueRPCResponse = RueRPCResponse.builder()
                .responseCode(responseCode)
                .serializeType(serializeType)
                .compressType(compressType)
                .requestId(requestId)
                .timeStamp(timeStamp)
                .build();

        // 7. 读取响应体
        int bodyLength = fullLength - MessageFormatConstant.HEADER_LENGTH;
        byte[] bodyBytes = new byte[bodyLength];
        byteBuf.readBytes(bodyBytes);

        byte[] deCompressedBody;
        if (bodyBytes != null && bodyBytes.length != 0) {
            //解压缩
            Compressor compressor = CompressorFactory
                    .getCompressor(compressType)
                    .getImpl();
            deCompressedBody = compressor.deCompress(bodyBytes);
            //反序列化
            Serializer serializer = SerializerFactory
                    .getSerializer(rueRPCResponse.getSerializeType()).getImpl();
            Object body = serializer.deserialize(deCompressedBody, Object.class);
            rueRPCResponse.setBody(body);
        }

        if (log.isDebugEnabled()) {
            log.debug("响应【{}】已经在调用端完成解码", rueRPCResponse.getRequestId());
        }

        return rueRPCResponse;
    }
}
