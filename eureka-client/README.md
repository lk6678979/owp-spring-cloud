# Eureka客户端（不使用配置中心）
## 1 Pom依赖：
```xml
   <!-- 继承springboot项目-->
    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>2.0.3.RELEASE</version>
        <relativePath/> <!-- lookup parent from repository -->
    </parent>

    <properties>
        <java.version>1.8</java.version>
        <spring-cloud.version>Finchley.RELEASE</spring-cloud.version>
        <spring_boot.version>2.0.3.RELEASE</spring_boot.version>
    </properties>

    <dependencies>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-actuator</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-test</artifactId>
            <scope>test</scope>
        </dependency>
        <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-starter-netflix-eureka-client</artifactId>
        </dependency>
        <!-- feign配置，非必须 -->
        <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-starter-openfeign</artifactId>
        </dependency>
    </dependencies>

    <dependencyManagement>
        <dependencies>
            <dependency>
                <groupId>org.springframework.cloud</groupId>
                <artifactId>spring-cloud-dependencies</artifactId>
                <version>${spring-cloud.version}</version>
                <type>pom</type>
                <scope>import</scope>
            </dependency>
        </dependencies>
    </dependencyManagement>

    <build>
        <plugins>
            <plugin>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-maven-plugin</artifactId>
                <configuration>
                    <!-- 是否打包为可执行jar包-->
                    <executable>true</executable>
                </configuration>
            </plugin>
        </plugins>
    </build>
``` 
## 2 代码，只需要在Application类上添加`@EnableEurekaClient`或者`@EnableDiscoveryClient`注解
```java
package com.owp.client;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
//import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
//可以使用@EnableEurekaClient和@EnableDiscoveryClient两种注解，
//@EnableEurekaClient是Eureka注册中心专用
//@EnableEurekaClient
//@EnableDiscoveryClient支持所有类型的注册中心
@EnableDiscoveryClient
//@EnableFeignClients是Feign远程调用所需配置，一般都会使用，后面再介绍
@EnableFeignClients
public class Application {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
```
```java
@RestController
@RequestMapping("/demo")
public class DemoController {
    @Value("${server.port}")
    String port;

        @GetMapping("/hello")
    public String demoHello(@RequestParam String name){
        return "hi "+name+",i am from port:" +port;
    }
}

```
## 3 配置文件
```yml
logging:
  #日志配置路径
  config: classpath:logback-spring-local.xml
server:
  port: 9999
spring:
  application:
    name: eureka-client
eureka:
  instance:
    #设置当前实例的主机名称(说明：该host将会在服务调用时使用，调用方需要配置该host对应的ip)
    #如果不想使用host使用ip在注册使用，则配置eureka.instance.perferIpAddress=true
    #preferIpAddress: true
    health-check-url-path: /actuator/health
    perferIpAddress: true
    #ipAddress: 192.168.0.40
  client:
    #指定服务注册中心地址，这里使用3个注册中心相互注册实现集群
    #配置中心，客户端访问，eureka.client.serviceUrl.defaultZone必须在bootstrap下面,
    #必须和spring.cloud.config.discovery.enabled、spring.cloud.config.discovery.serviceId写在一个文件里
   serviceUrl:
      defaultZone: http://127.0.0.1:8806/eureka/,http://127.0.0.1:8807/eureka/

```
## 4 启动
###### 5.1 使用maven打包项目
##### 5.2 启动jar
依次执行下面指令启动3个集群的服务：  
java -jar eureka-client-1.0.0.jar --server.port=9999  
java -jar eureka-client-1.0.0.jar --server.port=9998  
java -jar eureka-client-1.0.0.jar --server.port=9997  
###### 5.3 在服务注册中的前端页面可以看到注册的服务
![](https://github.com/lk6678979/image/blob/master/spring-cloud/eureka-client-center-2.jpg) 
### 5.4 浏览器输入127.0.0.1:9999/demo/hello?name=ezreal返回数据
![](https://github.com/lk6678979/image/blob/master/spring-cloud/eureka-client-2.jpg) 
