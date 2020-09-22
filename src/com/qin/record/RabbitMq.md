## RabbitMq


### 简介



### 入门
生产者消费者模型  
produce | exchange queue | consumer  
Procedure:  
创建消息(消息体 + 标签)  
消息体: payload 带有业务逻辑结构的数据  
标签: 表述消息  
Consumer:  
存入消息队列的只有消息体，消费者消费消息体  
Broker:  
消息中间件的服务节点(exchanger + queue)  
```
流程
生产者包装数据，封装成消息，发送(Basic.Publish) 到 Broker
消费者订阅接收消息(Basic.Comsume 或 Basic.Get) 得到原始数据
```
Queue:  
存储消息  
多个消费者可以订阅同一个队列，队列中的消息会被平均分摊(round robin轮询)给多个消费者，
不是每个消费者都收到所有的消息  
Exchanger  
交换器： 生产者将消息送到Exchanger，交换器将消息路由到一个或多个队列中  
```
producer -> exchanger -> queue1  
                      -> queue2
```
RoutingKey:  
路由键： 指定消息的路由规则， 路由键 和 绑定键 联合使用生效  
Binding:  
绑定：通过绑定将交换器与队列关联起来，指定一个绑定键BindingKey
```
producer  ------------->  exchanger  ---------------->  queue1
           routingKey                 BindkingKey --->  queue2
```
#### 交换器类型
fanout: 将发送到该交换器的所有消息路由到所有与该交换器绑定的队列中  
direct: 将消息路由到BindingKey 与 RoutingKey 完全匹配的队列中  
topic: 对匹配规则进行扩展
```
BindingKey 和 RoutingKey 是.分割的字符串
可以存在两种字符做模糊匹配
* 匹配一个单词
# 匹配零个或多个单词
```
headers: 不依赖路由键的匹配规则来路由信息，根据发送的消息的内容中的headers属性进行匹配  
Connection: TCP连接(减少性能开销，便于管理)，创建AMQP信道，每个信道被指定唯一的ID。信道Channel是建立在Connection之上的虚拟连接,
RabbitMq处理的每条AMQPA指令都是通过信道完成的  
AMQP: advanced message queuing protocol 高级消息队列协议  
包含三层:
1. Module layer: 最高层，定义客户端调用的命令  
2. Session layer: 中间层，将客户端发送的信息发送给服务器，将服务器的应答返回给客户端
为客户端和服务器端提供可靠的同步机制和错误处理  
3. Transport layer: 最底层， 传输二进制数据流  

### 客户端开发向导
#### 连接RabbitMQ
```
ConnectionFactory factory = new ConnectionFactory();
factory.setUserName / Password / Host / Port / VirtualHost
Connection conn = factory.newConnection();
或设置uri
factory.setUri("amqp://userName:password@ipAddress:port/virtualHost");
Connection可以创建多个Channel， 但不能在线程间共享，
```
#### 使用交换器和队列
使用前要先声明 Declare
```
// 声明一个交换器
channel.exchangeDeclare(exchangeName, "direct", true);
// 生命一个队列
String queueName = channel.queueDeclare().getQueue();
// 绑定 队列 和 交换器
channel.queueBind(queueName, exchangeName, routingKey)
```














