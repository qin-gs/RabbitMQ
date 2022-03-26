package com.example.chapter04;

import com.rabbitmq.client.*;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicReference;

public class RpcClient implements AutoCloseable {
    private Connection connection;
    private Channel channel;
    private final String requestQueueName = "rpc-queue";
    private String replyQueueName;
    private Consumer consumer;

    public RpcClient() throws IOException, TimeoutException {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");
        factory.setPort(5672);
        factory.setVirtualHost("/");
        factory.setUsername("admin");
        factory.setPassword("admin");

        connection = factory.newConnection("生产者");
        channel = connection.createChannel();
        // 创建一个回调队列
        this.replyQueueName = this.channel.queueDeclare().getQueue();
        this.consumer = new DefaultConsumer(this.channel);
        this.channel.basicConsume(requestQueueName, true, consumer);
    }

    /**
     * 发送请求
     */
    public String call(String message) throws IOException {
        AtomicReference<String> response = new AtomicReference<>();
        String corrId = UUID.randomUUID().toString();
        // 客户端配置两个参数：回调队列标记 和 请求 id
        AMQP.BasicProperties properties = new AMQP.BasicProperties().builder()
                .correlationId(corrId)
                .replyTo(requestQueueName)
                .build();
        channel.basicPublish("", requestQueueName, properties, message.getBytes(StandardCharsets.UTF_8));

        // 监听回调队列，有消息并且 id 匹配时拿到结果
        String cTag = channel.basicConsume(
                replyQueueName,
                true,
                (consumerTag, delivery) -> {
                    if (delivery.getProperties().getCorrelationId().equals(corrId)) {
                        response.set(new String(delivery.getBody(), StandardCharsets.UTF_8));
                    }
                },
                consumerTag -> {
                });

        channel.basicCancel(cTag);
        return response.get();
    }

    @Override
    public void close() throws IOException {
        connection.close();
    }


    public static void main(String[] args) throws IOException, TimeoutException {
        RpcClient client = new RpcClient();
        String response = client.call("30");
        System.err.println("response = " + response);
        client.close();
    }
}
