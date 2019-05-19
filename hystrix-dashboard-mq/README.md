# Hystrix仪表盘监控-基于Turbine实现
## 1. 监控服务项目创建、工程pom.xml文件中的依赖如下：
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
```yml
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
### 3.1 Pom添加依赖
```xml
        <!-- Hystrix监控依赖-->
        <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-starter-netflix-hystrix</artifactId>
        </dependency>
```
### 3.2 application.yml添加配置
```yml
eureka: 
 instance: 
  #自定义元数据：可以使用eureka.instance.metadata-map配置，
  #这些元数据可以在远程客户端中访问，但是一般不改变客户端行为，
  #除非客户端知道该元数据的含义。
  metadata-map: 
   #定义turbine监控中所属的集群名称
   #需要和hystrix监控服务中的turbine.aggregator.cluster-config配置使用相同的值
   cluster: owp-demo
```
### 3.3 配置UrlMappings(springboot2需要）
```java
package com.owp.configclient;

import com.netflix.hystrix.contrib.metrics.eventstream.HystrixMetricsStreamServlet;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class HystrixConfig {
    @Bean
    public ServletRegistrationBean getServlet(){
        HystrixMetricsStreamServlet streamServlet = new HystrixMetricsStreamServlet();
        ServletRegistrationBean registrationBean = new ServletRegistrationBean(streamServlet);
        registrationBean.setLoadOnStartup(1);
        registrationBean.addUrlMappings("/actuator/hystrix.stream");
        registrationBean.setName("HystrixMetricsStreamServlet");
        return registrationBean;
    }
}

```
## 4 启动
### 4.1 使用maven打包项目
### 4.2 启动服务端
java -jar hystrix-dashboard-1.0.0.jar --server.port=8339   
### 4.3 启动客户端（这里我们使用前面搭建的config-client)
java -jar config-client-1.0.0.jar --server.port=8301   
### 4.4 前端测试
#### 在浏览器查看监听（cluster参数和配置中保持一致）:
http://127.0.0.1:8339/turbine.stream?cluster=owp-demo  
 ![](https://github.com/lk6678979/image/blob/master/spring-cloud/hystrix-listent.png)
 #### 在浏览器查看监听UI
http://127.0.0.1:8339/hystrix/  
 ![](https://github.com/lk6678979/image/blob/master/spring-cloud/hystrix-ui.png)
#### 输入参数后进入详情页面：
![](https://github.com/lk6678979/image/blob/master/spring-cloud/hystrix-detail.png)  
#### UI说明：
![](https://github.com/lk6678979/image/blob/master/spring-cloud/hystrixs.png)  
# 说明：只有在被监控的微服务调用使用hystrix的接口后，才会有统计，例如使用feign并且配置了fallback
