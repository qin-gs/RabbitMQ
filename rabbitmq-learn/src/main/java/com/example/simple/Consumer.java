package com.example.simple;

import com.rabbitmq.client.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

public class Consumer {

    private static final Logger log = LoggerFactory.getLogger(Consumer.class);


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
        // 队列名称，是否持久化，是否独占，最后一个消费者消费完后是否自动删除队列，一些参数
        String queueName = "queue1";
        channel.basicConsume(
                queueName,
                true,
                new DeliverCallback() {
                    @Override
                    public void handle(String s, Delivery delivery) throws IOException {
                        log.info("收到的消息是: " + new String(delivery.getBody()));
                    }
                },
                new CancelCallback() {
                    @Override
                    public void handle(String s) throws IOException {
                        log.warn("接收失败");
                    }
                });
        System.in.read();
        // 7. 关闭通道
        channel.close();
        // 8. 关闭连接
        connection.close();
    }
}
