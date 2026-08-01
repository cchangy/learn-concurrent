package com.cchangy.concurrent.lock;

import com.cchangy.concurrent.util.SleepUtils;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.locks.LockSupport;

/**
 * 基于unsafe 实现的加锁解锁
 *
 * park 和unpark 需配合使用
 * 先 park 后unpark 线程才会阻塞
 * 先 unpark 后park 线程不会阻塞
 *
 * @author cchangy
 * @date 2026/08/01 12:52
 */
@Slf4j
public class LockSupportDemo {

    public static void main(String[] args) {
        // 先 park 后 unpark
        // parkBeforeUnpark();

        // 先 unpark 后 park
        // parkAfterUnpark();
    }

    private static void parkBeforeUnpark() {
        Thread t1 = new Thread(() -> {
            log.info("park...");
            LockSupport.park();
            log.info("done...");
        }, "t1");
        t1.start();

        SleepUtils.sleep(2);
        log.info("unpark...");
        LockSupport.unpark(t1);
        log.info("done...");
    }

    private static void parkAfterUnpark() {
        Thread t1 = new Thread(() -> {
            SleepUtils.sleep(2);
            log.info("park...");
            LockSupport.park();
            log.info("done...");
        }, "t1");
        t1.start();

        log.info("unpark...");
        LockSupport.unpark(t1);
        log.info("done...");
    }
}
