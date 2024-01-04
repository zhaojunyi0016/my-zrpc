package com.my.rpc.api;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @Author : Williams
 * Date : 2023/12/16 12:05
 */
@RestController
public class TestController {

    @GetMapping("testProvide")
    public String hello() {
        return "你好, provide";
    }
}
