package com.example.mq.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

// @Configuration
public class DirectConfig {

    /**
     * 声明 direct 交换机
     */
    @Bean
    public DirectExchange directExchange() {
        return new DirectExchange("direct_order_exchange", true, false);
    }

    /**
     * 声明队列
     */
    @Bean
    public Queue smsQueue() {
        return new Queue("sms.direct.queue", true);
    }

    /**
     * 声明队列
     */
    @Bean
    public Queue emsQueue() {
        return new Queue("ems.direct.queue", true);
    }

    /**
     * 声明队列
     */
    @Bean
    public Queue emailQueue() {
        return new Queue("email.direct.queue", true);
    }

    /**
     * 绑定 队列 和 交换机
     */
    @Bean
    public Binding smsBinding() {
        return BindingBuilder.bind(smsQueue()).to(directExchange()).with("sms");
    }

    @Bean
    public Binding emsBinding() {
        return BindingBuilder.bind(emsQueue()).to(directExchange()).with("ems");
    }

    @Bean
    public Binding emailBinding() {
        return BindingBuilder.bind(emailQueue()).to(directExchange()).with("email");
    }
}
