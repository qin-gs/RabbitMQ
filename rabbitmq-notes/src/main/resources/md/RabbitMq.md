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

// Connection可以创建多个Channel， 但不能在线程间共享，应该为每个线程开辟一个 Channel
```


#### 使用交换器和队列

使用前要先声明 declare
```java
// 声明一个交换器
channel.exchangeDeclare(exchangeName, "direct", true);
// 声明一个队列
// 该队列，对当前应用中同一个 Connection 层面可用，同一个 Connection 的不同 Channel 可共用，会在应用连接断开时自动删除
String queueName = channel.queueDeclare().getQueue();
// 绑定 队列 和 交换器
channel.queueBind(queueName, exchangeName, routingKey);
```



声明交换机

```java
// 声明交换机的参数，返回一个 Ok 命令
Exchange.DeclareOk exchangeDeclare(
    String exchange, // 交换机名称
    BuiltinExchangeType type, // 交换机类型 fanout, direct, topic
    boolean durable, // 是否持久化
    boolean autoDelete, // 是否自动删除 (自动删除的前提是至少有一个队列或者交换器与这个交换器绑定， 之后所有与这个交换器绑定的队列或者交换器都与此解绑)
    boolean internal, // 是否是内置的 (如果是 true，客户端程序无法直接向这个交换机发送消息，只能通过交换机路由到交换机的方式)
    Map<String, Object> arguments // 一些结构化参数
) throws IOException;

// 这样声明交换机不需要客户端返回(一般不使用)
void exchangeDeclareNoWait(String exchange,
    BuiltinExchangeType type,
    boolean durable,
    boolean autoDelete,
    boolean internal,
    Map<String, Object> arguments) throws IOException;

// 检测交换机是否被使用
Exchange.DeclareOk exchangeDeclarePassive(String name) throws IOException;


// 删除交换机
Exchange.DeleteOk exchangeDelete(
    String exchange, // 交换机名称
    boolean ifUnused // 设置是否在交换器没有被使用的情况下删除 (如果 isUnused 设置为 true ，则只有在此交换器没有被使用的情况下才会被删除：如果设置 false ，则无论如何这个交换器都要被删除)
) throws IOException;
```



声明队列

```java
// 声明队列
Queue.DeclareOk queueDeclare(
    String queue, // 队列名称
    boolean durable, // 是否持久化
    boolean exclusive, //是否排他 (排他队列基于连接可见；：即使该队列是持久化的，一旦连接关闭或者客户端退出，该排他队列都会被自动删除，这种队列适用于一个客户端同时发送和读取消息的应用场景。)
    boolean autoDelete, // 是否自动删除 (至少有一个消费者连接到这个队列，之后所有与这个队列连接的消费者都断开时，才会自动删除；为生产者客户端创建这个队列，或者没有消费者客户端与这个队列连接时，都不会自动删除这个队列。)
    Map<String, Object> arguments // 一些参数
) throws IOException;


// 声明队列不需要客户端返回
// 检测队列是否存在
// 删除队列

// 清空队列消息
Queue.PurgeOk queuePurge(String queue) throws IOException;
```



绑定/解绑 交换机 和 队列

```java
Queue.BindOk queueBind(
    String queue, // 队列名称
    String exchange, // 交换机名称
    String routingKey, // 绑定队列 和 交换机 的路由键
    Map<String, Object> arguments // 一些参数
) throws IOException;

Queue.UnbindOk queueUnbind(
    String queue, 
    String exchange, 
    String routingKey, 
    Map<String, Object> arguments 
) throws IOException;
```



绑定/解绑 交换机 和 交换机

```java
// 绑定后消息从 source 交换机转发到 des
Exchange.BindOk exchangeBind(
    String destination, 
    String source, 
    String routingKey, 
    Map<String, Object> arguments
) throws IOException;
```





#### 发送消息

```java
// 发送消息
void basicPublish(
    String exchange, // 交换器名称
    String routingKey, // 路由键
    boolean mandatory, // 
    boolean immediate, // 
    BasicProperties props, // 消息属性集合
    byte[] body // 消息体
) throws IOException;

