package com.example.simple;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.MessageProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeoutException;

public class Producer {

    private static final Logger log = LoggerFactory.getLogger(Producer.class);

    public static void main(String[] args) throws IOException, TimeoutException {
        // 1. 创建连接工程
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");
        factory.setPort(5672);
        factory.setUsername("admin");
        factory.setPassword("admin");
        factory.setVirtualHost("/");
        // 2. 创建连接
        Connection connection = factory.newConnection("producer");
        // 3. 通过连接获取通道 Channel
        Channel channel = connection.createChannel();
        // 4. 创建交换机，声明队列，绑定关系，路由 key，发送消息，接收消息
        String queueName = "queue1";
        // 队列名称，是否持久化，是否独占，最后一个消费者消费完后是否自动删除队列，一些参数
        channel.queueDeclare(queueName, false, false, false, null);
        // 5. 准备消息内容
        String message = "hello world";
        // 6. 发送消息给队列
        // 交换机，队列/路由key，消息的状态控制，消息
        // 必须有交换机(不指定使用默认的)
        channel.basicPublish("", queueName, MessageProperties.PERSISTENT_TEXT_PLAIN, message.getBytes(StandardCharsets.UTF_8));
        // 7. 关闭通道
        channel.close();
        // 8. 关闭连接
        connection.close();
    }
}
