package com.ruerpc.compress;

/**
 * @author Rue
 * @date 2025/6/10 13:09
 */
public interface Compressor {

    /**
     * 压缩字节数组
     * @param bytes 待压缩的字节数组
     * @return 压缩后的字节数组
     */
    byte[] compress(byte[] bytes);

    /**
     * 解压字节数组
     * @param bytes 待解压的字节数组
     * @return 解压后的字节数组
     */
    byte[] deCompress(byte[] bytes);
}
