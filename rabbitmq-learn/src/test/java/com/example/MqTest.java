package com.example;

import com.rabbitmq.client.*;
import org.junit.jupiter.api.Test;

import java.io.IOException;

public class MqTest {

    @Test
    void consumerPush(Channel channel) throws IOException {
        channel.basicQos(64);
        boolean autoAck = false;
        channel.basicConsume(
                // 队列名称
                "queueName",
                // 接收到消息后需要显式的确认
                autoAck,
                // 消费者标签 (区分消费者)
                "consumerTag",
                // 接收消息
                new DefaultConsumer(channel) {
                    @Override
                    public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
                        String routingKey = envelope.getRoutingKey();
                        String contentType = properties.getContentType();
                        long deliveryTag = envelope.getDeliveryTag();
                        channel.basicAck(deliveryTag, false);
                    }
                });

    }

    @Test
    void consumerPull(Channel channel) throws IOException {
        GetResponse response = channel.basicGet("queueName", false);
        String message = new String(response.getBody());
        channel.basicAck(response.getEnvelope().getDeliveryTag(), false);

        channel.addShutdownListener(new ShutdownListener() {
            @Override
            public void shutdownCompleted(ShutdownSignalException cause) {
                Method reason = cause.getReason();
            }
        });
    }
}
