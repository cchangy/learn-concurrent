package com.chency.concurrent.base;

import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;

/**
 * 创建线程示例
 *
 * @author chency
 * @date 2022/08/06 10:42
 */
@Slf4j
public class CreateThread {

    public static void main(String[] args) throws Exception {

        case1();
        case2();
        case3();
        case4();
    }

    /**
     * 结合futureTask，使用callable创建有返回值的线程
     *
     * @throws InterruptedException
     * @throws ExecutionException
     */
    private static void case4() throws InterruptedException, ExecutionException {
        Callable<String> callable = new Callable<String>() {
            @Override
            public String call() throws Exception {
                log.info("case4 running...");
                return "call result";
            }
        };
        FutureTask<String> futureTask = new FutureTask<>(callable);
        new Thread(futureTask).start();
        log.info(futureTask.get());
    }

    /**
     * 使用lambda精简后的
     */
    private static void case3() {
        new Thread(() -> log.info("case3 running...")).start();
    }

    /**
     * 使用runningnable接口
     */
    private static void case2() {
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                log.info("case2 running...");
            }
        };
        new Thread(runnable).start();
    }

    /**
     * 使用thread类重写running方法
     */
    private static void case1() {
        Thread thread = new Thread() {
            @Override
            public void run() {
                log.info("case1 running...");
            }
        };
        thread.start();
    }
}
