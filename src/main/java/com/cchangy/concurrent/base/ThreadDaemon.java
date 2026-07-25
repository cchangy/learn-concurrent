package com.cchangy.concurrent.base;

import com.cchangy.concurrent.util.SleepUtils;
import lombok.extern.slf4j.Slf4j;

/**
 * 守护线程示例
 * <p>
 * 默认情况下，java进程需要等待所有线程都执行结束之后，进程才会终止，
 * 有一种特殊的线程叫做守护线程，只要其它非守护线程执行结束后，即使守护线程还没执行完，也会强制结束
 *
 * 在构建守护线程时，不能依靠finally来执行关闭或清理资源等操作，finally不一定会执行
 *
 * @author cchangy
 * @date 2022/08/07 15:38
 */
@Slf4j
public class ThreadDaemon {

    public static void main(String[] args){
        log.info("main方法开始...");

        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    SleepUtils.sleepSeconds(10);
                } finally {
                    log.info("DaemonThread finally execute...");
                }
            }
        });

        // 设置为守护线程，默认为非守护线程
        thread.setDaemon(true);
        thread.start();
        log.info("main方法结束...");
    }
}
