## RabbitMq:rabbit2:



### 1. 简介

消息队列中间件（ Message Queue Middleware ，简称为MQ ）是指利用高效可靠的消息传递
机制进行与平台无关的数据交流，并基于数据通信来进行分布式系统的集成。通过提供消息传
递和消息排队模型，它可以在分布式环境下扩展进程间的通信。

- 点对点：基于队列
- 发布/订阅：主题作为消息传递的中介 (生产者将消息发布到主题，消费者从中取出)



#### 消息中间件的作用

- 解耦
- 冗余存储
- 扩展性
- 削峰
- 可恢复性
- 顺序保证
- 缓冲
- 异步通信



#### 起源

rabbitmq 使用 Erlang 语言实现 AMQP(advabced messge queuing protocol 高级消息队列协议) 协议

特点

- 可靠
- 灵活
- 扩展性
- 高可用性
- 多种协议
- 多语言客户端
- 管理界面
- 插件机制



### 2. 入门
#### 生产者消费者模型

![Rabbitmq架构模型](../img/Rabbitmq架构模型.png)

- Producer:

  创建消息(消息体 + 标签)

  消息体: payload 带有业务逻辑结构的数据

  标签: 表述消息

- Consumer:

  存入消息队列的只有消息体，消费者消费消息体 payload

- Broker:

  消息中间件的服务节点(exchanger + queue)  

```
流程
生产者包装数据，封装成消息，发送(Basic.Publish) 到 Broker
消费者订阅接收消息(Basic.Comsume 或 Basic.Get) 得到原始数据
```


#### Queue

消息存储在队列中

多个消费者可以订阅同一个队列，队列中的消息会被**平均分摊**(round robin轮询)给多个消费者，不是每个消费者都收到所有的消息

不支持广播



#### **交换机，路由键**，绑定

- Exchanger

  交换器：生产者将消息送到 Exchanger，交换器将消息路由到一个或多个队列中  

  ```
  producer -> exchanger -> queue1  
                        -> queue2
  ```

- RoutingKey:

  路由键： 指定消息的路由规则， 路由键 和 绑定键 联合使用生效

- Binding:

  绑定：通过绑定将交换器与队列关联起来，指定一个绑定键 BindingKey

  ```
  producer  ------------->  exchanger  ---------------->  queue1
             routingKey                 BindkingKey --->  queue2
  ```

RoutingKey：邮件上的地址

BindingKey：目的地



#### 交换器类型

- fanout:     将发送到该交换器的所有消息路由到**所有**与该交换器绑定的队列中

- direct:      将消息路由到BindingKey 与 RoutingKey **完全匹配**的队列中

- topic:       对匹配规则进行扩展(**模糊匹配**)

  BindingKey 和 RoutingKey 是 `.` 分割的字符串，可以存在两种字符做模糊匹配

  - `*` 匹配一个单词

  - `#` 匹配零个或多个单词

- headers:   不依赖路由键的匹配规则来路由信息，根据发送的消息的**内容中的headers**属性进行匹配



Connection: TCP连接(减少性能开销，便于管理)，创建 AMQP 信道，每个信道被指定唯一的 ID。

信道 Channel 是建立在 Connection 之上的虚拟连接,  RabbitMq 处理的每条 AMQP 指令都是通过信道完成的 (复用 TCP 连接)

![Connection与Channel](../img/Connection与Channel.png)





#### AMQP 0-9-1 协议介绍

AMQP: advanced message queuing protocol 高级消息队列协议

包含三层:

- Module layer:       最高层，定义供客户端调用的命令  

- Session layer:       中间层，将客户端发送的信息发送给服务器，将服务器的应答返回给客户端
  为客户端和服务器端提供可靠的**同步机制** 和 **错误处理**  

- Transport layer:   最底层， 传输二进制数据流，提供 **帧处理，信道复用，错误检测，数据表示**



#### AMQP 生产者/消费者 流转过程

```java
ConnectionFactory factory = new ConnectionFactory();
// ... 设置参数
// 从工厂中获取连接
Connection connection = factory.newConnection("生产者");
// 从连接中获取通道
Channel channel = connection.createChannel();
// 发送消息
channel.basicPublish(exchangeName, routeKey, MessageProperties.PERSISTENT_TEXT_PLAIN, message.getBytes(StandardCharsets.UTF_8));
// 关闭资源
channel.close();
connection.close();
```

连接过程涉及 6 个命令的交互

- Connection.Start / Start-Ok
- Connection.Tune / Connection.Tune-Ok
- Connection.Open / Connection.Open-Ok



- Basic.Qos / Qos-Ok
- Basic.Consume / Basic.Consume-Ok
- Basic.Deliver / Basic.Ack



- Channel.Close / Close-Ok



#### AMQP 命令 与 客户端的方法对应

命令类型

- Connection
- Channel
- Exchange
- Queue
- Basic
- Tx
- Confirm



### 3. 客户端开发向导



#### 连接RabbitMQ

```java
ConnectionFactory factory = new ConnectionFactory();
factory.setUserName / Password / Host / Port / VirtualHost
Connection conn = factory.newConnection();
// 或设置uri
factory.setUri("amqp://userName:password@ipAddress:port/virtualHost");

// Connection可以创建多个Channel， 但不能在线程间共享，应该为每个线程开辟一个 C
```


#### 使用交换器和队列

使用前要先声明 Declare
```java
// 声明一个交换器
channel.exchangeDeclare(exchangeName, "direct", true);
// 生命一个队列
String queueName = channel.queueDeclare().getQueue();
// 绑定 队列 和 交换器
channel.queueBind(queueName, exchangeName, routingKey)
```













