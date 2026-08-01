package com.cchangy.concurrent.cases;

import com.cchangy.concurrent.util.SleepUtils;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 保护性暂停模式
 * <p>
 * 一个线程等待另一个线程的执行结果
 * <p>
 * JDK 中，join 的实现、Future 的实现，采用的就是此模式
 *
 * @author cchangy
 * @date 2026/08/01 10:33
 */
@Slf4j
public class GuardedSuspension {

    public static void main(String[] args) {
        // v1();
        // v2();
    }

    /**
     * 生产者和消费者一一对应
     */
    private static void v2() {
        for (int i = 0; i < 3; i++) {
            new People().start();
        }

        SleepUtils.sleep(1);
        Mailboxes.getIds().forEach(id -> new Postman(id, "信来了" + UUID.randomUUID()).start());
    }

    private static void v1() {
        GuardedObject guardedObject = new GuardedObject();
        new Thread(() -> {
            log.info("wait...");
            Object response = guardedObject.getResponse(3000);
            log.info("response is {}", response);
        }, "t1").start();

        new Thread(() -> {
            log.info("execute...");
            SleepUtils.sleep(1);
            guardedObject.setResponse("哈哈哈");
        }, "t2").start();
    }

    static class People extends Thread {
        @Override
        public void run() {
            // 收信
            GuardedObject guardedObject = Mailboxes.createGuardedObject();
            log.info("收信, id={}", guardedObject.getId());
            Object response = guardedObject.getResponse(5000);
            log.info("收到信, id={}, mail={}", guardedObject.getId(), response);
        }
    }

    static class Postman extends Thread {

        private String id;
        private String mail;

        public Postman(String id, String mail) {
            this.id = id;
            this.mail = mail;
        }

        @Override
        public void run() {
            // 送信
            GuardedObject guardedObject = Mailboxes.getGuardedObject(id);
            log.info("送信, id={}, mail={}", id, mail);
            guardedObject.setResponse(mail);
        }
    }

    static class Mailboxes {
        private static Map<String, GuardedObject> mailboxes = new ConcurrentHashMap<>();

        private static String generateId() {
            return UUID.randomUUID().toString();
        }

        public static GuardedObject createGuardedObject() {
            GuardedObject guardedObject = new GuardedObject(generateId());
            mailboxes.put(guardedObject.getId(), guardedObject);
            return guardedObject;
        }

        public static GuardedObject getGuardedObject(String id) {
            return mailboxes.remove(id);
        }

        public static Set<String> getIds() {
            return mailboxes.keySet();
        }
    }

    /**
     * 保护性暂停模式协调对象
     */
    static class GuardedObject {
        // 唯一表示
        private String id;

        private Object response;

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public GuardedObject() {
        }

        public GuardedObject(String id) {
            this.id = id;
        }

        /**
         * 获取结果
         *
         * @return
         */
        public Object getResponse() {
            synchronized (this) {
                while (response == null) {
                    try {
                        this.wait();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

                return response;
            }
        }

        /**
         * 获取结果
         *
         * @param timeout 等待超时时间
         * @return
         */
        public Object getResponse(long timeout) {
            synchronized (this) {
                // 开始时间
                long begin = System.currentTimeMillis();
                // 经历的时间
                long passedTime = 0;
                while (response == null) {
                    // 这一轮应该等待的时间，虚假唤醒时response可能还为空，timeout应该减去已经等待的时间
                    long waitTime = timeout - passedTime;
                    if (waitTime <= 0) {
                        break;
                    }
                    try {
                        // 虚假唤醒
                        this.wait(waitTime);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                    passedTime = System.currentTimeMillis() - begin;
                }
                return response;
            }
        }

        /**
         * 产生结果
         *
         * @param response
         */
        public void setResponse(Object response) {
            synchronized (this) {
                this.response = response;
                this.notifyAll();
            }
        }

    }
}
