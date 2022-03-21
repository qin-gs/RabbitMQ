package com.example.service.direct;

import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

@Service("directEmailConsumer")
@RabbitListener(queues = {"email.direct.queue"})
public class EmailConsumer {

    @RabbitHandler
    public void receiveMessage(String message) {
        System.out.println("email direct get: " + message);
    }
}
