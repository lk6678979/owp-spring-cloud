# Hystrix仪表盘监控-基于turbine实现
## 1. 监控服务项目创建、工程pom.xml文件中的依赖如下：
```yml
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
        <!-- eureka 客户端依赖 开始-->
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
        <!-- eureka 客户端依赖 结束-->
        <!-- hystrix仪表盘依赖,基于turbine实现集群模式 开始 -->
        <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-starter-netflix-hystrix</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-starter-netflix-hystrix-dashboard</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-starter-netflix-turbine</artifactId>
        </dependency>
        <!-- hystrix仪表盘依赖,基于turbine实现集群模式 结束 -->
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

## 2.服务端代码编写
### 2.1 JAVA代码，只需要在Application类上添加`@EnableHystrixDashboard`,`@EnableTurbine`,`@EnableCircuitBreaker`注解
```java
package com.owp.turbine;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.circuitbreaker.EnableCircuitBreaker;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.netflix.hystrix.dashboard.EnableHystrixDashboard;
import org.springframework.cloud.netflix.turbine.EnableTurbine;

@SpringBootApplication
@EnableDiscoveryClient
@EnableHystrixDashboard
@EnableTurbine
@EnableCircuitBreaker
public class Application {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}

```
### 2.2 配置文件application.yml

```
server:
  port: 8339
logging:
  config: classpath:logback-spring-local.xml
spring:
  application:
    name: hystrix-dashboard
#忽略权限拦截，外部系统，例如springboot admin 和mq刷新配置都需要权限
management:
  endpoints:
    web:
      exposure:
        #开放所有页面节点  默认只开启了health、info两个节点，注意yml的*要使用双引号
        include: "*"
  endpoint:
    health:
      #显示健康具体信息  默认不会显示详细信息
      show-details: ALWAYS
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
#turbine配置
turbine:
  #可以让同一主机上的服务通过主机名与端口号的组合来进行区分，
  #默认情况下会以host来区分不同的服务，这会使得在本机调试的时候，
  #本机上的不同服务聚合成一个服务来统计
  combine-host-port: true
  #配置监控服务的列表，表明监控哪些服务多个使用","分割
  app-config: config-client
  #用于指定集群名称，当服务数量非常多的时候，可以启动多个Turbine服务来构建不同的聚合集群，
  #而该参数可以用来区分这些不同的聚合集群，同时该参数值可以再Hystrix仪表盘中用来定位不同的聚合集群，
  #只需在Hystrix Stream的URL中通过cluster参数来指定
  #当clusterNameExpression: metadata['cluster']时，
  #假设想要监控的应用配置了eureka.instance.metadata-map.cluster: ABC，
  #则需要配置，同时turbine.aggregator.clusterConfig: ABC
  cluster-name-expression: metadata['cluster']
  aggregator:
    #指定聚合哪些集群,多个使用","分割，默认为default
    cluster-config: owp-demo
  #Turbine的收集端点
  #这里和被监控启动类里的 registrationBean.addUrlMappings("/hystrix.stream")一致
  #springboot1.X默认是/hystrix.stream，2.0默认/actuator/hystrix.stream
#  instanceUrlSuffix: /actuator/hystrix.stream
```
说明：  
    1.`turbine.aggregator.cluster-config`配置的cluster名，在所有需要被监控的微服务中，要保持`eureka.instance.metadata-map.cluster`配置与这个值相同  
    2.需要被监控的服务的application.name需要在`turbine.app-config`中配置，逗号分隔多个name
## 3 被监控的客户端配置
```java
package com.owp.configclient;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

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

```
## 3 启动
### 3.1 使用maven打包项目
### 3.2 启动jar
依次执行下面指令启动3个集群的注册中心：  
java -jar config-client-1.0.0.jar --server.port=8301  
java -jar config-client-1.0.0.jar --server.port=8302  
java -jar config-client-1.0.0.jar --server.port=8303  
### 3.3 前端测试获取配置文件
#### 在浏览器依次打开:
http://127.0.0.1:8301/demo  
http://127.0.0.1:8302/demo  
http://127.0.0.1:8303/demo  
#### git上的配置文件如下：
```yml
logging:
  config: classpath:logback-spring-local.xml
spring:
  rabbitmq:
      host: 192.168.0.90
      port: 5672
      username: sziov
      password: sziov
#忽略权限拦截，外部系统，例如springboot admin 和mq刷新配置都需要权限
management:
  endpoints:
    web:
      exposure:
        #开放所有页面节点  默认只开启了health、info两个节点，注意yml的*要使用双引号
        include: "*"
  endpoint:
    health:
      #显示健康具体信息  默认不会显示详细信息
      show-details: ALWAYS
#自定义配置
business: 
  name: 张三
  sex: 男
```
#### 浏览器访问结果如下：
![](https://github.com/lk6678979/image/blob/master/spring-cloud/config-client.jpg)  
