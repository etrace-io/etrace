package io.etrace.common.util;

public class ThreadUtil {
    public static void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException ignore) {
        }
    }

    public static void sleepForSecond(long second) {
        try {
            Thread.sleep(second * 1000);
        } catch (InterruptedException ignore) {
        }
    }

    public static void join(Thread thread, long millis) {
        try {
            thread.join(millis);
        } catch (InterruptedException ignore) {
        }
    }
}
