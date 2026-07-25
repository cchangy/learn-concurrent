package com.cchangy.concurrent.base;

import lombok.extern.slf4j.Slf4j;

/**
 * 线程分组示例
 *
 * @author cchangy
 * @date 2022/08/27 11:49
 */
@Slf4j
public class ThreadGrouping {

    public static void main(String[] args) {
        log.info("{}线程的线程组是{}", Thread.currentThread().getName(), Thread.currentThread().getThreadGroup().getName());

        ThreadGroup threadGroup = new ThreadGroup("testGroup");

        Thread t1 = new Thread(threadGroup, "t1");
        Thread t2 = new Thread("t2");

        log.info("{}", threadGroup.getName());
        log.info("指定了线程组: {}", t1.getThreadGroup().getName());
        log.info("未指定线程组: {}", t2.getThreadGroup().getName());
    }
}
