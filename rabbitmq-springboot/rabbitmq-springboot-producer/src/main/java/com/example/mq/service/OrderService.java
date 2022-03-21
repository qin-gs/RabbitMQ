package com.example.mq.service;

import org.springframework.amqp.AmqpException;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessagePostProcessor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class OrderService {

    private final RabbitTemplate rabbitTemplate;

    public OrderService(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void makeOrderFanout(String userId, String productId, int num) {

        // 查询库存是否充足

        // 保存订单
        String orderId = UUID.randomUUID().toString();

        // 消息分发
        String exchangeName = "fanout_order_exchange";
        String routingKey = "";
        rabbitTemplate.convertAndSend(exchangeName, routingKey, orderId);
        System.out.println("订单生成成功: " + orderId);
    }

    public void makeOrderDirect(String userId, String productId, int num) {

        // 查询库存是否充足

        // 保存订单
        String orderId = UUID.randomUUID().toString();

        // 消息分发
        String exchangeName = "direct_order_exchange";
        rabbitTemplate.convertAndSend(exchangeName, "email", orderId);
        rabbitTemplate.convertAndSend(exchangeName, "ems", orderId);
        System.out.println("订单生成成功: " + orderId);
    }

    public void makeOrderTopic(String userId, String productId, int num) {

        // 查询库存是否充足

        // 保存订单
        String orderId = UUID.randomUUID().toString();

        // 消息分发
        String exchangeName = "topic_order_exchange";
        String routingKey = "com.ems.something";
        rabbitTemplate.convertAndSend(exchangeName, routingKey, orderId);
        System.out.println("订单生成成功: " + orderId);
    }

    public void makeOrderDirectTtlQueue(String userId, String productId, int num) {

        // 查询库存是否充足

        // 保存订单
        String orderId = UUID.randomUUID().toString();

        // 消息分发
        String exchangeName = "ttl_direct_exchange";
        String routingKey = "ttl";
        rabbitTemplate.convertAndSend(exchangeName, routingKey, orderId);
        System.out.println("订单生成成功: " + orderId);
    }

    public void makeOrderDirectTtlMessage(String userId, String productId, int num) {

        // 查询库存是否充足

        // 保存订单
        String orderId = UUID.randomUUID().toString();

        // 消息分发
        String exchangeName = "ttl_direct_exchange";
        String routingKey = "ttl.message";

        // 设置消息过期时间
        MessagePostProcessor processor = new MessagePostProcessor() {
            @Override
            public Message postProcessMessage(Message message) throws AmqpException {
                message.getMessageProperties().setExpiration("50000");
                message.getMessageProperties().setContentEncoding("utf-8");
                return message;
            }
        };

        rabbitTemplate.convertAndSend(exchangeName, routingKey, orderId, processor);
        System.out.println("订单生成成功: " + orderId);
    }
}
