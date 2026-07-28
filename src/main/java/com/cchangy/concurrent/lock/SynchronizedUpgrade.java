package com.cchangy.concurrent.lock;

import com.cchangy.concurrent.util.JOLParser;
import com.cchangy.concurrent.util.SleepUtils;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.CountDownLatch;

/**
 * synchronized 锁升级对象头变化演示
 * <p>
 * |--------------------------------------------------------------|
 * |                    Object Header (128 bits)                   |
 * |------------------------------------|-------------------------|
 * |       Mark Word (64 bits)          |   Klass Word (64 bits)  |
 * |------------------------------------|-------------------------|
 * <p>
 * <p>
 * |--------------------------------------------------------------------|--------------------|
 * |                          Mark Word (64 bits)                       |        State       |
 * |--------------------------------------------------------------------|--------------------|
 * | unused:25 | hashcode:31 | unused:1 | age:4 | biased_lock:0 |  01   |        Normal      |
 * |--------------------------------------------------------------------|--------------------|
 * | thread:54 |   epoch:2   | unused:1 | age:4 | biased_lock:1 |  01   |        Biased      |
 * |--------------------------------------------------------------------|--------------------|
 * |                    ptr_to_lock_record:62                   |  00   | Lightweight Locked |
 * |--------------------------------------------------------------------|--------------------|
 * |                 ptr_to_heavyweight_monitor:62              |  10   | Heavyweight Locked |
 * |--------------------------------------------------------------------|--------------------|
 * |                                                            |  11   |    Marked for GC   |
 * |--------------------------------------------------------------------|--------------------|
 *
 * @author cchangy
 * @date 2026/07/26 14:40
 */
@Slf4j
public class SynchronizedUpgrade {

    static Object lock = new Object();

    public static void main(String[] args) throws Exception {

        // noLockBiased();
        // noLockNoBiased();
        // biasedLockUpgradeLightweightLock();
        // biasedLockUpgradeHeavyweightLock();
        // biasedLockHashCodeUpgradeHeavyweightLock();
    }

    /**
     * 偏向锁持有的时候获取hashCode直接升级为重量级锁
     * <p>
     * hashCode 导致的偏向锁撤销并直接膨胀为 ObjectMonitor，并不是因为多线程竞争，因此不会立即产生重量级锁竞争的性能损耗。
     * 真正昂贵的是膨胀之后多个线程进入 ObjectMonitor 竞争，需要经历自旋、队列、park/unpark 等过程
     */
    private static void biasedLockHashCodeUpgradeHeavyweightLock() {
        synchronized (lock) {
            log.info(JOLParser.parse(lock));
            lock.hashCode();
            log.info(JOLParser.parse(lock));
        }
    }

    /**
     * 无锁可偏向状态，最后3位是“101”，1代表可偏向，01代表无锁状态
     * <p>
     * 偏向锁的前提条件是对象不存在锁的竞争，没有调用过hashCode方法
     * <p>
     * 偏向锁默认是延迟的，不会在程序启动时立即生效，
     * 如果想避免延迟，可以加 JVM 参数- XX:BiasedLockingStartupDelay=0 来禁用延迟
     */
    private static void noLockBiased() {
        log.info("\n======================刚创建的对象无锁状态======================");
        log.info(JOLParser.parse(lock));
    }

    /**
     * 无锁不可偏向状态，最后3位是“001”，0代表不可偏向，01代表无锁状态
     * <p>
     * 关闭了偏向锁或对象调用了hashCode方法，Mark Word会变为不可偏向状态
     */
    private static void noLockNoBiased() {
        log.info("\n======================无锁不可偏向状态======================");
        // 计算hashCode后，Mark Word会变为不可偏向状态
        lock.hashCode();
        log.info(JOLParser.parse(lock));
    }

