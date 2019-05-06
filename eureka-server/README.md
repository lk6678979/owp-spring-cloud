# spingcloud服务注册中心 

### 1.1 创建一个普通的maven项目

### 1.2 创建完的工程pom.xml文件中的依赖如下：

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
        <!-- springboot的actuator监控依赖jar-->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-actuator</artifactId>
        </dependency>
        <!-- springCloud eureka注册中心核心jar -->
        <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-starter-netflix-eureka-server</artifactId>
        </dependency>
        <!-- springboot的测试依赖jar-->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-test</artifactId>
            <scope>test</scope>
        </dependency>
    </dependencies>

    <!-- springCloud版本依赖-->
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
### 2.1 JAVA代码，仅需要在springboot工程的启动application类上添加`@EnableEurekaServer`注解：
```java
package com.owp.eureka;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.server.EnableEurekaServer;

@SpringBootApplication
@EnableEurekaServer
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}

```
### 2.2 创建3个配置文件，applycation-one.yml,applycation-two.yml,applycation-three.yml(用于注册中心高可用集群)，其中一个配置文件如下，3个配置文件指定不同的server.port，并在defaultZone中配置其他2个yml启动的服务器的ip端口
```yml
server:
  #服务启动端口号
  port: 8806
spring:
  #服务名称
  application:
    name: eureka-server
#eureka服务注册发现中心配置
eureka:
  #服务配置
  server:
    #是否启用注册中心的保护机制，Eureka 会统计15分钟之内心跳失败的比例低于85%将会触发保护机制，不剔除服务提供者，如果关闭服务注册中心将不可用的实例正确剔除
    enable-self-preservation: false
  instance:
    #是否使用ip注册（默认使用域名注册）
    preferIpAddress: true
    #健康检查页面的URL，1.X版本默认/health，2.X版本默认/actuator/health，一般不需要更改
    health-check-url-path: /actuator/health
  client:
    #是否将注册中心本身也注册到注册中心中
    registerWithEureka: true
    #此客户端是否获取eureka服务器注册表上的注册信息（注册中心不会去调用其他服务，所以不需要获取注册信息）
    fetchRegistry: false
    serviceUrl:
      #注册中心URL，配置需要注入的配置中心，如果有2个注册中心则需要配置2个，如果有3个，则只需要注入除自己的另外2个
      defaultZone: http://127.0.0.1:8807/eureka/,http://127.0.0.1:8807/eureka/
management:
  endpoints:
    web:
      exposure:
        #开放所有页面节点  默认只开启了health、info两个节点
        include: "*"
  endpoint:
    health:
      #显示健康具体信息  默认不会显示详细信息
      show-details: ALWAYS
```
### 2.3 启动
#### 2.3.1 使用maven打包项目
#### 2.3.2 启动jar
依次执行下面指令启动3个集群的注册中心：  
	java -jar eureka-server-1.0.0.jar --spring.profiles.active=one  
	java -jar eureka-server-1.0.0.jar --spring.profiles.active=two  
	java -jar eureka-server-1.0.0.jar --spring.profiles.active=three  
## 2.4可视化界面
在浏览器依次打开:  
http://127.0.0.1:8806/  
http://127.0.0.1:8807/  
http://127.0.0.1:8808/  
界面效果如下：
![](https://github.com/lk6678979/image/blob/master/spring-cloud/eureka-ui.png)  
	