// 消息属性集合 14 个
// contentType, contentEncoding, Headers, deliverMode, priority
// correlationId, replyTo, expiration, messageId, timestamp
// type, userId, appId, clusterId
```



#### 消费消息

- 推 push

  通过持续订阅的方式消费消息

  ```java
  String basicConsume(
      String queue, // 队列名称
      boolean autoAck, // 是否自动确认
      String consumerTag, // 消费者标签 (用来区分消费者)
      boolean noLocal, // 设为 true：不能将同一个 Connection 中生产者发送的消息传送给这个 Connection 中的消费者
      boolean exclusive, // 是否排他
      Map<String, Object> arguments, // 设置消费者的参数
      Consumer callback // 消费者的回调函数 (处理 mq 推送过来的消息)
  ) throws IOException;
  ```

  消费者客户端需要考虑线程安全问题



- 拉 pull

  接收 mq 推送的消息

  ```java
  GetResponse basicGet(
      String queue, // 队列名称
      boolean autoAck // 是否自动确认
  ) throws IOException;
  ```



#### 消费端的确认 和 拒绝

保证消息从队列可靠的到达消费者

ack 参数

- false：等待投递的消息 + 已经投递尚未收到确认

  如果消息此消息的消费者已经**断开连接**，rabbitMq 会安排重新进入队列，等待投递给下一个消费者



拒绝消息

```java
// 一次拒绝一条消息
void basicReject(
    long deliveryTag, // 消息编号
    boolean requeue // 是否重新存入队列发送给下一个消费者
) throws IOException;

// 批量拒绝
void basicNack(
    long deliveryTag, 
    boolean multiple, // false: 拒绝一条消息；true: 拒绝该编号之前的所有消息
    boolean requeue
) throws IOException;

// 请求 rabbitMq 重新发送还未被确认的消息
// requeue：true 未确认的消息重新加入到队列，可能会发送给不同的消费者
//          fakse 发送给之前相同的消费者
Basic.RecoverOk basicRecover(boolean requeue) throws IOException;
```



#### 关闭连接

```java
// Connection 或 Channel 状态转变为 Close 时会调用 ShutdownListener 的方法
channel.addShutdownListener(new ShutdownListener() {
	@Override
	public void shutdownCompleted(ShutdownSignalException cause) {
		// 分析关闭原因
	}
});
```



### 4. 进阶



#### 消息何去何从

- mandatory

  交换机无法根据自身的类型 和 路由键 找到符合条件的队列时

  - true：rabbitMq 调用 Basic.Return 将消息返回给生产者
  - false：直接丢弃

- ~~immediate~~

  - true：如果交换机将消息路由到队列中，发现队列时不存在任何消费者，该消息不会被存入队列；当与路由键匹配的所有队列都没有消费者是，返回值生产者



备份交换机 (alternate exchange)

存储没有发出去的消息



#### 过期时间 TTL(time to live)

两种设置方式 (同时设置则以小的为准)

- 消息的过期时间

  设置队列属性 `x-message-ttl`，队列中的所有消息都有相同过期时间

  单独设置消息的过期时间 (ttl=0，除非可以直接投递到消费者，否则立即丢弃)



- 队列的过期时间

  设置队列属性 `x-expires`，控制队列被自动删除前处于未使用状态 (队列上没有消费者，队列没有被重新声明，在过期时间段内未调用过 Basic.Get 命令) 的时间



#### 死信队列 DLX (dead letter exchange)

消息变成死信的情况：

- 消息被拒绝，并且 requeue = false
- 消息过期
- 队列达到最大长度

```java
@Test
void ttlDlxTest(Channel channel) throws IOException {
	// 创建两个交换机
	channel.exchangeDeclare("exchange.dlx", DIRECT, true);
	channel.exchangeDeclare("exchange.normal", FANOUT, true);
	Map<String, Object> args = Map.of("x-message-ttl", 10000,
			"x-dead-letter-exchange", "exchange.dlx",
			"x-dead-letter-routing-key", "routingKey");
	// 将两个交换机绑定到队列
	channel.queueDeclare("queue.normal", true, false, false, args);
	channel.queueBind("queue.normal", "exchange.normal", "routingKey");
	channel.queueDeclare("queue.dlx", true, false, false, null);
	channel.queueBind("queue.dlx", "exchange.dlx", "routingKey");
	// 发送条消息，由于 routingKey = 'rk'，消息路由到 queue.normal 中，如果 10s 内没有被消费就会被判定为过期
	// 过期之后，将消息转发给交换机 exchange.dlx，路由到 queue.dlx 死信队列中
	channel.basicPublish("exchange.normal", "rk", 
			MessageProperties.PERSISTENT_TEXT_PLAIN, "message".getBytes(StandardCharsets.UTF_8));
}
```



#### 延迟队列

消息发送出去后，等待特定时间，消费者才能进行消费

- 下单后指定时间内未支付成功
- 定时任务

ttl + dlx，指定过期时间，然后从消费死信队列中的消息



#### 优先级队列

声明队列时配置参数  `x-max-priority`

发送消息时，指定消息的优先级；优先级高的消息会被先消费



#### RPC 实现

客户端发送请求消息 (为了接收消息，需要在请求时携带一个回调队列 和 收到响应消息后匹配到响应请求的id)，服务端回复响应消息

```java
// 客户端配置两个参数：回调队列标记 和 请求 id
AMQP.BasicProperties properties = new AMQP.BasicProperties().builder()
        .correlationId(corrId)
        .replyTo(requestQueueName)
        .build();
