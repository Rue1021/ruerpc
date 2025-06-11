package com.ruerpc.compress;

import com.ruerpc.compress.impl.GzipCompressor;
import com.ruerpc.serialize.SerializerWrapper;
import com.ruerpc.serialize.impl.HessianSerializer;
import com.ruerpc.serialize.impl.JdkSerializer;
import com.ruerpc.serialize.impl.JsonSerializer;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Rue
 * @date 2025/6/10 13:18
 *
 * 简单工厂设计模式
 */
@Slf4j
public class CompressorFactory {

    private static final ConcurrentHashMap<String, CompressorWrapper> COMPRESSOR_CACHE = new ConcurrentHashMap<>(8);
    private static final ConcurrentHashMap<Byte, CompressorWrapper> COMPRESSOR_CACHE_CODE = new ConcurrentHashMap<>(8);


    static {

        CompressorWrapper gzipCompressWrapper = new CompressorWrapper((byte)1, "gzip", new GzipCompressor());

        COMPRESSOR_CACHE.put("gzip", gzipCompressWrapper);

        COMPRESSOR_CACHE_CODE.put((byte)1, gzipCompressWrapper);

    }

    /**
     * 使用工厂方法获取一个CompressorWrapper
     * @param compressType 压缩类型
     * @return
     */
    public static CompressorWrapper getCompressor(String compressType) {
        CompressorWrapper compressorWrapper = COMPRESSOR_CACHE.get(compressType);
        if (compressorWrapper == null) {
            log.error("未找到配置的压缩算法【{}】，默认选用gzip压缩算法",compressType);
            return COMPRESSOR_CACHE.get("gzip");
        }
        return compressorWrapper;
    }

    public static CompressorWrapper getCompressor(byte compressCode) {
        CompressorWrapper compressorWrapper = COMPRESSOR_CACHE_CODE.get(compressCode);
        if (compressorWrapper == null) {
            log.error("未找到配置的压缩算法【{}】,默认选用gzip压缩算法",compressCode);
            return COMPRESSOR_CACHE_CODE.get((byte)1);
        }
        return compressorWrapper;
    }
}
