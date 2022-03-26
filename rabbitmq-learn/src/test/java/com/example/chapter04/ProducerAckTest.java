package com.example.chapter04;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.ConfirmListener;
import com.rabbitmq.client.MessageProperties;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * 生产者确认
 */
public class ProducerAckTest {

    /**
     * 通过事务确认消息发送
     */
    @Test
    void txPublish(Channel channel) throws IOException {
        try {
            channel.txSelect();
            for (int i = 0; i < 10; i++) {
                channel.basicPublish("exchangeName",
                        "routingKey",
                        MessageProperties.PERSISTENT_TEXT_PLAIN,
                        "message".getBytes(StandardCharsets.UTF_8));
            }
            channel.txCommit();
        } catch (IOException e) {
            channel.txRollback();
            e.printStackTrace();
        }

    }

    /**
     * 一次确认一条消息
     */
    @Test
    void confirmPublish(Channel channel) {
        try {
            channel.confirmSelect();
            for (int i = 0; i < 10; i++) {
                channel.basicPublish(
                        "exchangeName",
                        "routingKey",
                        null,
                        "message".getBytes(StandardCharsets.UTF_8));
                if (!channel.waitForConfirms()) {
                    System.err.println("send message failed");
                }
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static final int BATCH_COUNT = 10;

    /**
     * 批量确认消息
     */
    @Test
    void batchPublish(Channel channel) {
        try {
            channel.confirmSelect();
            int count = 0;
            while (true) {
                channel.basicPublish("exchangeName", "routingKey", null, "message".getBytes(StandardCharsets.UTF_8));
                if (++count >= BATCH_COUNT) {
                    count = 0;
                    try {
                        // 确认
                        if (channel.waitForConfirms()) {
                            // 清空缓存中的消息
                        }
                        // 将缓存中的消息重发送
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                        // 将缓存中的消息重新发生
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 维护一个序号消息集合
     */
    SortedSet<Long> confirmSet = new TreeSet<>();

    /**
     * 异步确认消息
     */
    @Test
    void asyncPublish(Channel channel) throws IOException {
        try {
            channel.confirmSelect();
            channel.addConfirmListener(new ConfirmListener() {
                @Override
                public void handleAck(long deliveryTag, boolean multiple) throws IOException {
                    System.out.println("nack, SeqNo: " + deliveryTag + ", multiple: " + multiple);
                    // 如果确认多条消息，一次删除多条记录
                    if (multiple) {
                        // 删除小于 deliverTag 的所有消息
                        confirmSet.headSet(deliveryTag - 1).clear();
                    } else {
                        // 一次确认一条消息
                        confirmSet.remove(deliveryTag);
                    }
                }

                @Override
                public void handleNack(long deliveryTag, boolean multiple) throws IOException {
                    if (multiple) {
                        confirmSet.headSet(deliveryTag - 1).clear();
                    } else {
                        confirmSet.remove(deliveryTag);
                    }
                    // 可能需要消息重发
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }

        // 模拟不断发消息的场景
        while (true) {
            long nextPublishSeqNo = channel.getNextPublishSeqNo();
            channel.basicPublish("exchangeName", "routingKey", MessageProperties.PERSISTENT_TEXT_PLAIN, "message".getBytes(StandardCharsets.UTF_8));
            // 每发送一条消息都要向集合中放入元素
            confirmSet.add(nextPublishSeqNo);
        }
    }
}
