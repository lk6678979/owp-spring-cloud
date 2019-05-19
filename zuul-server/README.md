# Zuul路由网关（使用配置中心）
## 1. 项目创建、工程pom.xml文件中的依赖如下：
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
        <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-starter-openfeign</artifactId>
        </dependency>
        <!-- eureka 客户端依赖 结束-->
        <!-- 配置中心模式，客户端依赖 开始-->
        <!-- 使用BUS总线，如果不使用消息总线，可以不配置-->
        <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-bus</artifactId>
        </dependency>
        <!-- 使用RabbitMq作为BUS总线的MQ，如果不使用消息总线，可以不配置-->
        <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-starter-stream-rabbit</artifactId>
        </dependency>
        <!-- 配置中心核心类 -->
        <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-starter-config</artifactId>
        </dependency>
        <!-- 配置中心模式，客户端依赖 结束-->
        <!-- zull网关依赖-->
        <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-starter-netflix-zuul</artifactId>
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

## 2.代码编写
#### JAVA代码，在Application类上添加`@EnableEurekaClient`或者`@EnableDiscoveryClient`注解
#### 再添加`@EnableZuulProxy`注解开启zuul网关
```java
package com.owp.zuul;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.netflix.zuul.EnableZuulProxy;
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
@EnableZuulProxy
public class Application {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}

```
### 2.2 创建2个配置文件，applycation.yml,bootstrap.yml
* <h4>`applycation.yml`(仅配置端口号，实际部署的时候一般会再次设置端口号，这里测试用)</h4> 

```
#服务启动端口号
server:
  port: 8501
```
* <h4>`bootstrap.yml`(优先于applycation.yml加载)</h4>

```
#bootstrap.yml中的配置会先于application.yml加载,
#config部分的配置必须先于application.yml被加载
#文件路径规则：name-profile
spring:
  application:
    name: zuul-server
  cloud:
    config:
      #配置名称，在spring本地其实默认对应的是项目名字，如果不设置会去取spring.application.name
      #对应config-server端的{application}
      name: zuul-server
      #通过URL获取配置中心服务器
      #uri: http://localhost:8409
      label: master
      #逗号分割多个profile
      profile: local
      #从springcloud的注册中心的服务中获取配置
      discovery:
        enabled: true
        #注册到springcould注册中心的配置服务id
        serviceId: config-server
eureka:
  instance:
    #设置当前实例的主机名称(说明：该host将会在服务调用时使用，调用方需要配置该host对应的ip)
    #如果不想使用host使用ip在注册使用，则配置eureka.instance.perferIpAddress=true
    #preferIpAddress: true
    preferIpAddress: true
    health-check-url-path: /actuator/health
  client:
    #指定服务注册中心地址，这里使用3个注册中心相互注册实现集群
    #配置中心，客户端访问，eureka.client.serviceUrl.defaultZone必须在bootstrap下面,
    #必须和spring.cloud.config.discovery.enabled、spring.cloud.config.discovery.serviceId写在一个文件里
   serviceUrl:
    defaultZone: http://127.0.0.1:8806/eureka/,http://127.0.0.1:8807/eureka/
```
说明：  
    1.如果不需要使用mq做消息总线，可以去掉rabbitmq和management配置，pom中也去掉对应部分，但是在更新配置文件时，就需要每个使用配置中心的客户端都去做刷新操作/bus/refresh  
2.`GIT上的配置文件名称的格式都是application-profile,配置中心也是根据uri/search-paths/name-profire的格式寻找配置文件，如果name如果不设置就是该服务的spring.application.name`
### 2.3 创建拦截器
```java
package com.owp.zuul;

import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;

public class LoginFilter extends ZuulFilter {
    private static Logger log = LoggerFactory.getLogger(LoginFilter.class);

    /**
     * filterType：返回一个字符串代表过滤器的类型，在zuul中定义了四种不同生命周期的过滤器类型，具体如下：
     * pre：路由之前
     * routing：路由之时
     * post： 路由之后
     * error：发送错误调用
     * filterOrder：过滤的顺序
     * shouldFilter：这里可以写逻辑判断，是否要过滤，本文true,永远过滤。
     * run：过滤器的具体逻辑。可用很复杂，包括查sql，nosql去判断该请求到底有没有权限访问。
     */
    @Override
    public String filterType() {
        return "pre";
    }

    @Override
    public int filterOrder() {
        return 0;
    }

    @Override
    public boolean shouldFilter() {
        return true;
    }

    @Override
    public Object run() {
        RequestContext ctx = RequestContext.getCurrentContext();
        HttpServletRequest request = ctx.getRequest();
        log.info(String.format("%s >>> %s", request.getMethod(), request.getRequestURL().toString()));
        Object accessToken = request.getParameter("token");
        if (accessToken == null) {
            ctx.setSendZuulResponse(false);
            ctx.setResponseStatusCode(401);
            try {
                ctx.getResponse().getWriter().write("NO TOKEN");
            } catch (Exception e) {
            }

            return null;
        }
        log.info("ok");
        return null;
    }
}
```
### 2.4 注入拦截器
```java
package com.owp.zuul;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FilterConfig {

    @Bean
    public LoginFilter getLoginFilter(){
        return new LoginFilter();
    }
}
```
## 3 启动
### 3.1 使用maven打包项目
### 3.2 启动jar
依次执行下面指令启动3个集群的注册中心：  
java -jar zuul-server-1.0.0.jar --server.port=8501  
java -jar zuul-server-1.0.0.jar --server.port=8502  
java -jar zuul-server-1.0.0.jar --server.port=8503  
### 3.3 启动用来测试的服务config-client
java -jar config-client-1.0.0.jar --server.port=8401  
#### git上的配置文件如下：
```yml
server:
  port: 8501
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
##timeout config
hystrix:
  command:
    default:
      execution:
        timeout:
          enabled: true
        isolation:
          thread:
            timeoutInMilliseconds: 10000
zuul:
 #配置被忽略的路径，配置后从网关无法再访问这些路径
 ignored-patterns: /*/*/inside/**
 #设置超时时间
 host:
  connect-timeout-millis: 10000
  socket-timeout-millis: 10000
#设置ribbon超时时间和重试机制
ribbon:
  ReadTimeout: 10000
  ConnectTimeout: 10000
  MaxAutoRetries: 2
  MaxAutoRetriesNextServer: 1
```
### 3.3 前端测试获取配置文件
#### 在浏览器打开(无TOKEN会被拦截）:
http://127.0.0.1:8501/config-client/demo/hello  
#### 浏览器访问结果如下：
![](https://github.com/lk6678979/image/blob/master/spring-cloud/zuul-no-token.png)  
#### 在浏览器打开(有TOKEN正常输出结果）:
http://127.0.0.1:8501/config-client/demo/hello?token=1  
#### 浏览器访问结果如下：
![](https://github.com/lk6678979/image/blob/master/spring-cloud/zuul-token.png)  
