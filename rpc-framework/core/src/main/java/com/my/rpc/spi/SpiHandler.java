package com.my.rpc.spi;

import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * SPI 处理器
 *
 * @Author : Williams
 * Date : 2023/12/14 14:34
 */
@Slf4j
public class SpiHandler {

    private static final String BASE_PATH = "META-INF/fast-rpc-services";

    // 定义缓存, 保存 spi 相关的原始内容
    private static final Map<String, List<String>> SPI_CONTENT = new ConcurrentHashMap<>();

    // 每一个接口 对应的实现
    private static final Map<Class<?>, List<Object>> SPI_IMPL = new ConcurrentHashMap<>();


    static {
        ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
        URL resource = contextClassLoader.getResource(BASE_PATH);
        if (resource != null) {
            File file = new File(resource.getPath());
            if (file != null) {
                File[] childs = file.listFiles();
                if (childs != null) {
                    for (File child : childs) {
                        String key = child.getName();
                        List<String> value = getImplNames(child);
                        SPI_CONTENT.put(key, value);
                    }
                }
            }
        }
    }


    /**
     * 获取一个接口的实现类的实例
     *
     * @param clazz 接口 Class
     * @return 第一个实现类的实例
     */
    public static <T> T get(Class<?> clazz) {
        // 1. 优先取缓存
        List<Object> impls = SPI_IMPL.get(clazz);
        if (impls != null && impls.size() > 0) {
            return (T) impls.get(0);
        } else {
            // 2. 没有再构建实例, 并且放入缓存
            buildCache(clazz, clazz.getName());
            List<Object> objects = SPI_IMPL.get(clazz);
            if (objects != null && objects.size() > 0) {
                return (T) objects.get(0);
            } else {
                return null;
            }
        }

    }


    /**
     * 获取一个接口所有实现类的实例
     *
     * @param clazz 接口 Class
     * @return 所有实现类的实例
     */
    public static <T> List<T> getList(Class<?> clazz) {
        // 1. 优先取缓存
        List<Object> impls = SPI_IMPL.get(clazz);
        if (impls != null && impls.size() > 0) {
            return (List<T>) impls;
        } else {
            // 2. 没有再构建实例, 并且放入缓存
            buildCache(clazz, clazz.getName());
            return (List<T>) SPI_IMPL.get(clazz);
        }

    }


    private static List<Object> buildCache(Class<?> clazz, String name) {
        // 2. 没有再构建实例, 并且放入缓存
        List<String> implNames = SPI_CONTENT.get(name);
        List<Object> implList = new ArrayList<>();
        for (String implName : implNames) {
            try {
                Class<?> aClass = Class.forName(implName);
                Object o = aClass.getConstructor().newInstance();
                implList.add(o);
            } catch (InstantiationException | IllegalAccessException | InvocationTargetException |
                     NoSuchMethodException | ClassNotFoundException e) {
                log.error("实例化[{}]的实现时发生了异常 error ={}", implName, e);
            }
        }
        SPI_IMPL.put(clazz, implList);
        return implList;
    }


    /**
     * 获取文件的所有实现名称
     *
     * @param child 文件对象
     * @return 所有实现类的名称
     */
    private static List<String> getImplNames(File child) {
        List<String> values = new ArrayList<>();
        try (
                FileReader fileReader = new FileReader(child);
                BufferedReader bufferedReader = new BufferedReader(fileReader)
        ) {
            while (true) {
                String line = bufferedReader.readLine();
                if (line == null || "".equals(line)) {
                    break;
                } else {
                    values.add(line);
                }
            }
            return values;
        } catch (IOException e) {
            log.error("读取 spi 文件时发生异常");
        }
        return null;
    }

    public static void main(String[] args) {

    }
}
