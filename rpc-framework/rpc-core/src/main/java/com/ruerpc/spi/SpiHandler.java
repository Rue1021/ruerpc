package com.ruerpc.spi;

import com.ruerpc.config.ObjectWrapper;
import com.ruerpc.exceptions.SpiException;
import com.ruerpc.loadbalancer.LoadBalancer;
import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Rue
 * @date 2025/6/17 13:44
 */
@Slf4j
public class SpiHandler {

    //定义一个basePath
    private static final String BASE_PATH = "META-INF/ruerpc-services";

    //定义一个缓存，保存spi相关的原始内容
    private static final Map<String, List<String>> SPI_CONTENT = new ConcurrentHashMap<>(16);

    //缓存每一个接口对应实现的实例，我们要把这些实例构造出来
    private static final Map<Class<?>, List<ObjectWrapper<?>>> SPI_IMPLEMENT = new ConcurrentHashMap<>(32);

    //加载当前类后需要将spi信息进行保存，避免运行时频繁执行IO
    static {
        //加载当前工程中classPath中的资源
        //todo 加载jar包中classPath中的资源
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        URL fileUrl = classLoader.getResource(BASE_PATH);
        if (fileUrl != null) {
            File file = new File(fileUrl.getPath());
            File[] children = file.listFiles();
            if (children == null || children.length == 0) {
                for (File child : children) {
                    String key = child.getName();
                    List<String> value = getImplNames(child);
                    SPI_CONTENT.put(key, value);
                }
            }
        }
    }



    /**
     * 获取和当前接口相关的第一个实例
     * @param clazz 一个服务接口的Class实例
     * @return      实现类的实例
     * @param <T>
     */
    public static <T> ObjectWrapper<T> get(Class<T> clazz) {

        //1. 优先走缓存
        List<ObjectWrapper<?>> objectWrappers = SPI_IMPLEMENT.get(clazz);
        if (objectWrappers != null && objectWrappers.size() > 0) {
            return (ObjectWrapper<T>)objectWrappers.get(0);
        }

        //2. 构建缓存
        buildCache(clazz);

        List<ObjectWrapper<?>> result = SPI_IMPLEMENT.get(clazz);
        if (result == null || result.size() == 0) {
            log.error("当前接口没有spi配置");
            return null;
        }

        //3. 再次尝试获取
        return (ObjectWrapper<T>)SPI_IMPLEMENT.get(clazz).get(0);
    }

    /**
     * 获取所有和当前接口相关的实例
     * @param clazz 一个服务接口的Class实例
     * @return      实现类的实例集合
     * @param <T>
     */
    public synchronized static <T> List<ObjectWrapper<T>> getList(Class<T> clazz) {

        //1. 优先走缓存
        List<ObjectWrapper<?>> objectWrappers = SPI_IMPLEMENT.get(clazz);
        if (objectWrappers != null && objectWrappers.size() > 0) {
            return objectWrappers.stream().map(wrapper -> (ObjectWrapper<T>)wrapper).toList();
        }

        //2. 缓存中没有，就去构建缓存
        buildCache(clazz);

        //3. 构建好缓存后尝试查找
        objectWrappers = SPI_IMPLEMENT.get(clazz);
        if (objectWrappers != null && objectWrappers.size() > 0) {
            return objectWrappers.stream().map(wrapper -> (ObjectWrapper<T>)wrapper).toList();
        }
        return new ArrayList<>();
    }

    /**
     * 工具类，构建clazz的相关缓存
     * @param clazz 一个类的Class实例
     */
    private static void buildCache(Class<?> clazz) {
        //1. 通过clazz获取与之匹配的实现名称
        String name = clazz.getName();
        List<String> implNames = SPI_CONTENT.get(name);

        if (implNames == null || implNames.size() == 0) {
            return;
        }

        //2. 实例化所有的实现
        List<ObjectWrapper<?>> implInstances = new ArrayList<>();
        for (String implName : implNames) {
            try {
                //首先进行分割
                String[] codeAndTypeAndName = implName.split("-");
                if (codeAndTypeAndName.length != 3) {
                    throw new SpiException("illegal spi input format");
                }
                byte code = Byte.valueOf(codeAndTypeAndName[0]);
                String type = codeAndTypeAndName[1];
                String implementName = codeAndTypeAndName[2];
                Class<?> aClass = Class.forName(implementName);
                Object implInstance = aClass.getConstructor().newInstance();
                ObjectWrapper<?> objectWrapper = new ObjectWrapper<>(code, type, implInstance);
                implInstances.add(objectWrapper);
            } catch (ClassNotFoundException | InvocationTargetException | InstantiationException |
                     IllegalAccessException | NoSuchMethodException e) {
                log.error("实例化【{}】的实现时发生了异常", implName, e);
            }
        }
        SPI_IMPLEMENT.put(clazz, implInstances);

    }

    /**
     * 工具类，获取spi文件所有实现类的名称
     * @param child ruerpc-service下的一个文件对象
     * @return      实现类的全限定名集合
     */
    private static List<String> getImplNames(File child) {
        try (
                FileReader fileReader = new FileReader(child);
                BufferedReader bufferedReader = new BufferedReader(fileReader)
        )
        {
            List<String> implNames = new ArrayList<>();
            while (true) {
                String s = bufferedReader.readLine();
                if (s == null || "".equals(s)) break;
                implNames.add(s);
            }
            return implNames;
        } catch (IOException e) {
            log.error("读取spi文件时发生异常", e);
        }
        return null;
    }

}
