package com.cchangy.concurrent.aqs;

import com.cchangy.concurrent.util.SleepUtils;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 基于 AQS 实现的可重入锁
 * <p>
 * 相对于 synchronized 具备如下特点：
 * 可中断
 * 可以设置超时时间
 * 可以设置为公平锁
 * 支持多个条件变量
 *
 * @author cchangy
 * @date 2026/08/01 14:31
 */
@Slf4j
public class ReentrantLockDemo {

    // 默认是不公平的
    static ReentrantLock lock = new ReentrantLock();
    // 公平锁
    static ReentrantLock fairLock = new ReentrantLock(true);

    // 锁的条件变量
    static Condition waitCigaretteCondition = lock.newCondition();
    static Condition waitBreakfastCondition = lock.newCondition();
    static volatile boolean hasCigrette = false;
    static volatile boolean hasBreakfast = false;

    public static void main(String[] args) {
        // reentrant();
        // interruptible();
        // tryLock();
        // tryLockTimeout();
        // condition();
    }

    /**
     * 条件变量
     *
     */
    private static void condition() {
        new Thread(() -> {
            try {
                lock.lock();
                while (!hasCigrette) {
                    try {
                        waitCigaretteCondition.await();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                log.debug("等到了它的烟");
            } finally {
                lock.unlock();
            }
        }, "t1").start();

        new Thread(() -> {
            try {
                lock.lock();
                while (!hasBreakfast) {
                    try {
                        waitBreakfastCondition.await();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                log.debug("等到了它的早餐");
            } finally {
                lock.unlock();
            }
        }, "t2").start();
        SleepUtils.sleep(1);
        sendBreakfast();
        SleepUtils.sleep(1);
        sendCigarette();
    }


    private static void tryLockTimeout() {
        lock.lock();

        Thread t1 = new Thread(() -> {
            log.info("尝试获取锁...");
            try {
                // 带超时时间的尝试获取锁
                if (!lock.tryLock(3, TimeUnit.SECONDS)) {
                    log.info("没有获取到锁...");
                    return;
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            try {
                log.info("获取到锁...");
            } finally {
                lock.unlock();
            }
        }, "t1");
        t1.start();

        SleepUtils.sleep(5);
        lock.unlock();
    }

    private static void tryLock() {
        // lock.lock();

        Thread t1 = new Thread(() -> {
            log.info("尝试获取锁...");

            // 尝试获取锁，获取到锁返回 true
            if (!lock.tryLock()) {
                log.info("没有获取到锁...");
                return;
            }

            try {
                log.info("获取到锁...");
            } finally {
                lock.unlock();
            }
        }, "t1");
        t1.start();
    }

    /**
     * 可打断
     */
    private static void interruptible() {
        lock.lock();

        Thread t1 = new Thread(() -> {
            log.info("尝试获取锁...");
            try {
                // lock.lock();
                lock.lockInterruptibly(); // 可被打断的加锁
            } catch (InterruptedException e) {
                e.printStackTrace();
                log.info("线程被打断");
                return;
            }
            try {
                log.info("获取到锁...");
            } finally {
                lock.unlock();
            }

        }, "t1");
        t1.start();

        SleepUtils.sleep(1);
        log.info("打断t1线程...");
        t1.interrupt();
    }

    /**
     * 可重入
     *
     */
    private static void reentrant() {
        lock.lock();
        try {
            log.info("method1...");
            method2();
        } finally {
            lock.unlock();
        }
    }

    private static void method2() {
        lock.lock();
        try {
            log.info("method2...");
        } finally {
            lock.unlock();
        }
    }

    private static void sendCigarette() {
        lock.lock();
        try {
            log.debug("送烟来了");
            hasCigrette = true;
            waitCigaretteCondition.signal();
        } finally {
            lock.unlock();
        }
    }
    private static void sendBreakfast() {
        lock.lock();
        try {
            log.debug("送早餐来了");
            hasBreakfast = true;
            waitBreakfastCondition.signal();
        } finally {
            lock.unlock();
        }
    }
}
