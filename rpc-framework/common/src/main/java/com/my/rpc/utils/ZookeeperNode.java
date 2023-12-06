package com.my.rpc.utils;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @Author : Williams
 * Date : 2023/12/5 18:21
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ZookeeperNode {
    private String nodePath;
    private byte[] data;
}
