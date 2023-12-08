package com.my.rpc.transport.message;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 请求调用方 -> 请求的接口方法的描述
 *
 * @Author : Williams
 * Date : 2023/12/8 11:16
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RequestPayload implements Serializable {

    private static final long serialVersionUID = -7656622703980212018L;

    /**
     * 接口名字, 全限定类名
     */
    private String interfaceName;

    /**
     * 方法名字
     */
    private String methodName;

    /**
     * 参数列表 : 参数类型 全限定类名
     */
    private Class<?>[] parametersType;

    /**
     * 参数列表 : 参数值
     */
    private Object[] parametersValue;

    /**
     * 返回值类型 全限定类名
     */
    private Class<?> returnType;

}
