package com.example.chapter04;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.BasicProperties;
import com.rabbitmq.client.Channel;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Map;

public class AeTest {

	@Test
	void aeTest(Channel channel) throws IOException {
		// 配置 ae 交换机
		Map<String, Object> args = Collections.singletonMap("alternate-exchange", "myAe");

		// 声明两个交换机
		channel.exchangeDeclare("normalExchange", "direct", true, false, args);
		channel.exchangeDeclare("myAe", "fanout", true, false, null);

		// 两个交换机分别绑定队列
		channel.queueDeclare("normalQueue", true, false, false, null);
		channel.queueBind("normalQueue", "normalExchange", "normalKey");

		channel.queueDeclare("unroutedQueue", true, false, false, null);
		channel.queueBind("unroutedQueue", "myAe", "");

	}

	@Test
	void ttlTest(Channel channel) throws IOException {
		// 设置队列属性，队列中的所有消息都有相同的过期时间
		Map<String, Object> args = Collections.singletonMap("x-message-ttl", 6000);
		channel.queueDeclare("queueName", true, true, false, args);

		AMQP.BasicProperties.Builder builder = new AMQP.BasicProperties.Builder();
		builder.deliveryMode(2);
		builder.expiration("60000");
		AMQP.BasicProperties properties = builder.build();
		// 单独设置消息的过期时间
		channel.basicPublish("exchangeName", "routingKey", false, properties, "message".getBytes(StandardCharsets.UTF_8));
	}
}
