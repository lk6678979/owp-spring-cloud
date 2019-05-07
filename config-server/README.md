# 一、 centos7下安装rabbitmq3.6.1

# ★养成良好的习惯，安装好系统运行更新：
```
yum update -y
reboot  //一般情况不用重启，个人习惯。
```
# 1 安装依赖文件：
```
yum -y install gcc glibc-devel make ncurses-devel openssl-devel xmlto perl wget
```
# 2 安装erlang 语言环境：
## 2.1 下载安装：
```
wget http://www.erlang.org/download/otp_src_18.3.tar.gz  //下载erlang包
tar -xzvf otp_src_18.3.tar.gz  //解压
cd otp_src_18.3/ //切换到安装路径
./configure --prefix=/usr/local/erlang  //生产安装配置
make && make install  //编译安装
```
## 2.2 配置erlang环境变量：
```
vi /etc/profile  //在底部添加以下内容
    #set erlang environment
    ERL_HOME=/usr/local/erlang
    PATH=$ERL_HOME/bin:$PATH
    export ERL_HOME PATH

source /etc/profile  //生效
```
## 2.3 测试一下是否安装成功,在控制台输入命令erl
```
erl  //如果进入erlang的shell则证明安装成功，退出即可。
```
# 3 下载安装RabbitMQ：
## 3.1 下载安装
```
cd /usr/local  //切换到计划安装RabbitMQ的目录，我这里放在/usr/local
wget http://www.rabbitmq.com/releases/rabbitmq-server/v3.6.1/rabbitmq-server-generic-unix-3.6.1.tar.xz  //下载RabbitMQ安装包
xz -d rabbitmq-server-generic-unix-3.6.1.tar.xz
tar -xvf rabbitmq-server-generic-unix-3.6.1.tar
```
## 3.2 解压后多了个文件夹rabbitmq-server-3.6.1 ，重命名为rabbitmq以便记忆
```
mv rabbitmq_server-3.6.1/ rabbitmq
```
## 3.3 配置rabbitmq环境变量：
```
vi /etc/profile
    #set rabbitmq environment
    export PATH=$PATH:/usr/local/rabbitmq/sbin
source /etc/profile
```
## 3.4 启动服务：
```
rabbitmq-server -detached //启动rabbitmq，-detached代表后台守护进程方式启动。
```
## 3.5 查看状态，如果显示如下截图说明安装成功：
```
rabbitmqctl status
```
![](http://img.blog.csdn.net/20170419145307562?watermark/2/text/aHR0cDovL2Jsb2cuY3Nkbi5uZXQvU3VwZXJfUkQ=/font/5a6L5L2T/fontsize/400/fill/I0JBQkFCMA==/dissolve/70/gravity/SouthEast)

## 3.6 其他相关命令
```
启动服务：rabbitmq-server -detached【 /usr/local/rabbitmq/sbin/rabbitmq-server  -detached 】
查看状态：rabbitmqctl status【 /usr/local/rabbitmq/sbin/rabbitmqctl status  】
关闭服务：rabbitmqctl stop【 /usr/local/rabbitmq/sbin/rabbitmqctl stop  】
列出角色：rabbitmqctl list_users
```

# 4 配置网页插件：
## 4.1 首先创建目录，否则可能报错： 
```
mkdir /etc/rabbitmq
```
## 4.2 然后启用插件：
```
rabbitmq-plugins enable rabbitmq_management
```
# 5 配置防火墙：
## 5.1 配置linux 端口 15672 网页管理 5672 AMQP端口：
```
firewall-cmd --permanent --add-port=15672/tcp
firewall-cmd --permanent --add-port=5672/tcp
systemctl restart firewalld.service
```
## 5.2 现在在浏览器中输入服务器IP:15672 就可以看到RabbitMQ的WEB管理页面了
![](http://img.blog.csdn.net/20170419145800478?watermark/2/text/aHR0cDovL2Jsb2cuY3Nkbi5uZXQvU3VwZXJfUkQ=/font/5a6L5L2T/fontsize/400/fill/I0JBQkFCMA==/dissolve/70/gravity/SouthEast)
# 6 配置访问账号密码和权限：
## 6.1 默认网页是不允许访问的，需要增加一个用户修改一下权限，代码如下
```
rabbitmqctl add_user superrd superrd  //添加用户，后面两个参数分别是用户名和密码，我这都用superrd了。
rabbitmqctl set_permissions -p / superrd ".*" ".*" ".*"  //添加权限
rabbitmqctl set_user_tags superrd administrator  //修改用户角色
```
然后就可以远程访问了，然后可直接配置用户权限等信息。
## 6.2 登录：http://ip:15672 登录之后在admin里面把guest删除
![](http://img.blog.csdn.net/20170419150256878?watermark/2/text/aHR0cDovL2Jsb2cuY3Nkbi5uZXQvU3VwZXJfUkQ=/font/5a6L5L2T/fontsize/400/fill/I0JBQkFCMA==/dissolve/70/gravity/SouthEast)

## 问题

启动RabbitMQ后，没法访问Web管理页面

## 解决
RabbitMQ安装后默认是不启动管理模块的，所以需要配置将管理模块启动 
启动管理模块命令如下
```
rabbitmqctl start_app
rabbitmq-plugins enable rabbitmq_management
rabbitmqctl stop
```

# 二、 基于GIT、MQ的高可用配置中心
# spingcloud配置服务中心
## 整体架构
![](https://raw.githubusercontent.com/lk6678979/lk-spring-eureka-server/master/lk-eureka-server/readme/configjg.png)  

## 1. 项目创建、工程pom.xml文件中的依赖如下：
```
<dependencies>
	<dependency>
		<groupId>org.springframework.cloud</groupId>
		<artifactId>spring-cloud-config-server</artifactId>
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
	<!--基于MQ的配置实时更新依赖表!开始-->
	<dependency>
		<groupId>org.springframework.boot</groupId>
		<artifactId>spring-boot-starter-actuator</artifactId>
	</dependency>
	<dependency>
		<groupId>org.springframework.cloud</groupId>
		<artifactId>spring-cloud-starter-bus-amqp</artifactId>
	</dependency>
	<!--基于MQ的配置实时更新依赖表!结束-->
</dependencies>
```

## 2.代码编写
### 2.1 JAVA代码，仅需要在springboot工程的启动application类上添加`@EnableEurekaServer`和`@EnableEurekaClient`注解：
```
@SpringBootApplication
@EnableConfigServer
@EnableEurekaClient
public class ConfigServerApplication {
	public static void main(String[] args) {
		SpringApplication.run(ConfigServerApplication.class, args);
	}
}
```
### 2.2 创建3个配置文件，applycation.yml
```
#服务启动端口号
#服务启动端口号
server:
  port: 8409

spring:
  application:
    name: config-server
  cloud:
    config:
      server:
        git:
          #Git仓库地址
          #仓库地址下文件所属目录,可以直接在浏览器输入ip:post/{application}/{profile}等地址获取配置文件，文件名的-会自动被/识别为profile属性
          #配置文件只识别properties和yml，建议使用yml，可以处理中文乱码
          #获取git上的资源信息遵循如下规则
          #/{application}/{profile}[/{label}]
          #/{application}-{profile}.yml
          #/{label}/{application}-{profile}.yml
          #/{application}-{profile}.properties
          #/{label}/{application}-{profile}.properties
          uri: https://gitlab.com/lk-spring/config-center.git
          #{application}对应调用服务中心的其他服务的ID，spring.application.name,
          #前面一定要加/斜杠，源码是根据/识别的，然后使用string.replace去替换｛application｝
          #见源码AbstractScmAccessor.getSearchPaths（），和AbstractScmAccessor.getSearchLocations()方法
          #git路径下的目录
          search-paths: /{application}
          username: 6678979@qq.com
          password: liu2kai3...
  #mq连接信息
  rabbitmq:
    host: 39.108.128.40
    port: 5672
    username: lklcl
    password: liu2kai3
#刷新配置时去掉验证，不设置会报错
management:
  security:
    enabled: false
eureka:
  client:
    serviceUrl:
      defaultZone: http://eureka.server.one:8806/eureka/,http://eureka.server.two:8807/eureka/,http://eureka.server.three:8808/eureka/
```
说明：  
    1.如果不需要使用mq做消息总线，可以去掉rabbitmq和management配置，pom中也去掉对应部分，但是在更新配置文件时，就需要每个使用配置中心的客户端都去做刷新操作/bus/refresh  
2.`GIT上的配置文件名称的格式都是application-profile,配置中心也是根据uri/search-paths/name-profire的格式寻找配置文件,其中name和profire是【调用方】属性，在配置文件中配置，name在【调用方】如果不设置就是该服务的spring.application.name`
### 2.3 启动
#### 2.3.1 使用maven打包项目
#### 2.3.2 启动jar
依次执行下面指令启动3个集群的注册中心：  
java -jar lk-config-server-0.0.1-SNAPSHOT.jar --server.port=8409  
java -jar lk-config-server-0.0.1-SNAPSHOT.jar --server.port=8410  
java -jar lk-config-server-0.0.1-SNAPSHOT.jar --server.port=8411  
## 2.前端测试获取配置文件
在浏览器依次打开:  
http://http://127.0.0.1:8409/demo/dev/master/  
http://http://127.0.0.1:8410/demo/dev/master/  
http://http://127.0.0.1:8411/demo/dev/master/  
界面效果如下：
![](https://raw.githubusercontent.com/lk6678979/lk-spring-eureka-server/master/lk-eureka-server/readme/configdemo.png)  

## 3.修改配置后实时刷新  
### 方式一(手动)：使用/bus/refresh刷新，例如：http://http://127.0.0.1:8409/bus/refresh
### 方式二（GIT）：使用GIT的webhooks,直接在GIT上面的搜索栏搜索webhooks，PS：官网的免费版本没有这个功能


	
