package com.my.rpc.protocol;

import lombok.Data;

/**
 * 序列化协议配置类
 *
 * @Author : Williams
 * Date : 2023/12/5 15:24
 */
@Data
public class ProtocolConfig {

    private String protocolName;

    public ProtocolConfig(String protocolName) {
        this.protocolName = protocolName;
    }
}
