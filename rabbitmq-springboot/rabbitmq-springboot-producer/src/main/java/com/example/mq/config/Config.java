package com.example.mq.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.FanoutExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 1. 注册交换机
 * 2. 声明队列
 * 3. 绑定
 */
@Configuration
public class Config {

    /**
     * 声明 fanout 交换机
     */
    @Bean
    public FanoutExchange fanoutExchange() {
        return new FanoutExchange("fanout_order_exchange", true, false);
    }

    /**
     * 声明队列
     */
    @Bean
    public Queue smsQueue() {
        return new Queue("sms.fanout.queue", true);
    }

    /**
     * 声明队列
     */
    @Bean
    public Queue emsQueue() {
        return new Queue("ems.fanout.queue", true);
    }

    /**
     * 声明队列
     */
    @Bean
    public Queue emailQueue() {
        return new Queue("email.fanout.queue", true);
    }

    /**
     * 绑定 队列 和 交换机
     */
    @Bean
    public Binding smsBinding() {
        return BindingBuilder.bind(smsQueue()).to(fanoutExchange());
    }

    @Bean
    public Binding emsBinding() {
        return BindingBuilder.bind(emsQueue()).to(fanoutExchange());
    }

    @Bean
    public Binding emailBinding() {
        return BindingBuilder.bind(emailQueue()).to(fanoutExchange());
    }
}
