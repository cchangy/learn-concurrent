package com.cchangy.concurrent.lock;

import lombok.extern.slf4j.Slf4j;

/**
 * synchronized 使用示例
 *
 * @author cchangy
 * @date 2022/08/08 20:55
 */
@Slf4j
public class SynchronizedDemo {

    static int count = 0;
    static Object lock = new Object();

    public static void main(String[] args) throws InterruptedException {

        Thread t1 = new Thread(() -> {
            synchronized (lock) {
                for (int i = 0; i < 5000; i++) {
                    count++;
                }
            }
        });

        Thread t2 = new Thread(() -> {
            synchronized (lock) {
                for (int i = 0; i < 5000; i++) {
                    count--;
                }
            }
        });

        t1.start();
        t2.start();
        t1.join();
        t2.join();
        log.info("count={}", count);
    }
}
