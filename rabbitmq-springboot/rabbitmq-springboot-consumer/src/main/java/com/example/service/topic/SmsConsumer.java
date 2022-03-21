package com.example.service.topic;

import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.rabbit.annotation.*;
import org.springframework.stereotype.Service;

/**
 * 绑定队列
 */
@Service("topicSmsConsumer")
@RabbitListener(
        bindings = {@QueueBinding(
                value = @Queue(value = "sms.topic.queue", durable = "true", autoDelete = "false"),
                exchange = @Exchange(value = "topic_order_change", type = ExchangeTypes.TOPIC), key = "#.sms.#")
        })
public class SmsConsumer {

    @RabbitHandler
    public void receiveMessage(String message) {
        System.out.println("sms topic get: " + message);
    }
}
