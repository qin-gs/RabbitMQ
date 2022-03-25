package com.example.chapter04;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeoutException;

public class RpcServer {

	public static final String RPC_QUEUE_NAME = "rpc-queue";

	public static void main(String[] args) throws IOException, TimeoutException {
		Channel channel = getChannel();
		channel.queueDeclare(RPC_QUEUE_NAME, false, false, false, null);

		channel.basicQos(1);

		DefaultConsumer consumer = new DefaultConsumer(channel) {
			@Override
			public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
				AMQP.BasicProperties build = new AMQP.BasicProperties().builder()
						.correlationId(properties.getCorrelationId())
						.build();
				String response = "";
				try {
					String message = new String(body, StandardCharsets.UTF_8);
					int n = Integer.parseInt(message);
					response += fib(n);
				} finally {
					channel.basicPublish("", properties.getReplyTo(), build, response.getBytes(StandardCharsets.UTF_8));
					channel.basicAck(envelope.getDeliveryTag(), false);
				}

			}
		};
		channel.basicConsume(RPC_QUEUE_NAME, false, consumer);

	}

	private static int fib(int n) {
		if (n == 0) {
			return 0;
		}
		if (n == 1) {
			return 1;
		}
		return fib(n - 1) + fib(n - 2);
	}

	public static Channel getChannel() throws IOException, TimeoutException {
		ConnectionFactory factory = new ConnectionFactory();
		factory.setHost("localhost");
		factory.setPort(5672);
		factory.setVirtualHost("/");
		factory.setUsername("admin");
		factory.setPassword("admin");

		return factory.newConnection("生产者").createChannel();
	}
}
