package com.chency.concurrent.base;

import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.TimeUnit;

/**
 * 守护线程示例
 * <p>
 * 默认情况下，java进程需要等待所有线程都执行结束之后，进程才会终止，
 * 有一种特殊的线程叫做守护线程，只要其它非守护线程执行结束后，即使守护线程还没执行完，也会强制结束
 *
 * @author chency
 * @date 2022/08/07 15:38
 */
@Slf4j
public class ThreadDaemon {

    public static void main(String[] args) throws InterruptedException {
        log.info("main方法开始...");
        Thread thread = new Thread(() -> {
            log.info("开始运行...");
            for (int i = 0; i < 1000; i++) {
                log.info(">>>>>>>>>: {}", i);
                try {
                    TimeUnit.SECONDS.sleep(1);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            log.info("运行结束...");
        });
        thread.setDaemon(true); // 设置为守护线程
        thread.start();
        TimeUnit.SECONDS.sleep(5);
        log.info("main方法结束...");
    }
}
