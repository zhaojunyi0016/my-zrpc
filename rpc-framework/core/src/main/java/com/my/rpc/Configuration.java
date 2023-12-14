package com.my.rpc;

import com.my.rpc.discovery.RegistryConfig;
import com.my.rpc.loadbalance.LoadBalancer;
import com.my.rpc.loadbalance.impl.RoundRobinLoadBalance;
import com.my.rpc.utils.SnowflakeIdGenerator;
import lombok.Getter;
import lombok.Setter;
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
 * 全局的配置类 , 代码配置->xml 配置->spi 配置-> 默认项
 *
 * @Author : Williams
 * Date : 2023/12/13 15:58
 */
@Slf4j
@Getter
@Setter
public class Configuration {

    // 端口号
    private int port = 8090;


    // app name
    private String appName = "default";


    // 配置 - 注册中心
    private RegistryConfig registryConfig = new RegistryConfig("zookeeper", "127.0.0.1:2181");


    // 配置 -负载均衡策略 -> 默认轮训
    private LoadBalancer loadBalancer = new RoundRobinLoadBalance();


    // 配置 - 序列化方式
    private String serializeMode = "jdk";


    // 配置 - 压缩协议
    private String compressMode = "gzip";


    // id 生成器
    private SnowflakeIdGenerator snowflakeIdGenerator = new SnowflakeIdGenerator(1, 2);


    // 读 xml

    public Configuration() {
        loadFromXml(this);
    }

    public static void main(String[] args) {
        Configuration configuration = new Configuration();
    }

    /**
     * 加载 xml 配置
     *
     * @param configuration 配置实例
     */
    private void loadFromXml(Configuration configuration) {
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
            this.setSerializeMode(serializer);

            // 解析 port
            String port = parseString(doc, xPath, "/configuration/port");
            if (port != null && port.length() > 0) {
                this.setPort(Integer.parseInt(port));
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
    private String parseString(Document doc, XPath xPath, String expression) {
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
    private String parseString(Document doc, XPath xPath, String expression, String attributeName) {
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
    private <T> T parseObject(Document doc, XPath xPath, String expression, Class<?>[] paramType, Object... param) {
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