```



#### 持久化

- 交换机持久化

  保证交换机元数据不会丢失，声明交换机时`durable`

- 队列持久化

  保证队列元数据不会丢失，不保证消息，声明队列时 `durable`

- 消息持久化

  保证消息不会丢失 (如果只配置消息持久化，重启后队列丢失也会导致消息丢失，**单独设置没有意义**)

  投递消息时 `BasicProperties.deliveryMode = 2 或 MessageProperties.PERSISTENT_TEXT_PLAIN`



#### 生产者确认

- 事务机制

  - channel.txSelect：       将当前信道设置为事务模式
  - channel.txCommit：   提交事务
  - channel.txRollback：  回滚事务

  ```java
  try {
      channel.txSelect();
      for (int i = 0; i < 10; i++) {
          channel.basicPublish("exchangeName",
                  "routingKey",
                  MessageProperties.PERSISTENT_TEXT_PLAIN,
                  "message".getBytes(StandardCharsets.UTF_8));
      }
      channel.txCommit();
  } catch (IOException e) {
      channel.txRollback();
      e.printStackTrace();
  }
  ```

  

- 发送方确认机制

  - 一次确认一条
  - 批量确认
  - 异步确认

  将信道设置为 确认 模式，该信道上发布的消息都会被指派一个唯一的 id，如果消息被投递到匹配的队列中，rabbitmq 会发生一个确认(Basic.Ack)给生产者(包含消息的唯一 id)

  如果消息和队列时可持久化的，确认消息会在消息写入磁盘后发出；回传给生产者的确认消息 `deliverTag` 包含确认消息的序号，`multiple` 参数表示该序号之前的消息都已处理

  如果因为 rabbitmq 自身内部原因导致消息丢失，会发生一条 `Basic.nack` 命令

  ```java
  try {
      // 将信道设置为 确认 模式
      channel.confirmSelect();
      for (int i = 0; i < 10; i++) {
          channel.basicPublish(
                  "exchangeName",
                  "routingKey",
                  null,
                  "message".getBytes(StandardCharsets.UTF_8));
          if (!channel.waitForConfirms()) {
              System.err.println("send message failed");
          }
      }
  } catch (IOException | InterruptedException e) {
      e.printStackTrace();
  }
  ```

  

#### 消费端介绍

拒绝消息

- Channel.basicNack
- Channel.basicReject



- 消息分发

  默认分发消息采用 **轮询** 的方式

  channel.basicQos 限制信道上的消费者所能保持的最大未确认消息的数量 (队列中的数量达到指定值就不会向这个消费者发送消息了)

- 消息顺序性

- 弃用 QueuingConsumer
