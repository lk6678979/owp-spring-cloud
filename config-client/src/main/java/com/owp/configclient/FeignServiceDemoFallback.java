package com.owp.configclient;

import org.springframework.stereotype.Component;

@Component
public class FeignServiceDemoFallback implements FeignServiceDemo {
    @Override
    public String demoHello() {
        return "熔断降级";
    }
}
