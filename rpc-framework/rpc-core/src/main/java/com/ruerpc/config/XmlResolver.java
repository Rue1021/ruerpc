package com.ruerpc.config;

import com.ruerpc.IdGenerator;
import com.ruerpc.ProtocolConfig;
import com.ruerpc.compress.Compressor;
import com.ruerpc.compress.CompressorFactory;
import com.ruerpc.discovery.RegistryConfig;
import com.ruerpc.loadbalancer.LoadBalancer;
import com.ruerpc.serialize.Serializer;
import com.ruerpc.serialize.SerializerFactory;
import lombok.extern.slf4j.Slf4j;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.*;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.Objects;

/**
 * @author Rue
 * @date 2025/6/17 13:21
 */
@Slf4j
public class XmlResolver {
    /**
     * 使用原生的api从配置文件读取配置信息
     * @param configuration 配置实例
     */
    public void loadFromXml(Configuration configuration) {
        try {
            //1. 拿到一个document
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            // 禁用DTD校验：可以通过调用setValidating(false)方法来禁用DTD校验。
            factory.setValidating(false);
            // 禁用外部实体解析：可以通过调用setFeature(String name, boolean value)方法并将“http://apache.org/xml/features/nonvalidating/load-external-dtd”设置为“false”来禁用外部实体解析。
            factory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
            DocumentBuilder builder = factory.newDocumentBuilder();
            InputStream inputStream = ClassLoader
                    .getSystemClassLoader()
                    .getResourceAsStream("ruerpc.xml");
            Document doc = builder.parse(inputStream);

            //2. 获取一个xpath解析器（工厂模式）
            XPathFactory xPathFactory = XPathFactory.newInstance();
            XPath xPath = xPathFactory.newXPath();

            //3. 解析所有的标签
            configuration.setPort(resolvePort(doc, xPath));
            configuration.setAppName(resolveAppName(doc, xPath));

            configuration.setIdGenerator(resolveIdGenerator(doc, xPath));

            configuration.setRegistryConfig(resolveRegistryConfig(doc, xPath));

            //处理使用的压缩方式和序列化方式
            configuration.setCompressType(resolveCompressType(doc, xPath));
            configuration.setSerializeType(resolveSerializeType(doc,xPath));

            //配置新的压缩方式，纳入工厂
            ObjectWrapper<Compressor> compressorObjectWrapper = resolveCompressor(doc, xPath);
            CompressorFactory.addCompressor(compressorObjectWrapper);

            //配置新的序列化方式，纳入工厂
            ObjectWrapper<Serializer> serializerObjectWrapper = resolveSerializer(doc, xPath);
            SerializerFactory.addSerializer(serializerObjectWrapper);
            //configuration.setProtocolConfig(new ProtocolConfig(configuration.getSerializeType()));

            configuration.setLoadBalancer(resolveLoadBalancer(doc, xPath));


        } catch (ParserConfigurationException | IOException | SAXException e) {
            log.info("未发现配置文件或解析配置文件时发生异常，将使用默认配置");
        }
    }

    /**
     * 解析端口号
     *
     * @param doc
     * @param xPath
     * @return
     */
    private int resolvePort(Document doc, XPath xPath) {
        String expression = "/configuration/port";
        String portString = parseString(doc, xPath, expression);
        return Integer.parseInt(portString);
    }

    /**
     * 解析appName
     *
     * @param doc
     * @param xPath
     * @return
     */
    private String resolveAppName(Document doc, XPath xPath) {
        String expression = "/configuration/appName";
        return parseString(doc, xPath, expression);
    }

