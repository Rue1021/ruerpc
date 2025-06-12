package com.ruerpc.channelhandler.handler;

import com.ruerpc.compress.Compressor;
import com.ruerpc.compress.CompressorFactory;
import com.ruerpc.serialize.Serializer;
import com.ruerpc.serialize.SerializerFactory;
import com.ruerpc.transport.message.MessageFormatConstant;
import com.ruerpc.transport.message.RueRPCRequest;
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
public class RueRPCRequestEncoder extends MessageToByteEncoder<RueRPCRequest> {
    @Override
    protected void encode(ChannelHandlerContext channelHandlerContext,
                          RueRPCRequest rueRPCRequest,
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
        byteBuf.writeByte(rueRPCRequest.getRequestType());
        byteBuf.writeByte(rueRPCRequest.getSerializeType());
        byteBuf.writeByte(rueRPCRequest.getCompressType());

        //8字节的请求id
        byteBuf.writeLong(rueRPCRequest.getRequestId());

        byteBuf.writeLong(rueRPCRequest.getTimeStamp());

        byte[] body = null;
        if (rueRPCRequest.getRequestPayload() != null) {
            //序列化
            Serializer serializer = SerializerFactory
                    .getSerializer(rueRPCRequest.getSerializeType())
                    .getSerializer();
            body = serializer.serialize(rueRPCRequest.getRequestPayload());
            //压缩
            if (body != null && body.length != 0) {
                Compressor compressor = CompressorFactory
                        .getCompressor(rueRPCRequest.getCompressType())
                        .getCompressor();
                body = compressor.compress(body);
            }
            //写入请求体
            byteBuf.writeBytes(body);
        }

        int bodyLength = body == null ? 0 : body.length;


        //重新处理报文总长度
        int index = byteBuf.writerIndex(); //保存当前写指针位置
        byteBuf.writerIndex(MessageFormatConstant.LENGTH_FIELD_OFFSET); //移动写指针至总长位置
        byteBuf.writeInt(MessageFormatConstant.HEADER_LENGTH + bodyLength);
        byteBuf.writerIndex(index); //写指针归位

        if (log.isDebugEnabled()) {
            log.debug("请求【{}】已完成报文的编码", rueRPCRequest.getRequestId());
        }

    }

}
