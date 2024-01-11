https://learn.lianglianglee.com/%e4%b8%93%e6%a0%8f/%e6%b6%88%e6%81%af%e9%98%9f%e5%88%97%e9%ab%98%e6%89%8b%e8%af%be/31%20%20%e5%8a%a8%e6%89%8b%e5%ae%9e%e7%8e%b0%e4%b8%80%e4%b8%aa%e7%ae%80%e5%8d%95%e7%9a%84RPC%e6%a1%86%e6%9e%b6%ef%bc%88%e4%b8%80%ef%bc%89%ef%bc%9a%e5%8e%9f%e7%90%86%e5%92%8c%e7%a8%8b%e5%ba%8f%e7%9a%84%e7%bb%93%e6%9e%84.md

Invocation：调用  invoke：调用
responseInvocation没看懂

//clazz的作用是让logger与实例关联
private static final Logger logger = LoggerFactory.getLogger(SerializeSupport.class);
entry是某方法参数，实际上是一个实例对象
entry.getClass()，getclass就是通过反射，获取对象的类的class对象
Class<?> 这样的写法，表示任意类型的Class对象

xxNameService是注册中心
xxService是服务

对于客户端代码来说，调用就是 helloService 这个本地对象，但实际上，真正的服务是在远程的服务端进程中实现的
客户端调用的是桩，调用桩的hello方法，实际上是发送了一个请求
请求包含：
1.请求的服务名：HelloService#hello
2.请求的所有参数：只有一个
(String)

服务端解析请求，根据服务名（helloservice）找有没有对应的服务提供者（helloServiceImpl），调用hello方法
客户端如何找到服务器地址：桩找实现，通过nameService注册中心

跨语言调用：只要保证在不同的编程语言中，使用相同的序列化协议，就可以实现跨语言的通信。

总体结构：
1.RPC 框架对外提供的所有服务定义在一个接口RpcAccessPoint 
1.1RpcAccessPoint 中： 1.addServiceProvider（service） 2.getRemoteService（reference）
1.2nameService注册中心  1.注册服务地址 registerService   2.查询服务地址 lookupService（根据服务名查uri）

二、如何实现高性能的异步通信、如何来将结构化的数据序列化成字节流，用于网络传输或者存储到文件中
1.使用序列化的模块，只需要依赖 SerializeSupport 这个静态类，调用它的序列化和反序列化方法就可以了
2.序列化实现的提供者，也只需要依赖并实现 Serializer 这个接口就可以了
（序列化只发送方法参数？）
（还有一个更复杂的序列化实现 MetadataSerializer，用于将注册中心的数据持久化到文件中）
3.netty收发数据
客户端通过NettyTransport异步发送请求，包含请求id和内容数组byte[]，
并创建一个ResponseFuture存到InFlightRequests，包含一个信号量，控制发送速度
最后，客户端通过 ResponseInvocation 异步接收服务端响应

三、rpc框架客户端
通过桩工厂，生成桩（服务类+transport）（模板构造类源码，编译，加载），调用桩的方法发送请求，接收响应


xxClass.getCanonicalName(),//规范化类名
依赖倒置：调用方依赖接口而非实现类，实现类依赖接口，实现调用和实现的解耦
新问题：谁来创建实现类实例，如果调用方创建就没法实现解耦
解决方法：依赖注入

四、
默认方法是在接口中提供一个默认的实现，这样在实现该接口的类中，如果没有为该方法提供具体实现，就会使用默认的方法。

uri示例
	URIs:
		rpc://localhost:9999

## 项目结构

| Module            | 说明              |
|-------------------|-----------------|
| client            | 例子：客户端          |
| server            | 例子：服务端          |
| rpc-api           | RPC框架接口         |
| hello-service-api | 例子：接口定义         |
| rpc-netty         | 基于Netty实现的RPC框架 |

***RPC 框架的实现原理？通信协议：序列化：服务注册与发现：负载均衡：服务调用：线程池和并发处理：超时和重试机制：安全性和身份验证：