package com.ruerpc.compress.impl;

import com.ruerpc.compress.Compressor;
import com.ruerpc.exceptions.CompressException;
import lombok.extern.slf4j.Slf4j;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 * @author Rue
 * @date 2025/6/11 10:45
 *
 * 使用gzip算法实现压缩和解压
 */
@Slf4j
public class GzipCompressor implements Compressor {

    @Override
    public byte[] compress(byte[] bytes) {

        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
             GZIPOutputStream gzipOutputStream = new GZIPOutputStream(baos)
        ) {
            gzipOutputStream.write(bytes);
            gzipOutputStream.finish();
            byte[] result = baos.toByteArray();
            if (log.isDebugEnabled()) {
                log.debug("对字节数组以gzip的方式完成了压缩，长度由【{}】压缩至【{}】",
                        bytes.length,result.length);
            }
            return result;
        } catch (IOException e) {
            log.error("对字节数组进行压缩时发生异常", e);
            throw new CompressException(e);
        }
    }

    @Override
    public byte[] deCompress(byte[] bytes) {

        try (
                ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
                GZIPInputStream gzipInputStream = new GZIPInputStream(bais);
        ){
            byte[] result = gzipInputStream.readAllBytes();
            if (log.isDebugEnabled()) {
                log.debug("以gzip的方式完成了解压缩,长度由【{}】解压至【{}】",
                        bytes.length,result.length);
            }
            return result;
        } catch (IOException e) {
            log.error("对字节数组进行解压时发生异常", e);
            throw new CompressException(e);
        }
    }
}
