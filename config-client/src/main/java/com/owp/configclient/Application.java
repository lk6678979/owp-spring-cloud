package com.owp.configclient;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.netflix.hystrix.EnableHystrix;
import org.springframework.cloud.openfeign.EnableFeignClients;

//import org.springframework.cloud.netflix.eureka.EnableEurekaClient;

@SpringBootApplication
//可以使用@EnableEurekaClient和@EnableDiscoveryClient两种注解，
//@EnableEurekaClient是Eureka注册中心专用
//@EnableEurekaClient
//@EnableDiscoveryClient支持所有类型的注册中心
@EnableDiscoveryClient
//@EnableFeignClients是Feign远程调用所需配置，一般都会使用，后面再介绍
@EnableFeignClients
// 断路器
//@EnableHystrix
public class Application {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
