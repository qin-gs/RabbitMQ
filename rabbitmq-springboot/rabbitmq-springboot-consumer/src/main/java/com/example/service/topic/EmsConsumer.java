package com.example.service.topic;

import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.rabbit.annotation.*;
import org.springframework.stereotype.Service;

@Service("topicEmsConsumer")
@RabbitListener(bindings = {
        @QueueBinding(
                value = @Queue(value = "ems.topic.queue", durable = "true", autoDelete = "false"),
                exchange = @Exchange(value = "topic_order_change", type = ExchangeTypes.TOPIC), key = "#.ems.#")
})
public class EmsConsumer {
    @RabbitHandler
    public void receiveMessage(String message) {
        System.out.println("ems topic get: " + message);
    }
}
