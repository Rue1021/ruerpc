package com.ruerpc.config;

import com.ruerpc.compress.Compressor;
import com.ruerpc.loadbalancer.LoadBalancer;
import com.ruerpc.serialize.Serializer;
import com.ruerpc.spi.SpiHandler;
import lombok.extern.slf4j.Slf4j;

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

        LoadBalancer loadBalancer = SpiHandler.get(LoadBalancer.class);
        if (loadBalancer != null) {
            configuration.setLoadBalancer(loadBalancer);
        }

        Compressor compressor = SpiHandler.get(Compressor.class);
        if (compressor != null) {
            configuration.setCompressor(compressor);
        }

        Serializer serializer = SpiHandler.get(Serializer.class);
        if (serializer != null) {
            configuration.setSerializer(serializer);
        }
    }
}
