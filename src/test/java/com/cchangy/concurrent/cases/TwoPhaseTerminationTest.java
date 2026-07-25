package com.cchangy.concurrent.cases;

import com.cchangy.concurrent.util.SleepUtils;

/**
 * 两阶段终止模式测试类
 *
 * @author cchangy
 * @date 2026/07/25 21:07
 */
public class TwoPhaseTerminationTest {

    public static void main(String[] args) {
        TwoPhaseTermination twoPhaseTermination = new TwoPhaseTermination();
        twoPhaseTermination.start();

        SleepUtils.sleepSeconds(5);

        twoPhaseTermination.stop();
    }
}
