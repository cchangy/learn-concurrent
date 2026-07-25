package com.cchangy.concurrent.cases;

import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.TimeUnit;

/**
 *
 * 两阶段终止模式案例
 *
 * @author cchangy
 * @date 2026/07/25 21:01
 */
@Slf4j
public class TwoPhaseTermination {

    private Thread monitor;

    public void start() {
        monitor = new Thread(() -> {
            while (true) {
                boolean interrupted = Thread.currentThread().isInterrupted();
                if (interrupted) {
                    log.info("monitor thread is stop...");
                    break;
                }

                try {
                    TimeUnit.SECONDS.sleep(1);
                    log.info("monitor thread running...");
                } catch (InterruptedException e) {

                    // 阻塞中的线程被打断，打断标记会被重置，因此需要重新设置中断标志，否则线程还是依旧会继续执行
                    Thread.currentThread().interrupt();
                }
            }
        }, "monitor");
        monitor.start();
    }

    public void stop() {
        monitor.interrupt();
    }
}