    /**
     * 解析id发号器
     * @param doc
     * @param xPath
     * @return id发号器实例
     */
    private IdGenerator resolveIdGenerator(Document doc, XPath xPath) {
        String expression = "/configuration/idGenerator";
        String aClass = parseString(doc, xPath, expression, "class");
        String dataCenterId = parseString(doc, xPath, expression, "dataCenterId");
        String machineId = parseString(doc, xPath, expression, "machineId");

        Class<?> clazz = null;
        try {
            clazz = Class.forName(aClass);
            Object instance = clazz.getConstructor(new Class[]{long.class, long.class})
                    .newInstance(Long.parseLong(dataCenterId), Long.parseLong(machineId));
            return (IdGenerator) instance;
        } catch (ClassNotFoundException | NoSuchMethodException |
                 InstantiationException | IllegalAccessException |
                 InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }

    private LoadBalancer resolveLoadBalancer(Document doc, XPath xPath) {
        String expression = "/configuration/loadBalancer";
        return parseObject(doc, xPath, expression, null);
    }

    private String resolveCompressType(Document doc, XPath xPath) {
        String expression = "/configuration/compressType";
        return parseString(doc, xPath, expression, "type");
        //return parseString(doc, xPath, expression);
    }

    private ObjectWrapper<Compressor> resolveCompressor(Document doc, XPath xPath) {
        String expression = "/configuration/compressor";
        Compressor compressor = parseObject(doc, xPath, expression, null);
        byte code = Byte.parseByte(Objects
                .requireNonNull(parseString(doc, xPath, expression, "code")));
        String name = parseString(doc, xPath, expression, "name");
        return new ObjectWrapper<>(code, name, compressor);
    }

    private String resolveSerializeType(Document doc, XPath xPath) {
        String expression = "/configuration/serializeType";
        return parseString(doc, xPath, expression, "type");
    }

    private ObjectWrapper<Serializer> resolveSerializer(Document doc, XPath xPath) {
        String expression = "/configuration/serializer";
        Serializer serializer = parseObject(doc, xPath, expression, null);
        byte code = Byte.parseByte(Objects
                .requireNonNull(parseString(doc, xPath, expression, "code")));
        String name = parseString(doc, xPath, expression, "name");
        return new ObjectWrapper<>(code, name, serializer);
    }


    private RegistryConfig resolveRegistryConfig(Document doc, XPath xPath) {
        String expression = "/configuration/registry";
        String url = parseString(doc, xPath, expression, "url");
        return new RegistryConfig(url);
    }




    /**
     * 获取一个节点的文本值 <port>7777</port>
     *
     * @param document
     * @param xPath
     * @param expression
     * @return
     */
    private String parseString(Document document,
                               XPath xPath,
                               String expression) {
        try {
            XPathExpression expr = xPath.compile(expression);
            Node targetNode = (Node) expr.evaluate(document, XPathConstants.NODE);
            return targetNode.getTextContent();
        } catch (XPathExpressionException e) {
            log.error("解析字符串时发生异常", e);
        }
        return null;
    }

    /**
     * 解析字符串，获取一个节点属性的值
     *
     * @param document      文档对象
     * @param xPath         xpath解析器
     * @param expression    xpath表达式
     * @param attributeName 节点名称
     * @return 节点的值
     */
    private String parseString(Document document,
                               XPath xPath,
                               String expression,
                               String attributeName
    ) {
        try {
            XPathExpression expr = xPath.compile(expression);
            Node targetNode = (Node) expr.evaluate(document, XPathConstants.NODE);
            return targetNode.getAttributes().getNamedItem(attributeName).getNodeValue();
        } catch (XPathExpressionException e) {
            log.error("解析字符串时发生异常", e);
        }
        return null;
    }

    /**
     * 解析一个节点，返回一个实例
     *
     * @param document   文档对象
     * @param xPath      解析器
     * @param expression 表达式
     * @param paramType  参数列表
     * @param params     可变参数，可以不传
     * @param <T>        泛型
     * @return 配置的实例
     */
    private <T> T parseObject(Document document,
                              XPath xPath,
                              String expression,
                              Class<?>[] paramType,
                              Object... params) {
        try {
            XPathExpression expr = xPath.compile(expression);
            Node targetNode = (Node) expr.evaluate(document, XPathConstants.NODE);
            String className = targetNode.getAttributes().getNamedItem("class").getNodeValue();
            Class<?> aClass = Class.forName(className);
            Object instance = null;
            if (paramType == null) {
                instance = aClass.getConstructor().newInstance();
            } else {
                instance = aClass.getConstructor(paramType).newInstance(params);
            }
            return (T) instance;
        } catch (ClassNotFoundException | XPathExpressionException | InvocationTargetException |
                 InstantiationException | IllegalAccessException | NoSuchMethodException e) {
            log.error("解析表达式时发生异常", e);
        }
        return null;
    }
}
