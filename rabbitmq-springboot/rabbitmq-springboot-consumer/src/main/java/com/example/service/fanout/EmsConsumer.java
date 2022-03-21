package com.example.service.fanout;

import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

@Service("emsConsumer")
@RabbitListener(queues = {"ems.fanout.queue"})
public class EmsConsumer {
    @RabbitHandler
    public void receiveMessage(String message) {
        System.out.println("ems fanout get: " + message);
    }
}
