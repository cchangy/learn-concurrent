package com.cchangy.concurrent.lock;

import com.cchangy.concurrent.util.SleepUtils;
import lombok.extern.slf4j.Slf4j;

/**
 *
 * 通过8种不同的场景，展示synchronized锁的粒度、静态方法和实例方法的锁区别，以及多线程交替执行时的行为
 * <p>
 * 两个基本原则：
 * 锁是对象：synchronized 实例方法锁的是当前实例对象 this。不同实例对象之间的锁互不干扰。
 * 锁是Class：synchronized 静态方法锁的是当前类的 Class 对象（即 类名.class）。所有实例共享同一把Class锁。
 * <p>
 * 锁相同，则同步阻塞（先获得锁的先执行）；锁不同，则异步执行
 *
 * @author cchangy
 * @date 2026/07/26 10:46
 */
@Slf4j
public class ThreadEightLockDemo {

    // ==================== 资源类 ====================
    static class Phone {
        // 场景1、2、3、4、7、8 用到的普通同步方法
        public synchronized void sendEmail() {
            log.info("enter sendEmail...");
            SleepUtils.sleep(3);
            log.info("sendEmail...");
        }

        public synchronized void sendSMS() {
            log.info("sendSMS...");
        }

        // 场景3 用到的非同步方法
        public void hello() {
            log.info("hello...");
        }

        // 场景5、6、7、8 用到的静态同步方法
        public static synchronized void staticSendEmail() {
            log.info("enter staticSendEmail");
            SleepUtils.sleep(3);
            log.info("staticSendEmail...");
        }

        public static synchronized void staticSendSMS() {
            log.info("staticSendSMS...");
        }
    }

    // ==================== 测试方法 ====================
    public static void main(String[] args) {
        log.info("========== 场景1: 同一对象，两个普通同步方法，先调用 sendEmail（sleep3秒） ==========");
        scene1();

        SleepUtils.sleep(5); // 间隔一下便于观察
        log.info("========== 场景2: 同一对象，两个普通同步方法，但先调用 sendSMS（无sleep） ==========");
        scene2();

        SleepUtils.sleep(5);
        log.info("========== 场景3: 同一对象，一个普通同步方法，一个非同步方法 ==========");
        scene3();

        SleepUtils.sleep(5);
        log.info("========== 场景4: 两个不同对象，各自调用自己的普通同步方法 ==========");
        scene4();

        SleepUtils.sleep(5);
        log.info("========== 场景5: 同一对象，两个静态同步方法 ==========");
        scene5();

        SleepUtils.sleep(5);
        log.info("========== 场景6: 两个不同对象，各自调用自己的静态同步方法 ==========");
        scene6();

        SleepUtils.sleep(5);
        log.info("========== 场景7: 同一对象，一个静态同步方法，一个普通同步方法 ==========");
        scene7();

        SleepUtils.sleep(5);
        log.info("========== 场景8: 两个不同对象，一个静态同步方法，一个普通同步方法 ==========");
        scene8();
    }

    // ==================== 8 个场景 ====================

    /**
     * 场景1：同一对象，两个普通同步方法
     * 线程A调用 sendEmail (sleep 3秒)，线程B调用 sendSMS
     * 预期：sendEmail 先执行，3秒后 sendSMS 执行（同步阻塞）
     */
    static void scene1() {
        Phone phone = new Phone();
        new Thread(() -> phone.sendEmail(), "scene1-A").start();

        SleepUtils.sleepMilliSeconds(100); // 确保线程A先获得锁

        new Thread(() -> phone.sendSMS(), "scene1-B").start();
    }

    /**
     * 场景2：同一对象，两个普通同步方法，但先调用 sendSMS（无sleep）
     * 预期：sendSMS 先执行，然后等待3秒后 sendEmail 执行
     */
    static void scene2() {
        Phone phone = new Phone();
        new Thread(() -> phone.sendSMS(), "scene2-A").start();

        SleepUtils.sleepMilliSeconds(100);

        new Thread(() -> phone.sendEmail(), "scene2-B").start();
    }

    /**
     * 场景3：同一对象，一个普通同步方法(sendEmail)，一个非同步方法(hello)
     * 预期：normalMethod 立即打印，3秒后 sendEmail 打印（异步，互不干扰）
     */
    static void scene3() {
        Phone phone = new Phone();
        new Thread(() -> phone.sendEmail(), "scene3-A").start();

        SleepUtils.sleepMilliSeconds(100);

        new Thread(() -> phone.hello(), "scene3-B").start();
    }

    /**
     * 场景4：两个不同对象，各自调用自己的普通同步方法
     * 预期：sendSMS 立即打印，3秒后 sendEmail 打印（两把不同的锁，异步）
     */
    static void scene4() {
        Phone phone1 = new Phone();
        Phone phone2 = new Phone();

        new Thread(() -> phone1.sendEmail(), "scene4-A").start();

        SleepUtils.sleepMilliSeconds(100);

        new Thread(() -> phone2.sendSMS(), "scene4-B").start();
    }

    /**
     * 场景5：同一对象，两个静态同步方法
     * 预期：staticSendEmail 先执行，3秒后 staticSendSMS 执行（Class锁，同步阻塞）
     */
    static void scene5() {
        Phone phone = new Phone();
        new Thread(() -> phone.staticSendEmail(), "scene5-A").start();

        SleepUtils.sleepMilliSeconds(100);

        new Thread(() -> phone.staticSendSMS(), "scene5-B").start();
    }

    /**
     * 场景6：两个不同对象，各自调用自己的静态同步方法
     * 预期：staticSendSMS 先执行，3秒后 staticSendEmail 执行（Class锁全局唯一，同步阻塞）
     */
    static void scene6() {
        Phone phone1 = new Phone();
        Phone phone2 = new Phone();

        new Thread(() -> phone1.staticSendEmail(), "scene6-A").start();

        SleepUtils.sleepMilliSeconds(100);

        new Thread(() -> phone2.staticSendSMS(), "scene6-B").start();
    }

    /**
     * 场景7：同一对象，一个静态同步方法，一个普通同步方法
     * 预期：sendSMS 立即打印，3秒后 staticSendEmail 打印（Class锁 vs 实例锁，异步）
     */
    static void scene7() {
        Phone phone = new Phone();
        new Thread(() -> phone.staticSendEmail(), "scene7-A").start();

        SleepUtils.sleepMilliSeconds(100);

        new Thread(() -> phone.sendSMS(), "scene7-B").start();
    }

    /**
     * 场景8：两个不同对象，一个静态同步方法，一个普通同步方法
     * 预期：sendSMS 立即打印，3秒后 staticSendEmail 打印（Class锁 vs 实例锁，异步）
     */
    static void scene8() {
        Phone phone1 = new Phone();
        Phone phone2 = new Phone();

        new Thread(() -> phone1.staticSendEmail(), "scene8-A").start();

        SleepUtils.sleepMilliSeconds(100);

        new Thread(() -> phone2.sendSMS(), "scene8-B").start();
    }
}
