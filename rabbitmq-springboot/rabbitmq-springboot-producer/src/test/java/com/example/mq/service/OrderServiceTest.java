package com.example.mq.service;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.nio.charset.StandardCharsets;

@SpringBootTest
class OrderServiceTest {

    @Autowired
    private OrderService service;

    @Test
    void makeOrderFanout() {
        service.makeOrderFanout("1", "2", 3);
    }

    @Test
    void makeOrderDirect() {
        service.makeOrderDirect("1", "2", 3);

    }

    @Test
    void makeOrderTopic() {
        service.makeOrderTopic("1", "2", 3);

    }

    @Test
    void makeOrderDirectTtlQueue() {
        service.makeOrderDirectTtlQueue("1", "2", 3);
    }

    @Test
    void makeOrderDirectTtlMessage() {
        service.makeOrderDirectTtlMessage("1", "2", 3);
    }

    @Test
    void test(Channel channel) throws Exception {
        channel.basicPublish("",
                "",
                new AMQP.BasicProperties.Builder()
                        .contentType("text/plain")
                        .deliveryMode(2)
                        .priority(1)
                        .userId("hidden")
                        .build(),
                "a message".getBytes(StandardCharsets.UTF_8));
    }
}