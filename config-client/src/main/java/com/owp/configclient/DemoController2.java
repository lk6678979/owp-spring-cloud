package com.owp.configclient;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/demo")
public class DemoController2 {

    /**
    * 通过注入的方式使用feign调用接口
    */
    @Autowired
    private FeignServiceDemo feignServiceDemo;

    @GetMapping("/feignDemo")
    public String feignDemo() {
        return feignServiceDemo.demoHello();
    }
}

