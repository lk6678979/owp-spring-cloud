# spingcloud配置服务中心
## 1. 项目创建、工程pom.xml文件中的依赖如下：
```
<dependencies>
	<dependency>
		<groupId>org.springframework.cloud</groupId>
		<artifactId>spring-cloud-starter-config</artifactId>
	</dependency>
	<dependency>
		<groupId>org.springframework.boot</groupId>
		<artifactId>spring-boot-starter-web</artifactId>
	</dependency>
	<dependency>
		<groupId>org.springframework.cloud</groupId>
		<artifactId>spring-cloud-starter-eureka</artifactId>
	</dependency>
	<dependency>
		<groupId>org.springframework.boot</groupId>
		<artifactId>spring-boot-starter-test</artifactId>
		<scope>test</scope>
	</dependency>
	<!--通过MQ动态刷新!开始-->
	<dependency>
		<groupId>org.springframework.boot</groupId>
		<artifactId>spring-boot-starter-actuator</artifactId>
	</dependency>
	<dependency>
		<groupId>org.springframework.cloud</groupId>
		<artifactId>spring-cloud-starter-bus-amqp</artifactId>
	</dependency>
	<!--通过MQ动态刷新!介绍-->
</dependencies>
```

## 2.代码编写
### 2.1 JAVA代码，仅需要在springboot工程的启动application类上添加`@EnableEurekaClient`注解
```
@SpringBootApplication
@EnableEurekaClient
public class ConfigClientApplication {
	public static void main(String[] args) {
		SpringApplication.run(ConfigClientApplication.class, args);
	}
}
```
### 2.2 创建2个配置文件，applycation.yml,bootstrap.yml
* <h4>`applycation.yml`(仅配置端口号，实际部署的时候一般会再次设置端口号，这里测试用)</h4> 

```
#服务启动端口号
server:
  port: 8301
```
* <h4>`bootstrap.yml`(优先于applycation.yml加载)</h4>

```
#bootstrap.yml中的配置会先于application.yml加载,
#config部分的配置必须先于application.yml被加载
#文件路径规则：name-profile
spring:
  application:
    name: demo
  cloud:
    config:
      #配置名称，在spring本地其实默认对应的是项目名字，如果不设置会去取spring.application.name
      #对应config-server端的{application}
      name: demo
      #通过URL获取配置中心服务器
      #uri: http://localhost:8409
      label: master
      #逗号分割多个profile
      profile: dev,datasource
      #从springcloud的注册中心的服务中获取配置
      discovery:
        enabled: true
        #注册到springcould注册中心的配置服务id
        serviceId: config-server
 #mq连接信息,`可以放到配置中心的配置文件中`
  rabbitmq:
    host: 39.108.128.40
    port: 5672
    username: lklcl
    password: liu2kai3
#刷新配置时去掉验证，不设置会报错，`可以放到配置中心的配置文件中`
management:
  security:
    enabled: false
eureka:
  client:
    #指定服务注册中心地址，这里使用3个注册中心相互注册实现集群
    #配置中心，客户端访问，eureka.client.serviceUrl.defaultZone必须在bootstrap下面,
    #必须和spring.cloud.config.discovery.enabled、spring.cloud.config.discovery.serviceId写在一个文件里
    serviceUrl:
      defaultZone: http://eureka.server.one:8806/eureka/,http://eureka.server.two:8807/eureka/,http://eureka.server.three:8808/eureka/
```
说明：  
    1.如果不需要使用mq做消息总线，可以去掉rabbitmq和management配置，pom中也去掉对应部分，但是在更新配置文件时，就需要每个使用配置中心的客户端都去做刷新操作/bus/refresh  
2.`GIT上的配置文件名称的格式都是application-profile,配置中心也是根据uri/search-paths/name-profire的格式寻找配置文件，如果name如果不设置就是该服务的spring.application.name`
### 2.3 测试属性读取的代码
```
@RestController
@RequestMapping("/demo")
public class DemoController {
    @Value("${server.port}")
    String port;
    @Value("${user.sex}")
    String name;
    @Value("${jdbc.username}")
    String jdbcUserName;

    @GetMapping("/hello")
    public String demoHello(@RequestParam String name) {
        return "hi " + name + ",i am from port:" + port;
    }


    @GetMapping(value = "/hi")
    public String hi() {
        return name + ":" + jdbcUserName;
    }
}
```
## 3 启动
### 3.1 使用maven打包项目
### 3.2 启动jar
依次执行下面指令启动3个集群的注册中心：  
java -jar lk-config-client-0.0.1-SNAPSHOT.jar --server.port=8301  
java -jar lk-config-client-0.0.1-SNAPSHOT.jar --server.port=8302  
java -jar lk-config-client-0.0.1-SNAPSHOT.jar --server.port=8303  
#### 3.3 前端测试获取配置文件
* <h4>在浏览器依次打开:</h4>  
http://127.0.0.1:8301/demo/hi  
http://127.0.0.1:8302/demo/hi  
http://127.0.0.1:8303/demo/hi  
* <h4>git上的配置文件如下：</h4> 
![](https://raw.githubusercontent.com/lk6678979/lk-spring-eureka-server/master/lk-eureka-server/readme/democonfig.jpg)
* <h4>浏览器访问结果如下：</h4> 
![](https://raw.githubusercontent.com/lk6678979/lk-spring-eureka-server/master/lk-eureka-server/readme/demotestconfig.jpg)  