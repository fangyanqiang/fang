package com.fyq.utils;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @description:
 * @author: fyq
 * @date: 2020-07-04 13:06
 **/
public class ExecutorUtil {
    public static final ScheduledExecutorService executorService = Executors.newScheduledThreadPool(4, new ThreadFactory() {
        final AtomicInteger atomicInteger = new AtomicInteger();
        @Override
        public Thread newThread(Runnable r) {
            int i = atomicInteger.incrementAndGet();
            return new Thread(r,"线程" + i);
        }
    });

    public static void scheduleWithFixedDelay(Runnable runnable, long initialDelay, long period, TimeUnit timeUnit) {
        executorService.scheduleWithFixedDelay(runnable, initialDelay, period, timeUnit);
    }

}
