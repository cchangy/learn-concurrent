package com.cchangy.concurrent.base;

import com.cchangy.concurrent.util.SleepUtils;
import lombok.extern.slf4j.Slf4j;

/**
 * 等待通知实例
 *
 * wait和notify方法必须在同步代码块或者方法里面，且成对出现使用
 * wait 在等待的时候会释放对象锁
 * 必须先wait后notify
 *
 * @author cchangy
 * @date 2025/08/09 17:09
 */
@Slf4j
public class WaitNotify {

    private static Object lock = new Object();
    private static boolean flag = true;

    public static void main(String[] args) {
        Thread waitThread = new Thread(new Wait(), "waitThread");
        waitThread.start();

        SleepUtils.sleep(1);

        Thread notifyThread = new Thread(new Notify(), "notifyThread");
        notifyThread.start();
    }

    private static class Wait implements Runnable {
        @Override
        public void run() {
            // 加锁，拥有lock的monitor
            synchronized(lock) {
                // 当条件不满足时，继续等待，同时释放lock的锁
                while (flag) {
                    try {
                        log.info("flag is true, wait start");
                        lock.wait();
                    } catch (InterruptedException e) {
                    }
                }

                // 条件满足时
                log.info("flag is false, wait end");
            }
        }
    }

    private static class Notify implements Runnable {
        @Override
        public void run() {
            // 加锁，拥有lock的monitor
            synchronized(lock) {
                // 获取到lock的锁，然后进行通知，通知时不会释放lock的锁，直到当前线程释放了lock后，WaitThread才能从wait方法中返回
                log.info("hold lock, notify start");
                lock.notify(); // 唤醒lock上等待的一个线程
                // lock.notifyAll(); // 唤醒lock上等待的所有线程
                flag = false;
                SleepUtils.sleep(5);
            }
            synchronized (lock) {
                log.info("hold lock again");
                SleepUtils.sleep(5);
            }
        }
    }
}
