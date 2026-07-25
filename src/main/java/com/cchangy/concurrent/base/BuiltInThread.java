package com.cchangy.concurrent.base;

import lombok.extern.slf4j.Slf4j;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;

/**
 * Java内置的线程
 * main: main线程，程序的入口
 * Reference Handler: Reference清除线程
 * Finalizer: 调用对象finalize方法的线程
 * Signal Dispatcher: 分发和管理JVM信号的线程
 *
 * @author cchangy
 * @date 2024/1/21
 */
@Slf4j
public class BuiltInThread {

    public static void main(String[] args) {
        // 1. 获取Java线程管理MxBean
        ThreadMXBean threadMXBean = ManagementFactory.getThreadMXBean();
        // 2. 仅获取线程和线程堆栈信息
        ThreadInfo[] threadInfos = threadMXBean.dumpAllThreads(false, false);
        for (ThreadInfo threadInfo : threadInfos) {
            log.info("[{}] {}", threadInfo.getThreadId(), threadInfo.getThreadName());
        }
    }
}
