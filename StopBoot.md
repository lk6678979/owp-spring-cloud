# Spring-boot 如何关闭
## 1. 将boot打包成可执行的自带脚本的jar包
### 1.1 修改pom
```xml
 <build>
        <plugins>
            <plugin>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-maven-plugin</artifactId>
                <configuration>
                    <executable>true</executable>
                </configuration>
            </plugin>
        </plugins>
    </build>
```
* 配置executable为true
### 1.2 部署
例如我们有个jar在/var/app目录下，jar为app.jar
```shell
sudo ln -s /var/app/app.jar /etc/init.d/app
chmod u+x app.jar
```
### 1.3 命令
```shell
#启动
service app start
#关闭
service app stop
#重启
service app restart
```
## 2. 使用restfull请求关闭
### 2.1 加入actuator jar包
```xml
<dependency>
			<groupId>org.springframework.boot</groupId>
			<artifactId>spring-boot-starter-actuator</artifactId>
</dependency>
```
### 2.2. 在application.properties/application.yml中加入配置
management.endpoint.shutdown.enabled=true
management.endpoints.web.exposure.include=shutdown
## 3.启动项目
在linux系统中执行一下代码
url -X POST http://host:port/actuator/shutdown
host 代表 服务的地址
port 代表 服务的端口

如果收到以下信息表示关闭成功
```
{
    "message": "Shutting down, bye..."
}
```

## 4.上面的有一个弊端 如果别人知道了ip地址和端口号就能远程操作你的服务了，怎么解决呢
* 在application.properties或application.yml中加入
```yml
#自定义管理端点的前缀(保证安全)
management.endpoints.web.base-path=/MyActuator
#自定义端口
management.server.port=12581(不能和你的tomcat服务器端口号一样)
#不允许远程管理连接(不允许外部调用保证安全)
management.server.address=127.0.0.1
```

* 这样访问地址就变成这样了
curl -X POST http://127.0.0.1:12581/MyActuator/shutdown

* 注意：在zuul中将MyActuator过滤掉，以防外网访问

## 5.如果这样还不够安全可以结合 spring security
### 5.1 加入 spring security jar包
<dependency> <groupId>org.springframework.boot</groupId> <artifactId>spring-boot-starter-security</artifactId> </dependency>
### 5.2 加入用户名和密码
```yml
spring.security.user.name=actuator
spring.security.user.password=123456
 ```
### 5.3
加入 spring security config
```java
@Configuration
public class ActuatorWebSecurityConfigurationAdapter extends WebSecurityConfigurerAdapter {

  @Override
  protected void configure(HttpSecurity http) throws Exception {
      http.csrf().disable().authorizeRequests().antMatchers("/").permitAll()
              .requestMatchers(EndpointRequest.toAnyEndpoint()).authenticated().and()
              //开启basic认证，若不添加此项，将不能通过curl的basic方式传递用户信息
              .httpBasic();
  }

/**
如果有swagger 并且想排除swagger的可以加入下面的代码
*/
  @Override
  public void configure(WebSecurity web) throws Exception {
      web.ignoring().antMatchers("/swagger-ui.html")
              .antMatchers("/webjars/springfox-swagger-ui/**")
              .antMatchers("/swagger-resources/**")
              .antMatchers("/v2/api-docs");
  }

}
```
### 5.4 执行新的命令
curl -X POST -u user:password http://host:port/actuator/shutdown

最后执行
curl -X POST -u actuator:123456 http://host:port/actuator/shutdown


