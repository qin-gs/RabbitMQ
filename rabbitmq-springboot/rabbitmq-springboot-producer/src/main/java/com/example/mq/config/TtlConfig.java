package com.example.mq.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Collections;
import java.util.Map;

@Configuration
public class TtlConfig {

    @Bean
    public DirectExchange directExchange() {
        return new DirectExchange("ttl_direct_exchange", true, false);
    }

    @Bean
    public Queue directTtlQueue() {
        Map<String, Object> arguments = Collections.singletonMap("x-message-ttl", 5000);
        return new Queue("ttl.direct.queue", true, false, false, arguments);
    }

    @Bean
    public Queue directTtlQueueMessage() {
        return new Queue("ttl.message.direct.queue", true);
    }

    @Bean
    public Binding directTtlBinding() {
        return BindingBuilder.bind(directTtlQueue()).to(directExchange()).with("ttl");
    }

    @Bean
    public Binding directTtlMessageBinding() {
        return BindingBuilder.bind(directTtlQueueMessage()).to(directExchange()).with("ttl.message");
    }
}
