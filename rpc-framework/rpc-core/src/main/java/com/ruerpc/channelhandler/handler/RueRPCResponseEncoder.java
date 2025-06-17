package com.ruerpc.channelhandler.handler;

import com.ruerpc.compress.Compressor;
import com.ruerpc.compress.CompressorFactory;
import com.ruerpc.serialize.Serializer;
import com.ruerpc.serialize.SerializerFactory;
import com.ruerpc.transport.message.MessageFormatConstant;
import com.ruerpc.transport.message.RueRPCResponse;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import lombok.extern.slf4j.Slf4j;

/**
 * @author Rue
 * @date 2025/5/28 15:17
 * 出站时第一个经过的处理器
 * <p>
 * 自定义协议编码器
 * 4B magic(魔数) -> rrpc.getBytes()
 * 1B version
 * 2B header length 首部的长度
 * 4B full length 报文总长度
 * 1B serialize
 * 1B compress
 * 1B requestType
 * 8B requestId
 * body
 */
@Slf4j
public class RueRPCResponseEncoder extends MessageToByteEncoder<RueRPCResponse> {
    @Override
    protected void encode(ChannelHandlerContext channelHandlerContext,
                          RueRPCResponse rueRPCResponse,
                          ByteBuf byteBuf) throws Exception {
        //4字节的魔数值
        byteBuf.writeBytes(MessageFormatConstant.MAGIC);
        //1个字节的版本号
        byteBuf.writeByte(MessageFormatConstant.VERSION);

        //2字节的首部长度HEADER_LENGTH
        byteBuf.writeShort(MessageFormatConstant.HEADER_LENGTH);

        //FULL_LENGTH
        byteBuf.writerIndex(byteBuf.writerIndex() + MessageFormatConstant.FULL_FIELD_LENGTH);

        //3个1字节的类型
        byteBuf.writeByte(rueRPCResponse.getResponseCode());
        byteBuf.writeByte(rueRPCResponse.getSerializeType());
        byteBuf.writeByte(rueRPCResponse.getCompressType());

        //8字节的请求id
        byteBuf.writeLong(rueRPCResponse.getRequestId());

        //时间戳
        byteBuf.writeLong(rueRPCResponse.getTimeStamp());

        //序列化请求体
        byte[] body = null;
        if (rueRPCResponse.getBody() != null) {
            Serializer serializer = SerializerFactory
                    .getSerializer(rueRPCResponse.getSerializeType()).getImpl();
            body = serializer.serialize(rueRPCResponse.getBody());
            //压缩
            Compressor compressor = CompressorFactory
                    .getCompressor(rueRPCResponse.getCompressType())
                    .getImpl();
            body = compressor.compress(body);
            //写入
            byteBuf.writeBytes(body);
        }

//        //写入
//        if (body != null) {
//
//        }
        int bodyLength = body == null ? 0 : body.length;

        //重新处理报文总长度
        int index = byteBuf.writerIndex(); //保存当前写指针位置
        byteBuf.writerIndex(MessageFormatConstant.LENGTH_FIELD_OFFSET); //移动写指针至总长位置
        byteBuf.writeInt(MessageFormatConstant.HEADER_LENGTH + bodyLength);
        byteBuf.writerIndex(index); //写指针归位

    }
}