    /**
     * 偏向锁升级轻量级锁，最后2位是“00”
     * 一旦升级为轻量级锁，最终对象的锁完全释放后，通常会被重置为无锁不可偏向状态，而不是回到无锁可偏向状态
     */
    private static void biasedLockUpgradeLightweightLock() throws InterruptedException {
        log.info("\n======================偏向锁升级轻量级锁======================");
        log.info("\n======================对象刚创建，处于无锁可偏向状态======================");
        log.info(JOLParser.parse(lock));

        synchronized (lock) {
            log.info("\n======================第一次获取锁，处于可偏向有锁状态，此时会将持有锁的线程id设置到对象头======================");
            log.info(JOLParser.parse(lock));

            synchronized (lock) {
                log.info("\n======================第二次获取锁，锁重入，偏向锁状态保持======================");
                log.info(JOLParser.parse(lock));
            }
        }

        log.info("\n======================退出synchronized代码块后，释放了锁，但依旧保持偏向锁状态，线程id不会清除======================");
        log.info(JOLParser.parse(lock));

        Thread thread = new Thread(() -> {
            synchronized (lock) {
                log.info("\n======================其他线程获取锁，出现锁竞争，偏向锁升级为轻量级锁======================");
                log.info(JOLParser.parse(lock));
            }
        });
        thread.start();
        thread.join();

        log.info("\n======================主线程结束，最终会处于无锁不可偏向状态======================");
        log.info(JOLParser.parse(lock));
    }

    /**
     * 轻量级锁升级重量级锁，最后两位是“10”
     * <p>
     * 锁可以升级不可以降级，一旦膨胀（inflate）成为重量级锁，即使后续没有线程竞争，也不会回退成轻量级锁或无锁状态
     * <p>
     * 但是有一个容易混淆的点：重量级锁对应的 ObjectMonitor 会被回收（deflation），对象 Mark Word 会被恢复到无锁状态，但这不是“锁降级”
     *
     * @throws InterruptedException
     */
    private static void biasedLockUpgradeHeavyweightLock() throws InterruptedException {
        log.info("\n======================轻量级锁升级重量级锁======================");
        log.info("\n======================对象刚创建，处于无锁可偏向状态======================{}", JOLParser.parse(lock));

        CountDownLatch latch = new CountDownLatch(1);
        // 线程A：持有锁并触发偏向锁
        Thread threadA = new Thread(() -> {
            synchronized (lock) {
                log.info("\n======================持有锁并触发偏向锁======================{}", JOLParser.parse(lock));
                // 延迟让锁被竞争
                SleepUtils.sleep(2);
            }
            latch.countDown();
        }, "thread-A");
        threadA.start();

        // 等待线程A获取锁并开始睡眠
        SleepUtils.sleepMilliSeconds(2500);

        // 线程B：触发偏向锁撤销，将锁升级为轻量级锁
        Thread threadB = new Thread(() -> {
            synchronized (lock) {
                log.info("\n======================触发偏向锁撤销，将锁升级为轻量级锁======================{}", JOLParser.parse(lock));
                SleepUtils.sleep(3);
            }
            log.info("\n======================释放锁======================{}", JOLParser.parse(lock));
        }, "thread-B");
        threadB.start();

        // 等待线程B获取锁并开始睡眠
        SleepUtils.sleep(1);

        // 线程C：与线程B竞争，触发轻量级锁升级为重量级锁
        Thread threadC = new Thread(() -> {
            synchronized (lock) {
                log.info("\n======================获取锁(重量级锁)======================{}", JOLParser.parse(lock));
            }
        }, "thread-C");
        threadC.start();

        SleepUtils.sleepMilliSeconds(500);

        // 线程D：在重量级锁下竞争
        Thread threadD = new Thread(() -> {
            synchronized (lock) {
                log.info("\n======================获取锁(重量级锁)======================{}", JOLParser.parse(lock));
            }
        }, "thread-D");
        threadD.start();

        // 等待所有线程结束
        latch.await();
        threadB.join();
        threadC.join();
        threadD.join();

        log.info("\n======================在经历了竞争之后，锁已经升级为轻量级锁或重量级锁======================{}", JOLParser.parse(lock));
        // 在经历了竞争之后，锁已经升级为轻量级锁或重量级锁
        // 此时让一个新线程去获取锁
        Thread t3 = new Thread(() -> {
            synchronized (lock) {
                // 打印出的锁状态绝不会是偏向锁(101)
                log.info("\n======================新线程获取锁(不再是偏向锁)======================{}", JOLParser.parse(lock));
            }
        }, "thread-3");
        t3.start();
        t3.join();
    }
}
