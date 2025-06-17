package com.ruerpc.compress;

import com.ruerpc.compress.impl.GzipCompressor;
import com.ruerpc.config.ObjectWrapper;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Rue
 * @date 2025/6/10 13:18
 *
 * 简单工厂设计模式
 */
@Slf4j
public class CompressorFactory {

    private static final Map<String, ObjectWrapper<Compressor>> COMPRESSOR_CACHE = new ConcurrentHashMap<>(8);

    private static final Map<Byte, ObjectWrapper<Compressor>> COMPRESSOR_CACHE_CODE = new ConcurrentHashMap<>(8);


    static {

        ObjectWrapper<Compressor> gzipCompressWrapper = new ObjectWrapper<>((byte)1, "gzip", new GzipCompressor());

        COMPRESSOR_CACHE.put("gzip", gzipCompressWrapper);

        COMPRESSOR_CACHE_CODE.put((byte)1, gzipCompressWrapper);

    }

    /**
     * 使用工厂方法获取一个CompressorWrapper
     * @param compressType 压缩类型
     * @return
     */
    public static ObjectWrapper<Compressor> getCompressor(String compressType) {
        ObjectWrapper<Compressor> compressorObjectWrapper = COMPRESSOR_CACHE.get(compressType);
        if (compressorObjectWrapper == null) {
            log.error("未找到配置的压缩算法【{}】，默认选用gzip压缩算法",compressType);
            return COMPRESSOR_CACHE.get("gzip");
        }
        return compressorObjectWrapper;
    }

    /**
     * 使用工厂方法获取一个CompressorWrapper
     * @param compressCode 压缩类型对应的编码
     * @return
     */
    public static ObjectWrapper<Compressor> getCompressor(byte compressCode) {
        ObjectWrapper<Compressor> compressorObjectWrapper = COMPRESSOR_CACHE_CODE.get(compressCode);
        if (compressorObjectWrapper == null) {
            log.error("未找到配置的压缩算法【{}】,默认选用gzip压缩算法",compressCode);
            return COMPRESSOR_CACHE_CODE.get((byte)1);
        }
        return compressorObjectWrapper;
    }

    /**
     * 添加一个新的压缩策略
     * @param compressorObjectWrapper 压缩类型的包装
     */
    public static void addCompressor(ObjectWrapper<Compressor> compressorObjectWrapper) {
        COMPRESSOR_CACHE.put(compressorObjectWrapper.getName(), compressorObjectWrapper);
        COMPRESSOR_CACHE_CODE.put(compressorObjectWrapper.getCode(), compressorObjectWrapper);
    }
}
