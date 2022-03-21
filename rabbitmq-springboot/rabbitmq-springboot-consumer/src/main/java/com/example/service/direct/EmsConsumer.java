package com.example.service.direct;

import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

@RabbitListener(queues = {"ems.direct.queue"})
@Service("directEmsConsumer")
public class EmsConsumer {
    @RabbitHandler
    public void receiveMessage(String message) {
        System.out.println("ems direct get: " + message);
    }
}
