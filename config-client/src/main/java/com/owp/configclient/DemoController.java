package com.owp.configclient;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * @描述:
 * @公司:
 * @作者: 刘恺
 * @版本: 1.0.0
 * @日期: 2019-05-08 23:58:14
 */
@RestController
@RequestMapping("/demo")
public class DemoController {
    @Value("${business.name}")
    String name;

    @Value("${business.sex}")
    String sex;

    @GetMapping("/hello")
    public String demoHello() {
        return "姓名：" + name + ",性别:" + sex;
    }
}

