package com.example.mq.config;

import com.rabbitmq.client.AMQP;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 死信队列设置
 */
@Configuration
public class DeadLetterConfig {

    /**
     * 死信交换机
     */
    @Bean
    public DirectExchange deadDirectExchange() {
        return new DirectExchange("dead_letter_direct_exchange", true, false);
    }

    @Bean
    public Queue deadLetterQueue() {
        return new Queue("dead.direct.queue", true);
    }

    @Bean
    public Binding deadBinding() {
        return BindingBuilder.bind(deadLetterQueue()).to(deadDirectExchange()).with("dead");
    }
}
