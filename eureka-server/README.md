# spingcloud服务注册中心 

## 1. 使用spring boot initializer获取maven项目，http://start.spring.io/
### 1.1 组建选择如下：

![](https://raw.githubusercontent.com/lk6678979/lk-spring-eureka-server/master/lk-eureka-server/readme/iochoose.png)  

### 1.2 创建完的工程pom.xml文件中的依赖如下：

```
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
```

## 2.代码编写
### 2.1 JAVA代码，仅需要在springboot工程的启动application类上添加`@EnableEurekaServer`注解：
```
@SpringBootApplication
@EnableEurekaServer
public class EurekaServerApplication {

	public static void main(String[] args) {
		SpringApplication.run(EurekaServerApplication.class, args);
	}
}
```
### 2.2 创建3个配置文件，applycation-one.yml,applycation-two.yml,applycation-three.yml(用于注册中心高可用集群)，其中一个配置文件如下
```
#服务启动端口号
server:
  port: 8806
spring:
  profiles: one
  boot:
    admin:
      #springboot监控中心地址
      url: boot.admin.web:7000
  application:
    name: eureka-server
eureka:
  instance:
    #设置当前实例的主机名称(说明：该host将会在服务调用时使用，调用方需要配置该host对应的ip)
    #如果不想使用host使用ip在注册使用，则配置eureka.instance.perferIpAddress=true
    #preferIpAddress:true
    hostname: eureka.server.one
  client:
    #是否注册自身到eureka服务器
    registerWithEureka: true
    #是否检索服务
    fetchRegistry: false
    #指定服务注册中心地址，这里使用3个注册中心相互注册实现集群
    serviceUrl:
      defaultZone: http://eureka.server.three:8808/eureka/,http://eureka.server.two:8807/eureka/
#忽略权限拦截，外部系统，例如springboot admin 和mq刷新配置都需要权限
management:
  security:
    enabled: false
```
### 2.3 启动
#### 2.3.1 使用maven打包项目
#### 2.3.2 启动jar
依次执行下面指令启动3个集群的注册中心：  
	java -jar lk-eureka-server-0.0.1-SNAPSHOT.jar --spring.profiles.active=one  
	java -jar lk-eureka-server-0.0.1-SNAPSHOT.jar --spring.profiles.active=two  
	java -jar lk-eureka-server-0.0.1-SNAPSHOT.jar --spring.profiles.active=three
#### 2.3.3 域名配置
在计算机host目录下添加3个hostname：  
127.0.0.1 eureka.server.one  
127.0.0.1 eureka.server.two  
127.0.0.1 eureka.server.three  
## 2.可视化界面
在浏览器依次打开:  
http://eureka.server.one:8806/  
http://eureka.server.two:8807/  
http://eureka.server.three:8808/  
界面效果如下：
![](https://raw.githubusercontent.com/lk6678979/lk-spring-eureka-server/master/lk-eureka-server/readme/springcloudui.png)  
	
