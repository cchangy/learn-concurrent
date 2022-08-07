package com.chency.concurrent.base;

import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.TimeUnit;

/**
 * thread类常用方法示例
 *
 * @author chency
 * @date 2022/08/06 16:45
 */
@Slf4j
public class ThreadMethod {

    public static void main(String[] args) throws Exception {

//        startMethod();
//        runMethod();
//        sleepMethod();
//        yieldMethod();
//        joinMethod();
//        interruptMethod();
    }


    /**
     * 使用start方法来启动一个新的线程，在新的线程中执行run方法
     * run方法里的代码不一定立刻运行（CPU 的时间片还没分给它）。
     * 每个线程对象的start方法只能调用一次，如果调用了多次会出现IllegalThreadStateException
     */
    private static void startMethod() {
        Thread thread = new Thread() {
            @Override
            public void run() {
                log.info("start method...");
            }
        };
        thread.start();
        //thread.start();
    }

    /**
     * 新线程启动后会调用的方法，直接调用run方法不会启动一个新的线程，而是以当前线程来执行
     * 如果在构造Thread对象时传递了Runnable参数，则线程启动后会调用Runnable 中的 run 方法，
     * 否则默认不执行任何操作。但可以创建Thread的子类对象，来覆盖默认行为
     */
    private static void runMethod() {
        new Thread() {
            @Override
            public void run() {
                log.info("run method...");
            }
        }.run();
    }

    /**
     * 调用sleep方法会使当前线程休眠，进入Timed Waiting 状态（阻塞）
     * 其它线程可以使用 interrupt 方法打断正在睡眠的线程，这时 sleep 方法会抛出
     * 睡眠结束后的线程未必会立刻得到执行
     * 建议用 TimeUnit 的 sleep 代替 Thread 的 sleep 来获得更好的可读性
     */
    private static void sleepMethod() {
        new Thread() {
            @Override
            public void run() {
                try {
                    Thread.sleep(1000);
                    TimeUnit.SECONDS.sleep(1);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                log.info("sleep method...");
            }
        }.start();
    }

    /**
     * 线程让步
     * 调用 yield 会让当前线程从 Running 进入 Runnable 就绪状态，然后调度执行其它线程
     * 具体的实现依赖于操作系统的任务调度器
     */
    private static void yieldMethod() {
        Thread t1 = new Thread(() -> {
            int count = 0;
            for (; ; ) {
                log.info(">>>>>>>1: {}", count++);
            }
        }, "t1");

        Thread t2 = new Thread(() -> {
            int count = 0;
            for (; ; ) {
                Thread.yield();
                log.info("             >>>>>>>2: {}", count++);
            }
        }, "t2");

        //t1.start();
        //t2.start();
    }

    /**
     * 等待线程运行结束
     */
    static int count = 0;

    private static void joinMethod() throws InterruptedException {

        Thread t1 = new Thread(() -> {
            log.info("开始...");
            try {
                TimeUnit.SECONDS.sleep(1);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            count = 10;
            log.info("结束...");
        }, "t1");

        t1.start();
        t1.join();
        // t1.join(1000); // 有时效的join，等待N秒如果线程还没执行结束，则会继续往下执行
        log.info("结果为: {}", count);
    }

    /**
     * 线程打断方法，打断 sleep，wait，join 的线程
     *
     * 打断sleep中的线程会清空打断状态
     */
    private static void interruptMethod() {
        Thread t1 = new Thread(() -> {
            try {
                TimeUnit.SECONDS.sleep(2);
            } catch (InterruptedException e) {
                e.printStackTrace();
                log.info("sleep thread interrupt state: {}", Thread.currentThread().isInterrupted());
            }
        });
        t1.start();
        t1.interrupt();

        Thread t2 = new Thread(() -> {
            while (true) {
                boolean interrupted = Thread.currentThread().isInterrupted();
                if (interrupted) {
                    log.info("normal thread interrupt state: {}", interrupted);
                    break;
                }
            }
        });
        t2.start();
        t2.interrupt();
    }
}
