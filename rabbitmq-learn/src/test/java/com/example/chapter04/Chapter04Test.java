package com.example.chapter04;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.BasicProperties;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.MessageProperties;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Map;

public class Chapter04Test {

	public static final String DIRECT = "direct";
	public static final String FANOUT = "fanout";

	@Test
	void aeTest(Channel channel) throws IOException {
		// 配置 ae 交换机
		Map<String, Object> args = Collections.singletonMap("alternate-exchange", "myAe");

		// 声明两个交换机
		channel.exchangeDeclare("normalExchange", DIRECT, true, false, args);
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

	@Test
	void dlxTest(Channel channel) throws IOException {
		// 声明一个死信队列 和 路由键
		channel.exchangeDeclare("dlx-exchange", DIRECT);
		Map<String, Object> args = Map.of("x-dead-letter-exchange", "dlx-exchange",
				"x-dead-letter-routing-key", "dlx-routing-key");

		// 为队列绑定
		channel.queueDeclare("myQueue", false, false, false, args);

	}

	@Test
	void ttlDlxTest(Channel channel) throws IOException {
		// 创建两个交换机
		channel.exchangeDeclare("exchange.dlx", DIRECT, true);
		channel.exchangeDeclare("exchange.normal", FANOUT, true);

		Map<String, Object> args = Map.of("x-message-ttl", 10000,
				"x-dead-letter-exchange", "exchange.dlx",
				"x-dead-letter-routing-key", "routingKey");

		// 将两个交换机绑定到队列
		channel.queueDeclare("queue.normal", true, false, false, args);
		channel.queueBind("queue.normal", "exchange.normal", "routingKey");

		channel.queueDeclare("queue.dlx", true, false, false, null);
		channel.queueBind("queue.dlx", "exchange.dlx", "routingKey");

		// 发送条消息，由于 routingKey = 'rk'，消息路由到 queue.normal 中，如果 10s 内没有被消费就会被判定为过期
		// 过期之后，将消息转发给交换机 exchange.dlx 中，路由到 queue.dlx 死信队列中
		channel.basicPublish("exchange.normal", "rk",
				MessageProperties.PERSISTENT_TEXT_PLAIN, "message".getBytes(StandardCharsets.UTF_8));
	}

	@Test
	void priorityTest(Channel channel) throws IOException {
		channel.exchangeDeclare("exchange-priority", FANOUT);

		// 设置队列优先级
		Map<String, Object> args = Map.of("x-max-priority", 10);
		channel.queueDeclare("queue.priority", true, false, false, args);

		// 设置消息优先级，优先级高的消息会被优先消费
		AMQP.BasicProperties.Builder builder = new AMQP.BasicProperties().builder();
		builder.priority(5);
		AMQP.BasicProperties properties = builder.build();
		channel.basicPublish("exchange-priority", "rk-priority", properties, "message".getBytes(StandardCharsets.UTF_8));
	}

	@Test
	void rpcTest(Channel channel) throws IOException {
		String queue = channel.queueDeclare().getQueue();
		AMQP.BasicProperties properties = new AMQP.BasicProperties().builder().replyTo(queue).build();
		channel.basicPublish("", "rpc-queue", properties, "message".getBytes(StandardCharsets.UTF_8));
	}
}
