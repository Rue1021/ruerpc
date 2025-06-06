package com.ruerpc.transport.message;

/**
 * @author Rue
 * @date 2025/5/28 15:41
 */
public class MessageFormatConstant {

    public static final byte[] MAGIC = "rrpc".getBytes();
    public static final int MAGIC_LENGTH = MAGIC.length;

    public static final byte VERSION = 1;
    public static final int VERSION_LENGTH = 1;

    //首部信息的长度
    public static final short HEADER_LENGTH = (byte)(MAGIC.length + 1 + 2 + 4 + 1 + 1 + 1 + 8);
    //存放首部长度占用的字节数
    public static final int HEADER_FIELD_LENGTH = 2;

    //总长度占用的字节数
    public static final int FULL_FIELD_LENGTH = 4;


    public static final int MAX_FRAME_LENGTH = 1024 * 1024;

    //7
    public static final int LENGTH_FIELD_OFFSET = MAGIC_LENGTH + VERSION_LENGTH + HEADER_FIELD_LENGTH;

    public static final int LENGTH_ADJUSTMENT = LENGTH_FIELD_OFFSET + FULL_FIELD_LENGTH;
}
