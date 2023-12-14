package com.my.rpc.config;

import lombok.extern.slf4j.Slf4j;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;
import java.io.InputStream;

/**
 * xml解析器
 *
 * @Author : Williams
 * Date : 2023/12/14 14:22
 */
@Slf4j
public class XmlResolver {


    /**
     * 加载 xml 配置
     *
     * @param configuration 配置实例
     */
    public static void loadFromXml(Configuration configuration) {
        // 1. 创建一个 document
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            InputStream resourceAsStream = ClassLoader.getSystemClassLoader().getResourceAsStream("properties.xml");
            Document doc = builder.parse(resourceAsStream);

            XPathFactory xPathFactory = XPathFactory.newInstance();
            XPath xPath = xPathFactory.newXPath();

            //  解析序列化方式
            String serializer = parseString(doc, xPath, "/configuration/serializer");
            configuration.setSerializeMode(serializer);

            // 解析 port
            String port = parseString(doc, xPath, "/configuration/port");
            if (port != null && port.length() > 0) {
                configuration.setPort(Integer.parseInt(port));
            }
        } catch (Exception e) {
            log.error("An exception occurred while parsing the xml configuration file,   error = {}", e);
        }

    }

    /**
     * 获取一个节点的文本
     *
     * @param doc        文档对象
     * @param xPath      解析器
     * @param expression xml节点的表达式
     * @return 配置的实例
     */
    private static String parseString(Document doc, XPath xPath, String expression) {
        try {
            XPathExpression expr = xPath.compile(expression);
            Node evaluate = (Node) expr.evaluate(doc, XPathConstants.NODE);
            return evaluate.getTextContent();
        } catch (Exception e) {
            log.error("An exception occurred while parsing the expression.", e);
        }
        return null;
    }

    /**
     * 获取一个节点属性的值
     *
     * @param doc           文档对象
     * @param xPath         解析器
     * @param expression    xml节点的表达式
     * @param attributeName 节点名称
     * @return 配置的实例
     */
    private static String parseString(Document doc, XPath xPath, String expression, String attributeName) {
        try {
            XPathExpression expr = xPath.compile(expression);
            Node evaluate = (Node) expr.evaluate(doc, XPathConstants.NODE);
            return evaluate.getAttributes().getNamedItem(attributeName).getNodeValue();
        } catch (Exception e) {
            log.error("An exception occurred while parsing the expression.", e);
        }
        return null;
    }


    // 代码配置

    /**
     * 解析一个 xml节点, 返回一个实例对象
     *
     * @param doc        文档对象
     * @param xPath      解析器
     * @param expression xml节点的表达式
     * @param paramType  参数列表
     * @param param      参数
     * @return 配置的实例
     */
    private static <T> T parseObject(Document doc, XPath xPath, String expression, Class<?>[] paramType, Object... param) {
        try {
            XPathExpression expr = xPath.compile(expression);
            Node evaluate = (Node) expr.evaluate(doc, XPathConstants.NODE);
            Object instance = null;
            if (evaluate != null) {
                String className = evaluate.getAttributes().getNamedItem("class").getNodeValue();
                Class<?> aClass = Class.forName(className);
                if (paramType == null) {
                    instance = aClass.getConstructor().newInstance();
                } else {
                    instance = aClass.getConstructor(paramType).newInstance(param);
                }
            }
            return (T) instance;
        } catch (Exception e) {
            log.error("An exception occurred while parsing the expression.", e);
        }
        return null;
    }
}
