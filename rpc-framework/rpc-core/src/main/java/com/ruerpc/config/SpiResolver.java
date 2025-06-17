package com.ruerpc.config;

import com.ruerpc.compress.Compressor;
import com.ruerpc.compress.CompressorFactory;
import com.ruerpc.loadbalancer.LoadBalancer;
import com.ruerpc.serialize.Serializer;
import com.ruerpc.serialize.SerializerFactory;
import com.ruerpc.spi.SpiHandler;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

/**
 * @author Rue
 * @date 2025/6/17 13:28
 */
@Slf4j
public class SpiResolver {

    /**
     * 将信息读取到configuration中/通过spi的方式加载配置项
     * @param configuration 配置上下文
     */
    public void loadFromSpi(Configuration configuration) {

        List<ObjectWrapper<LoadBalancer>> loadBalancerWrappersList = SpiHandler.getList(LoadBalancer.class);
        if (loadBalancerWrappersList != null && loadBalancerWrappersList.size() > 0) {
            configuration.setLoadBalancer(loadBalancerWrappersList.get(0).getImpl());
        }

        List<ObjectWrapper<Compressor>> compressorWrappers = SpiHandler.getList(Compressor.class);
        if (compressorWrappers != null && compressorWrappers.size() > 0) {
            compressorWrappers.forEach(CompressorFactory::addCompressor);
        }

        List<ObjectWrapper<Serializer>> serializerWrappers = SpiHandler.getList(Serializer.class);
        if (serializerWrappers != null && serializerWrappers.size() > 0) {
            serializerWrappers.forEach(SerializerFactory::addSerializer);
        }
    }
}
