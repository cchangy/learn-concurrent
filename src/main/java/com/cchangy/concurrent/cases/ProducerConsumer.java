package com.cchangy.concurrent.cases;

import com.cchangy.concurrent.util.SleepUtils;
import lombok.extern.slf4j.Slf4j;

import java.util.LinkedList;

/**
 *
 * 生产者消费者模式
 *
 * @author cchangy
 * @date 2026/08/01 11:23
 */
@Slf4j
public class ProducerConsumer {

    public static void main(String[] args) {
        MessageQueue messageQueue = new MessageQueue(2);
        for (int i = 0; i < 5; i++) {
            int finalI = i;
            new Thread(() -> {
                messageQueue.put(new Message(finalI + "", "message_" + finalI));
            }, "produce" + i).start();
        }

        for (int i = 0; i < 1; i++) {
            new Thread(() -> {
                while (true) {
                    SleepUtils.sleep(1);
                    messageQueue.take();
                }
            }, "consumer" + i).start();
        }
    }


    static class MessageQueue {

        // 消息队列集合
        private static LinkedList<Message> queue = new LinkedList<>();
        // 队列容量
        private int capacity;

        public MessageQueue(int capacity) {
            this.capacity = capacity;
        }

        public Message take() {
            synchronized (queue) {
                // 检查队列是否是空的
                while (queue.isEmpty()) {
                    try {
                        log.info("队列为空, 消费者线程等待...");
                        queue.wait();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

                // 从头部出队
                Message message = queue.poll();
                log.info("队列有消息了，从头部出队, messageId={}", message.getId());
                // 唤醒生产者线程
                queue.notifyAll();
                return message;
            }
        }

        public void put(Message message) {
            synchronized (queue) {
                // 检查队列是否已满
                while (queue.size() == capacity) {
                    try {
                        log.info("队列已满, 生产者线程等待...");
                        queue.wait();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

                log.info("队列有空间了，加入队列尾部, messageId={}", message.getId());
                // 加入到队列尾部
                queue.addLast(message);
                // 唤醒消费者线程
                queue.notifyAll();
            }
        }

    }

    static class Message {
        private String id;
        private String message;

        public String getId() {
            return id;
        }

        public String getMessage() {
            return message;
        }

        public Message(String id, String message) {
            this.id = id;
            this.message = message;
        }
    }
}
