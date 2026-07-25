package com.cchangy.concurrent.base;

import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.TimeUnit;

/**
 * 线程状态示例
 *
 * 从操作系统层面，线程有五种状态：
 *      1. 初始状态：仅是在语言层面创建了线程对象，还未与操作系统线程关联
 *      2. 可运行状态（就绪状态）：指该线程已经被创建（与操作系统线程关联），可以由 CPU 调度执行
 *      3. 运行状态：指线程获取了CPU时间片运行中的状态
 *      4. 阻塞状态：如果调用了阻塞API，如BIO读写文件，这时该线程实际不会用到 CPU，会导致线程上下文切换，进入"阻塞状态"，等BIO操作完毕，会由操作系统唤醒阻塞的线程，转换至"可运行状态"
 *      5. 终止状态：表示线程已经执行完毕，生命周期已经结束，不会再转换为其它状态
 *
 * 从Java API层面（Thread.State），线程有六种状态：
 *      1. NEW: 初始状态，线程刚被创建，还没调用start方法
 *      2. RUNNABLE：调用了start方法之后，此状态覆盖了操作系统层面的"可运行状态"，"运行状态"，"阻塞状态"（由代码执行过程中导致的阻塞，java无法识别，仍认为是可运行的）
 *      3. BLOCKED：阻塞状态，未获取到锁时的阻塞状态
 *      4. WAITING：等待状态，表示线程进入等待状态，进入该状态表示当前线程需要等待其他线程做出一些特定的动作（通知或中断）
 *      5. TIMED_WAITING：超时等待状态，有时间限制的阻塞状态，他是可以在指定的时间自行返回
 *      6. TERMINATED：终止状态，表示当前线程已执行完毕
 *
 * @author cchangy
 * @date 2022/08/07 15:44
 */
@Slf4j
public class ThreadState {

    public static void main(String[] args) {
        Thread t1 = new Thread(() -> {}, "t1");

        Thread t2 = new Thread(() -> {
            while (true) {
            }
        }, "t2");
        t2.start();

        new Thread(() -> {
            synchronized (ThreadState.class) {
                try {
                    TimeUnit.SECONDS.sleep(60);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();

        Thread t3 = new Thread(() -> {
            synchronized (ThreadState.class){
            }
        }, "t3");
        t3.start();

        Thread t4 = new Thread(() -> {
            try {
                t3.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }, "t4");
        t4.start();

        Thread t5 = new Thread(() -> {
            try {
                TimeUnit.SECONDS.sleep(2);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }, "t5");
        t5.start();

        Thread t6 = new Thread(() -> {}, "t6");
        t6.start();

        log.info("t1 state: {}", t1.getState()); // NEW
        log.info("t2 state: {}", t2.getState()); // RUNNABLE
        log.info("t3 state: {}", t3.getState()); // BLOCKED
        log.info("t4 state: {}", t4.getState()); // WAITING
        log.info("t5 state: {}", t5.getState()); // TIMED_WAITING
        log.info("t6 state: {}", t6.getState()); // TERMINATED
    }
}
