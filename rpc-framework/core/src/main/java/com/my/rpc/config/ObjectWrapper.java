package com.my.rpc.config;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @Author : Williams
 * Date : 2023/12/14 16:26
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ObjectWrapper<T> {

    private String name;
    private T impl;

}
