package com.owp.configclient;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Feign的fallback测试类
 * 使用@FeignClient的fallback属性指定回退类
 */
@FeignClient(name = "feign-server", fallback = FeignServiceDemoFallback.class, path = "/demo")
public interface FeignServiceDemo {

    @GetMapping("/feigndemo")
    String demoHello();
}
