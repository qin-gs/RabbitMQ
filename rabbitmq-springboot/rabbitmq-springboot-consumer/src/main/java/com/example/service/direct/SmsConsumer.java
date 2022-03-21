package com.example.service.direct;

import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

/**
 * 绑定队列
 */
@Service("directSmsConsumer")
@RabbitListener(queues = {"sms.direct.queue"})
public class SmsConsumer {

    @RabbitHandler
    public void receiveMessage(String message) {
        System.out.println("sms direct get: " + message);
    }
}
