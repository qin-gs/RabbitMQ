package com.example.direct;

import com.rabbitmq.client.*;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeoutException;

public class Consumer {

    public static void main(String[] args) {
        new Thread(runnable, "queue-1").start();
        new Thread(runnable, "queue-2").start();
        new Thread(runnable, "queue-3").start();
    }

    private static final Runnable runnable = new Runnable() {
        @Override
        public void run() {
            ConnectionFactory factory = new ConnectionFactory();
            factory.setHost("localhost");
            factory.setPort(5672);
            factory.setVirtualHost("/");
            factory.setUsername("admin");
            factory.setPassword("admin");

            String queueName = Thread.currentThread().getName();
            Connection connection = null;
            Channel channel = null;
            try {
                connection = factory.newConnection("生产者");
                channel = connection.createChannel();

                // 队列名称，是否持久化，是否哦爱他，是否自动删除，附加参数
                channel.basicConsume(queueName,
                        true,
                        new DeliverCallback() {
                            @Override
                            public void handle(String s, Delivery delivery) throws IOException {
                                System.out.println(delivery.getEnvelope().getDeliveryTag());
                                System.out.println(queueName + ": 消息 " + new String(delivery.getBody(), StandardCharsets.UTF_8));
                            }
                        },
                        new CancelCallback() {
                            @Override
                            public void handle(String s) throws IOException {

                            }
                        });
                System.out.println("开始接收消息");
                System.in.read();
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
    };

}
