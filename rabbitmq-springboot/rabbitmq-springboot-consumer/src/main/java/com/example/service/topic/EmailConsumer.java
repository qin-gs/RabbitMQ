package com.example.service.topic;

import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.rabbit.annotation.*;
import org.springframework.stereotype.Service;

@Service("topicEmailConsumer")
@RabbitListener(bindings = {
        @QueueBinding(
                value = @Queue(value = "email.topic.queue", durable = "true", autoDelete = "false"),
                exchange = @Exchange(value = "topic_order_change", type = ExchangeTypes.TOPIC), key = "#.email.#"
        )
})
public class EmailConsumer {

    @RabbitHandler
    public void receiveMessage(String message) {
        System.out.println("email topic get: " + message);
    }
}
