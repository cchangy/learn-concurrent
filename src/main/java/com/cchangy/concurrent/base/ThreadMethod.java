package com.cchangy.concurrent.base;

import com.cchangy.concurrent.util.SleepUtils;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.LockSupport;

/**
 * Thread 常用方法示例
 *
 * 过时不推荐使用的方法：stop()，suspend()，resume()
 *
 * @author cchangy
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
//        priorityMethod();
//        interruptMethod();
//        interrupted();
//        interruptedPark();
    }


    /**
     * 使用start方法来启动一个新的线程，在新的线程中执行run方法
     * run方法里的代码不一定会立刻运行（CPU 的时间片还没分给它）。
     */
    private static void startMethod() {
        Thread thread = new Thread() {
            @Override
            public void run() {
                log.info("start method...");
            }
        };
        thread.start();

        // 每个线程对象的start方法只能调用一次，如果调用了多次会抛出IllegalThreadStateException
        // thread.start();
    }

    /**
     * 新线程启动后会调用的方法，直接调用run方法不会启动一个新的线程，而是以当前线程来执行
     * 构造Thread对象时传递了Runnable参数，则会调用Runnable中的run方法，
     */
    private static void runMethod() {
        new Thread() {
            @Override
            public void run() {
                log.info("run method with thread...");
            }
        }.run();

        new Thread(new Runnable() {
            @Override
            public void run() {
                log.info("run method with runnable...");
            }
        }).run();
    }

    /**
     * 调用sleep方法会使当前线程休眠，进入Timed Waiting 状态（阻塞）
     * 其它线程可以使用 interrupt 方法打断正在睡眠的线程，这时 sleep 方法会抛出 InterruptedException
     * 睡眠结束后的线程未必会立刻得到执行
     */
    private static void sleepMethod() {
        Thread t1 = new Thread() {
            @Override
            public void run() {
                try {
                    // 用 TimeUnit的sleep代替Thread的sleep来获得更好的可读性
                    // Thread.sleep(1000);
                    TimeUnit.SECONDS.sleep(1);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                log.info("sleep method...");
            }
        };
        t1.start();
        log.info("t1 state: {}", t1.getState());

        // 主线程睡眠500毫秒
        SleepUtils.sleepMilliSeconds(500);

        log.info("t1 state: {}", t1.getState());
    }

    /**
     * 线程让步
     * <p>
     * 调用yield会让当前线程从Running进入Runnable就绪状态（在Java API 层面还是处理Runnable状态），让出CPU，让CPU调度执行其它线程
     * 具体的实现依赖于操作系统的任务调度器
     */
    private static void yieldMethod() {
        Thread t1 = new Thread(() -> {
            int count = 0;
            for (; ; ) {
                log.info(">>>>>>>1: {}, threadState: {}", count++, Thread.currentThread().getState());
            }
        }, "t1");

        Thread t2 = new Thread(() -> {
            int count = 0;
            for (; ; ) {
                Thread.yield();
                log.info("             >>>>>>>2: {}, threadState: {}", count++, Thread.currentThread().getState());
            }
        }, "t2");

        t1.start();
        t2.start();
    }

    static int count = 0;

    /**
     * 等待线程运行结束
     */
    private static void joinMethod() throws InterruptedException {
        Thread t1 = new Thread(() -> {
            log.info("开始...");
            SleepUtils.sleep(1);
            count = 10;
            log.info("结束...");
        }, "t1");

        long start = System.currentTimeMillis();
        t1.start();
        t1.join();
        // 有时效的join，等待N秒如果线程还没执行结束，则会继续往下执行
        // 当线程执行时间没有超过join设定时间，线程执行完后join也会结束
        // t1.join(3000);
        long end = System.currentTimeMillis();
        log.info("count: {}, cost: {}", count, end - start);
    }

    /**
     * 线程打断方法，打断 sleep，wait，join 的线程，这几个方法都会让线程进入阻塞状态，打断后会抛出 InterruptedException 异常
     * <p>
     * 打断阻塞中的线程会清空打断状态，isInterrupted会返回false，结束线程的运行。但如果该异常被线程捕获住，该线程依然可以自行决定后续处理
     * 打断正在运行中的线程并不会影响线程的运行，isInterrupted会返回true，线程根据打断标记自行决定后续处理
     */
    private static void interruptMethod() {
        Thread t1 = new Thread(() -> {
            log.info("sleep...");
            try {
                TimeUnit.SECONDS.sleep(2);
            } catch (InterruptedException e) {
                e.printStackTrace();
                log.info("sleep thread interrupt state: {}", Thread.currentThread().isInterrupted());
            }
        }, "t1");
        t1.start();
        SleepUtils.sleepMilliSeconds(500);
        log.info("interrupting t1...");
        t1.interrupt();

        Thread t2 = new Thread(() -> {
            while (true) {
                boolean interrupted = Thread.currentThread().isInterrupted();
                if (interrupted) {
                    log.info("normal thread interrupt state: {}", interrupted);
                    break;
                }
            }
        }, "t2");
        t2.start();
        SleepUtils.sleepMilliSeconds(500);
        log.info("interrupting t2...");
        t2.interrupt();
    }

    /**
     * interrupted 是 Thread 的静态方法，同样用来判断线程是否被打断，但是它会清除线程的打断状态
     */
    public static void interrupted() {
        Thread t1 = new Thread(() -> {
            while (true) {
                boolean isInterrupted = Thread.currentThread().isInterrupted();
                if (isInterrupted) {
                    log.info("execute isInterrupted after interrupt state: {}", Thread.currentThread().isInterrupted());
                    break;
                }
            }
        }, "t1");

        Thread t2 = new Thread(() -> {
            while (true) {
                boolean interrupted = Thread.interrupted();
                if (interrupted) {
                    log.info("execute interrupted after interrupt state: {}", Thread.currentThread().isInterrupted());
                    break;
                }
            }
        }, "t2");
        t1.start();
        t2.start();

        SleepUtils.sleepMilliSeconds(500);
        log.info("interrupting t1...");
        t1.interrupt();

        log.info("interrupting t2...");
        t2.interrupt();
    }

    /**
     * 打断 park 方法，park 方法是 LockSupport 类提供的一个阻塞线程的方法，
     * park 方法会让当前线程进入阻塞状态，直到被 unpark 或者被打断
     */
    private static void interruptedPark() {
        Thread t1 = new Thread(() -> {
            log.info("park...");
            // park 方法会让当前线程进入阻塞状态，直到被 unpark 或者被打断
             LockSupport.park();

            // 当park线程被打断后，如果interrupted是true时，当前线程再次执行park将会失效，只有interrupted为false时，park才会生效
            // 可以使用带清除状态的Thread.interrupted()
            log.info("park thread interrupt state: {}", Thread.currentThread().isInterrupted());

            LockSupport.park();
            log.info("again park...");
        }, "t1");
        t1.start();
        SleepUtils.sleep(2);
        log.info("interrupting t1...");
        t1.interrupt();
    }

    private static volatile boolean notStart = true;
    private static volatile boolean notEnd = true;

    /**
     * 设置线程优先级，范围是1~10，默认优先级是5
     * 优先级高的线程被分配的时间片的数量要多于优先级低的线程
     * 频繁阻塞（休眠或者I/O操作较多的）的线程需要设置较高的优先级，而偏重计算（需要较多CPU时间）的线程则设置较低的优先级，以确保处理器不会被独占
     * 优先级不能作为程序正确性的依赖，因为操作系统可以完全不理会优先级的设定
     * <p>
     * 通过下面的案例结果可以看出线程优先级没有生效
     */
    private static void priorityMethod() throws InterruptedException {
        List<Job> jobs = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            int priority = i < 5 ? Thread.MIN_PRIORITY : Thread.MAX_PRIORITY;
            Job job = new Job(priority);
            jobs.add(job);

            Thread thread = new Thread(job, "Thread" + i);
            thread.setPriority(priority);
            thread.start();
        }

        notStart = false;
        TimeUnit.SECONDS.sleep(10);
        notEnd = false;

        jobs.forEach(job -> System.out.println("Job Priority=" + job.getPriority() + ", Count=" + job.getCount()));
    }

    @Data
    private static class Job implements Runnable {
        private int priority;
        private long count;

        public Job(int priority) {
            this.priority = priority;
        }

        @Override
        public void run() {
            while (notStart) {
                Thread.yield();
            }
            while (notEnd) {
                Thread.yield();
                count++;
            }
        }
    }
}
