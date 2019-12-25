## Eureka服务注册中心内置的REST节点列表
转自https://www.jianshu.com/p/d4d69516bad6
### REST节点一览
Eureka Server内部通过JAX-RS(Java API for RESTful Web Services)规范提供了一系列的管理服务节点的请求节点，这样也保证了在非JVM环境运行的程序可以通过HTTP REST方式进行管理维护指定服务节点，所以只要遵循Eureka协议的服务节点都可以进行注册到Eureka Server。
Eureka提供的REST请求可以支持XML以及JSON形式通信，默认采用XML方式，REST列表如表所示：

请求名称|请求方式|HTTP地址|请求描述
-|-|-|-|
注册新服务|POST|/eureka/apps/{appID}|传递JSON或者XML格式参数内容，HTTP code为204时表示成功
取消注册服务|DELETE	/eureka/apps/{appID}/{instanceID}|HTTP code为200时表示成功
发送服务心跳|PUT	/eureka/apps/{appID}/{instanceID}|HTTP code为200时表示成功
查询所有服务|GET	/eureka/apps|HTTP code为200时表示成功，返回XML/JSON数据内容
查询指定|appID的服务列表	GET	/eureka/apps/{appID}|HTTP code为200时表示成功，返回XML/JSON数据内容
查询指定|appID&instanceID	GET	/eureka/apps/{appID}/{instanceID}|获取指定appID以及InstanceId的服务信息，HTTP code为200时表示成功，返回XML/JSON数据内容
查询指定instanceID服务列表|	GET	/eureka/apps/instances/{instanceID}|获取指定instanceID的服务列表，HTTP code为200时表示成功，返回XML/JSON数据内容
变更服务状态|	PUT	/eureka/apps/{appID}/{instanceID}/status?value=DOWN|服务上线、服务下线等状态变动，HTTP code为200时表示成功
变更元数据|	PUT	/eureka/apps/{appID}/{instanceID}/metadata?key=value|HTTP code为200时表示成功
查询指定IP下的服务列表|	GET	/eureka/vips/{vipAddress}|HTTP code为200时表示成功
查询指定安全IP下的服务列表|	GET	/eureka/svips/{svipAddress}|HTTP code为200时表示成功
