## 该项目是测试feign而使用的服务端，用来被客户端使用feign调用，本身没有特殊配置，只是个普通项目
### 项目测试代码：
```java
package com.owp.feign;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/demo")
public class DemoController {
    @GetMapping("/feigndemo")
    public String demoHello() {
        return "成功调用";
    }
}
```
# 使用feign远程调用的微服务相关配置和代码
## 1. 在需要使用feign远程调用的微服务中添加pom依赖：
```yml
 <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-starter-openfeign</artifactId>
        </dependency>
```

## 2.代码编写
### 2.1 在Springboot启动类上添加`@EnableFeignClients`注解
```
package com.owp.configclient;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients
public class Application {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
```
### 2.2 编写feign调用接口
```java
package com.owp.configclient;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Feign的fallback测试类
 * 使用@FeignClient的fallback属性指定回退类
 * name:对应被调用的微服务的spring.application.name
 * fallback:调用失败的回调方法类
 * path：访问服务的基础路径
 */
@FeignClient(name = "feign-server", fallback = FeignServiceDemoFallback.class, path = "/demo")
public interface FeignServiceDemo {

    @GetMapping("/feigndemo")
    String demoHello();
}
```
### 2.3 编写feign调用失败回调类
```java
package com.owp.configclient;

import org.springframework.stereotype.Component;

@Component
public class FeignServiceDemoFallback implements FeignServiceDemo {
    @Override
    public String demoHello() {
        return "熔断降级";
    }
}
```
### 2.4 编写测试Controller
```java
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
```
### 2.5 application.yml中新增配置
```
#服务启动端口号
#springboot1.X默认是treu，2.X默认是false，需要手动开启
feign:
  hystrix:
    enabled: true
```
## 3 启动服务
### 3.1 使用maven打包项目
### 3.2 启动feign-server微服务和调用客户端，我们这俩使用前面的项目config-client
### 3.3 前端测试
#### 在浏览器访问（正常调用）:
http://127.0.0.1:8501/config-client/demo/feignDemo?token=1  
![](https://github.com/lk6678979/image/blob/master/spring-cloud/feign-success.jpg) 
#### 关闭feign-server服务，让客户端无法使用feign正常调用，然后前端查看
http://127.0.0.1:8501/config-client/demo/feignDemo?token=1  
![](https://github.com/lk6678979/image/blob/master/spring-cloud/feign-fail.jpg)  
