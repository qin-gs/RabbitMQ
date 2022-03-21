package com.example.mq.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

@Configuration
public class TtlConfig {

    @Bean
    public DirectExchange directExchange() {
        return new DirectExchange("ttl_direct_exchange", true, false);
    }

    @Bean
    public Queue directTtlQueue() {
        // 给队列中的消息设置过期时间
        // 将过期消息通过 死信交换机 放入 死信队列，让 死信消费者 处理
        Map<String, Object> arguments = Map.of("x-message-ttl", 5000,
                "x-dead-letter-exchange", "dead_letter_direct_exchange",
                "x-dead-letter-routing-key", "dead"); // 如果是 fanout 模式不需要 key
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
