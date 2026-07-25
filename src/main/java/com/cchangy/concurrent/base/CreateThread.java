package com.cchangy.concurrent.base;

import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;

/**
 * 创建线程示例
 *
 * @author cchangy
 * @date 2022/08/06 10:42
 */
@Slf4j
public class CreateThread {

    public static void main(String[] args) throws Exception {

        overrideRunMethod();
        useRunnable();
        useFutureTask();
    }

    /**
     * 使用thread类重写run方法
     */
    private static void overrideRunMethod() {

        // 完成写法
        Thread thread = new Thread() {
            @Override
            public void run() {
                log.info("override run method running...");
            }
        };
        thread.start();
    }

    /**
     * 使用runnable接口（推荐使用）
     *
     * 更容易与线程池配合
     * 让任务类脱离了Thread继承体系，更灵活
     */
    private static void useRunnable() {
        // 完整写法
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                log.info("useRunnable running...");
            }
        };
        new Thread(runnable).start();

        // lambda简化写法
        new Thread(() -> log.info("useRunnable lambda running...")).start();
    }

    /**
     * FutureTask能够接收 callable 类型的参数，用来处理有返回结果的情况
     *
     * @throws InterruptedException
     * @throws ExecutionException
     */
    private static void useFutureTask() throws InterruptedException, ExecutionException {
        Callable<String> callable = new Callable<String>() {
            @Override
            public String call() throws Exception {
                log.info("useFutureTask running...");
                return "call result";
            }
        };
        FutureTask<String> futureTask = new FutureTask<>(callable);
        new Thread(futureTask).start();
        log.info(futureTask.get());
    }
}
