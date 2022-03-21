package com.example.service.fanout;

import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

/**
 * 绑定队列
 */
@Service("fanoutSmsConsumer")
@RabbitListener(queues = {"sms.fanout.queue"})
public class SmsConsumer {

    @RabbitHandler
    public void receiveMessage(String message) {
        System.out.println("sms fanout get: " + message);
    }
}
