package com.example.routing;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

import javax.swing.*;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeoutException;

public class Producer {

    public static void main(String[] args) {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");
        factory.setPort(5672);
        factory.setVirtualHost("/");
        factory.setUsername("admin");
        factory.setPassword("admin");

        Connection connection = null;
        Channel channel = null;
        try {
            // 从工厂中获取连接
            connection = factory.newConnection("生产者");
            // 从连接中获取通道
            channel = connection.createChannel();
            // 要发送的消息
            String message = "hello rabbitmq";
            // 交换机名称
            String exchangeName = "fanout-mode";
            // 路由 key
            String routeKey = "com.example";
            String type = "fanout";
            channel.basicPublish(exchangeName, routeKey, null, message.getBytes(StandardCharsets.UTF_8));
            System.out.println("消息发送成功");
        } catch (IOException | TimeoutException e) {
            e.printStackTrace();
        } finally {
            if (channel != null && channel.isOpen()) {
                try {
                    channel.close();
                } catch (IOException | TimeoutException e) {
                    e.printStackTrace();
                }
            }
            if (connection != null) {
                try {
                    connection.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
