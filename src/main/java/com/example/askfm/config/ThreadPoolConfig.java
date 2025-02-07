package com.example.askfm.config;


import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

@Configuration
public class ThreadPoolConfig {
    @Value("${server.cpu-cores:4}")
    private int availableCores;

@Bean(name = "messageProcessor")
public ExecutorService messageProcessingPool() {
    int optimalThreads = Math.max(2, availableCores);
    AtomicInteger threadCounter = new AtomicInteger(0);

    return new ThreadPoolExecutor(
            optimalThreads,
            optimalThreads,
            60L, TimeUnit.SECONDS,
            new ArrayBlockingQueue<>(100),
            r -> {
                Thread thread = new Thread(r);
                thread.setName("MsgProcessor-" + threadCounter.incrementAndGet());
                thread.setPriority(Thread.NORM_PRIORITY);
                return thread;
            },
            new ThreadPoolExecutor.CallerRunsPolicy()
    );
}
}
