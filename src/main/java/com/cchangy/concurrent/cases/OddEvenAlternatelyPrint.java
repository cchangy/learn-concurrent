package com.cchangy.concurrent.cases;

import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.LockSupport;
import java.util.concurrent.locks.ReentrantLock;

/**
 *
 * 奇数（odd）偶数（even）交替打印
 * <p>
 * 位运算（效率高）：
 * 奇数: (i & 1) = 1
 * 偶数: (i & 1) = 0
 * <p>
 * 取模运算（效率低）：
 * 奇数:（i % 2）!= 0
 * 偶数:（i % 2）== 0
 *
 * @author cchangy
 * @date 2026/08/01 15:30
 */
@Slf4j
public class OddEvenAlternatelyPrint {
    static Object lock = new Object();
    static int maxCount = 10;
    static volatile int count = 1;

    public static void main(String[] args) {
        // v1();
        // v2();
        // v3();
        v4();
    }

    /**
     * 使用
     */
    private static void v4() {

    }

    static Thread oddThread = null;
    static Thread evenThread = null;

    /**
     * 使用 park/unpark 实现
     */
    private static void v3() {
        oddThread = new Thread(() -> {
            while (count <= maxCount) {
                // 如果是偶数就打断自己
                while (count <= maxCount && (count & 1) == 0) {
                    LockSupport.park();
                }
                if (count > maxCount) {
                    break;
                }
                log.info("count={}", count);
                count++;
                LockSupport.unpark(evenThread);
            }
        }, "oddThread");

        evenThread = new Thread(() -> {
            while (count <= maxCount) {
                // 如果是奇数就打断自己
                while (count <= maxCount && (count & 1) == 1) {
                    LockSupport.park();
                }
                if (count > maxCount) {
                    break;
                }
                log.info("count={}", count);
                count++;
                LockSupport.unpark(oddThread);
            }
        }, "evenThread");

        oddThread.start();
        evenThread.start();
    }

    /**
     * 使用 reentrantLock 实现
     */
    private static void v2() {
        ReentrantLock lock = new ReentrantLock();
        Condition condition = lock.newCondition();

        new Thread(() -> {
            while (count <= maxCount) {
                lock.lock();
                try {
                    // 如果是偶数就等待
                    while (count <= maxCount && (count & 1) == 0) {
                        try {
                            condition.await();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                    if (count > maxCount) {
                        break;
                    }
                    log.info("count={}", count);
                    count++;
                    condition.signal();
                } finally {
                    lock.unlock();
                }
            }
        }, "oddThread").start();

        new Thread(() -> {
            while (count <= maxCount) {
                lock.lock();
                try {
                    // 如果是奇数就等待
                    while (count <= maxCount && (count & 1) == 1) {
                        try {
                            condition.await();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                    if (count > maxCount) {
                        break;
                    }
                    log.info("count={}", count);
                    count++;
                    condition.signal();
                } finally {
                    lock.unlock();
                }
            }
        }, "evenThread").start();
    }

    /**
     * 使用 synchronized 实现
     */
    private static void v1() {
        new Thread(() -> {
            synchronized (lock) {
                while (count <= maxCount) {
                    // 如果是偶数
                    while (count <= maxCount && (count & 1) == 0) {
                        try {
                            lock.wait();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                    if (count > maxCount) {
                        break;
                    }
                    log.info("count={}", count);
                    count++;
                    lock.notify();
                }
            }
        }, "oddThread").start();

        new Thread(() -> {
            synchronized (lock) {
                while (true) {
                    // 如果是奇数
                    while (count <= maxCount && (count & 1) == 1) {
                        try {
                            lock.wait();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                    if (count > maxCount) {
                        break;
                    }
                    log.info("count={}", count);
                    count++;
                    lock.notify();
                }
            }
        }, "evenThread").start();
    }
}
