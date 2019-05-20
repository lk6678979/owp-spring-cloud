# Zipkin链路跟踪(普通版+MQ版） 
## 1. 搭建Zipkin服务端
### 1.1 去zipkin的git中拉取源码自己免疫jar包，或者在git上直接下载jar（git有使用说明，建议都了解一下），地址：  
https://github.com/apache/incubator-zipkin/tree/master/zipkin-server  
![](https://github.com/lk6678979/image/blob/master/spring-cloud/zipkin-git.jpg)  
### 1.2 启动Zipkin服务端
#### 1.2.1 jar直接启动方式
```shell
java -jar zipkin-server-2.12.9-exec.jar --server.port=9411
```
#### 1.2.2 使用service启动
```shell
ln -s zipkin-server-2.12.9-exec.jar /etc/init.d/zipkin-server
chown U+X zipkin-server
service zipkin-server start --server.port=9411
```
#### 1.2.2 启动后访问ip:port/zipkin/,例如默认的http://127.0.0.1:9411/zipkin/,可以看到如下界面
![](https://github.com/lk6678979/image/blob/master/spring-cloud/zipkin-home-1.jpg)  
## 2 Zipkin客户端搭建
### 2.1 添加pom依赖
```xml
  <dependency>
      <groupId>org.springframework.cloud</groupId>
      <artifactId>spring-cloud-starter-zipkin</artifactId>
  </dependency>
```
⭐注意：如果pom中有依赖`spring-cloud-starter-stream-rabbit`或者`spring-cloud-starter-stream-rabbit`会使用MQ的方式去实现zipkin，如果要使用普通http版，要删除这2个依赖
### 2.2 application.yml添加配置
```yml
spring:
  sleuth:
    sampler:
      #收集追踪信息的比率，如果是0.1则表示只记录10%的追踪数据，如果要全部追踪，设置为1（实际场景不推荐，因为会造成不小的性能消耗）
      probability: 1.0
  zipkin:
    base-url: http://127.0.0.1:9411/ # 指定了 Zipkin 服务器的地址
    enabled: true
```
## 3 启动
### 3.1 这里我们使用前面搭建的项目config-client和feign-server进行测试
```shell
java -jar config-client-1.0.0.jar --server.port=8301
java -jar feign-server-1.0.0.jar --server.port=8333
```
### 3.2 调用config-client的接口（内部会使用feign访问feign-server的接口）
http://127.0.0.1:8301/demo/feignDemo?token=1  
### 查看zipkin界面
![](https://github.com/lk6678979/image/blob/master/spring-cloud/zipkin-home.jpg)  
![](https://github.com/lk6678979/image/blob/master/spring-cloud/zipkin-detail.jpg)  
![](https://github.com/lk6678979/image/blob/master/spring-cloud/zipkin-detail-info.jpg)  
![](https://github.com/lk6678979/image/blob/master/spring-cloud/zipkin-dependency.jpg)  
![](https://github.com/lk6678979/image/blob/master/spring-cloud/zipkin-dependency-detail.jpg)  

## 4. 搭建MQ版本
### 4.1 首先我们来看一下mq对应的配置清单
https://github.com/openzipkin/zipkin/blob/master/zipkin-server/src/main/resources/zipkin-server-shared.yml  
![](https://github.com/lk6678979/image/blob/master/spring-cloud/zipkin-yml.jpg)  
### 4.2 启动Zipkin服务端（指定mq配置）
#### 4.2.1 jar直接启动方式
```shell
java -jar zipkin-server-2.12.9-exec.jar --server.port=9411 --zipkin.collector.rabbitmq.addresses=192.168.0.90:5672 --zipkin.collector.rabbitmq.password=sziov --zipkin.collector.rabbitmq.username=sziov
```
#### 4.2.2 使用service启动
```shell
ln -s zipkin-server-2.12.9-exec.jar /etc/init.d/zipkin-server
chown U+X zipkin-server
service zipkin-server start  --server.port=9411 --zipkin.collector.rabbitmq.addresses=192.168.0.90:5672 --zipkin.collector.rabbitmq.password=sziov --zipkin.collector.rabbitmq.username=sziov
```
### 4.3 Zipkin客户端搭建
```xml
  <dependency>
      <groupId>org.springframework.cloud</groupId>
      <artifactId>spring-cloud-starter-zipkin</artifactId>
  </dependency>
  <dependency>
      <groupId>org.springframework.cloud</groupId>
      <artifactId>spring-cloud-starter-stream-rabbit</artifactId>
  </dependency>
```
### 2.2 application.yml添加配置
```yml
spring:
  sleuth:
    sampler:
      #收集追踪信息的比率，如果是0.1则表示只记录10%的追踪数据，如果要全部追踪，设置为1（实际场景不推荐，因为会造成不小的性能消耗）
      probability: 1.0
```
	
