package com.cchangy.concurrent.cases;

import com.cchangy.concurrent.util.SleepUtils;
import lombok.extern.slf4j.Slf4j;

/**
 *
 * 线程活跃性演示
 *
 * 线程因为某种原因一直无法结束
 *
 * @author cchangy
 * @date 2026/08/01 13:48
 */
@Slf4j
public class ThreadActivity {

    static volatile int count = 10;
    static final Object lock = new Object();

    public static void main(String[] args) {
        // deadlock();
        // liveLock();
    }

    /**
     * 活锁演示，出现在两个线程互相改变对方的结束条件，最后谁也无法结束
     *
     * 解决方案：错开线程的运行时间，使得一方不能改变另一方的结束条件
     */
    private static void liveLock() {
        new Thread(() -> {
            // 期望减到 0 退出循环
            while (count > 0) {
                SleepUtils.sleepMilliSeconds(2);
                count--;
                log.info("count: {}", count);
            }
        }, "t1").start();
        new Thread(() -> {
            // 期望超过 20 退出循环
            while (count < 20) {
                SleepUtils.sleepMilliSeconds(2);
                count++;
                log.info("count: {}", count);
            }
        }, "t2").start();
    }

    /**
     * 死锁演示，一个线程需要同时获取多把锁，这时就容易发生死锁
     *
     * t1 线程 获得 A对象 锁，接下来想获取 B对象 的锁， t2 线程 获得 B对象 锁，接下来想获取 A对象 的锁
     *
     * 解决方案：避免死锁要注意加锁顺序
     *
     * 使用 jstack 查看死锁：jstack [进程id]
     *
     * Found one Java-level deadlock:
     * =============================
     * "t2":
     *   waiting to lock monitor 0x000000014701f530 (object 0x00000006c15ec670, a java.lang.Object),
     *   which is held by "t1"
     * "t1":
     *   waiting to lock monitor 0x0000000144915300 (object 0x00000006c15ec680, a java.lang.Object),
     *   which is held by "t2"
     *
     * Java stack information for the threads listed above:
     * ===================================================
     * "t2":
     *         at com.cchangy.concurrent.cases.ThreadActivity.lambda$deadlock$1(ThreadActivity.java:48)
     *         - waiting to lock <0x00000006c15ec670> (a java.lang.Object)
     *         - locked <0x00000006c15ec680> (a java.lang.Object)
     *         at com.cchangy.concurrent.cases.ThreadActivity$$Lambda$2/0x00000008000eec28.run(Unknown Source)
     *         at java.lang.Thread.run(Thread.java:750)
     * "t1":
     *         at com.cchangy.concurrent.cases.ThreadActivity.lambda$deadlock$0(ThreadActivity.java:38)
     *         - waiting to lock <0x00000006c15ec680> (a java.lang.Object)
     *         - locked <0x00000006c15ec670> (a java.lang.Object)
     *         at com.cchangy.concurrent.cases.ThreadActivity$$Lambda$1/0x00000008000ee428.run(Unknown Source)
     *         at java.lang.Thread.run(Thread.java:750)
     *
     * Found 1 deadlock.
     */
    private static void deadlock() {
        Object a = new Object();
        Object b = new Object();

        Thread t1 = new Thread(() -> {
            synchronized (a) {
                log.info("lock a...");
                SleepUtils.sleep(1);
                synchronized (b) {
                    log.info("lock b...");
                }
            }
        }, "t1");

        Thread t2 = new Thread(() -> {
            synchronized (b) {
                log.info("lock b...");
                SleepUtils.sleep(1);
                synchronized (a) {
                    log.info("lock a...");
                }
            }
        }, "t2");

        t1.start();
        t2.start();
    }
}
