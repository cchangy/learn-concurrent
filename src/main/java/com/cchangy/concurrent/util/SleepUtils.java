package com.cchangy.concurrent.util;

import java.util.concurrent.TimeUnit;

/**
 * 线程睡眠工具类
 *
 * @author cchangy
 * @date 2025/08/09 16:45
 */
public class SleepUtils {

    public static void sleepSeconds(long seconds) {
        try {
            TimeUnit.SECONDS.sleep(seconds);
        } catch (InterruptedException e) {
            // ignore
        }
    }

    public static void sleepMilliseconds(long milliseconds) {
        try {
            TimeUnit.MILLISECONDS.sleep(milliseconds);
        } catch (InterruptedException e) {
            // ignore
        }
    }
}
