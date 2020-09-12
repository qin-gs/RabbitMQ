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
















